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
    public Mono<CardAccumulatedValues> fetchCardAccumValuesByCardNumber(String cardId) {
        return cardAccumulatedValuesDataService.findCardBasicValuesByCardNumber(cardId)
                .map(cardAccumulatedValuesOptional -> {
                    if(cardAccumulatedValuesOptional.isPresent()){
                        return cardAccumulatedValuesOptional.get();
                    }
                   throw new NotFoundException("Card Number  Not Found" + cardId);
                });
    }

    @Override
    public Mono<Optional<CardAccumulatedValues>> fetchCardAccumValuesOptional(String cardId) {
        return cardAccumulatedValuesDataService.findCardBasicValuesByCardNumber(cardId);
    }

    @Override
    public Mono<CardAccumulatedValues> deleteCardAccumValuesByCardNumber(String cardId) {
        return cardAccumulatedValuesDataService.deleteCardBasicByCardNumber(cardId)
                .map(cardAccumulatedValuesOptional -> {
                    if(cardAccumulatedValuesOptional.isEmpty()){
                        throw  new NotFoundException(" Card Number Not Found " + cardId);
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
    public Mono<CardAccumulatedValues> initializeCardAccumValues(String cardId, List<PeriodicCardLimitDTO> periodicCardLimitDTOList, int org, int product) {

        CardAccumulatedValues cardAccumulatedValues = initializeAccumValues(cardId,periodicCardLimitDTOList,org, product);
        return saveAccountAccumValues(cardAccumulatedValues);
    }

    @Override
    public Mono<CardAccumulatedValues> updateNewAccumValues(List<PeriodicCardLimitDTO> periodicCardLimitDTOAddList, List<PeriodicCardLimitDTO> periodicCardLimitDTODeleteList, String cardId) {
        return fetchCardAccumValuesByCardNumber(cardId)
                .map(cardAccumulatedValues -> updateNewAccumValues(periodicCardLimitDTOAddList,periodicCardLimitDTODeleteList,cardAccumulatedValues))
                .flatMap(cardAccumulatedValues -> saveAccountAccumValues(cardAccumulatedValues))
                ;
    }

    @Override
    public CardAccumValuesDTO convertToDTO(CardAccumulatedValues cardAccumulatedValues) {

    ;
        CardAccumValuesDTO.CardAccumValuesDTOBuilder builder = CardAccumValuesDTO.builder()
                .cardId(cardAccumulatedValues.getCardId())
                .org(cardAccumulatedValues.getOrg())
                .product(cardAccumulatedValues.getProduct());;

                if(cardAccumulatedValues.getPeriodicTypePeriodicCardLimitMap() != null) {                    builder
                            .periodicCardLimitDTOList(convertPeriodicTypeMapToDTO(cardAccumulatedValues.getPeriodicTypePeriodicCardLimitMap()));
                }
                if(cardAccumulatedValues.getPeriodicCardAccumulatedValueMap() != null) {
                    builder
                            .periodicCardAccumulatedValueList(convertPeriodicTypeMapToDTO(cardAccumulatedValues.getPeriodicCardAccumulatedValueMap()));
                }
                return builder.build()
                ;
    }

    @Override
    public CardAccumulatedValues initializeAccumValues(String cardId, List<PeriodicCardLimitDTO> periodicCardLimitDTOList, int org, int product) {

        Map<PeriodicType,Map<LimitType,PeriodicCardAmount>> periodicCardAmountMap = new HashMap<>();

        Map<PeriodicType,Map<LimitType,PeriodicCardAmount>> periodicCardLimitMap = new HashMap<>();

        if(periodicCardLimitDTOList != null){
            convertDTOPeriodicMap(periodicCardLimitDTOList,periodicCardLimitMap,periodicCardAmountMap);
        }


        return CardAccumulatedValues.builder()
                .product(product)
                .org(org)
                .cardId(cardId)
                .periodicTypePeriodicCardLimitMap(periodicCardLimitMap)
                .periodicCardAccumulatedValueMap(periodicCardAmountMap)
                .build()
                ;
    }



    @Override
    public CardAccumulatedValues updateNewAccumValues(List<PeriodicCardLimitDTO> periodicCardLimitDTOAddList,
                                                      List<PeriodicCardLimitDTO> periodicCardLimitDTODeleteList,
                                                      CardAccumulatedValues cardAccumulatedValues) {


        Map<PeriodicType,Map<LimitType,PeriodicCardAmount>> periodicTypeLimitMap;
        Map<PeriodicType,Map<LimitType,PeriodicCardAmount>> periodicAmountMap;

        if(periodicCardLimitDTOAddList != null){

            if(cardAccumulatedValues.getPeriodicTypePeriodicCardLimitMap() == null){
                periodicTypeLimitMap = new HashMap<>();
            } else {
                periodicTypeLimitMap = cardAccumulatedValues.getPeriodicTypePeriodicCardLimitMap();
            }

            if(cardAccumulatedValues.getPeriodicCardAccumulatedValueMap() == null){
                periodicAmountMap = new HashMap<>();
            } else {
                periodicAmountMap = cardAccumulatedValues.getPeriodicCardAccumulatedValueMap();
            }

            convertDTOPeriodicMap(periodicCardLimitDTOAddList,periodicTypeLimitMap,periodicAmountMap);

        }

        if(periodicCardLimitDTODeleteList != null){
            if(cardAccumulatedValues.getPeriodicTypePeriodicCardLimitMap() != null){
                deleteLimits(periodicCardLimitDTODeleteList,cardAccumulatedValues.getPeriodicTypePeriodicCardLimitMap());
            }
        }

        return cardAccumulatedValues;
    }




    private void deleteLimits(List<PeriodicCardLimitDTO> periodicCardLimitDTODeleteList,
                              Map<PeriodicType, Map<LimitType, PeriodicCardAmount>> periodicTypePeriodicCardLimitMap) {

        periodicCardLimitDTODeleteList
                .forEach(periodicCardLimitDTO -> {
                    Map<LimitType,PeriodicCardAmount> limitCardLimitMap = periodicTypePeriodicCardLimitMap.get(Util.getPeriodicType(periodicCardLimitDTO.getPeriodicType()));
                    if(limitCardLimitMap != null){
                        periodicCardLimitDTO.getCardLimitsDTOList()
                                .forEach(cardLimitsDTO -> {
                                    limitCardLimitMap.remove(Util.getLimitType(cardLimitsDTO.getLimitType()));
                                });
                    }


                });

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


    private void convertDTOPeriodicMap(List<PeriodicCardLimitDTO> periodicCardLimitDTOList,
                                       Map<PeriodicType, Map<LimitType, PeriodicCardAmount>> periodicCardLimitMap,
                                       Map<PeriodicType, Map<LimitType, PeriodicCardAmount>> periodicCardAmountMap) {

        periodicCardLimitDTOList.forEach(periodicCardLimitDTO -> {

            Map<LimitType, PeriodicCardAmount> limitMap = periodicCardLimitMap.get(Util.getPeriodicType(periodicCardLimitDTO.getPeriodicType()));
            Map<LimitType, PeriodicCardAmount> amountMap = periodicCardAmountMap.get(Util.getPeriodicType(periodicCardLimitDTO.getPeriodicType()));

            if(limitMap == null){
                limitMap = new HashMap<>();
            }
            if(amountMap == null){
                amountMap = new HashMap<>();
            }

            periodicCardLimitMap.put(Util.getPeriodicType(periodicCardLimitDTO.getPeriodicType()),updateLimitsFromDTO(periodicCardLimitDTO.getCardLimitsDTOList(),limitMap));
            periodicCardAmountMap.put(Util.getPeriodicType(periodicCardLimitDTO.getPeriodicType()),initializeAmountsFromDTO(periodicCardLimitDTO.getCardLimitsDTOList(),amountMap));

        });
    }

    private Map<LimitType, PeriodicCardAmount> updateLimitsFromDTO(List<CardLimitsDTO> cardLimitsDTOList, Map<LimitType, PeriodicCardAmount> limitMap) {

        cardLimitsDTOList.forEach(cardLimitsDTO -> {

            limitMap.put(Util.getLimitType(cardLimitsDTO.getLimitType()),PeriodicCardAmount.builder()
                    .limitType(Util.getLimitType(cardLimitsDTO.getLimitType()))
                    .transactionAmount(cardLimitsDTO.getLimitAmount())
                    .transactionNumber(cardLimitsDTO.getLimitNumber())
                    .build()
            );
        });

        return limitMap;
    }

    private Map<LimitType, PeriodicCardAmount> initializeAmountsFromDTO(List<CardLimitsDTO> cardLimitsDTOList, Map<LimitType, PeriodicCardAmount> amountMap) {

        cardLimitsDTOList.forEach(cardLimitsDTO -> {
            LimitType limitType = Util.getLimitType(cardLimitsDTO.getLimitType());

            if(!amountMap.containsKey(limitType)) {

                amountMap.put(Util.getLimitType(cardLimitsDTO.getLimitType()),PeriodicCardAmount.builder()
                        .limitType(Util.getLimitType(cardLimitsDTO.getLimitType()))
                        .transactionAmount(0L)
                        .transactionNumber(0)
                        .build()
                );
            }
        });

        return amountMap;
    }


    private List<PeriodicCardLimitDTO> convertPeriodicTypeMapToDTO(Map<PeriodicType, Map<LimitType, PeriodicCardAmount>> periodicTypePeriodicCardLimitMap) {

        List<PeriodicCardLimitDTO> periodicCardLimitDTOList = new ArrayList<>();
        periodicTypePeriodicCardLimitMap.entrySet()
                .forEach(periodicTypeMapEntry -> {
                    PeriodicCardLimitDTO periodicCardLimitDTO = PeriodicCardLimitDTO.builder()
                            .periodicType(Util.getPeriodicType(periodicTypeMapEntry.getKey()))
                            .cardLimitsDTOList(convertLimitMapToDTO(periodicTypeMapEntry.getValue()))
                            .build();
                    periodicCardLimitDTOList.add(periodicCardLimitDTO);
                });

        return periodicCardLimitDTOList;

    }

    private List<CardLimitsDTO> convertLimitMapToDTO(Map<LimitType, PeriodicCardAmount> periodicCardAmountMap) {

        List<CardLimitsDTO> cardLimitsDTOList = new ArrayList<>();

        periodicCardAmountMap.entrySet()
                .forEach(limitTypePeriodicCardAmountEntry -> {

                    CardLimitsDTO cardLimitsDTO = CardLimitsDTO.builder()
                            .limitType(Util.getLimitType(limitTypePeriodicCardAmountEntry.getKey()))
                            .limitAmount(limitTypePeriodicCardAmountEntry.getValue().getTransactionAmount())
                            .limitNumber(limitTypePeriodicCardAmountEntry.getValue().getTransactionNumber())
                            .build();
                    cardLimitsDTOList.add(cardLimitsDTO);

                } );

        return cardLimitsDTOList;

    }



}
