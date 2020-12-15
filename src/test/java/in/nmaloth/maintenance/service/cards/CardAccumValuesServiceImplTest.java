package in.nmaloth.maintenance.service.cards;

import in.nmaloth.entity.card.CardAccumulatedValues;
import in.nmaloth.entity.card.LimitType;
import in.nmaloth.entity.card.PeriodicCardAmount;
import in.nmaloth.entity.card.PeriodicType;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.card.CardAccumValuesDTO;
import in.nmaloth.maintenance.model.dto.card.CardLimitsDTO;
import in.nmaloth.maintenance.model.dto.card.PeriodicCardLimitDTO;
import in.nmaloth.maintenance.model.dto.card.PeriodicLimitSet;
import in.nmaloth.maintenance.repository.card.CardAccumulatedValuesRepository;
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

    @BeforeEach
    void setup(){

        cardAccumulatedValuesRepository.findAll()
                .forEach(cardAccumulatedValues -> cardAccumulatedValuesRepository.delete(cardAccumulatedValues));

    }

    @Test
    void fetchCardAccumValuesByCardNumber(){

        String cardNumber = UUID.randomUUID().toString().replace("-","");
        CardAccumulatedValues cardAccumulatedValues = createCardAccumValues(cardNumber);
        cardAccumulatedValuesRepository.save(cardAccumulatedValues);

        Mono<CardAccumulatedValues> cardAccumulatedValuesMono = cardAccumValuesService.fetchCardAccumValuesByCardNumber(cardNumber);

        StepVerifier
                .create(cardAccumulatedValuesMono)
                .expectNextCount(1)
                .verifyComplete();

    }


    @Test
    void fetchCardAccumValuesByCardNumber1(){

        String cardNumber = UUID.randomUUID().toString().replace("-","");
        CardAccumulatedValues cardAccumulatedValues = createCardAccumValues(cardNumber);
//        cardAccumulatedValuesRepository.save(cardAccumulatedValues);

        Mono<CardAccumulatedValues> cardAccumulatedValuesMono = cardAccumValuesService.fetchCardAccumValuesByCardNumber(cardNumber);

        StepVerifier
                .create(cardAccumulatedValuesMono)
                .expectError(NotFoundException.class)
                .verify();

    }

    @Test
    void fetchCardAccumValuesOptional(){

        String cardNumber = UUID.randomUUID().toString().replace("-","");
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
    void fetchCardAccumValuesOptional1(){

        String cardNumber = UUID.randomUUID().toString().replace("-","");
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
    void deleteCardAccumValuesByCardNumber(){

        String cardNumber = UUID.randomUUID().toString().replace("-","");
        CardAccumulatedValues cardAccumulatedValues = createCardAccumValues(cardNumber);
        cardAccumulatedValuesRepository.save(cardAccumulatedValues);

        cardAccumValuesService.deleteCardAccumValuesByCardNumber(cardNumber).block();
        Optional<CardAccumulatedValues> cardAccumulatedValuesOptional = cardAccumulatedValuesRepository.findById(cardNumber);

        assertTrue(cardAccumulatedValuesOptional.isEmpty());

    }

    @Test
    void deleteCardAccumValuesByCardNumber1(){

        String cardNumber = UUID.randomUUID().toString().replace("-","");
        CardAccumulatedValues cardAccumulatedValues = createCardAccumValues(cardNumber);
//        cardAccumulatedValuesRepository.save(cardAccumulatedValues);

        Mono<CardAccumulatedValues> cardAccumulatedValuesMono = cardAccumValuesService.deleteCardAccumValuesByCardNumber(cardNumber);

        StepVerifier.create(cardAccumulatedValuesMono)
                .expectError(NotFoundException.class)
                .verify();

    }

    @Test
    void saveAccountAccumValues(){

        String cardNumber = UUID.randomUUID().toString().replace("-","");
        CardAccumulatedValues cardAccumulatedValues = createCardAccumValues(cardNumber);

        cardAccumValuesService.saveAccountAccumValues(cardAccumulatedValues).block();

        Optional<CardAccumulatedValues> cardAccumulatedValuesOptional = cardAccumulatedValuesRepository.findById(cardNumber);

        assertTrue(cardAccumulatedValuesOptional.isPresent());
    }



    @Test
    void convertToDTO() {

        CardAccumulatedValues cardAccumulatedValues = createCardAccumValues(UUID.randomUUID().toString().replace("-",""));

        CardAccumValuesDTO cardAccumValuesDTO = cardAccumValuesService.convertToDTO(cardAccumulatedValues);

        List<PeriodicCardLimitDTO> periodicAccumList = cardAccumValuesDTO.getPeriodicCardAccumulatedValueList();
        List<CardLimitsDTO> cardLimitsDTOListSingle = extractLimitCardDTO(PeriodicType.SINGLE,periodicAccumList).getCardLimitsDTOList();
        List<CardLimitsDTO> cardLimitsDTOListDaily = extractLimitCardDTO(PeriodicType.DAILY,periodicAccumList).getCardLimitsDTOList();
        List<CardLimitsDTO> cardLimitsDTOListMonthly = extractLimitCardDTO(PeriodicType.MONTHLY,periodicAccumList).getCardLimitsDTOList();


        Long amtSingleNoSpecific = extractCardLimit(LimitType.NO_SPECIFIC,cardLimitsDTOListSingle).getLimitAmount();
        Integer nbrSingleCash = extractCardLimit(LimitType.CASH,cardLimitsDTOListSingle).getLimitNumber();

        Long amtRetailMonth = extractCardLimit(LimitType.RETAIL,cardLimitsDTOListMonthly).getLimitAmount();
        Integer nbrOTCMonth = extractCardLimit(LimitType.OTC,cardLimitsDTOListMonthly).getLimitNumber();

        Long amtCashDaily = extractCardLimit(LimitType.CASH,cardLimitsDTOListDaily).getLimitAmount();
        Integer nbrNoSpecificDaily = extractCardLimit(LimitType.NO_SPECIFIC,cardLimitsDTOListDaily).getLimitNumber();

        assertAll(
                ()-> assertEquals(1000,amtSingleNoSpecific),
                ()-> assertEquals(300,nbrSingleCash),
                ()-> assertEquals(2000,amtRetailMonth),
                ()-> assertEquals(400,nbrOTCMonth),
                ()-> assertEquals(3000,amtCashDaily),
                ()-> assertEquals(100,nbrNoSpecificDaily),
                ()-> assertEquals(cardAccumulatedValues.getCardNumber(),cardAccumValuesDTO.getCardNumber()),
                ()-> assertEquals(cardAccumulatedValues.getOrg(),cardAccumValuesDTO.getOrg()),
                ()-> assertEquals(cardAccumulatedValues.getProduct(),cardAccumValuesDTO.getProduct())
        );



    }

    @Test
    void initializeAccumValues() {

        String cardNumber = UUID.randomUUID().toString().replace("-","");

        PeriodicLimitSet periodicLimitSetSingle = PeriodicLimitSet.builder()
                .periodicType(PeriodicType.SINGLE)
                .limitTypeSet(new HashSet<>(Arrays.asList(LimitType.RETAIL,LimitType.NO_SPECIFIC,LimitType.CASH)))
                .build();

        PeriodicLimitSet periodicLimitSetDaily = PeriodicLimitSet.builder()
                .periodicType(PeriodicType.DAILY)
                .limitTypeSet(new HashSet<>(Arrays.asList(LimitType.RETAIL,LimitType.NO_SPECIFIC,LimitType.CASH,LimitType.OTC)))
                .build();

        PeriodicLimitSet periodicLimitSetMonthly = PeriodicLimitSet.builder()
                .periodicType(PeriodicType.MONTHLY)
                .limitTypeSet(new HashSet<>(Arrays.asList(LimitType.RETAIL,LimitType.NO_SPECIFIC,LimitType.CASH,LimitType.OTC,LimitType.QUASI_CASH)))
                .build();

        Set<PeriodicLimitSet> periodicLimitSet = new HashSet<>(Arrays.asList(periodicLimitSetDaily,periodicLimitSetMonthly,periodicLimitSetSingle));

        CardAccumulatedValues cardAccumulatedValues = cardAccumValuesService.initializeAccumValues(cardNumber,periodicLimitSet,1 , 201);

        assertAll(
                ()-> assertEquals(cardNumber,cardAccumulatedValues.getCardNumber()),
                ()-> assertEquals(1,cardAccumulatedValues.getOrg()),
                ()-> assertEquals(201,cardAccumulatedValues.getProduct()),
                ()-> assertEquals(3,cardAccumulatedValues.getPeriodicCardAccumulatedValueMap().get(PeriodicType.SINGLE).size()),
                ()-> assertEquals(4,cardAccumulatedValues.getPeriodicCardAccumulatedValueMap().get(PeriodicType.DAILY).size()),
                ()-> assertEquals(5,cardAccumulatedValues.getPeriodicCardAccumulatedValueMap().get(PeriodicType.MONTHLY).size()),
                ()-> assertEquals(0L,cardAccumulatedValues.getPeriodicCardAccumulatedValueMap().get(PeriodicType.SINGLE).get(LimitType.NO_SPECIFIC).getTransactionAmount()),
                ()-> assertEquals(0,cardAccumulatedValues.getPeriodicCardAccumulatedValueMap()
                        .get(PeriodicType.SINGLE).get(LimitType.NO_SPECIFIC).getTransactionNumber())


                );
    }

    @Test
    void updateNewAccumValues() {

        String cardNumber = UUID.randomUUID().toString().replace("-","");
        CardAccumulatedValues cardAccumulatedValues = createCardAccumValues(cardNumber);

        PeriodicLimitSet periodicLimitSetSingle = PeriodicLimitSet.builder()
                .periodicType(PeriodicType.SINGLE)
                .limitTypeSet(new HashSet<>(Arrays.asList(LimitType.RETAIL,LimitType.NO_SPECIFIC,LimitType.CASH,LimitType.ATM)))
                .build();

        PeriodicLimitSet periodicLimitSetDaily = PeriodicLimitSet.builder()
                .periodicType(PeriodicType.DAILY)
                .limitTypeSet(new HashSet<>(Arrays.asList(LimitType.RETAIL,LimitType.NO_SPECIFIC,LimitType.CASH,LimitType.OTC)))
                .build();

        PeriodicLimitSet periodicLimitSetMonthly = PeriodicLimitSet.builder()
                .periodicType(PeriodicType.MONTHLY)
                .limitTypeSet(new HashSet<>(Arrays.asList(LimitType.RETAIL,LimitType.NO_SPECIFIC,LimitType.CASH,LimitType.OTC,
                        LimitType.QUASI_CASH,LimitType.ATM)))
                .build();

        Set<PeriodicLimitSet> periodicLimitSet = new HashSet<>(Arrays.asList(periodicLimitSetDaily,periodicLimitSetMonthly,periodicLimitSetSingle));

        CardAccumulatedValues cardAccumulatedValues1 = cardAccumValuesService.updateNewAccumValues(periodicLimitSet,cardAccumulatedValues);

        Long amtSingleNoSpecific = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.SINGLE).get(LimitType.NO_SPECIFIC).getTransactionAmount();
        Integer nbrSingleCash =  cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.SINGLE).get(LimitType.CASH).getTransactionNumber();
        Long amtRetailMonth = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.MONTHLY).get(LimitType.RETAIL).getTransactionAmount();
        Integer nbrOTCMonth =  cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.MONTHLY).get(LimitType.OTC).getTransactionNumber();
        Long amtCashDaily = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.DAILY).get(LimitType.CASH).getTransactionAmount();
        Integer nbrNoSpecificDaily =  cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.DAILY).get(LimitType.NO_SPECIFIC).getTransactionNumber();

        int singleCount = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.SINGLE).size();
        int dailyCount = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.DAILY).size();
        int monthlyCount = cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                .get(PeriodicType.MONTHLY).size();

        assertAll(
                ()-> assertEquals(1000,amtSingleNoSpecific),
                ()-> assertEquals(300,nbrSingleCash),
                ()-> assertEquals(2000,amtRetailMonth),
                ()-> assertEquals(400,nbrOTCMonth),
                ()-> assertEquals(3000,amtCashDaily),
                ()-> assertEquals(100,nbrNoSpecificDaily),
                ()-> assertEquals(5, singleCount),
                ()-> assertEquals(4, dailyCount),
                ()-> assertEquals(6, monthlyCount),
                ()-> assertEquals(0, cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                        .get(PeriodicType.SINGLE).get(LimitType.ATM).getTransactionNumber()),
                ()-> assertEquals(0L, cardAccumulatedValues1.getPeriodicCardAccumulatedValueMap()
                        .get(PeriodicType.SINGLE).get(LimitType.ATM).getTransactionAmount())


                );

    }

    private PeriodicCardLimitDTO extractLimitCardDTO(PeriodicType periodicType, List<PeriodicCardLimitDTO> periodicCardLimitDTOList){

        return periodicCardLimitDTOList
                .stream()
                .filter(periodicCardLimitDTO -> periodicCardLimitDTO.getPeriodicType().equals(Util.getPeriodicType(periodicType)))
                .findFirst()
                .get();
    }

    private CardLimitsDTO extractCardLimit(LimitType limitType,List<CardLimitsDTO> cardLimitsDTOList){

        return cardLimitsDTOList
                .stream()
                .filter(cardLimitsDTO -> cardLimitsDTO.getLimitType().equals(Util.getLimitType(limitType)))
                .findFirst()
                .get();

    }

    private CardAccumulatedValues createCardAccumValues(String cardNumber){

        Map<PeriodicType,Map<LimitType, PeriodicCardAmount>> periodicMap = new HashMap<>();

        Map<LimitType,PeriodicCardAmount> limitAmountMapSingle = new HashMap<>();
        Map<LimitType,PeriodicCardAmount> limitAmountMapDaily = new HashMap<>();
        Map<LimitType,PeriodicCardAmount> limitAmountMapMonthly = new HashMap<>();

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
                .build()
                ;
        PeriodicCardAmount periodicCardAmountOTC = PeriodicCardAmount.builder()
                .transactionAmount(4000L)
                .transactionNumber(400)
                .limitType(LimitType.OTC)
                .build()
                ;

        limitAmountMapSingle.put(periodicCardAmountNoSpecific.getLimitType(),periodicCardAmountNoSpecific);
        limitAmountMapSingle.put(periodicCardAmountRetail.getLimitType(),periodicCardAmountRetail);
        limitAmountMapSingle.put(periodicCardAmountCash.getLimitType(),periodicCardAmountCash);
        limitAmountMapSingle.put(periodicCardAmountOTC.getLimitType(),periodicCardAmountOTC);

        limitAmountMapDaily.put(periodicCardAmountNoSpecific.getLimitType(),periodicCardAmountNoSpecific);
        limitAmountMapDaily.put(periodicCardAmountRetail.getLimitType(),periodicCardAmountRetail);
        limitAmountMapDaily.put(periodicCardAmountCash.getLimitType(),periodicCardAmountCash);
        limitAmountMapDaily.put(periodicCardAmountOTC.getLimitType(),periodicCardAmountOTC);

        limitAmountMapMonthly.put(periodicCardAmountNoSpecific.getLimitType(),periodicCardAmountNoSpecific);
        limitAmountMapMonthly.put(periodicCardAmountRetail.getLimitType(),periodicCardAmountRetail);
        limitAmountMapMonthly.put(periodicCardAmountCash.getLimitType(),periodicCardAmountCash);
        limitAmountMapMonthly.put(periodicCardAmountOTC.getLimitType(),periodicCardAmountOTC);

        periodicMap.put(PeriodicType.SINGLE,limitAmountMapSingle);
        periodicMap.put(PeriodicType.DAILY,limitAmountMapDaily);
        periodicMap.put(PeriodicType.MONTHLY,limitAmountMapMonthly);






        return CardAccumulatedValues.builder()
                .cardNumber(cardNumber)
                .org(001)
                .product(201)
                .periodicCardAccumulatedValueMap(periodicMap)
                .build()
                ;
    }

}