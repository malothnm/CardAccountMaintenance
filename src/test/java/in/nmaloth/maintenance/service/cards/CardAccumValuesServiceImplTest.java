package in.nmaloth.maintenance.service.cards;

import in.nmaloth.entity.BlockType;
import in.nmaloth.entity.account.AccountType;
import in.nmaloth.entity.account.BalanceTypes;
import in.nmaloth.entity.card.*;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.entity.product.ProductId;
import in.nmaloth.maintenance.config.data.ProductTable;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.account.AccountDefDTO;
import in.nmaloth.maintenance.model.dto.card.*;
import in.nmaloth.maintenance.repository.card.CardAccumulatedValuesRepository;
import in.nmaloth.maintenance.repository.product.ProductDefRepository;
import in.nmaloth.maintenance.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CardAccumValuesServiceImplTest {

    @Autowired
    private CardAccumValuesService cardAccumValuesService;

    @Autowired
    private CardAccumulatedValuesRepository cardAccumulatedValuesRepository;


    @Autowired
    private ProductDefRepository productDefRepository;

    @Autowired
    private ProductTable productTable;

    @BeforeEach
    void setup() {

        cardAccumulatedValuesRepository.findAll()
                .forEach(cardAccumulatedValues -> cardAccumulatedValuesRepository.delete(cardAccumulatedValues));

        ProductDef productDef = createProductDef();

        productDefRepository.save(productDef);
        productTable.loadMap(productDef);

        ProductDef productDef1 = createProductDef();
        productDef1.getProductId().setOrg(001);
        productDef1.getProductId().setProduct(202);
        productDefRepository.save(productDef1);
        productTable.loadMap(productDef1);

        ProductDef productDef2 = createProductDef();
        productDef2.getProductId().setOrg(001);
        productDef2.getProductId().setProduct(203);
        productDefRepository.save(productDef2);
        productTable.loadMap(productDef2);


    }

    @Test
    void fetchCardAccumValuesByCardNumber() {

        String cardNumber = UUID.randomUUID().toString().replace("-", "");
        CardAccumulatedValues cardAccumulatedValues = createCardAccumValues(cardNumber);
        cardAccumulatedValuesRepository.save(cardAccumulatedValues);

        Mono<CardAccumulatedValues> cardAccumulatedValuesMono = cardAccumValuesService.fetchCardAccumValuesByCardNumber(cardNumber);
        StepVerifier
                .create(cardAccumulatedValuesMono)
                .expectNextCount(1)
                .verifyComplete();
    }


    @Test
    void fetchCardAccumValuesByCardNumber1() {

        String cardNumber = UUID.randomUUID().toString().replace("-", "");
        CardAccumulatedValues cardAccumulatedValues = createCardAccumValues(cardNumber);
//        cardAccumulatedValuesRepository.save(cardAccumulatedValues);

        Mono<CardAccumulatedValues> cardAccumulatedValuesMono = cardAccumValuesService.fetchCardAccumValuesByCardNumber(cardNumber);

        StepVerifier
                .create(cardAccumulatedValuesMono)
                .expectError(NotFoundException.class)
                .verify();

    }

    @Test
    void fetchCardAccumValuesOptional() {

        String cardNumber = UUID.randomUUID().toString().replace("-", "");
        CardAccumulatedValues cardAccumulatedValues = createCardAccumValues(cardNumber);
        cardAccumulatedValuesRepository.save(cardAccumulatedValues);

        Mono<Optional<CardAccumulatedValues>> cardAccumulatedValuesOptionalMono =
                cardAccumValuesService.fetchCardAccumValuesOptional(cardNumber);


        StepVerifier
                .create(cardAccumulatedValuesOptionalMono)
                .consumeNextWith(cardAccumulatedValuesOptional -> {
                    assertTrue(cardAccumulatedValuesOptional.isPresent());
                })
                .verifyComplete();

    }

    @Test
    void fetchCardAccumValuesOptional1() {

        String cardNumber = UUID.randomUUID().toString().replace("-", "");
        CardAccumulatedValues cardAccumulatedValues = createCardAccumValues(cardNumber);
//        cardAccumulatedValuesRepository.save(cardAccumulatedValues);

        Mono<Optional<CardAccumulatedValues>> cardAccumulatedValuesOptionalMono =
                cardAccumValuesService.fetchCardAccumValuesOptional(cardNumber);


        StepVerifier
                .create(cardAccumulatedValuesOptionalMono)
                .consumeNextWith(cardAccumulatedValuesOptional -> {
                    assertFalse(cardAccumulatedValuesOptional.isPresent());
                })
                .verifyComplete();

    }

    @Test
    void deleteCardAccumValuesByCardNumber() {

        String cardNumber = UUID.randomUUID().toString().replace("-", "");
        CardAccumulatedValues cardAccumulatedValues = createCardAccumValues(cardNumber);
        cardAccumulatedValuesRepository.save(cardAccumulatedValues);

        cardAccumValuesService.deleteCardAccumValuesByCardNumber(cardNumber).block();
        Optional<CardAccumulatedValues> cardAccumulatedValuesOptional = cardAccumulatedValuesRepository.findById(cardNumber);

        assertTrue(cardAccumulatedValuesOptional.isEmpty());

    }

    @Test
    void deleteCardAccumValuesByCardNumber1() {

        String cardNumber = UUID.randomUUID().toString().replace("-", "");
        CardAccumulatedValues cardAccumulatedValues = createCardAccumValues(cardNumber);
//        cardAccumulatedValuesRepository.save(cardAccumulatedValues);

        Mono<CardAccumulatedValues> cardAccumulatedValuesMono = cardAccumValuesService.deleteCardAccumValuesByCardNumber(cardNumber);

        StepVerifier.create(cardAccumulatedValuesMono)
                .expectError(NotFoundException.class)
                .verify();

    }

    @Test
    void saveCardAccumValues() {

        String cardNumber = UUID.randomUUID().toString().replace("-", "");
        CardAccumulatedValues cardAccumulatedValues = createCardAccumValues(cardNumber);
        cardAccumValuesService.saveAccountAccumValues(cardAccumulatedValues).block();

        Optional<CardAccumulatedValues> cardAccumulatedValuesOptional = cardAccumulatedValuesRepository.findById(cardNumber);

        assertTrue(cardAccumulatedValuesOptional.isPresent());
    }


    @Test
    void initializeAccumValuesOnDb(){

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");
        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO(deleteAccountNumber);



        CardAccumulatedValues cardAccumulatedValues1 = cardAccumValuesService.initializeCardAccumValues(cardBasicAddDTO.getCardId(),
                cardBasicAddDTO.getPeriodicCardLimitDTOList(), cardBasicAddDTO.getOrg(), cardBasicAddDTO.getProduct()).block();

        CardAccumulatedValues cardAccumulatedValues = cardAccumulatedValuesRepository.findById(cardBasicAddDTO.getCardId()).get();

        assertAll(
                () -> assertEquals(cardBasicAddDTO.getCardId(), cardAccumulatedValues.getCardId()),
                () -> assertEquals(1, cardAccumulatedValues.getOrg()),
                () -> assertEquals(201, cardAccumulatedValues.getProduct()),
                () -> assertEquals(3, cardAccumulatedValues.getPeriodicCardAccumulatedValueMap().get(PeriodicType.SINGLE).size()),
                () -> assertEquals(3, cardAccumulatedValues.getPeriodicCardAccumulatedValueMap().get(PeriodicType.DAILY).size()),
                () -> assertEquals(0L, cardAccumulatedValues.getPeriodicCardAccumulatedValueMap().get(PeriodicType.SINGLE).get(LimitType.NO_SPECIFIC).getTransactionAmount()),
                () -> assertEquals(0, cardAccumulatedValues.getPeriodicCardAccumulatedValueMap()
                        .get(PeriodicType.SINGLE).get(LimitType.NO_SPECIFIC).getTransactionNumber()),
                () -> assertEquals(0L, cardAccumulatedValues.getPeriodicCardAccumulatedValueMap().get(PeriodicType.DAILY).get(LimitType.RETAIL).getTransactionAmount()),
                () -> assertEquals(0, cardAccumulatedValues.getPeriodicCardAccumulatedValueMap()
                        .get(PeriodicType.DAILY).get(LimitType.RETAIL).getTransactionNumber()),
                () -> assertEquals(0L, cardAccumulatedValues.getPeriodicCardAccumulatedValueMap().get(PeriodicType.SINGLE).get(LimitType.CASH).getTransactionAmount()),
                () -> assertEquals(0, cardAccumulatedValues.getPeriodicCardAccumulatedValueMap()
                        .get(PeriodicType.DAILY).get(LimitType.CASH).getTransactionNumber()),

                () -> assertEquals(3, cardAccumulatedValues.getPeriodicTypePeriodicCardLimitMap().get(PeriodicType.SINGLE).size()),
                () -> assertEquals(3, cardAccumulatedValues.getPeriodicTypePeriodicCardLimitMap().get(PeriodicType.DAILY).size()),
                () -> assertEquals(10000L, cardAccumulatedValues.getPeriodicTypePeriodicCardLimitMap().get(PeriodicType.SINGLE).get(LimitType.NO_SPECIFIC).getTransactionAmount()),
                () -> assertEquals(100, cardAccumulatedValues.getPeriodicTypePeriodicCardLimitMap()
                        .get(PeriodicType.SINGLE).get(LimitType.NO_SPECIFIC).getTransactionNumber()),
                () -> assertEquals(20000L, cardAccumulatedValues.getPeriodicTypePeriodicCardLimitMap().get(PeriodicType.SINGLE).get(LimitType.CASH).getTransactionAmount()),
                () -> assertEquals(200, cardAccumulatedValues.getPeriodicTypePeriodicCardLimitMap()
                        .get(PeriodicType.SINGLE).get(LimitType.CASH).getTransactionNumber()),
                () -> assertEquals(30000L, cardAccumulatedValues.getPeriodicTypePeriodicCardLimitMap().get(PeriodicType.DAILY).get(LimitType.RETAIL).getTransactionAmount()),
                () -> assertEquals(300, cardAccumulatedValues.getPeriodicTypePeriodicCardLimitMap()
                        .get(PeriodicType.DAILY).get(LimitType.RETAIL).getTransactionNumber())
        );



    }

    @Test
    void initializeAccumValuesOnDb1(){

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");
        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO(deleteAccountNumber);



        Mono<CardAccumulatedValues> cardAccumulatedValuesMono = cardAccumValuesService.initializeCardAccumValues(cardBasicAddDTO.getCardId(),
                cardBasicAddDTO.getPeriodicCardLimitDTOList(), cardBasicAddDTO.getOrg(), cardBasicAddDTO.getProduct());

        StepVerifier.create(cardAccumulatedValuesMono)
                .expectNextCount(1)
                .verifyComplete();

    }

    @Test
    void convertToDTO() {

        CardAccumulatedValues cardAccumulatedValues = createCardAccumValues(UUID.randomUUID().toString().replace("-", ""));

        CardAccumValuesDTO cardAccumValuesDTO = cardAccumValuesService.convertToDTO(cardAccumulatedValues);

        List<PeriodicCardLimitDTO> periodicAccumList = cardAccumValuesDTO.getPeriodicCardAccumulatedValueList();
        List<CardLimitsDTO> cardAccumDTOListSingle = extractLimitCardDTO(PeriodicType.SINGLE, periodicAccumList).getCardLimitsDTOList();
        List<CardLimitsDTO> cardAccumDTOListDaily = extractLimitCardDTO(PeriodicType.DAILY, periodicAccumList).getCardLimitsDTOList();
        List<CardLimitsDTO> cardAccumDTOListMonthly = extractLimitCardDTO(PeriodicType.MONTHLY, periodicAccumList).getCardLimitsDTOList();

        List<PeriodicCardLimitDTO> periodicLimitList = cardAccumValuesDTO.getPeriodicCardLimitDTOList();
        List<CardLimitsDTO> cardLimitsDTOListSingle = extractLimitCardDTO(PeriodicType.SINGLE, periodicLimitList).getCardLimitsDTOList();
        List<CardLimitsDTO> cardLimitsDTOListDaily = extractLimitCardDTO(PeriodicType.DAILY, periodicLimitList).getCardLimitsDTOList();
        List<CardLimitsDTO> cardLimitsDTOListMonthly = extractLimitCardDTO(PeriodicType.MONTHLY, periodicLimitList).getCardLimitsDTOList();





        Long amtSingleNoSpecific = extractCardLimit(LimitType.NO_SPECIFIC, cardAccumDTOListSingle).getLimitAmount();
        Integer nbrSingleCash = extractCardLimit(LimitType.CASH, cardAccumDTOListSingle).getLimitNumber();

        Long amtRetailMonth = extractCardLimit(LimitType.RETAIL, cardAccumDTOListMonthly).getLimitAmount();
        Integer nbrOTCMonth = extractCardLimit(LimitType.OTC, cardAccumDTOListMonthly).getLimitNumber();

        Long amtCashDaily = extractCardLimit(LimitType.CASH, cardAccumDTOListDaily).getLimitAmount();
        Integer nbrNoSpecificDaily = extractCardLimit(LimitType.NO_SPECIFIC, cardAccumDTOListDaily).getLimitNumber();


        Long limitSingleNoSpecific = extractCardLimit(LimitType.NO_SPECIFIC, cardLimitsDTOListSingle).getLimitAmount();
        Integer nbrLimitSingleCash = extractCardLimit(LimitType.CASH, cardLimitsDTOListSingle).getLimitNumber();

        Long limitRetailMonth = extractCardLimit(LimitType.RETAIL, cardAccumDTOListMonthly).getLimitAmount();
        Integer nbrLimitOTCMonth = extractCardLimit(LimitType.OTC, cardLimitsDTOListMonthly).getLimitNumber();

        Long limitCashDaily = extractCardLimit(LimitType.CASH, cardLimitsDTOListDaily).getLimitAmount();
        Integer nbrLimitNoSpecificDaily = extractCardLimit(LimitType.NO_SPECIFIC, cardLimitsDTOListDaily).getLimitNumber();



        assertAll(
                () -> assertEquals(1000, limitSingleNoSpecific),
                () -> assertEquals(300, nbrLimitSingleCash),
                () -> assertEquals(2000, limitRetailMonth),
                () -> assertEquals(400, nbrLimitOTCMonth),
                () -> assertEquals(3000, limitCashDaily),
                () -> assertEquals(100, nbrLimitNoSpecificDaily),
                () -> assertEquals(1000, amtSingleNoSpecific),
                () -> assertEquals(300, nbrSingleCash),
                () -> assertEquals(2000, amtRetailMonth),
                () -> assertEquals(400, nbrOTCMonth),
                () -> assertEquals(3000, amtCashDaily),
                () -> assertEquals(100, nbrNoSpecificDaily),
                () -> assertEquals(cardAccumulatedValues.getCardId(), cardAccumValuesDTO.getCardId()),
                () -> assertEquals(cardAccumulatedValues.getOrg(), cardAccumValuesDTO.getOrg()),
                () -> assertEquals(cardAccumulatedValues.getProduct(), cardAccumValuesDTO.getProduct())
        );


    }

    @Test
    void initializeAccumValues() {

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");
        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO(deleteAccountNumber);



        CardAccumulatedValues cardAccumulatedValues = cardAccumValuesService.initializeAccumValues(cardBasicAddDTO.getCardId(),
                cardBasicAddDTO.getPeriodicCardLimitDTOList(), cardBasicAddDTO.getOrg(), cardBasicAddDTO.getProduct());

        assertAll(
                () -> assertEquals(cardBasicAddDTO.getCardId(), cardAccumulatedValues.getCardId()),
                () -> assertEquals(1, cardAccumulatedValues.getOrg()),
                () -> assertEquals(201, cardAccumulatedValues.getProduct()),
                () -> assertEquals(3, cardAccumulatedValues.getPeriodicCardAccumulatedValueMap().get(PeriodicType.SINGLE).size()),
                () -> assertEquals(3, cardAccumulatedValues.getPeriodicCardAccumulatedValueMap().get(PeriodicType.DAILY).size()),
                () -> assertEquals(0L, cardAccumulatedValues.getPeriodicCardAccumulatedValueMap().get(PeriodicType.SINGLE).get(LimitType.NO_SPECIFIC).getTransactionAmount()),
                () -> assertEquals(0, cardAccumulatedValues.getPeriodicCardAccumulatedValueMap()
                        .get(PeriodicType.SINGLE).get(LimitType.NO_SPECIFIC).getTransactionNumber()),
                () -> assertEquals(0L, cardAccumulatedValues.getPeriodicCardAccumulatedValueMap().get(PeriodicType.DAILY).get(LimitType.RETAIL).getTransactionAmount()),
                () -> assertEquals(0, cardAccumulatedValues.getPeriodicCardAccumulatedValueMap()
                        .get(PeriodicType.DAILY).get(LimitType.RETAIL).getTransactionNumber()),
                () -> assertEquals(0L, cardAccumulatedValues.getPeriodicCardAccumulatedValueMap().get(PeriodicType.SINGLE).get(LimitType.CASH).getTransactionAmount()),
                () -> assertEquals(0, cardAccumulatedValues.getPeriodicCardAccumulatedValueMap()
                        .get(PeriodicType.DAILY).get(LimitType.CASH).getTransactionNumber()),

                () -> assertEquals(3, cardAccumulatedValues.getPeriodicTypePeriodicCardLimitMap().get(PeriodicType.SINGLE).size()),
                () -> assertEquals(3, cardAccumulatedValues.getPeriodicTypePeriodicCardLimitMap().get(PeriodicType.DAILY).size()),
                () -> assertEquals(10000L, cardAccumulatedValues.getPeriodicTypePeriodicCardLimitMap().get(PeriodicType.SINGLE).get(LimitType.NO_SPECIFIC).getTransactionAmount()),
                () -> assertEquals(100, cardAccumulatedValues.getPeriodicTypePeriodicCardLimitMap()
                        .get(PeriodicType.SINGLE).get(LimitType.NO_SPECIFIC).getTransactionNumber()),
                () -> assertEquals(20000L, cardAccumulatedValues.getPeriodicTypePeriodicCardLimitMap().get(PeriodicType.SINGLE).get(LimitType.CASH).getTransactionAmount()),
                () -> assertEquals(200, cardAccumulatedValues.getPeriodicTypePeriodicCardLimitMap()
                        .get(PeriodicType.SINGLE).get(LimitType.CASH).getTransactionNumber()),
                () -> assertEquals(30000L, cardAccumulatedValues.getPeriodicTypePeriodicCardLimitMap().get(PeriodicType.DAILY).get(LimitType.RETAIL).getTransactionAmount()),
                () -> assertEquals(300, cardAccumulatedValues.getPeriodicTypePeriodicCardLimitMap()
                        .get(PeriodicType.DAILY).get(LimitType.RETAIL).getTransactionNumber())

        );
    }

    @Test
    void updateNewAccumValues() {

        String cardNumber = UUID.randomUUID().toString().replace("-", "");
        CardAccumulatedValues cardAccumulatedValues = createCardAccumValues(cardNumber);

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");
        CardBasicUpdateDTO cardBasicUpdateDTO = createCardBasic(true,null,deleteAccountNumber);
        cardBasicUpdateDTO.setCardId(cardAccumulatedValues.getCardId());

        CardAccumulatedValues cardAccumulatedValues1 = cardAccumValuesService
                .updateNewAccumValues(cardBasicUpdateDTO.getPeriodicCardLimitDTOAddList(),
                        cardBasicUpdateDTO.getPeriodicCardLimitDTODeleteList(),cardAccumulatedValues);



        Long amtSingleNoSpecific = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.SINGLE).get(LimitType.NO_SPECIFIC).getTransactionAmount();
        Integer nbrSingleNoSpecific = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.SINGLE).get(LimitType.NO_SPECIFIC).getTransactionNumber();
        Long amtSingleRetail = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.SINGLE).get(LimitType.RETAIL).getTransactionAmount();
        Integer nbrSingleRetail= cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.SINGLE).get(LimitType.RETAIL).getTransactionNumber();
        Long amtSingleCash = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.SINGLE).get(LimitType.CASH).getTransactionAmount();
        Integer nbrSingleCash = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.SINGLE).get(LimitType.CASH).getTransactionNumber();
        Long amtSingleOTC = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.SINGLE).get(LimitType.OTC).getTransactionAmount();
        Integer nbrSingleOTC= cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.SINGLE).get(LimitType.OTC).getTransactionNumber();
        Long amtSingleQuasiCash = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.SINGLE).get(LimitType.QUASI_CASH).getTransactionAmount();
        Integer nbrSingleQuasiCash= cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.SINGLE).get(LimitType.QUASI_CASH).getTransactionNumber();
        Long amtMonthlyNoSpecific = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.MONTHLY).get(LimitType.NO_SPECIFIC).getTransactionAmount();
        Integer nbrMonthlyNoSpecific = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.MONTHLY).get(LimitType.NO_SPECIFIC).getTransactionNumber();
        Long amtMonthlyRetail = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.MONTHLY).get(LimitType.RETAIL).getTransactionAmount();
        Integer nbrMonthlyRetail= cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.MONTHLY).get(LimitType.RETAIL).getTransactionNumber();
        Long amtMonthlyCash = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.MONTHLY).get(LimitType.CASH).getTransactionAmount();
        Integer nbrMonthlyCash = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.MONTHLY).get(LimitType.CASH).getTransactionNumber();
        Long amtMonthlyOTC = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.MONTHLY).get(LimitType.OTC).getTransactionAmount();
        Integer nbrMonthlyOTC= cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.MONTHLY).get(LimitType.OTC).getTransactionNumber();
        Long amtMonthlyQuasiCash = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.MONTHLY).get(LimitType.QUASI_CASH).getTransactionAmount();
        Integer nbrMonthlyQuasiCash= cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.DAILY).get(LimitType.QUASI_CASH).getTransactionNumber();
        Long amtDailyNoSpecific = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.DAILY).get(LimitType.NO_SPECIFIC).getTransactionAmount();
        Integer nbrDailyNoSpecific = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.DAILY).get(LimitType.NO_SPECIFIC).getTransactionNumber();
        Long amtDailyRetail = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.DAILY).get(LimitType.RETAIL).getTransactionAmount();
        Integer nbrDailyRetail= cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.DAILY).get(LimitType.RETAIL).getTransactionNumber();
        Long amtDailyCash = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.DAILY).get(LimitType.CASH).getTransactionAmount();
        Integer nbrDailyCash = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.DAILY).get(LimitType.CASH).getTransactionNumber();
        Long amtDailyOTC = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.DAILY).get(LimitType.OTC).getTransactionAmount();
        Integer nbrDailyOTC= cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.DAILY).get(LimitType.OTC).getTransactionNumber();
        Long amtDailyQuasiCash = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.DAILY).get(LimitType.QUASI_CASH).getTransactionAmount();
        Integer nbrDailyQuasiCash= cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.DAILY).get(LimitType.QUASI_CASH).getTransactionNumber();


        int singleAccumCount = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.SINGLE).size();
        int dailyAccumCount = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.DAILY).size();
        int monthlyAccumCount = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.MONTHLY).size();

        int singleLimitCount = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.SINGLE).size();
        int dailyLimitCount = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.DAILY).size();
        int monthlyLimitCount = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.MONTHLY).size();


        PeriodicCardAmount limitSingleNoSpecific = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.SINGLE).get(LimitType.NO_SPECIFIC);
//        Integer nbrLimitSingleNoSpecific = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
//                .get(PeriodicType.SINGLE).get(LimitType.NO_SPECIFIC).getTransactionNumber();
        Long limitSingleRetail = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.SINGLE).get(LimitType.RETAIL).getTransactionAmount();
        Integer nbrLimitSingleRetail= cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.SINGLE).get(LimitType.RETAIL).getTransactionNumber();
        PeriodicCardAmount limitSingleCash = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.SINGLE).get(LimitType.CASH);
//        Integer nbrLimitSingleCash = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
//                .get(PeriodicType.SINGLE).get(LimitType.CASH).getTransactionNumber();
        Long limitSingleOTC = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.SINGLE).get(LimitType.OTC).getTransactionAmount();
        Integer nbrLimitSingleOTC= cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.SINGLE).get(LimitType.OTC).getTransactionNumber();
        Long limitSingleQuasiCash = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.SINGLE).get(LimitType.QUASI_CASH).getTransactionAmount();
        Integer nbrLimitSingleQuasiCash= cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.SINGLE).get(LimitType.QUASI_CASH).getTransactionNumber();
        Long limitMonthlyNoSpecific = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.MONTHLY).get(LimitType.NO_SPECIFIC).getTransactionAmount();
        Integer nbrLimitMonthlyNoSpecific = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.MONTHLY).get(LimitType.NO_SPECIFIC).getTransactionNumber();
        Long limitMonthlyRetail = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.MONTHLY).get(LimitType.RETAIL).getTransactionAmount();
        Integer nbrLimitMonthlyRetail= cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.MONTHLY).get(LimitType.RETAIL).getTransactionNumber();
        Long limitMonthlyCash = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.MONTHLY).get(LimitType.CASH).getTransactionAmount();
        Integer nbrLimitMonthlyCash = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.MONTHLY).get(LimitType.CASH).getTransactionNumber();
        Long limitMonthlyOTC = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.MONTHLY).get(LimitType.OTC).getTransactionAmount();
        Integer nbrLimitMonthlyOTC= cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.MONTHLY).get(LimitType.OTC).getTransactionNumber();
        Long limitMonthlyQuasiCash = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.MONTHLY).get(LimitType.QUASI_CASH).getTransactionAmount();
        Integer nbrLimitMonthlyQuasiCash= cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.DAILY).get(LimitType.QUASI_CASH).getTransactionNumber();
        PeriodicCardAmount limitDailyNoSpecific = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.DAILY).get(LimitType.NO_SPECIFIC);
//        Integer nbrLimitDailyNoSpecific = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
//                .get(PeriodicType.DAILY).get(LimitType.NO_SPECIFIC).getTransactionNumber();
        Long limitDailyRetail = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.DAILY).get(LimitType.RETAIL).getTransactionAmount();
        Integer nbrLimitDailyRetail= cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.DAILY).get(LimitType.RETAIL).getTransactionNumber();
        PeriodicCardAmount limitDailyCash = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.DAILY).get(LimitType.CASH);
//        Integer nbrLimitDailyCash = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
//                .get(PeriodicType.DAILY).get(LimitType.CASH).getTransactionNumber();
        Long limitDailyOTC = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.DAILY).get(LimitType.OTC).getTransactionAmount();
        Integer nbrLimitDailyOTC= cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.DAILY).get(LimitType.OTC).getTransactionNumber();
        Long limitDailyQuasiCash = cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.DAILY).get(LimitType.QUASI_CASH).getTransactionAmount();
        Integer nbrLimitDailyQuasiCash= cardAccumulatedValues1.getPeriodicTypePeriodicCardLimitMap()
                .get(PeriodicType.DAILY).get(LimitType.QUASI_CASH).getTransactionNumber();

        assertAll(
                () -> assertEquals(1000, amtSingleNoSpecific ),
                () -> assertEquals(100, nbrSingleNoSpecific ),
                () -> assertEquals(2000, amtSingleRetail  ),
                () -> assertEquals(200, nbrSingleRetail ),
                () -> assertEquals(3000, amtSingleCash   ),
                () -> assertEquals(300, nbrSingleCash  ),
                () -> assertEquals(4000, amtSingleOTC   ),
                () -> assertEquals(400, nbrSingleOTC ),
                () -> assertEquals(0, amtSingleQuasiCash ),
                () -> assertEquals(0, nbrSingleQuasiCash ),

                () -> assertEquals(1000, amtDailyNoSpecific ),
                () -> assertEquals(100, nbrDailyNoSpecific ),
                () -> assertEquals(2000, amtDailyRetail  ),
                () -> assertEquals(200, nbrDailyRetail ),
                () -> assertEquals(3000, amtDailyCash   ),
                () -> assertEquals(300, nbrDailyCash  ),
                () -> assertEquals(4000, amtDailyOTC   ),
                () -> assertEquals(400, nbrDailyOTC ),
                () -> assertEquals(0, amtDailyQuasiCash ),
                () -> assertEquals(0, nbrDailyQuasiCash ),

                () -> assertEquals(1000, amtMonthlyNoSpecific ),
                () -> assertEquals(100, nbrMonthlyNoSpecific ),
                () -> assertEquals(2000, amtMonthlyRetail  ),
                () -> assertEquals(200, nbrMonthlyRetail ),
                () -> assertEquals(3000, amtMonthlyCash   ),
                () -> assertEquals(300, nbrMonthlyCash  ),
                () -> assertEquals(4000, amtMonthlyOTC   ),
                () -> assertEquals(400, nbrMonthlyOTC ),
                () -> assertEquals(0, amtMonthlyQuasiCash ),
                () -> assertEquals(0, nbrMonthlyQuasiCash ),

                () -> assertNull(limitSingleNoSpecific ),
                () -> assertEquals(2000, limitSingleRetail  ),
                () -> assertEquals(200, nbrLimitSingleRetail ),
                () -> assertNull( limitSingleCash),
//                () -> assertNull(300, nbrLimitSingleCash  ),
                () -> assertEquals(60000, limitSingleOTC   ),
                () -> assertEquals(600, nbrLimitSingleOTC ),
                () -> assertEquals(70000, limitSingleQuasiCash ),
                () -> assertEquals(700, nbrLimitSingleQuasiCash ),

                () -> assertNull(limitDailyNoSpecific ),
                () -> assertEquals(2000, limitDailyRetail  ),
                () -> assertEquals(200, nbrLimitDailyRetail ),
                () -> assertNull( limitDailyCash),
//                () -> assertNull(300, nbrLimitSingleCash  ),
                () -> assertEquals(60000, limitDailyOTC   ),
                () -> assertEquals(600, nbrLimitDailyOTC ),
                () -> assertEquals(70000, limitDailyQuasiCash ),
                () -> assertEquals(700, nbrLimitDailyQuasiCash ),
                () -> assertEquals(40000,limitMonthlyNoSpecific ),
                () -> assertEquals(400,nbrLimitMonthlyNoSpecific ),
                () -> assertEquals(2000, limitDailyRetail  ),
                () -> assertEquals(200, nbrLimitDailyRetail ),
                () -> assertEquals( 3000,limitMonthlyCash),
                () -> assertEquals(300, nbrLimitMonthlyCash  ),
                () -> assertEquals(60000, limitDailyOTC   ),
                () -> assertEquals(600, nbrLimitDailyOTC ),
                () -> assertEquals(70000, limitDailyQuasiCash ),
                () -> assertEquals(700, nbrLimitDailyQuasiCash ),
                ()-> assertEquals(5,singleAccumCount),
                ()-> assertEquals(5,dailyAccumCount ),
                ()-> assertEquals(5,monthlyAccumCount ),
                ()-> assertEquals(3, singleLimitCount ),
                ()-> assertEquals(3, dailyLimitCount ),
                ()-> assertEquals(5,monthlyLimitCount)



        );

    }

    private PeriodicCardLimitDTO extractLimitCardDTO(PeriodicType periodicType, List<PeriodicCardLimitDTO> periodicCardLimitDTOList) {

        return periodicCardLimitDTOList
                .stream()
                .filter(periodicCardLimitDTO -> periodicCardLimitDTO.getPeriodicType().equals(Util.getPeriodicType(periodicType)))
                .findFirst()
                .get();
    }

    private CardLimitsDTO extractCardLimit(LimitType limitType, List<CardLimitsDTO> cardLimitsDTOList) {

        return cardLimitsDTOList
                .stream()
                .filter(cardLimitsDTO -> cardLimitsDTO.getLimitType().equals(Util.getLimitType(limitType)))
                .findFirst()
                .get();

    }

    private CardAccumulatedValues createCardAccumValues(String cardNumber) {



        Map<PeriodicType, Map<LimitType, PeriodicCardAmount>> periodicMap = new HashMap<>();

        Map<PeriodicType, Map<LimitType, PeriodicCardAmount>> periodicLimitMap = new HashMap<>();


        Map<LimitType, PeriodicCardAmount> limitAmountMapSingle = new HashMap<>();
        Map<LimitType, PeriodicCardAmount> limitAmountMapDaily = new HashMap<>();
        Map<LimitType, PeriodicCardAmount> limitAmountMapMonthly = new HashMap<>();

        Map<LimitType, PeriodicCardAmount> limitMapSingle = new HashMap<>();
        Map<LimitType, PeriodicCardAmount> limitMapDaily = new HashMap<>();
        Map<LimitType, PeriodicCardAmount> limitMapMonthly = new HashMap<>();


        createLimitTypeMap(limitAmountMapSingle);
        createLimitTypeMap(limitAmountMapDaily);
        createLimitTypeMap(limitAmountMapMonthly);


        createLimitTypeMap(limitMapSingle);
        createLimitTypeMap(limitMapDaily);
        createLimitTypeMap(limitMapMonthly);



        periodicMap.put(PeriodicType.SINGLE, limitAmountMapSingle);
        periodicMap.put(PeriodicType.DAILY, limitAmountMapDaily);
        periodicMap.put(PeriodicType.MONTHLY, limitAmountMapMonthly);


        periodicLimitMap.put(PeriodicType.SINGLE, limitMapSingle);
        periodicLimitMap.put(PeriodicType.DAILY, limitMapDaily);
        periodicLimitMap.put(PeriodicType.MONTHLY, limitMapMonthly);


        return CardAccumulatedValues.builder()
                .cardId(cardNumber)
                .org(001)
                .product(201)
                .periodicCardAccumulatedValueMap(periodicMap)
                .periodicTypePeriodicCardLimitMap(periodicLimitMap)
                .build()
                ;
    }


    private void createLimitTypeMap( Map<LimitType, PeriodicCardAmount> limitAmountMap){

        PeriodicCardAmount periodicCardAmountNoSpecific = PeriodicCardAmount.builder()
                .transactionAmount(1000L)
                .transactionNumber(100)
                .limitType(LimitType.NO_SPECIFIC)
                .build();

        PeriodicCardAmount periodicCardAmountRetail = PeriodicCardAmount.builder()
                .transactionAmount(2000L)
                .transactionNumber(200)
                .limitType(LimitType.RETAIL)
                .build();

        PeriodicCardAmount periodicCardAmountCash = PeriodicCardAmount.builder()
                .transactionNumber(300)
                .transactionAmount(3000L)
                .limitType(LimitType.CASH)
                .build();
        PeriodicCardAmount periodicCardAmountOTC = PeriodicCardAmount.builder()
                .transactionAmount(4000L)
                .transactionNumber(400)
                .limitType(LimitType.OTC)
                .build();

        limitAmountMap.put(periodicCardAmountNoSpecific.getLimitType(), periodicCardAmountNoSpecific);
        limitAmountMap.put(periodicCardAmountRetail.getLimitType(), periodicCardAmountRetail);
        limitAmountMap.put(periodicCardAmountCash.getLimitType(), periodicCardAmountCash);
        limitAmountMap.put(periodicCardAmountOTC.getLimitType(), periodicCardAmountOTC);

    }


    private CardBasicAddDTO createCardBasicAddDTO(String deleteAccountNumber) {


        AccountDefDTO accountDefDTO1 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.SAVINGS))
                .billingCurrencyCode("124")
                .accountId(UUID.randomUUID().toString().replace("-", ""))
                .build();


        AccountDefDTO accountDefDTO4 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.UNIVERSAL))
                .billingCurrencyCode("840")
                .accountId(deleteAccountNumber)
                .build();

        Set<AccountDefDTO> accountDefDTOSet = new HashSet<>();


        accountDefDTOSet.add(accountDefDTO1);
        accountDefDTOSet.add(accountDefDTO4);

        List<PeriodicCardLimitDTO> periodicCardLimitDTOList = new ArrayList<>();

        List<CardLimitsDTO> cardLimitsDTOList = new ArrayList<>();

        CardLimitsDTO cardLimitsDTO1 = CardLimitsDTO.builder()
                .limitType(Util.getLimitType(LimitType.NO_SPECIFIC))
                .limitNumber(100)
                .limitAmount(10000L)
                .build();

        CardLimitsDTO cardLimitsDTO2 = CardLimitsDTO.builder()
                .limitType(Util.getLimitType(LimitType.CASH))
                .limitNumber(200)
                .limitAmount(20000L)
                .build();

        CardLimitsDTO cardLimitsDTO3 = CardLimitsDTO.builder()
                .limitType(Util.getLimitType(LimitType.RETAIL))
                .limitNumber(300)
                .limitAmount(30000L)
                .build();

        cardLimitsDTOList.add(cardLimitsDTO1);
        cardLimitsDTOList.add(cardLimitsDTO2);
        cardLimitsDTOList.add(cardLimitsDTO3);


        PeriodicCardLimitDTO periodicCardLimitDTO1 = PeriodicCardLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.SINGLE))
                .cardLimitsDTOList(cardLimitsDTOList)
                .build();


        PeriodicCardLimitDTO periodicCardLimitDTO2 = PeriodicCardLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.DAILY))
                .cardLimitsDTOList(cardLimitsDTOList)
                .build();

        periodicCardLimitDTOList.add(periodicCardLimitDTO1);
        periodicCardLimitDTOList.add(periodicCardLimitDTO2);


        return CardBasicAddDTO.builder()
                .cardId(Util.generateCardNumberFromStarter("491652996363189"))
                .cardholderType(Util.getCardHolderType(CardHolderType.PRIMARY))
                .blockType(Util.getBlockType(BlockType.APPROVE))
                .cardStatus(Util.getCardStatus(CardStatus.ACTIVE))
                .org(001)
                .product(201)
                .waiverDaysActivation(10)
                .periodicCardLimitDTOList(periodicCardLimitDTOList)
                .accountDefDTOSet(accountDefDTOSet)
                .customerNumber(UUID.randomUUID().toString().replace("-", ""))
                .build();
    }

    private CardBasicUpdateDTO createCardBasic(boolean allFields, List<Integer> integerList, String deleteAccountNumber) {

        CardBasicUpdateDTO.CardBasicUpdateDTOBuilder builder = CardBasicUpdateDTO.builder()
                .cardId(Util.generateCardNumberFromStarter("491652996363189"));

        List<PeriodicCardLimitDTO> periodicCardLimitDTOList = new ArrayList<>();
        List<PeriodicCardLimitDTO> periodicCardLimitDTOListDelete = new ArrayList<>();


        List<CardLimitsDTO> cardLimitsDTOList = new ArrayList<>();
        List<CardLimitsDTO> cardLimitsDTOListDelete = new ArrayList<>();


        CardLimitsDTO cardLimitsDTO1 = CardLimitsDTO.builder()
                .limitType(Util.getLimitType(LimitType.NO_SPECIFIC))
                .limitNumber(400)
                .limitAmount(40000L)
                .build();

        CardLimitsDTO cardLimitsDTO2 = CardLimitsDTO.builder()
                .limitType(Util.getLimitType(LimitType.OTC))
                .limitNumber(600)
                .limitAmount(60000L)
                .build();

        CardLimitsDTO cardLimitsDTO3 = CardLimitsDTO.builder()
                .limitType(Util.getLimitType(LimitType.QUASI_CASH))
                .limitNumber(700)
                .limitAmount(70000L)
                .build();

        CardLimitsDTO cardLimitsDTO4 = CardLimitsDTO.builder()
                .limitType(Util.getLimitType(LimitType.CASH))
                .limitNumber(800)
                .limitAmount(80000L)
                .build();


        cardLimitsDTOList.add(cardLimitsDTO1);
        cardLimitsDTOList.add(cardLimitsDTO2);
        cardLimitsDTOList.add(cardLimitsDTO3);

        cardLimitsDTOListDelete.add(cardLimitsDTO4);
        cardLimitsDTOListDelete.add(cardLimitsDTO1);

        PeriodicCardLimitDTO periodicCardLimitDTO1 = PeriodicCardLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.SINGLE))
                .cardLimitsDTOList(cardLimitsDTOList)
                .build();

        PeriodicCardLimitDTO periodicCardLimitDTO2 = PeriodicCardLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.MONTHLY))
                .cardLimitsDTOList(cardLimitsDTOList)
                .build();


        PeriodicCardLimitDTO periodicCardLimitDTO4 = PeriodicCardLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.SINGLE))
                .cardLimitsDTOList(cardLimitsDTOListDelete)
                .build();

        PeriodicCardLimitDTO periodicCardLimitDTO5 = PeriodicCardLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.DAILY))
                .cardLimitsDTOList(cardLimitsDTOListDelete)
                .build();

        PeriodicCardLimitDTO periodicCardLimitDTO3 = PeriodicCardLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.DAILY))
                .cardLimitsDTOList(cardLimitsDTOList)
                .build();

        periodicCardLimitDTOList.add(periodicCardLimitDTO1);
        periodicCardLimitDTOList.add(periodicCardLimitDTO2);
        periodicCardLimitDTOList.add(periodicCardLimitDTO3);

        periodicCardLimitDTOListDelete.add(periodicCardLimitDTO4);
        periodicCardLimitDTOListDelete.add(periodicCardLimitDTO5);


        AccountDefDTO accountDefDTO1 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.SAVINGS))
                .billingCurrencyCode("124")
                .accountId(UUID.randomUUID().toString().replace("-", ""))
                .build();

        AccountDefDTO accountDefDTO2 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.CREDIT))
                .billingCurrencyCode("840")
                .accountId(UUID.randomUUID().toString().replace("-", ""))
                .build();
        AccountDefDTO accountDefDTO3 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.CURRENT))
                .billingCurrencyCode("484")
                .accountId(UUID.randomUUID().toString().replace("-", ""))
                .build();

        AccountDefDTO accountDefDTO4 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.UNIVERSAL))
                .billingCurrencyCode("840")
                .accountId(deleteAccountNumber)
                .build();

        Set<AccountDefDTO> accountDefDTOSetAdd = new HashSet<>();

        Set<AccountDefDTO> accountDefDTOSetDelete = new HashSet<>();
        accountDefDTOSetDelete.add(accountDefDTO4);

        accountDefDTOSetAdd.add(accountDefDTO1);
        accountDefDTOSetAdd.add(accountDefDTO2);
        accountDefDTOSetAdd.add(accountDefDTO3);


        if (allFields) {
            return builder
                    .blockType(Util.getBlockType(BlockType.BLOCK_DECLINE))
                    .cardHolderType(Util.getCardHolderType(CardHolderType.SECONDARY))
                    .cardsReturned(1)
                    .cardStatus(Util.getCardStatus(CardStatus.FRAUD))
                    .waiverDaysActivation(20)
                    .periodicCardLimitDTOAddList(periodicCardLimitDTOList)
                    .periodicCardLimitDTODeleteList(periodicCardLimitDTOListDelete)
                    .customerNumber(UUID.randomUUID().toString().replace("-", ""))
                    .accountDefDTOSetDelete(accountDefDTOSetDelete)
                    .accountDefDTOSetAdd(accountDefDTOSetAdd)
                    .build();
        }

        integerList.forEach(integer -> evaluateBuilders(integer, builder, periodicCardLimitDTOList,
                periodicCardLimitDTOListDelete, deleteAccountNumber));

        return builder.build();

    }

    private void evaluateBuilders(Integer integer, CardBasicUpdateDTO.CardBasicUpdateDTOBuilder builder,
                                  List<PeriodicCardLimitDTO> periodicCardLimitDTOList,
                                  List<PeriodicCardLimitDTO> periodicCardLimitDTOListDelete, String deleteAccountNumber) {


        AccountDefDTO accountDefDTO1 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.SAVINGS))
                .billingCurrencyCode("124")
                .accountId(UUID.randomUUID().toString().replace("-", ""))
                .build();

        AccountDefDTO accountDefDTO2 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.CREDIT))
                .billingCurrencyCode("840")
                .accountId(UUID.randomUUID().toString().replace("-", ""))
                .build();
        AccountDefDTO accountDefDTO3 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.CURRENT))
                .billingCurrencyCode("484")
                .accountId(UUID.randomUUID().toString().replace("-", ""))
                .build();

        AccountDefDTO accountDefDTO4 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.UNIVERSAL))
                .billingCurrencyCode("840")
                .accountId(deleteAccountNumber)
                .build();

        Set<AccountDefDTO> accountDefDTOSetAdd = new HashSet<>();

        Set<AccountDefDTO> accountDefDTOSetDelete = new HashSet<>();
        accountDefDTOSetDelete.add(accountDefDTO4);

        accountDefDTOSetAdd.add(accountDefDTO1);
        accountDefDTOSetAdd.add(accountDefDTO2);
        accountDefDTOSetAdd.add(accountDefDTO3);

        switch (integer) {
            case 1: {
                builder.blockType(Util.getBlockType(BlockType.BLOCK_SUSPECTED_FRAUD));
                break;
            }
            case 2: {
                builder.cardHolderType(Util.getCardHolderType(CardHolderType.ADDITIONAL));
                break;
            }
            case 3: {
                builder.cardsReturned(2);
                break;
            }
            case 4: {
                builder.cardStatus(Util.getCardStatus(CardStatus.TRANSFER));
                break;
            }
            case 5: {
                builder.waiverDaysActivation(30);
                break;
            }
            case 6: {
                builder.periodicCardLimitDTOAddList(periodicCardLimitDTOList);
                break;
            }
            case 7: {
                builder.periodicCardLimitDTODeleteList(periodicCardLimitDTOListDelete);
                break;
            }
            case 8: {
                builder.accountDefDTOSetAdd(accountDefDTOSetAdd);
                break;
            }
            case 9: {
                builder.accountDefDTOSetDelete(accountDefDTOSetDelete);
                break;
            }
            case 10: {
                builder.customerNumber(UUID.randomUUID().toString().replace("-", ""));
            }
        }
    }

        private ProductDef createProductDef(){

        Map<BalanceTypes,Long> percentMap = new HashMap<>();
        percentMap.put(BalanceTypes.CASH_BALANCE,2000000L);
        percentMap.put(BalanceTypes.INTERNATIONAL_CASH,1000000L);
        percentMap.put(BalanceTypes.INTERNATIONAL_CASH_INSTALLMENT,3000000L);


        return ProductDef.builder()
                .serviceCode(301)
                .productId(new ProductId(001,201))
                .daysToCardsValid(11)
                .dateRangeNewExpDate(10)
                .cardsWaiverActivationDays(5)
                .cardsValidityMonthReplace(35)
                .cardsValidityMonthReIssue(40)
                .cardsValidityMonthNew(44)
                .cardsActivationRequired(false)
                .limitPercents(percentMap)
                .cardsReturn(10)
                .build();

    }
}


