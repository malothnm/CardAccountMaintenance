package in.nmaloth.maintenance.controllers.instrument;

import in.nmaloth.entity.BlockType;
import in.nmaloth.entity.account.AccountBasic;
import in.nmaloth.entity.account.AccountDef;
import in.nmaloth.entity.account.AccountType;
import in.nmaloth.entity.account.BalanceTypes;
import in.nmaloth.entity.card.*;
import in.nmaloth.entity.customer.AddressType;
import in.nmaloth.entity.customer.CustomerDef;
import in.nmaloth.entity.customer.CustomerIDType;
import in.nmaloth.entity.customer.CustomerType;
import in.nmaloth.entity.instrument.Instrument;
import in.nmaloth.entity.instrument.InstrumentType;
import in.nmaloth.entity.product.ProductCardGenDef;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.entity.product.ProductId;
import in.nmaloth.maintenance.config.data.ProductTable;
import in.nmaloth.maintenance.controllers.EndPoints;
import in.nmaloth.maintenance.model.dto.account.AccountDefDTO;
import in.nmaloth.maintenance.model.dto.card.*;
import in.nmaloth.maintenance.model.dto.instrument.InstrumentAddDTO;
import in.nmaloth.maintenance.model.dto.instrument.InstrumentDto;
import in.nmaloth.maintenance.model.dto.instrument.InstrumentUpdateDTO;
import in.nmaloth.maintenance.repository.account.AccountBasicRepository;
import in.nmaloth.maintenance.repository.card.CardAccumulatedValuesRepository;
import in.nmaloth.maintenance.repository.card.CardsBasicRepository;
import in.nmaloth.maintenance.repository.card.PlasticRepository;
import in.nmaloth.maintenance.repository.customer.CustomerRepository;
import in.nmaloth.maintenance.repository.instrument.InstrumentRepository;
import in.nmaloth.maintenance.repository.product.ProductCardGenRepository;
import in.nmaloth.maintenance.repository.product.ProductDefRepository;
import in.nmaloth.maintenance.service.cards.CardAccumValuesService;
import in.nmaloth.maintenance.util.Util;
import org.apache.geode.cache.client.internal.Op;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import javax.swing.text.html.Option;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext
@AutoConfigureWebTestClient
class InstrumentControllerTest {


    @Autowired
    private ProductDefRepository productDefRepository;

    @Autowired
    private ProductTable productTable;

    @Autowired
    private InstrumentRepository instrumentRepository;


    @Autowired
    private PlasticRepository plasticRepository;

    @Autowired
    private CardsBasicRepository cardsBasicRepository;

    @Autowired
    private AccountBasicRepository accountBasicRepository;

    @Autowired
    private ProductCardGenRepository productCardGenRepository;

    @Autowired
    private CardAccumValuesService cardAccumValuesService;


    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CardAccumulatedValuesRepository cardAccumulatedValuesRepository;

    private Set<AccountDef> accountDefSet;

    private CustomerDef customerDef;

    private AccountBasic accountBasic;



    @BeforeEach
    void setup(){


        cardAccumulatedValuesRepository.findAll()
                .forEach(cardAccumulatedValues -> cardAccumulatedValuesRepository.delete(cardAccumulatedValues));


        cardsBasicRepository.findAll()
                .forEach(cardsBasic -> cardsBasicRepository.delete(cardsBasic));

        plasticRepository.findAll()
                .forEach(plastic -> plasticRepository.delete(plastic));

        instrumentRepository.findAll()
                .forEach(instrument -> instrumentRepository.delete(instrument));

        setupProductTable();

        setupCustomer();
        setupCustomer();
        setupAccounts();

    }

    @Test
    void createNewInstrument() {

        CardsBasic cardsBasic = createCardBasic(accountDefSet);
        cardsBasicRepository.save(cardsBasic);

        InstrumentAddDTO instrumentAddDTO = createAddInstrumentDTO(true, cardsBasic.getCardNumber(),
                cardsBasic.getOrg(),cardsBasic.getProduct() );

        webTestClient.post()
                .uri(EndPoints.INSTRUMENT)
                .body(Mono.just(instrumentAddDTO),InstrumentAddDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(InstrumentDto.class)
                .value(instrumentDto -> {

                    Instrument instrument = instrumentRepository.findById(instrumentDto.getInstrumentNumber()).get();

                    assertAll(
                            () -> assertEquals(instrument.getInstrumentNumber(), instrumentDto.getInstrumentNumber()),
//                () -> assertEquals(instrument.getAccountNumber(), instrumentDto.getAccountNumber()),
                            () -> assertEquals(instrumentAddDTO.getCardNumber(), instrument.getCardNumber()),
                            () -> assertEquals(cardsBasic.getCustomerNumber(), instrument.getCustomerNumber()),
                            () -> assertEquals(cardsBasic.getCorporateNumber(), instrument.getCorporateNumber()),
                            () -> assertEquals(Util.getBlockType(instrumentAddDTO.getBlockType()), instrument.getBlockType()),
                            () -> assertEquals(instrumentAddDTO.getExpiryDate(), instrumentDto.getExpiryDate()),
                            () -> assertEquals(instrumentAddDTO.getOrg(), instrument.getOrg()),
                            () -> assertEquals(instrumentAddDTO.getProduct(), instrument.getProduct()),
                            () -> assertEquals(instrument.isActive(), instrumentDto.isActive()),
                            ()-> assertEquals(cardsBasic.getAccountDefSet().size(),instrument.getAccountDefSet().size()),
                            ()-> assertEquals(Util.getInstrumentType(instrumentAddDTO.getInstrumentType()),instrument.getInstrumentType())

                    );

                });
    }

    @Test
    void createNewInstrument1() {

        CardsBasic cardsBasic = createCardBasic(accountDefSet);
        cardsBasicRepository.save(cardsBasic);

        InstrumentAddDTO instrumentAddDTO = createAddInstrumentDTO(true, cardsBasic.getCardNumber(),
                cardsBasic.getOrg(),cardsBasic.getProduct() );

        instrumentAddDTO.setInstrumentNumber(null);

        webTestClient.post()
                .uri(EndPoints.INSTRUMENT)
                .body(Mono.just(instrumentAddDTO),InstrumentAddDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(InstrumentDto.class)
                .value(instrumentDto -> {

                    Instrument instrument = instrumentRepository.findById(instrumentDto.getInstrumentNumber()).get();

                    assertAll(
                            ()-> assertNotNull(instrumentDto.getInstrumentNumber()),
//                            () -> assertEquals(instrument.getInstrumentNumber(), instrumentDto.getInstrumentNumber()),
//                () -> assertEquals(instrument.getAccountNumber(), instrumentDto.getAccountNumber()),
                            () -> assertEquals(instrumentAddDTO.getCardNumber(), instrument.getCardNumber()),
                            () -> assertEquals(cardsBasic.getCustomerNumber(), instrument.getCustomerNumber()),
                            () -> assertEquals(cardsBasic.getCorporateNumber(), instrument.getCorporateNumber()),
                            () -> assertEquals(Util.getBlockType(instrumentAddDTO.getBlockType()), instrument.getBlockType()),
                            () -> assertEquals(instrumentAddDTO.getExpiryDate(), instrumentDto.getExpiryDate()),
                            () -> assertEquals(instrumentAddDTO.getOrg(), instrument.getOrg()),
                            () -> assertEquals(instrumentAddDTO.getProduct(), instrument.getProduct()),
                            () -> assertEquals(instrument.isActive(), instrumentDto.isActive()),
                            ()-> assertEquals(cardsBasic.getAccountDefSet().size(),instrument.getAccountDefSet().size()),
                            ()-> assertEquals(Util.getInstrumentType(instrumentAddDTO.getInstrumentType()),instrument.getInstrumentType())

                    );

                });
    }

    @Test
    void createNewInstrument2() {

        CardsBasic cardsBasic = createCardBasic(accountDefSet);
//        cardsBasicRepository.save(cardsBasic);

        InstrumentAddDTO instrumentAddDTO = createAddInstrumentDTO(true, cardsBasic.getCardNumber(),
                cardsBasic.getOrg(),cardsBasic.getProduct() );

        instrumentAddDTO.setInstrumentNumber(null);

        webTestClient.post()
                .uri(EndPoints.INSTRUMENT)
                .body(Mono.just(instrumentAddDTO),InstrumentAddDTO.class)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .value(s -> {
                    assertNotNull(s);
                });
    }

    @Test
    void fetchInstrumentNumber() {

        CardsBasic cardsBasic = createCardBasic(accountDefSet);
        cardsBasicRepository.save(cardsBasic);

        Instrument instrument = createInstrument(cardsBasic.getCardNumber(),cardsBasic.getCustomerNumber(),null,accountDefSet);
        instrumentRepository.save(instrument);

        String uri = EndPoints.INSTRUMENT_NBR.replace("{instrumentNumber}",instrument.getInstrumentNumber());

        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBody(InstrumentDto.class)
                .value(instrumentDto -> {

                    assertAll(
                            ()-> assertEquals(instrument.getBlockType(),Util.getBlockType(instrumentDto.getBlockType())),
                            ()-> assertEquals(instrument.getInstrumentNumber(), instrumentDto.getInstrumentNumber())
                    );
                });

    }

    @Test
    void fetchInstrumentNumber1() {

        CardsBasic cardsBasic = createCardBasic(accountDefSet);
        cardsBasicRepository.save(cardsBasic);

        Instrument instrument = createInstrument(cardsBasic.getCardNumber(),cardsBasic.getCustomerNumber(),null,accountDefSet);
//        instrumentRepository.save(instrument);

        String uri = EndPoints.INSTRUMENT_NBR.replace("{instrumentNumber}",instrument.getInstrumentNumber());

        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .value(s -> {

                    assertNotNull(s);
                });

    }

    @Test
    void deleteInstrumentNumber() {

        CardsBasic cardsBasic = createCardBasic(accountDefSet);
        cardsBasicRepository.save(cardsBasic);

        Instrument instrument = createInstrument(cardsBasic.getCardNumber(),cardsBasic.getCustomerNumber(),null,accountDefSet);
        instrumentRepository.save(instrument);

        String uri = EndPoints.INSTRUMENT_NBR.replace("{instrumentNumber}",instrument.getInstrumentNumber());

        webTestClient.delete()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBody(InstrumentDto.class)
                .value(instrumentDto -> {

                    Optional<Instrument> optionalInstrument = instrumentRepository.findById(instrument.getInstrumentNumber());
                    assertTrue(optionalInstrument.isEmpty());

                });

    }

    @Test
    void updateinstrument() {

        CardsBasic cardsBasic = createCardBasic(accountDefSet);
        cardsBasicRepository.save(cardsBasic);

        Instrument instrument = createInstrument(cardsBasic.getCardNumber(),cardsBasic.getCustomerNumber(),null,accountDefSet);
        instrument.setInstrumentType(InstrumentType.PLASTIC_DEBIT);
        instrumentRepository.save(instrument);

        InstrumentUpdateDTO instrumentUpdateDTO = updateInstrumentDTO(true,instrument.getInstrumentNumber(),null,null);

        String uri = EndPoints.INSTRUMENT_NBR.replace("{instrumentNumber}",instrument.getInstrumentNumber());

        webTestClient.put()
                .uri(EndPoints.INSTRUMENT)
                .body(Mono.just(instrumentUpdateDTO),InstrumentUpdateDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(InstrumentDto.class)
                .value(instrumentDto -> {

                    Instrument instrument1 = instrumentRepository.findById(instrument.getInstrumentNumber()).get();

                    assertAll(
                            ()-> assertEquals(instrument.getCardNumber(), instrument1.getCardNumber()),
                            ()-> assertEquals(Util.getInstrumentType(instrumentUpdateDTO.getInstrumentType()), instrument1.getInstrumentType()),
                            ()-> assertNotEquals(instrument.getInstrumentType(), instrument1.getInstrumentType()),
                            ()-> assertEquals(Util.getBlockType(instrumentUpdateDTO.getBlockType()), instrument1.getBlockType()),
                            ()-> assertNotEquals(instrument.getInstrumentType(), instrument1.getBlockType())

                    );

                });


    }

    @Test
    void updateinstrument1() {

        CardsBasic cardsBasic = createCardBasic(accountDefSet);
        cardsBasicRepository.save(cardsBasic);

        Instrument instrument = createInstrument(cardsBasic.getCardNumber(),cardsBasic.getCustomerNumber(),null,accountDefSet);
        instrument.setInstrumentType(InstrumentType.PLASTIC_DEBIT);
//        instrumentRepository.save(instrument);

        InstrumentUpdateDTO instrumentUpdateDTO = updateInstrumentDTO(true,instrument.getInstrumentNumber(),null,null);

        String uri = EndPoints.INSTRUMENT_NBR.replace("{instrumentNumber}",instrument.getInstrumentNumber());

        webTestClient.put()
                .uri(EndPoints.INSTRUMENT)
                .body(Mono.just(instrumentUpdateDTO),InstrumentUpdateDTO.class)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .value(s -> {

                    assertNotNull(s);
                });


    }


    @Test
    void fetchAllInstrumentsForCard() {

        CardsBasic cardsBasic = createCardBasic(accountDefSet);
        cardsBasicRepository.save(cardsBasic);

        Instrument[] instruments = createInstruments(4,cardsBasic.getCardNumber(),cardsBasic.getCustomerNumber(),
                null, accountDefSet);

        instrumentRepository.saveAll(Arrays.asList(instruments));

        String uri = EndPoints.CARD_INSTRUMENT.replace("{cardNumber}", cardsBasic.getCardNumber());

        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InstrumentDto.class)
                .hasSize(4)
                .consumeWith(listEntityExchangeResult -> {
                    List<InstrumentDto> instrumentDtoList = listEntityExchangeResult.getResponseBody();

                    assertEquals(cardsBasic.getCardNumber(),instrumentDtoList.get(0).getCardNumber());
                });

    }

    @Test
    void deleteAllInstrumentsForCard() {

        CardsBasic cardsBasic = createCardBasic(accountDefSet);
        cardsBasicRepository.save(cardsBasic);

        Instrument[] instruments = createInstruments(4,cardsBasic.getCardNumber(),cardsBasic.getCustomerNumber(),
                null, accountDefSet);

        instrumentRepository.saveAll(Arrays.asList(instruments));

        String uri = EndPoints.CARD_INSTRUMENT.replace("{cardNumber}", cardsBasic.getCardNumber());

        webTestClient.delete()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InstrumentDto.class)
                .hasSize(4)
                .consumeWith(listEntityExchangeResult -> {
                    List<InstrumentDto> instrumentDtoList = listEntityExchangeResult.getResponseBody();

                    List instrumentList = new ArrayList();
                     instrumentRepository.findAllByCardNumber(cardsBasic.getCardNumber())
                     .forEach(instrumentList::add);

                     assertEquals(0,instrumentList.size());
                });

    }


    private Plastic createPlastic(CardAction cardAction, String cardNumber){

        return Plastic.builder()
                .dynamicCVV(true)
                .cardAction(cardAction)
                .activationWaiveDuration(Duration.ofDays(10))
                .cardActivated(true)
                .cardActivatedDate(LocalDateTime.now())
                .cardNumber(cardNumber)
                .dateCardValidFrom(LocalDate.of(2020,12,02))
                .datePlasticIssued(LocalDateTime.now())
                .expiryDate(LocalDate.of(2022,04,30))
                .pendingCardAction(CardAction.NO_ACTION)
                .plasticKey(new PlasticKey(UUID.randomUUID().toString().replace("-",""),cardNumber))
                .build()
                ;
    }



    private CardsBasic createCardBasic(Set<AccountDef> accountDefSet){

        Map<PeriodicType,Map<LimitType,PeriodicCardAmount>> periodicTypeMap = new HashMap<>();

        Map<LimitType,PeriodicCardAmount> cardLimitMap = new HashMap<>();

        PeriodicCardAmount periodicCardAmount1 = PeriodicCardAmount.builder()
                .limitType(LimitType.NO_SPECIFIC)
                .transactionNumber(100)
                .transactionAmount(10000L)
                .build();

        PeriodicCardAmount periodicCardAmount2  = PeriodicCardAmount.builder()
                .limitType(LimitType.CASH)
                .transactionNumber(200)
                .transactionAmount(20000L)
                .build();

        PeriodicCardAmount periodicCardAmount3 = PeriodicCardAmount.builder()
                .limitType(LimitType.RETAIL)
                .transactionNumber(300)
                .transactionAmount(30000L)
                .build();

        cardLimitMap.put(periodicCardAmount1.getLimitType(),periodicCardAmount1);
        cardLimitMap.put(periodicCardAmount2.getLimitType(),periodicCardAmount2);
        cardLimitMap.put(periodicCardAmount3.getLimitType(),periodicCardAmount3);


        periodicTypeMap.put(PeriodicType.SINGLE,cardLimitMap);
        periodicTypeMap.put(PeriodicType.DAILY,cardLimitMap);
        periodicTypeMap.put(PeriodicType.SINGLE,cardLimitMap);
        periodicTypeMap.put(PeriodicType.MONTHLY,cardLimitMap);



        return CardsBasic.builder()
                .cardNumber(Util.generateCardNumberFromStarter("491652996363189"))
                .cardholderType(CardHolderType.PRIMARY)
                .blockType(BlockType.APPROVE)
                .cardStatus(CardStatus.ACTIVE)
                .org(001)
                .product(201)
                .waiverDaysActivation(10)
                .periodicTypePeriodicCardLimitMap(periodicTypeMap)
                .accountDefSet(accountDefSet)
                .customerNumber(UUID.randomUUID().toString().replace("-",""))
                .build();
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
                .cardsActivationRequired(true)
                .limitPercents(percentMap)
                .cardsReturn(10)
                .primaryAccountType(AccountType.CREDIT)
                .billingCurrencyCode("840")
                .build();

    }

    private ProductCardGenDef createProductCardGenDef(){

        return ProductCardGenDef.builder()
                .productId(new ProductId(1,201))
                .startingCardNumber("5500000000000004")
                .numberIncrementBy(1)
                .lastGeneratedCardNumber("5500000000000045")
                .endingGeneratedCardNumber("5500000099999999")
                .build();
    }

    private AccountBasic createAccountBasic(String customerNumber){

        Map<BalanceTypes,Long> balanceTypesMap = new HashMap<>();

        balanceTypesMap.put(BalanceTypes.CURRENT_BALANCE,100000L);
        balanceTypesMap.put(BalanceTypes.CASH_BALANCE,50000L);
        balanceTypesMap.put(BalanceTypes.INTERNATIONAL_CASH,30000L);
        balanceTypesMap.put(BalanceTypes.INTERNATIONAL,70000L);
        balanceTypesMap.put(BalanceTypes.INSTALLMENT_BALANCE,80000L);
        balanceTypesMap.put(BalanceTypes.INSTALLMENT_CASH,20000L);
        balanceTypesMap.put(BalanceTypes.INTERNATIONAL_INSTALLMENT,10000L);


        return AccountBasic.builder()
                .org(001)
                .product(201)
                .accountNumber(UUID.randomUUID().toString().replace("-",""))
                .blockType(BlockType.BLOCK_DECLINE)
                .dateBlockApplied(LocalDateTime.now())
                .billingCurrencyCode("840")
                .limitsMap(balanceTypesMap)
                .datePreviousBLockType(LocalDateTime.of(2020,12,23,11,24,30))
                .previousBlockType(BlockType.BLOCK_SUSPECTED_FRAUD)
                .accountType(AccountType.CREDIT)
                .customerNumber(customerNumber)
                .corporateNumber(UUID.randomUUID().toString().replace("-",""))
                .previousAccountNumber(UUID.randomUUID().toString().replace("-",""))
                .dateTransfer(LocalDateTime.now())
                .build();

    }

    private CustomerDef createCustomerDef(boolean allFields){

        Map<CustomerIDType,String> customerIDMap = new HashMap<>();
        customerIDMap.put(CustomerIDType.SSN_OR_NATIONAL_ID,"12345678");
        customerIDMap.put(CustomerIDType.DRIVERS_LICENCE,"ABCDEFGHIJ");


        CustomerDef.CustomerDefBuilder builder = CustomerDef.builder()
                .customerNumber(UUID.randomUUID().toString().replace("-",""))
                .customerType(CustomerType.OWNER)
                .addressType(AddressType.PRIMARY)
                .customerName("Test 1")
                .addressLine1("29, Janatha Road")
                .postalCode("123456")
                .countryCode("IND")
                ;

        if(allFields){
            builder
                    .addressLine2("vyttilla, kochi")
                    .state("kerala")
                    .customerIDMap(customerIDMap)
                    .primaryEmail("testemail.com")
                    .primaryPhoneNumber("34567890");
        }
        return builder.build();
    }

    private void setupCustomer() {

        customerRepository.findAll()
                .forEach(customerDef1 -> customerRepository.delete(customerDef1));

        customerDef = createCustomerDef(true);
        customerRepository.save(customerDef);

    }

    private void setupProductTable() {

        productDefRepository.findAll()
                .forEach(productDef -> productDefRepository.delete(productDef));

        productCardGenRepository.findAll()
                .forEach(productCardGenDef -> productCardGenRepository.delete(productCardGenDef));


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
        productDef2.getProductId().setOrg(001);
        productDef2.getProductId().setProduct(203);

        productDefRepository.save(productDef2);
        productTable.loadMap(productDef2);

        ProductCardGenDef productCardGenDef = createProductCardGenDef();
        productCardGenDef.getProductId().setOrg(1);
        productCardGenDef.getProductId().setProduct(201);
        productCardGenRepository.save(productCardGenDef);

        ProductCardGenDef productCardGenDef1 = createProductCardGenDef();
        productCardGenDef1.getProductId().setOrg(001);
        productCardGenDef1.getProductId().setProduct(202);
        productCardGenRepository.save(productCardGenDef1);

        ProductCardGenDef productCardGenDef2 = createProductCardGenDef();
        productCardGenDef2.getProductId().setOrg(001);
        productCardGenDef2.getProductId().setProduct(203);
        productCardGenRepository.save(productCardGenDef2);

    }

    private void setupAccounts() {


        accountBasicRepository.findAll()
                .forEach(accountBasic -> accountBasicRepository.delete(accountBasic));

        AccountBasic accountBasic = createAccountBasic(customerDef.getCustomerNumber());
        accountBasic.setAccountType(AccountType.UNIVERSAL);
        accountBasicRepository.save(accountBasic);

        accountBasic = createAccountBasic(customerDef.getCustomerNumber());
        accountBasic.setAccountType(AccountType.CREDIT);
        accountBasicRepository.save(accountBasic);

        accountBasic = createAccountBasic(customerDef.getCustomerNumber());
        accountBasic.setAccountType(AccountType.SAVINGS);
        this.accountBasic = accountBasicRepository.save(accountBasic);



        accountDefSet = StreamSupport.stream(accountBasicRepository.findAll().spliterator(),false)
                .map(accountBasic1 -> AccountDef.builder()
                        .accountNumber(accountBasic1.getAccountNumber())
                        .billingCurrencyCode(accountBasic1.getBillingCurrencyCode())
                        .accountType(accountBasic1.getAccountType())
                        .build()
                ).collect(Collectors.toSet());


    }

    private Instrument createInstrument(boolean allFields, String deleteAccountNumber) {

        Instrument instrument = new Instrument();



        String instrumentNumber = Util.generateCardNumberFromStarter("491652996363189");


        AccountDef accountDef1 = AccountDef.builder()
                .accountType(AccountType.SAVINGS)
                .billingCurrencyCode("124")
                .accountNumber(UUID.randomUUID().toString().replace("-",""))
                .build();



        AccountDef accountDef2 = AccountDef.builder()
                .accountType(AccountType.UNIVERSAL)
                .billingCurrencyCode("840")
                .accountNumber(deleteAccountNumber)
                .build();

        Set<AccountDef> accountDefSet = new HashSet<>();



        accountDefSet.add(accountDef1);
        accountDefSet.add(accountDef2);

        instrument.setInstrumentNumber(instrumentNumber);
        instrument.setInstrumentType(InstrumentType.PLASTIC_CREDIT);
        instrument.setActive(true);
        String cardNumber = Util.generateNextCardNumber(instrumentNumber);
        instrument.setCardNumber(cardNumber);
//        instrument.setAccountNumber(UUID.randomUUID().toString().replace("-", ""));
        instrument.setAccountDefSet(accountDefSet);
        instrument.setCustomerNumber(UUID.randomUUID().toString().replace("-", ""));
        if (allFields) {
            instrument.setCorporateNumber(UUID.randomUUID().toString().replace("-", ""));
            instrument.setBlockType(BlockType.APPROVE);
            instrument.setExpiryDate(LocalDate.of(2020, 11, 23));
        }

        return instrument;
    }

    private Instrument[] createInstruments(int count, String cardNumber, String customerNumber,
                                           String corporateNumber, Set<AccountDef> accountDefSet){



        Instrument[] instruments = new Instrument[count];
        String instrumentNumber = Util.generateNextCardNumber(cardNumber);
        for (int i = 0 ; i < count; i ++ ){
            instruments[i] = createInstrument(cardNumber,customerNumber,corporateNumber,accountDefSet,instrumentNumber);
            instrumentNumber = Util.generateNextCardNumber(instrumentNumber);
        }
        return instruments;
    }

    private Instrument createInstrument(String cardNumber, String customerNumber,
                                        String corporateNumber, Set<AccountDef> accountDefSet) {

        Instrument instrument = new Instrument();

        String instrumentNumber = Util.generateNextCardNumber(cardNumber);



        instrument.setInstrumentNumber(instrumentNumber);
        instrument.setInstrumentType(InstrumentType.PLASTIC_CREDIT);
        instrument.setActive(true);
        instrument.setCardNumber(cardNumber);
//        instrument.setAccountNumber(accountNumber);
        instrument.setCustomerNumber(customerNumber);
        instrument.setCorporateNumber(corporateNumber);
        instrument.setBlockType(BlockType.APPROVE);
        instrument.setAccountDefSet(accountDefSet);
        instrument.setExpiryDate(LocalDate.of(2020, 11, 23));

        return instrument;
    }

    private Instrument createInstrument(String cardNumber, String customerNumber,
                                        String corporateNumber, Set<AccountDef> accountDefSet,String instrumentNumber) {

        Instrument instrument = new Instrument();




        instrument.setInstrumentNumber(instrumentNumber);
        instrument.setInstrumentType(InstrumentType.PLASTIC_CREDIT);
        instrument.setActive(true);
        instrument.setCardNumber(cardNumber);
//        instrument.setAccountNumber(accountNumber);
        instrument.setCustomerNumber(customerNumber);
        instrument.setCorporateNumber(corporateNumber);
        instrument.setBlockType(BlockType.APPROVE);
        instrument.setAccountDefSet(accountDefSet);
        instrument.setExpiryDate(LocalDate.of(2020, 11, 23));

        return instrument;
    }



    private InstrumentAddDTO createAddInstrumentDTO(boolean allFields,String cardNumber, int org, int product) {


        String instrumentNumber = Util.generateCardNumberFromStarter("491652996363189");

        InstrumentAddDTO.InstrumentAddDTOBuilder instrumentBuilder = InstrumentAddDTO.builder()
                .instrumentNumber(instrumentNumber)
                .cardNumber(cardNumber)
//                .accountNumber(UUID.randomUUID().toString().replace("-", ""))
                .active(true)
                .instrumentType("0")
                .org(org)
                .product(product);
        ;

        if (allFields) {
            instrumentBuilder
                    .expiryDate("20201125")
                    .blockType("2")
            ;
        }

        return instrumentBuilder.build();

    }

    private InstrumentUpdateDTO updateInstrumentDTO(boolean allFields, String instrumentNumber, Integer[] numbers, String deleteAccountNumber) {


        String cardNumber = null;
        InstrumentUpdateDTO.InstrumentUpdateDTOBuilder builder = InstrumentUpdateDTO.builder()
                .instrumentNumber(instrumentNumber);

        if (allFields) {

            return builder
                    .active(false)
                    .blockType(Util.getBlockType(BlockType.BLOCK_SUSPECTED_FRAUD))
                    .expiryDate("20201125")
                    .instrumentType(Util.getInstrumentType(InstrumentType.PLASTIC_CREDIT))
                    .org(001)
                    .product(125)
                    .build();

        }


        for (Integer i : numbers) {

            updateFieldsToBuilder(builder, i,cardNumber,deleteAccountNumber);
        }

        return builder.build();


    }

    private void updateFieldsToBuilder(InstrumentUpdateDTO.InstrumentUpdateDTOBuilder builder, Integer i,String cardNumber,String deleteAccountNumber) {


        switch (i) {
            case 1: {
                builder.cardNumber(cardNumber);
                break;
            }
            case 2: {
                builder.active(false);
                break;
            }
            case 3: {
//                builder
//                        .accountNumber(UUID.randomUUID().toString().replace("-", ""));
//                break;
            }
            case 4: {
                builder
                        .blockType(Util.getBlockType(BlockType.BLOCK_SUSPECTED_FRAUD));
                break;
            }
            case 5:
            case 12:
            case 11:
            case 6: {
                break;
            }
            case 7: {
                builder.expiryDate("20201125");
                break;
            }
            case 8: {
                builder.instrumentType(Util.getInstrumentType(InstrumentType.PLASTIC_CREDIT));
                break;
            }
            case 9: {
                builder
                        .org(001);
                break;
            }
            case 10: {
                builder.product(125);
                break;
            }
        }
    }
}