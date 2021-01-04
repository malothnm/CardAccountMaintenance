package in.nmaloth.maintenance.service.cards;

import in.nmaloth.entity.BlockType;
import in.nmaloth.entity.account.AccountDef;
import in.nmaloth.entity.card.*;
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
    public Mono<CardsBasic> saveCardsRecord(CardsBasic cardsBasic) {
        return cardBasicDataService.saveCardBasicValues(cardsBasic);
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

        return cardBasicDataService.findCardBasicValuesById(cardBasicUpdateDTO.getCardId())
                .map(cardsBasicOptional -> {
                    if(cardsBasicOptional.isPresent()){
                        return cardsBasicOptional.get();
                    }
                    throw new NotFoundException("Card Number Not Found" + cardBasicUpdateDTO.getCardId());
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
                .product(cardsBasic.getProduct())
                .org(cardsBasic.getOrg())
                .cardStatus(Util.getCardStatus(cardsBasic.getCardStatus()))
                .cardholderType(Util.getCardHolderType(cardsBasic.getCardholderType()))
                .blockType(Util.getBlockType(cardsBasic.getBlockType()))
                .waiverDaysActivation(cardsBasic.getWaiverDaysActivation())
                .customerNumber(cardsBasic.getCustomerNumber())
                .cardId(cardsBasic.getCardId())
                .cardReturnNumber(cardsBasic.getCardReturnNumber())
                .accountDefDTOSet(cardsBasic.getAccountDefSet().stream()
                        .map(accountDef -> AccountDefDTO.builder()
                                .accountId(accountDef.getAccountNumber())
                                .billingCurrencyCode(accountDef.getBillingCurrencyCode())
                                .accountType(Util.getAccountType(accountDef.getAccountType()))
                                .build()
                        )
                        .collect(Collectors.toSet())

                )
                ;

        if(cardsBasic.getPlasticList() != null){
            List<PlasticsDTO> plasticsDTOList = cardsBasic.getPlasticList()
                    .stream()
                    .map(plastic -> convertPlasticDTO(plastic))
                    .collect(Collectors.toList());

            builder.plasticsDTOList(plasticsDTOList);
        }

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

        return builder.build();
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


        CardsBasic.CardsBasicBuilder builder = CardsBasic.builder()
                .org(cardBasicAddDTO.getOrg())
                .product(cardBasicAddDTO.getProduct())
                .cardStatus(Util.getCardStatus(cardBasicAddDTO.getCardStatus()))
                .cardholderType(Util.getCardHolderType(cardBasicAddDTO.getCardholderType()))
                .blockType(Util.getBlockType(cardBasicAddDTO.getBlockType()))
                .cardReturnNumber(0)
                .customerNumber(cardBasicAddDTO.getCustomerNumber())
                .waiverDaysActivation(cardWaiverActivationDays)
                .cardId(cardBasicAddDTO.getCardId())
                .accountDefSet(cardBasicAddDTO.getAccountDefDTOSet()
                        .stream()
                        .map(accountDefDTO -> AccountDef.builder()
                                .accountNumber(accountDefDTO.getAccountId())
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


        return cardsBasic;
    }

    @Override
    public PlasticsDTO convertPlasticDTO(Plastic plastic) {

        PlasticsDTO.PlasticsDTOBuilder builder = PlasticsDTO.builder()
                .plasticId(plastic.getPlasticId())
                .activationWaiveDuration(plastic.getActivationWaiveDuration().toDays())
                .cardAction(Util.getCardAction(plastic.getCardAction()))
                .cardActivated(plastic.getCardActivated());

        if(plastic.getExpiryDate() != null){
            builder.expiryDate(plastic.getExpiryDate());
        }

        if(plastic.getCardActivatedDate() != null){
            builder.cardActivatedDate(plastic.getCardActivatedDate());
        }
        if(plastic.getDatePlasticIssued() != null){
            builder.datePlasticIssued(plastic.getDatePlasticIssued());
        }
        if(plastic.getDateCardValidFrom() != null){
            builder.dateCardValidFrom(plastic.getDateCardValidFrom());
        }
        if(plastic.getDynamicCVV() != null){
            builder.dynamicCVV(plastic.getDynamicCVV());
        }
        if(plastic.getPendingCardAction() != null){
            builder.pendingCardAction(Util.getCardAction(plastic.getPendingCardAction()));
        }

        return builder.build();
    }

    private void deleteAccountDef(Set<AccountDefDTO> accountDefDTOSetDelete, CardsBasic cardsBasic) {

        accountDefDTOSetDelete.stream()
                .map(accountDefDTO -> AccountDef.builder()
                        .accountType(Util.getAccountType(accountDefDTO.getAccountType()))
                        .billingCurrencyCode(accountDefDTO.getBillingCurrencyCode())
                        .accountNumber(accountDefDTO.getAccountId())
                        .build()
                )
                .forEach(accountDef -> cardsBasic.getAccountDefSet().remove(accountDef));
    }

    private void updateAccountDef(Set<AccountDefDTO> accountDefDTOSetAdd, CardsBasic cardsBasic) {
        
        accountDefDTOSetAdd.stream()
                .map(accountDefDTO -> AccountDef.builder()
                        .accountType(Util.getAccountType(accountDefDTO.getAccountType()))
                        .billingCurrencyCode(accountDefDTO.getBillingCurrencyCode())
                        .accountNumber(accountDefDTO.getAccountId())
                        .build()
                )
                .forEach(accountDef -> cardsBasic.getAccountDefSet().add(accountDef));
        
    }


}
