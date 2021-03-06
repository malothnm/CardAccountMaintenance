package in.nmaloth.maintenance.service.cards;

import in.nmaloth.entity.BlockType;
import in.nmaloth.entity.account.AccountDef;
import in.nmaloth.entity.account.AccountType;
import in.nmaloth.entity.account.BalanceTypes;
import in.nmaloth.entity.card.*;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.entity.product.ProductId;
import in.nmaloth.maintenance.config.data.ProductTable;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.account.AccountDefDTO;
import in.nmaloth.maintenance.model.dto.card.*;
import in.nmaloth.maintenance.repository.card.CardsBasicRepository;
import in.nmaloth.maintenance.repository.product.ProductDefRepository;
import in.nmaloth.maintenance.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CardsBasicServiceImplTest {


    @Autowired
    private CardsBasicService cardsBasicService;

    @Autowired
    private ProductDefRepository productDefRepository;

    @Autowired
    private ProductTable productTable;


    @Autowired
    private CardsBasicRepository cardsBasicRepository;


    @BeforeEach
    void setup(){

        productDefRepository.findAll()
                .forEach(productDef -> productDefRepository.delete(productDef));


        cardsBasicRepository.findAll()
                .forEach(cardsBasic -> cardsBasicRepository.delete(cardsBasic));

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
    void createNewCardsRecord(){

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO(deleteAccountNumber);

        CardsBasic cardsBasic1 = cardsBasicService.createNewCardsRecord(cardBasicAddDTO).block();
        CardBasicDTO cardBasicDTO = cardsBasicService.convertToDTO(cardsBasic1);
        CardsBasic cardsBasic =  cardsBasicRepository.findById(cardBasicDTO.getCardId()).get();








        assertAll(
                ()-> assertEquals(cardBasicAddDTO.getCardId(),cardsBasic.getCardId()),
                ()-> assertEquals(cardBasicAddDTO.getCardholderType(),Util.getCardHolderType(cardsBasic.getCardholderType())),
                ()-> assertEquals(cardBasicAddDTO.getBlockType(),Util.getBlockType(cardsBasic.getBlockType())),
                ()-> assertEquals(cardBasicAddDTO.getOrg(),cardsBasic.getOrg()),
                ()-> assertEquals(cardBasicAddDTO.getProduct(),cardsBasic.getProduct()),
                ()-> assertEquals(cardBasicAddDTO.getWaiverDaysActivation(),cardsBasic.getWaiverDaysActivation()),
                ()-> assertEquals(cardBasicAddDTO.getCardStatus(),Util.getCardStatus(cardsBasic.getCardStatus())),
                ()-> assertNull(cardsBasic.getPrevBlockType()),
                ()-> assertNull(cardsBasic.getDatePrevBlockCode()),
                ()-> assertNull(cardsBasic.getDateBlockCode())

        );


    };

    @Test
    void updateCards(){

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");
        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO(deleteAccountNumber);

        CardsBasic cardsBasicNew = cardsBasicService.createNewCardsRecord(cardBasicAddDTO).block();
        CardBasicDTO cardBasicDTO = cardsBasicService.convertToDTO(cardsBasicNew);
        BlockType blockType = Util.getBlockType(cardBasicDTO.getBlockType());


        CardBasicUpdateDTO cardBasicUpdateDTO = createCardBasic(true,null,deleteAccountNumber);

        CardsBasic cardBasicNew1 = cardsBasicService.updateCards(cardBasicUpdateDTO).block();
        CardBasicDTO cardBasicDTO1 = cardsBasicService.convertToDTO(cardBasicNew1);
        CardsBasic cardsBasic = cardsBasicRepository.findById(cardBasicAddDTO.getCardId()).get();





        assertAll(
                ()-> assertEquals(cardBasicUpdateDTO.getCardHolderType(),Util.getCardHolderType(cardsBasic.getCardholderType())),
                ()-> assertEquals(cardBasicUpdateDTO.getBlockType(),Util.getBlockType(cardsBasic.getBlockType())),
                ()-> assertEquals(cardBasicUpdateDTO.getWaiverDaysActivation(),cardsBasic.getWaiverDaysActivation()),
                ()-> assertEquals(cardBasicUpdateDTO.getCardStatus(),Util.getCardStatus(cardsBasic.getCardStatus())),
                ()-> assertEquals(cardBasicUpdateDTO.getCardsReturned(),cardsBasic.getCardReturnNumber()),
                ()-> assertEquals(blockType,cardsBasic.getPrevBlockType()),
                ()-> assertNull(cardsBasic.getDatePrevBlockCode()),
                ()-> assertNotNull(cardsBasic.getDateBlockCode())



        );


    }

    @Test
    void updateCards1(){

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");

        CardBasicUpdateDTO cardBasicUpdateDTO = createCardBasic(true,null,deleteAccountNumber);

        Mono<CardsBasic> cardsBasicMono = cardsBasicService.updateCards(cardBasicUpdateDTO);
        StepVerifier
                .create(cardsBasicMono)
                .expectError(NotFoundException.class)
                .verify();

    }
    @Test
    void fetchCardInfo(){

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO(deleteAccountNumber);

        CardsBasic cardsBasic = cardsBasicService.createNewCardsRecord(cardBasicAddDTO).block();

        Mono<CardsBasic> cardsBasicMono = cardsBasicService.fetchCardInfo(cardBasicAddDTO.getCardId());

        StepVerifier
                .create(cardsBasicMono)
                .expectNextCount(1)
                .verifyComplete();

    }

    @Test
    void fetchCardInfo1(){



        Mono<CardsBasic> cardsBasicMono = cardsBasicService.fetchCardInfo("12345");

        StepVerifier
                .create(cardsBasicMono)
                .expectError(NotFoundException.class)
                .verify();

    }

    @Test
    void deleteCardInfo(){

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO(deleteAccountNumber);

         cardsBasicService.createNewCardsRecord(cardBasicAddDTO).block();

         cardsBasicService.deleteCardInfo(cardBasicAddDTO.getCardId()).block();

        Optional<CardsBasic> cardsBasicOptional = cardsBasicRepository.findById(cardBasicAddDTO.getCardId());

        assertTrue(cardsBasicOptional.isEmpty());

    };

    @Test
    void deleteCardInfo1(){

        Mono<CardsBasic> cardsBasicMono = cardsBasicService.deleteCardInfo("123456");

        StepVerifier
                .create(cardsBasicMono)
                .expectError(NotFoundException.class)
                .verify();

    };


    @Test
    void convertDTOToCardBasic() {

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO(deleteAccountNumber);
        ProductDef productDef = createProductDef();

        CardsBasic cardsBasic = cardsBasicService.convertDTOToCardBasic(cardBasicAddDTO,productDef);


        String[] accountDefs1 = cardBasicAddDTO.getAccountDefDTOSet()
                .stream()
                .map(accountDefDTO -> accountDefDTO.getAccountId())
                .toArray(String[]::new);

        Arrays.sort(accountDefs1);

        String[] accountDefs = cardsBasic.getAccountDefSet()
                .stream()
                .map(accountDef -> accountDef.getAccountNumber())
                .toArray(String[]::new);

        Arrays.sort(accountDefs);



        assertAll(
                ()-> assertEquals(cardBasicAddDTO.getCardId(),cardsBasic.getCardId()),
                ()-> assertEquals(cardBasicAddDTO.getCardholderType(),Util.getCardHolderType(cardsBasic.getCardholderType())),
                ()-> assertEquals(cardBasicAddDTO.getBlockType(),Util.getBlockType(cardsBasic.getBlockType())),
                ()-> assertEquals(cardBasicAddDTO.getOrg(),cardsBasic.getOrg()),
                ()-> assertEquals(cardBasicAddDTO.getProduct(),cardsBasic.getProduct()),
                ()-> assertEquals(cardBasicAddDTO.getWaiverDaysActivation(),cardsBasic.getWaiverDaysActivation()),
                ()-> assertEquals(cardBasicAddDTO.getCardStatus(),Util.getCardStatus(cardsBasic.getCardStatus())),
                ()-> assertNull(cardsBasic.getPrevBlockType()),
                ()-> assertNull(cardsBasic.getDatePrevBlockCode()),
                ()-> assertNull(cardsBasic.getDateBlockCode()),
                ()-> assertEquals(cardsBasic.getCustomerNumber(),cardBasicAddDTO.getCustomerNumber()),
                ()-> assertArrayEquals(accountDefs,accountDefs1)

        );


    }


    @Test
    void convertDTOToCardBasic1() {

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO(deleteAccountNumber);
        ProductDef productDef = createProductDef();
        cardBasicAddDTO.setWaiverDaysActivation(null);
        cardBasicAddDTO.setBlockType(Util.getBlockType(BlockType.BLOCK_DECLINE));

        CardsBasic cardsBasic = cardsBasicService.convertDTOToCardBasic(cardBasicAddDTO,productDef);

        String[] accountDefs1 = cardBasicAddDTO.getAccountDefDTOSet()
                .stream()
                .map(accountDefDTO -> accountDefDTO.getAccountId())
                .toArray(String[]::new);

        Arrays.sort(accountDefs1);

        String[] accountDefs = cardsBasic.getAccountDefSet()
                .stream()
                .map(accountDef -> accountDef.getAccountNumber())
                .toArray(String[]::new);

        Arrays.sort(accountDefs);

        assertAll(
                ()-> assertEquals(cardBasicAddDTO.getCardId(),cardsBasic.getCardId()),
                ()-> assertEquals(cardBasicAddDTO.getCardholderType(),Util.getCardHolderType(cardsBasic.getCardholderType())),
                ()-> assertEquals(cardBasicAddDTO.getBlockType(),Util.getBlockType(cardsBasic.getBlockType())),
                ()-> assertEquals(cardBasicAddDTO.getOrg(),cardsBasic.getOrg()),
                ()-> assertEquals(cardBasicAddDTO.getProduct(),cardsBasic.getProduct()),
                ()-> assertEquals(productDef.getCardsWaiverActivationDays(),cardsBasic.getWaiverDaysActivation()),
                ()-> assertEquals(cardBasicAddDTO.getCardStatus(),Util.getCardStatus(cardsBasic.getCardStatus())),
                ()-> assertNull(cardsBasic.getPrevBlockType()),
                ()-> assertNull(cardsBasic.getDatePrevBlockCode()),
                ()-> assertNotNull(cardsBasic.getDateBlockCode()),
                ()-> assertEquals(cardBasicAddDTO.getCustomerNumber(),cardsBasic.getCustomerNumber()),
                ()-> assertArrayEquals(accountDefs,accountDefs1)

        );
    }


    @Test
    void convertToDTO() {

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO(deleteAccountNumber);
        ProductDef productDef = createProductDef();

        CardsBasic cardsBasic = cardsBasicService.convertDTOToCardBasic(cardBasicAddDTO,productDef);

        CardBasicDTO cardBasicDTO = cardsBasicService.convertToDTO(cardsBasic);

        String[] accountDefs1 = cardBasicAddDTO.getAccountDefDTOSet()
                .stream()
                .map(accountDefDTO -> accountDefDTO.getAccountId())
                .toArray(String[]::new);

        Arrays.sort(accountDefs1);

        String[] accountDefs = cardsBasic.getAccountDefSet()
                .stream()
                .map(accountDef -> accountDef.getAccountNumber())
                .toArray(String[]::new);

        Arrays.sort(accountDefs);




        assertAll(
                ()-> assertEquals(cardsBasic.getCardId(),cardBasicDTO.getCardId()),
                ()-> assertEquals(cardsBasic.getCardholderType(),Util.getCardHolderType(cardBasicDTO.getCardholderType())),
                ()-> assertEquals(cardsBasic.getBlockType(),Util.getBlockType(cardBasicDTO.getBlockType())),
                ()-> assertEquals(cardsBasic.getOrg(),cardBasicDTO.getOrg()),
                ()-> assertEquals(cardsBasic.getProduct(),cardBasicDTO.getProduct()),
                ()-> assertEquals(cardsBasic.getWaiverDaysActivation(),cardBasicDTO.getWaiverDaysActivation()),
                ()-> assertEquals(cardsBasic.getCardStatus(),Util.getCardStatus(cardBasicDTO.getCardStatus())),
                ()-> assertNull(cardBasicDTO.getPrevBlockType()),
                ()-> assertNull(cardBasicDTO.getDatePrevBlockCode()),
                ()-> assertNull(cardBasicDTO.getDateBlockCode()),
                ()-> assertEquals(cardBasicAddDTO.getCustomerNumber(),cardsBasic.getCustomerNumber()),
                ()-> assertArrayEquals(accountDefs,accountDefs1)

        );



    }

    @Test
    void convertToDTO1() {

        String cardId = UUID.randomUUID().toString().replace("-","");


        Set<AccountDef> accountDefSet = new HashSet<>();

        accountDefSet.add(AccountDef.builder()
                .accountNumber(UUID.randomUUID().toString().replace("-",""))
                .billingCurrencyCode("484")
                .accountType(AccountType.CREDIT)
                .build());

        accountDefSet.add(AccountDef.builder()
                .accountNumber(UUID.randomUUID().toString().replace("-",""))
                .billingCurrencyCode("840")
                .accountType(AccountType.CREDIT)
                .build());



        List<Plastic> plasticList = new ArrayList<>();

        plasticList.add(
                Plastic.builder()
                        .plasticId(UUID.randomUUID().toString().replace("-",""))
                        .cardActivatedDate(LocalDateTime.now())
                        .expiryDate(LocalDate.now())
                        .activationWaiveDuration(Duration.ofDays(10))
                        .cardAction(CardAction.NEW_CARD)
                        .cardActivated(true)
                        .dynamicCVV(false)
                        .dateCardValidFrom(LocalDate.now())
                        .pendingCardAction(CardAction.NO_ACTION)
                        .datePlasticIssued(LocalDateTime.now())
                        .build()

        );
        plasticList.add(
                Plastic.builder()
                        .plasticId(UUID.randomUUID().toString().replace("-",""))
                        .expiryDate(LocalDate.now())
                        .activationWaiveDuration(Duration.ofDays(10))
                        .cardAction(CardAction.NO_ACTION)
                        .cardActivated(true)
                        .dynamicCVV(false)
                        .dateCardValidFrom(LocalDate.now())
                        .pendingCardAction(CardAction.NEW_CARD)
                        .build()

        );


        CardsBasic cardsBasic = createCardBasic(cardId,accountDefSet,plasticList);

        CardBasicDTO cardBasicDTO = cardsBasicService.convertToDTO(cardsBasic);

        String[] accountDefs1 = cardBasicDTO.getAccountDefDTOSet()
                .stream()
                .map(accountDefDTO -> accountDefDTO.getAccountId())
                .toArray(String[]::new);

        Arrays.sort(accountDefs1);

        String[] accountDefs = cardsBasic.getAccountDefSet()
                .stream()
                .map(accountDef -> accountDef.getAccountNumber())
                .toArray(String[]::new);

        Arrays.sort(accountDefs);




        assertAll(
                ()-> assertEquals(cardsBasic.getCardId(),cardBasicDTO.getCardId()),
                ()-> assertEquals(cardsBasic.getCardholderType(),Util.getCardHolderType(cardBasicDTO.getCardholderType())),
                ()-> assertEquals(cardsBasic.getBlockType(),Util.getBlockType(cardBasicDTO.getBlockType())),
                ()-> assertEquals(cardsBasic.getOrg(),cardBasicDTO.getOrg()),
                ()-> assertEquals(cardsBasic.getProduct(),cardBasicDTO.getProduct()),
                ()-> assertEquals(cardsBasic.getWaiverDaysActivation(),cardBasicDTO.getWaiverDaysActivation()),
                ()-> assertEquals(cardsBasic.getCardStatus(),Util.getCardStatus(cardBasicDTO.getCardStatus())),
                ()-> assertNull(cardBasicDTO.getPrevBlockType()),
                ()-> assertNull(cardBasicDTO.getDatePrevBlockCode()),
                ()-> assertNull(cardBasicDTO.getDateBlockCode()),
                ()-> assertEquals(cardsBasic.getCustomerNumber(),cardBasicDTO.getCustomerNumber()),
                ()-> assertEquals(2, cardBasicDTO.getPlasticsDTOList().size()),
                ()-> assertArrayEquals(accountDefs,accountDefs1)

        );


    }


    @Test
    void updateCardBasicFromDTO() {

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO(deleteAccountNumber);
        ProductDef productDef = createProductDef();

        CardsBasic cardsBasic = cardsBasicService.convertDTOToCardBasic(cardBasicAddDTO,productDef);

        BlockType blockType = cardsBasic.getBlockType();

        CardBasicUpdateDTO cardBasicUpdateDTO = createCardBasic(true,null,deleteAccountNumber);

        CardsBasic cardsBasic1 = cardsBasicService.updateCardBasicFromDTO(cardBasicUpdateDTO,productDef,cardsBasic);




        assertAll(
                ()-> assertEquals(cardBasicUpdateDTO.getCardHolderType(),Util.getCardHolderType(cardsBasic.getCardholderType())),
                ()-> assertEquals(cardBasicUpdateDTO.getBlockType(),Util.getBlockType(cardsBasic.getBlockType())),
                ()-> assertEquals(cardBasicUpdateDTO.getWaiverDaysActivation(),cardsBasic.getWaiverDaysActivation()),
                ()-> assertEquals(cardBasicUpdateDTO.getCardStatus(),Util.getCardStatus(cardsBasic.getCardStatus())),
                ()-> assertEquals(cardBasicUpdateDTO.getCardsReturned(),cardsBasic.getCardReturnNumber()),
                ()-> assertEquals(blockType,cardsBasic.getPrevBlockType()),
                ()-> assertNull(cardsBasic.getDatePrevBlockCode()),
                ()-> assertNotNull(cardsBasic.getDateBlockCode()),
                ()-> assertEquals(cardBasicUpdateDTO.getCustomerNumber(), cardsBasic.getCustomerNumber()),
                ()-> assertEquals(4,cardsBasic.getAccountDefSet().size())


        );

    }


    @Test
    void updateCardBasicFromDTO1() {

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");


        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO(deleteAccountNumber);
        ProductDef productDef = createProductDef();

        CardsBasic cardsBasic = cardsBasicService.convertDTOToCardBasic(cardBasicAddDTO,productDef);

        BlockType blockType = cardsBasic.getBlockType();
        List<Integer> integerList = Arrays.asList(1,2,3,6,9);

        CardBasicUpdateDTO cardBasicUpdateDTO = createCardBasic(false,integerList,deleteAccountNumber);

        CardsBasic cardsBasic1 = cardsBasicService.updateCardBasicFromDTO(cardBasicUpdateDTO,productDef,cardsBasic);



        assertAll(
                ()-> assertEquals(cardBasicUpdateDTO.getCardHolderType(),Util.getCardHolderType(cardsBasic.getCardholderType())),
                ()-> assertEquals(cardBasicUpdateDTO.getBlockType(),Util.getBlockType(cardsBasic.getBlockType())),
                ()-> assertEquals(cardBasicAddDTO.getWaiverDaysActivation(),cardsBasic.getWaiverDaysActivation()),
                ()-> assertEquals(cardBasicAddDTO.getCardStatus(),Util.getCardStatus(cardsBasic.getCardStatus())),
                ()-> assertEquals(cardBasicUpdateDTO.getCardsReturned(),cardsBasic.getCardReturnNumber()),
                ()-> assertEquals(blockType,cardsBasic.getPrevBlockType()),
                ()-> assertNull(cardsBasic.getDatePrevBlockCode()),
                ()-> assertNotNull(cardsBasic.getDateBlockCode()),
                ()-> assertEquals(1,cardsBasic.getAccountDefSet().size())


        );

    }

    @Test
    void updateCardBasicFromDTO2() {

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO(deleteAccountNumber);
        ProductDef productDef = createProductDef();

        CardsBasic cardsBasic = cardsBasicService.convertDTOToCardBasic(cardBasicAddDTO,productDef);

        BlockType blockType = cardsBasic.getBlockType();
        List<Integer> integerList = Arrays.asList(4,5,7,8,10);

        CardBasicUpdateDTO cardBasicUpdateDTO = createCardBasic(false,integerList,deleteAccountNumber);

        CardsBasic cardsBasic1 = cardsBasicService.updateCardBasicFromDTO(cardBasicUpdateDTO,productDef,cardsBasic);




        assertAll(
                ()-> assertEquals(cardBasicAddDTO.getCardholderType(),Util.getCardHolderType(cardsBasic.getCardholderType())),
                ()-> assertEquals(cardBasicAddDTO.getBlockType(),Util.getBlockType(cardsBasic.getBlockType())),
                ()-> assertEquals(cardBasicUpdateDTO.getWaiverDaysActivation(),cardsBasic.getWaiverDaysActivation()),
                ()-> assertEquals(cardBasicUpdateDTO.getCardStatus(),Util.getCardStatus(cardsBasic.getCardStatus())),
                ()-> assertEquals(0,cardsBasic.getCardReturnNumber()),
                ()-> assertNull(cardsBasic.getPrevBlockType()),
                ()-> assertNull(cardsBasic.getDatePrevBlockCode()),
                ()-> assertNull(cardsBasic.getDateBlockCode()),
                ()-> assertEquals(cardBasicUpdateDTO.getCustomerNumber(),cardsBasic.getCustomerNumber()),
                ()-> assertEquals(5,cardsBasic.getAccountDefSet().size())


        );

    }


    private CardBasicAddDTO createCardBasicAddDTO(String deleteAccountNumber){


        AccountDefDTO accountDefDTO1 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.SAVINGS))
                .billingCurrencyCode("124")
                .accountId(UUID.randomUUID().toString().replace("-",""))
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
                .build()
                ;


        PeriodicCardLimitDTO periodicCardLimitDTO2 = PeriodicCardLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.DAILY))
                .cardLimitsDTOList(cardLimitsDTOList)
                .build()
                ;

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
                .customerNumber(UUID.randomUUID().toString().replace("-",""))
                .build();
    }

    private CardBasicUpdateDTO createCardBasic(boolean allFields, List<Integer> integerList,String deleteAccountNumber){

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
                .build()
                ;

        PeriodicCardLimitDTO periodicCardLimitDTO2 = PeriodicCardLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.MONTHLY))
                .cardLimitsDTOList(cardLimitsDTOList)
                .build()
                ;


        PeriodicCardLimitDTO periodicCardLimitDTO4 = PeriodicCardLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.SINGLE))
                .cardLimitsDTOList(cardLimitsDTOListDelete)
                .build()
                ;

        PeriodicCardLimitDTO periodicCardLimitDTO5 = PeriodicCardLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.DAILY))
                .cardLimitsDTOList(cardLimitsDTOListDelete)
                .build()
                ;

        PeriodicCardLimitDTO periodicCardLimitDTO3 = PeriodicCardLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.DAILY))
                .cardLimitsDTOList(cardLimitsDTOList)
                .build()
                ;

        periodicCardLimitDTOList.add(periodicCardLimitDTO1);
        periodicCardLimitDTOList.add(periodicCardLimitDTO2);
        periodicCardLimitDTOList.add(periodicCardLimitDTO3);

        periodicCardLimitDTOListDelete.add(periodicCardLimitDTO4);
        periodicCardLimitDTOListDelete.add(periodicCardLimitDTO5);


        AccountDefDTO accountDefDTO1 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.SAVINGS))
                .billingCurrencyCode("124")
                .accountId(UUID.randomUUID().toString().replace("-",""))
                .build();

        AccountDefDTO accountDefDTO2 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.CREDIT))
                .billingCurrencyCode("840")
                .accountId(UUID.randomUUID().toString().replace("-",""))
                .build();
        AccountDefDTO accountDefDTO3 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.CURRENT))
                .billingCurrencyCode("484")
                .accountId(UUID.randomUUID().toString().replace("-",""))
                .build();

        AccountDefDTO accountDefDTO4 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.UNIVERSAL))
                .billingCurrencyCode("840")
                .accountId(deleteAccountNumber)
                .build();

        Set<AccountDefDTO> accountDefDTOSetAdd = new HashSet<>();

        Set<AccountDefDTO> accountDefDTOSetDelete = new HashSet<>();
        accountDefDTOSetDelete .add(accountDefDTO4);

        accountDefDTOSetAdd.add(accountDefDTO1);
        accountDefDTOSetAdd.add(accountDefDTO2);
        accountDefDTOSetAdd.add(accountDefDTO3);



        if(allFields){
            return builder
                    .blockType(Util.getBlockType(BlockType.BLOCK_DECLINE))
                    .cardHolderType(Util.getCardHolderType(CardHolderType.SECONDARY))
                    .cardsReturned(1)
                    .cardStatus(Util.getCardStatus(CardStatus.FRAUD))
                    .waiverDaysActivation(20)
                    .periodicCardLimitDTOAddList(periodicCardLimitDTOList)
                    .periodicCardLimitDTODeleteList(periodicCardLimitDTOListDelete)
                    .customerNumber(UUID.randomUUID().toString().replace("-",""))
                    .accountDefDTOSetDelete(accountDefDTOSetDelete)
                    .accountDefDTOSetAdd(accountDefDTOSetAdd)
                    .build();
        }

        integerList.forEach(integer -> evaluateBuilders(integer,builder,periodicCardLimitDTOList,
                periodicCardLimitDTOListDelete,deleteAccountNumber));

        return builder.build();

    }

    private void evaluateBuilders(Integer integer, CardBasicUpdateDTO.CardBasicUpdateDTOBuilder builder,
                                  List<PeriodicCardLimitDTO> periodicCardLimitDTOList,
                                  List<PeriodicCardLimitDTO> periodicCardLimitDTOListDelete,String deleteAccountNumber) {


        AccountDefDTO accountDefDTO1 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.SAVINGS))
                .billingCurrencyCode("124")
                .accountId(UUID.randomUUID().toString().replace("-",""))
                .build();

        AccountDefDTO accountDefDTO2 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.CREDIT))
                .billingCurrencyCode("840")
                .accountId(UUID.randomUUID().toString().replace("-",""))
                .build();
        AccountDefDTO accountDefDTO3 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.CURRENT))
                .billingCurrencyCode("484")
                .accountId(UUID.randomUUID().toString().replace("-",""))
                .build();

        AccountDefDTO accountDefDTO4 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.UNIVERSAL))
                .billingCurrencyCode("840")
                .accountId(deleteAccountNumber)
                .build();

        Set<AccountDefDTO> accountDefDTOSetAdd = new HashSet<>();

        Set<AccountDefDTO> accountDefDTOSetDelete = new HashSet<>();
        accountDefDTOSetDelete .add(accountDefDTO4);

        accountDefDTOSetAdd.add(accountDefDTO1);
        accountDefDTOSetAdd.add(accountDefDTO2);
        accountDefDTOSetAdd.add(accountDefDTO3);

        switch (integer){
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
            case 6 : {
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
            case 10:{
                builder.customerNumber(UUID.randomUUID().toString().replace("-",""));
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

    private CardsBasic createCardBasic(String cardId,Set<AccountDef> accountDefSet,List<Plastic> plasticList){




        return CardsBasic.builder()
                .cardId(cardId)
                .cardholderType(CardHolderType.PRIMARY)
                .blockType(BlockType.APPROVE)
                .cardStatus(CardStatus.ACTIVE)
                .org(001)
                .product(201)
                .waiverDaysActivation(10)
                .accountDefSet(accountDefSet)
                .customerNumber(UUID.randomUUID().toString().replace("-",""))
                .plasticList(plasticList)
                .build();
    }



}