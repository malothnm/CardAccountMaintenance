package in.nmaloth.maintenance.service.cards;

import in.nmaloth.entity.BlockType;
import in.nmaloth.entity.account.AccountDef;
import in.nmaloth.entity.card.CardsBasic;
import in.nmaloth.entity.card.LimitType;
import in.nmaloth.entity.card.PeriodicCardAmount;
import in.nmaloth.entity.card.PeriodicType;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.maintenance.config.data.ProductTable;
import in.nmaloth.maintenance.dataService.card.CardBasicDataService;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.account.AccountDefDTO;
import in.nmaloth.maintenance.model.dto.card.*;
import in.nmaloth.maintenance.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CardsBasicServiceImpl implements CardsBasicService {

    private final ProductTable productTable;
    private final CardBasicDataService cardBasicDataService;


    public CardsBasicServiceImpl(ProductTable productTable, CardBasicDataService cardBasicDataService) {
        this.productTable = productTable;
        this.cardBasicDataService = cardBasicDataService;
    }


    @Override
    public Mono<CardsBasic> createNewCardsRecord(CardBasicAddDTO cardBasicAddDTO) {

        ProductDef productDef = productTable.findProductDef(cardBasicAddDTO.getOrg(),cardBasicAddDTO.getProduct());
        CardsBasic cardsBasic = convertDTOToCardBasic(cardBasicAddDTO,productDef);

        return cardBasicDataService.saveCardBasicValues(cardsBasic)
                ;
    }


    @Override
    public Mono<CardsBasic> fetchCardInfo(String cardNumber) {
        return cardBasicDataService.findCardBasicValuesById(cardNumber)
                .map(cardsBasicOptional -> {
                    if(cardsBasicOptional.isPresent()){
                        return cardsBasicOptional.get();
                    }
                    throw  new NotFoundException("CardNumber Not Found "+ cardNumber);
                })
                ;
    }

    @Override
    public Mono<Optional<CardsBasic>> fetchCardInfOptional(String cardNumber) {
        return cardBasicDataService.findCardBasicValuesById(cardNumber);
    }


    @Override
    public Mono<CardsBasic> updateCards(CardBasicUpdateDTO cardBasicUpdateDTO) {

        return cardBasicDataService.findCardBasicValuesById(cardBasicUpdateDTO.getCardNumber())
                .map(cardsBasicOptional -> {
                    if(cardsBasicOptional.isPresent()){
                        return cardsBasicOptional.get();
                    }
                    throw new NotFoundException("Card Number Not Found" + cardBasicUpdateDTO.getCardNumber());
                })
                .map(cardsBasic -> {
                    ProductDef productDef = productTable.findProductDef(cardsBasic.getOrg(),cardsBasic.getProduct());
                    if(productDef == null){
                        throw  new NotFoundException("Not Found ... Org "+ cardsBasic.getOrg() + " product " + cardsBasic.getProduct());
                    }
                    return updateCardBasicFromDTO(cardBasicUpdateDTO,productDef,cardsBasic);
                })
                .flatMap(cardsBasic -> cardBasicDataService.saveCardBasicValues(cardsBasic));

    }



    @Override
    public Mono<CardsBasic> deleteCardInfo(String cardNumber) {


        return cardBasicDataService.deleteCardBasicByCardNumber(cardNumber)
                .map(cardsBasicOptional -> {
                    if(cardsBasicOptional.isPresent()){
                        return cardsBasicOptional.get();
                    }
                    throw  new NotFoundException(" Not Found Card Number "+ cardNumber);
                })
                ;
    }

    @Override
    public CardBasicDTO convertToDTO(CardsBasic cardsBasic) {

        CardBasicDTO.CardBasicDTOBuilder builder = CardBasicDTO.builder()
                .cardNumber(cardsBasic.getCardNumber())
                .product(cardsBasic.getProduct())
                .org(cardsBasic.getOrg())
                .cardStatus(Util.getCardStatus(cardsBasic.getCardStatus()))
                .cardholderType(Util.getCardHolderType(cardsBasic.getCardholderType()))
                .blockType(Util.getBlockType(cardsBasic.getBlockType()))
                .waiverDaysActivation(cardsBasic.getWaiverDaysActivation())
                .customerNumber(cardsBasic.getCustomerNumber())
                .cardReturnNumber(cardsBasic.getCardReturnNumber())
                .accountDefDTOSet(cardsBasic.getAccountDefSet().stream()
                        .map(accountDef -> AccountDefDTO.builder()
                                .accountNumber(accountDef.getAccountNumber())
                                .billingCurrencyCode(accountDef.getBillingCurrencyCode())
                                .accountType(Util.getAccountType(accountDef.getAccountType()))
                                .build()
                        )
                        .collect(Collectors.toSet())

                )
                ;

        if(cardsBasic.getPrevBlockType() != null){
            builder.prevBlockType(Util.getBlockType(cardsBasic.getPrevBlockType()));
        }

        if(cardsBasic.getDateBlockCode() != null){
            builder.dateBlockCode(cardsBasic.getDateBlockCode());
        }

        if(cardsBasic.getCorporateNumber() != null){
            builder.corporateNumber(cardsBasic.getCorporateNumber());
        }
        if(cardsBasic.getPrevCardNumber() != null){
            builder.prevCardNumber(cardsBasic.getPrevCardNumber());
        }

        if(cardsBasic.getDateTransfer() != null){
            builder.dateTransfer(cardsBasic.getDateTransfer());
        }

        if(cardsBasic.getDatePrevBlockCode() != null){
            builder.datePrevBlockCode(cardsBasic.getDatePrevBlockCode());
        }

        if(cardsBasic.getPeriodicTypePeriodicCardLimitMap() != null){
            builder.periodicCardLimitDTOList(updatePeriodicType(cardsBasic.getPeriodicTypePeriodicCardLimitMap()));
        }


        return builder.build();
    }

    private List<PeriodicCardLimitDTO> updatePeriodicType(Map<PeriodicType, Map<LimitType, PeriodicCardAmount>> periodicTypePeriodicCardLimitMap) {

        List<PeriodicCardLimitDTO> periodicCardLimitDTOList = new ArrayList<>();
        periodicTypePeriodicCardLimitMap.entrySet()
                .forEach(periodicTypeMapEntry -> {
                    PeriodicCardLimitDTO periodicCardLimitDTO = PeriodicCardLimitDTO.builder()
                            .periodicType(Util.getPeriodicType(periodicTypeMapEntry.getKey()))
                            .cardLimitsDTOList(updateCardLimts(periodicTypeMapEntry.getValue()))
                            .build();
                    periodicCardLimitDTOList.add(periodicCardLimitDTO);
                });

        return periodicCardLimitDTOList;

    }

    private List<CardLimitsDTO> updateCardLimts(Map<LimitType, PeriodicCardAmount> periodicCardAmountMap) {

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

    @Override
    public CardsBasic convertDTOToCardBasic(CardBasicAddDTO cardBasicAddDTO, ProductDef productDef) {

        Integer cardWaiverActivationDays;
        Integer cardReturnNumber;

        if(cardBasicAddDTO.getWaiverDaysActivation() != null){
            cardWaiverActivationDays = cardBasicAddDTO.getWaiverDaysActivation();
        } else {
            cardWaiverActivationDays = productDef.getCardsWaiverActivationDays();
        }

        Map<PeriodicType,Map<LimitType,PeriodicCardAmount>> periodicTypeMapMap = new HashMap<>();

        if(cardBasicAddDTO.getPeriodicCardLimitDTOList() != null){
            convertDTOPeriodicMap(cardBasicAddDTO.getPeriodicCardLimitDTOList(),periodicTypeMapMap);
        }


        CardsBasic.CardsBasicBuilder builder = CardsBasic.builder()
                .cardNumber(cardBasicAddDTO.getCardNumber())
                .org(cardBasicAddDTO.getOrg())
                .product(cardBasicAddDTO.getProduct())
                .cardStatus(Util.getCardStatus(cardBasicAddDTO.getCardStatus()))
                .cardholderType(Util.getCardHolderType(cardBasicAddDTO.getCardholderType()))
                .blockType(Util.getBlockType(cardBasicAddDTO.getBlockType()))
                .cardReturnNumber(0)
                .customerNumber(cardBasicAddDTO.getCustomerNumber())
                .waiverDaysActivation(cardWaiverActivationDays)
                .periodicTypePeriodicCardLimitMap(periodicTypeMapMap)
                .accountDefSet(cardBasicAddDTO.getAccountDefDTOSet()
                        .stream()
                        .map(accountDefDTO -> AccountDef.builder()
                                .accountNumber(accountDefDTO.getAccountNumber())
                                .billingCurrencyCode(accountDefDTO.getBillingCurrencyCode())
                                .accountType(Util.getAccountType(accountDefDTO.getAccountType()))
                                .build()
                        )
                        .collect(Collectors.toSet())
                )
                ;

        if(!cardBasicAddDTO.getBlockType().equals(Util.getBlockType(BlockType.APPROVE))){
            builder.dateBlockCode(LocalDateTime.now());
        }

        if(cardBasicAddDTO.getCorporateNumber() != null){
            builder.corporateNumber(cardBasicAddDTO.getCorporateNumber());
        }

        return builder.build();

    }

    private void convertDTOPeriodicMap(List<PeriodicCardLimitDTO> periodicCardLimitDTOList,
                                       Map<PeriodicType, Map<LimitType, PeriodicCardAmount>> periodicTypeMapMap) {

        periodicCardLimitDTOList.forEach(periodicCardLimitDTO -> {

            Map<LimitType, PeriodicCardAmount> limitMap = periodicTypeMapMap.get(Util.getPeriodicType(periodicCardLimitDTO.getPeriodicType()));

            if(limitMap == null){
                limitMap = new HashMap<>();
            }
            periodicTypeMapMap.put(Util.getPeriodicType(periodicCardLimitDTO.getPeriodicType()),updateLimitsFromDTO(periodicCardLimitDTO.getCardLimitsDTOList(),limitMap));
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


    @Override
    public CardsBasic updateCardBasicFromDTO(CardBasicUpdateDTO cardBasicUpdateDTO, ProductDef productDef,CardsBasic cardsBasic) {


            if(cardBasicUpdateDTO.getCardHolderType() != null){
                cardsBasic.setCardholderType(Util.getCardHolderType(cardBasicUpdateDTO.getCardHolderType()));
            }

            if(cardBasicUpdateDTO.getBlockType() != null){
                if(!cardBasicUpdateDTO.getBlockType().equals(Util.getBlockType(cardsBasic.getBlockType()))){
                    cardsBasic.setPrevBlockType(cardsBasic.getBlockType());
                    if(cardsBasic.getDateBlockCode() != null){
                        cardsBasic.setDatePrevBlockCode(cardsBasic.getDateBlockCode());
                    }
                    cardsBasic.setDateBlockCode(LocalDateTime.now());
                    cardsBasic.setBlockType(Util.getBlockType(cardBasicUpdateDTO.getBlockType()));
                }
            }
            if(cardBasicUpdateDTO.getCustomerNumber() != null){
                cardsBasic.setCustomerNumber(cardBasicUpdateDTO.getCustomerNumber());
            }
            
            if(cardBasicUpdateDTO.getCorporateNumber() != null){
                cardsBasic.setCorporateNumber(cardBasicUpdateDTO.getCorporateNumber());
            }
            
            if(cardBasicUpdateDTO.getAccountDefDTOSetAdd() != null){
                updateAccountDef(cardBasicUpdateDTO.getAccountDefDTOSetAdd(),cardsBasic);
            }

        if(cardBasicUpdateDTO.getAccountDefDTOSetDelete() != null){
            deleteAccountDef(cardBasicUpdateDTO.getAccountDefDTOSetDelete(),cardsBasic);
        }


        if(cardBasicUpdateDTO.getWaiverDaysActivation() != null){
                cardsBasic.setWaiverDaysActivation(cardBasicUpdateDTO.getWaiverDaysActivation());
            }
            if(cardBasicUpdateDTO.getCardsReturned() != null){
                cardsBasic.setCardReturnNumber(cardBasicUpdateDTO.getCardsReturned());
            }
            if(cardBasicUpdateDTO.getCardStatus() != null){
                cardsBasic.setCardStatus(Util.getCardStatus(cardBasicUpdateDTO.getCardStatus()));
            }
            Map<PeriodicType,Map<LimitType,PeriodicCardAmount>> periodicTypeMapMap;
            if(cardBasicUpdateDTO.getPeriodicCardLimitDTOAddList() != null){
                if(cardsBasic.getPeriodicTypePeriodicCardLimitMap() == null){
                    periodicTypeMapMap = new HashMap<>();
                } else {
                    periodicTypeMapMap = cardsBasic.getPeriodicTypePeriodicCardLimitMap();
                }
                convertDTOPeriodicMap(cardBasicUpdateDTO.getPeriodicCardLimitDTOAddList(),periodicTypeMapMap);

            }

            if(cardBasicUpdateDTO.getPeriodicCardLimitDTODeleteList() != null){
                if(cardsBasic.getPeriodicTypePeriodicCardLimitMap() != null){
                    deleteLimits(cardBasicUpdateDTO.getPeriodicCardLimitDTODeleteList(),cardsBasic.getPeriodicTypePeriodicCardLimitMap());
                }
            }

        return cardsBasic;
    }

    private void deleteAccountDef(Set<AccountDefDTO> accountDefDTOSetDelete, CardsBasic cardsBasic) {

        accountDefDTOSetDelete.stream()
                .map(accountDefDTO -> AccountDef.builder()
                        .accountType(Util.getAccountType(accountDefDTO.getAccountType()))
                        .billingCurrencyCode(accountDefDTO.getBillingCurrencyCode())
                        .accountNumber(accountDefDTO.getAccountNumber())
                        .build()
                )
                .forEach(accountDef -> cardsBasic.getAccountDefSet().remove(accountDef));
    }

    private void updateAccountDef(Set<AccountDefDTO> accountDefDTOSetAdd, CardsBasic cardsBasic) {
        
        accountDefDTOSetAdd.stream()
                .map(accountDefDTO -> AccountDef.builder()
                        .accountType(Util.getAccountType(accountDefDTO.getAccountType()))
                        .billingCurrencyCode(accountDefDTO.getBillingCurrencyCode())
                        .accountNumber(accountDefDTO.getAccountNumber())
                        .build()
                )
                .forEach(accountDef -> cardsBasic.getAccountDefSet().add(accountDef));
        
    }

    private void deleteLimits(List<PeriodicCardLimitDTO> periodicCardLimitDTODeleteList, Map<PeriodicType, Map<LimitType, PeriodicCardAmount>> periodicTypePeriodicCardLimitMap) {


        periodicCardLimitDTODeleteList
                .forEach(periodicCardLimitDTO -> {
                    Map<LimitType,PeriodicCardAmount> limitCardAmountMap = periodicTypePeriodicCardLimitMap.get(Util.getPeriodicType(periodicCardLimitDTO.getPeriodicType()));
                    if(limitCardAmountMap != null){
                        periodicCardLimitDTO.getCardLimitsDTOList()
                                .forEach(cardLimitsDTO -> {
                                    limitCardAmountMap.remove(Util.getLimitType(cardLimitsDTO.getLimitType()));
                                });
                    }
                });

    }


}
