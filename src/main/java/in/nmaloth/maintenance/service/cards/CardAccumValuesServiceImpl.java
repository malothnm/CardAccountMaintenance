package in.nmaloth.maintenance.service.cards;

import in.nmaloth.entity.card.CardAccumulatedValues;
import in.nmaloth.entity.card.LimitType;
import in.nmaloth.entity.card.PeriodicCardAmount;
import in.nmaloth.entity.card.PeriodicType;
import in.nmaloth.maintenance.dataService.card.CardAccumulatedValuesDataService;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.card.CardAccumValuesDTO;
import in.nmaloth.maintenance.model.dto.card.CardLimitsDTO;
import in.nmaloth.maintenance.model.dto.card.PeriodicCardLimitDTO;
import in.nmaloth.maintenance.model.dto.card.PeriodicLimitSet;
import in.nmaloth.maintenance.util.Util;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CardAccumValuesServiceImpl implements CardAccumValuesService {

    private final CardAccumulatedValuesDataService cardAccumulatedValuesDataService;

    public CardAccumValuesServiceImpl(CardAccumulatedValuesDataService cardAccumulatedValuesDataService) {
        this.cardAccumulatedValuesDataService = cardAccumulatedValuesDataService;
    }


    @Override
    public Mono<CardAccumulatedValues> fetchCardAccumValuesByCardNumber(String cardNumber) {
        return cardAccumulatedValuesDataService.findCardBasicValuesByCardNumber(cardNumber)
                .map(cardAccumulatedValuesOptional -> {
                    if(cardAccumulatedValuesOptional.isPresent()){
                        return cardAccumulatedValuesOptional.get();
                    }
                   throw new NotFoundException("Card Number  Not Found" + cardNumber);
                });
    }

    @Override
    public Mono<Optional<CardAccumulatedValues>> fetchCardAccumValuesOptional(String cardNumber) {
        return cardAccumulatedValuesDataService.findCardBasicValuesByCardNumber(cardNumber);
    }

    @Override
    public Mono<CardAccumulatedValues> deleteCardAccumValuesByCardNumber(String cardNumber) {
        return cardAccumulatedValuesDataService.deleteCardBasicByCardNumber(cardNumber)
                .map(cardAccumulatedValuesOptional -> {
                    if(cardAccumulatedValuesOptional.isEmpty()){
                        throw  new NotFoundException(" Card Number Not Found " + cardNumber);
                    }
                    return cardAccumulatedValuesOptional.get();
                })
                ;
    }


    @Override
    public Mono<CardAccumulatedValues> saveAccountAccumValues(CardAccumulatedValues cardAccumulatedValues) {
        return cardAccumulatedValuesDataService.saveCardBasicValues(cardAccumulatedValues);
    }

    @Override
    public CardAccumValuesDTO convertToDTO(CardAccumulatedValues cardAccumulatedValues) {

        List<PeriodicCardLimitDTO> periodicCardLimitDTOList =  cardAccumulatedValues
                .getPeriodicCardAccumulatedValueMap()
                .entrySet()
                .stream()
                .map(periodicTypeMapEntry -> PeriodicCardLimitDTO.builder()
                        .periodicType(Util.getPeriodicType(periodicTypeMapEntry.getKey()))
                        .cardLimitsDTOList(createCardLimitDTOLIS(periodicTypeMapEntry.getValue()))
                        .build()
                ).collect(Collectors.toList())

    ;
        return CardAccumValuesDTO.builder()
                .cardNumber(cardAccumulatedValues.getCardNumber())
                .org(cardAccumulatedValues.getOrg())
                .product(cardAccumulatedValues.getProduct())
                .periodicCardAccumulatedValueList(periodicCardLimitDTOList)
                .build()
                ;
    }

    @Override
    public CardAccumulatedValues initializeAccumValues(String cardNumber, Set<PeriodicLimitSet> periodicLimitDTOSet, int org, int product) {

        Map<PeriodicType,Map<LimitType,PeriodicCardAmount>> periodicCardAmountMap = new HashMap<>();

        periodicLimitDTOSet
                .forEach(periodicLimitSet -> periodicCardAmountMap.put(periodicLimitSet.getPeriodicType(), buildInitialLimitMap(periodicLimitSet.getLimitTypeSet())));

        return CardAccumulatedValues.builder()
                .product(product)
                .org(org)
                .cardNumber(cardNumber)
                .periodicCardAccumulatedValueMap(periodicCardAmountMap)
                .build()
                ;
    }

    private Map<LimitType, PeriodicCardAmount> buildInitialLimitMap(Set<LimitType> limitTypeSet) {
        Map<LimitType,PeriodicCardAmount> limitMap =  new HashMap<>();
        limitTypeSet
                .forEach(limitType -> limitMap.put(limitType,PeriodicCardAmount.builder()
                        .limitType(limitType)
                        .transactionNumber(0)
                        .transactionAmount(0L)
                        .build()
                ));
        return limitMap;
    }

    @Override
    public CardAccumulatedValues updateNewAccumValues(Set<PeriodicLimitSet> periodicLimitDTOSet, CardAccumulatedValues cardAccumulatedValues) {

        if(cardAccumulatedValues.getPeriodicCardAccumulatedValueMap() == null){
            cardAccumulatedValues.setPeriodicCardAccumulatedValueMap(new HashMap<>());
        }
        periodicLimitDTOSet
                .forEach(periodicLimitSet -> evaluateLimitMapUpdates(periodicLimitSet,cardAccumulatedValues.getPeriodicCardAccumulatedValueMap()));
        ;
        return cardAccumulatedValues;
    }

    private void evaluateLimitMapUpdates(PeriodicLimitSet periodicLimitSet, Map<PeriodicType, Map<LimitType, PeriodicCardAmount>> periodicCardAccumulatedValueMap) {

        Map<LimitType,PeriodicCardAmount> limitMap = periodicCardAccumulatedValueMap.get(periodicLimitSet.getPeriodicType());

        if(limitMap == null){
            limitMap = buildInitialLimitMap(periodicLimitSet.getLimitTypeSet());
        } else {
            periodicCardAccumulatedValueMap.put(periodicLimitSet.getPeriodicType(),
                    updateLimitMap(limitMap,periodicLimitSet.getLimitTypeSet()));
        }



    }

    private Map<LimitType, PeriodicCardAmount> updateLimitMap(Map<LimitType, PeriodicCardAmount> limitMap, Set<LimitType> limitTypeSet) {


        limitTypeSet
                .stream()
                .filter(limitType -> !limitMap.containsKey(limitType))
                .forEach(limitType -> limitMap.put(limitType,PeriodicCardAmount.builder()
                        .limitType(limitType)
                        .transactionAmount(0L)
                        .transactionNumber(0)
                        .build()
                ));
        return limitMap;

    }

    private List<CardLimitsDTO> createCardLimitDTOLIS(Map<LimitType, PeriodicCardAmount> cardAmountMap) {

        return cardAmountMap.entrySet()
                .stream()
                .map(limitTypeAmountEntry -> CardLimitsDTO.builder()
                        .limitType(Util.getLimitType(limitTypeAmountEntry.getKey()))
                        .limitAmount(limitTypeAmountEntry.getValue().getTransactionAmount())
                        .limitNumber(limitTypeAmountEntry.getValue().getTransactionNumber())
                        .build()
                )
                .collect(Collectors.toList());
    }





}
