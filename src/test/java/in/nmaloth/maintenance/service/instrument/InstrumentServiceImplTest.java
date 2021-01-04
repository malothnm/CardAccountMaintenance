package in.nmaloth.maintenance.service.instrument;

import in.nmaloth.entity.BlockType;
import in.nmaloth.entity.account.AccountDef;
import in.nmaloth.entity.account.AccountType;
import in.nmaloth.entity.account.BalanceTypes;
import in.nmaloth.entity.card.*;
import in.nmaloth.entity.instrument.Instrument;
import in.nmaloth.entity.instrument.InstrumentType;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.entity.product.ProductId;
import in.nmaloth.maintenance.config.data.ProductTable;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.account.AccountDefDTO;
import in.nmaloth.maintenance.model.dto.card.CardBasicAddDTO;
import in.nmaloth.maintenance.model.dto.card.CardLimitsDTO;
import in.nmaloth.maintenance.model.dto.card.PeriodicCardLimitDTO;
import in.nmaloth.maintenance.model.dto.instrument.InstrumentAddDTO;
import in.nmaloth.maintenance.model.dto.instrument.InstrumentDto;
import in.nmaloth.maintenance.model.dto.instrument.InstrumentUpdateDTO;
import in.nmaloth.maintenance.repository.card.CardsBasicRepository;
import in.nmaloth.maintenance.repository.instrument.InstrumentRepository;
import in.nmaloth.maintenance.repository.product.ProductDefRepository;
import in.nmaloth.maintenance.service.cards.CardsBasicService;
import in.nmaloth.maintenance.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InstrumentServiceImplTest {

    @Autowired
    private InstrumentService instrumentService;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private CardsBasicRepository cardsBasicRepository;

    @Autowired
    private ProductDefRepository productDefRepository;

    @Autowired
    private CardsBasicService cardsBasicService;

    @Autowired
    private ProductTable productTable;

    private CardsBasic cardsBasic;


    @BeforeEach
    void deleteAllInstruments(){

        instrumentRepository.findAll()
                .forEach(instrument -> instrumentRepository.delete(instrument));

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

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO(UUID.randomUUID().toString().replace("-",""));
        cardsBasic = cardsBasicService.createNewCardsRecord(cardBasicAddDTO).block();


    }

    @Test
    void createNewInstrument(){

        InstrumentAddDTO instrumentAddDTO = createAddInstrumentDTO(true,UUID.randomUUID().toString().replace("-",""));

        instrumentAddDTO.setCardId(cardsBasic.getCardId());

        Instrument instrumentDto = instrumentService.createNewInstrument(instrumentAddDTO)
                .block();

        Instrument instrument = instrumentRepository.findById(instrumentDto.getInstrumentNumber()).get();


        assertAll(
                () -> assertEquals(instrument.getInstrumentNumber(), instrumentDto.getInstrumentNumber()),
//                () -> assertEquals(instrument.getAccountNumber(), instrumentDto.getAccountNumber()),
                () -> assertEquals(instrument.getCardNumber(), instrumentDto.getCardNumber()),
                () -> assertEquals(instrument.getCustomerNumber(), instrumentDto.getCustomerNumber()),
                () -> assertEquals(instrument.getCorporateNumber(), instrumentDto.getCorporateNumber()),
                () -> assertEquals(instrument.getBlockType(), instrumentDto.getBlockType()),
                () -> assertEquals(instrument.getExpiryDate(), instrumentDto.getExpiryDate()),
                () -> assertEquals(instrument.getOrg(), instrumentDto.getOrg()),
                () -> assertEquals(instrument.getProduct(), instrumentDto.getProduct()),
                () -> assertEquals(instrument.isActive(), instrumentDto.isActive()),
                ()-> assertEquals(cardsBasic.getAccountDefSet().size(),instrument.getAccountDefSet().size()),
                ()-> assertEquals(instrument.getInstrumentType(),instrumentDto.getInstrumentType())

        );


    }

    @Test
    void updateAccounts(){

        InstrumentAddDTO instrumentAddDTO = createAddInstrumentDTO(true,UUID.randomUUID().toString().replace("-",""));

        instrumentAddDTO.setCardId(cardsBasic.getCardId());

        Instrument instrument = instrumentService.createNewInstrument(instrumentAddDTO)
                .block();

        cardsBasic.setCustomerNumber(UUID.randomUUID().toString().replace("-",""));
        cardsBasic.setCorporateNumber(UUID.randomUUID().toString().replace("-",""));
        cardsBasic.getAccountDefSet().add(AccountDef.builder()
                .accountNumber(UUID.randomUUID().toString().replace("-",""))
                .accountType(AccountType.LOANS)
                .billingCurrencyCode("340")
                .build()

        );

        instrumentService.updateAccounts(instrument,cardsBasic);

        assertAll(
                () -> assertEquals(cardsBasic.getAccountDefSet().size(), instrument.getAccountDefSet().size()),
//                () -> assertEquals(instrument.getAccountNumber(), instrumentDto.getAccountNumber()),
                () -> assertEquals(cardsBasic.getCustomerNumber(), instrument.getCustomerNumber()),
                () -> assertEquals(cardsBasic.getCorporateNumber(), instrument.getCorporateNumber())

        );


    }

    @Test
    void updateInstrumentFromDTO(){

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");

        InstrumentAddDTO instrumentAddDTO = createAddInstrumentDTO(true,deleteAccountNumber);
        instrumentAddDTO.setCardId(cardsBasic.getCardId());
        Instrument instrumentDto = instrumentService.createNewInstrument(instrumentAddDTO)
                .block();

        InstrumentUpdateDTO instrumentUpdateDTO = updateInstrumentDTO(true,null,deleteAccountNumber);
        instrumentUpdateDTO.setCardId(cardsBasic.getCardId());

        Instrument instrumentDto1 = instrumentService.updateInstrument(instrumentUpdateDTO)
                .block();

        Instrument instrument = instrumentRepository.findById(instrumentDto.getInstrumentNumber()).get();


        assertAll(
                () -> assertEquals(instrumentUpdateDTO.getInstrumentNumber(), instrument.getInstrumentNumber()),
//                () -> assertEquals(instrumentDto2.getAccountNumber(), instrumentDto1.getAccountNumber()),
                () -> assertEquals(instrumentAddDTO.getCardId(), instrument.getCardNumber()),
                () -> assertEquals(cardsBasic.getCustomerNumber(), instrument.getCustomerNumber()),
                () -> assertEquals(cardsBasic.getCorporateNumber(), cardsBasic.getCorporateNumber()),
                () -> assertEquals(Util.getBlockType(instrumentUpdateDTO.getBlockType()), instrument.getBlockType()),
//                () -> assertEquals(instrumentUpdateDTO.getExpiryDate(), instrument.getExpiryDate()),
                () -> assertEquals(instrumentAddDTO.getOrg(), instrument.getOrg()),
                () -> assertEquals(instrumentUpdateDTO.getProduct(), instrument.getProduct()),
                ()-> assertEquals(Util.getInstrumentType(instrumentUpdateDTO.getInstrumentType()),instrument.getInstrumentType())

        );


    }


    @Test
    void updateInstrumentFromDTO1(){

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");


        InstrumentAddDTO instrumentAddDTO = createAddInstrumentDTO(true,deleteAccountNumber);
        instrumentAddDTO.setCardId(cardsBasic.getCardId());
        Instrument instrumentDto = instrumentService.createNewInstrument(instrumentAddDTO)
                .block();

        InstrumentUpdateDTO instrumentUpdateDTO = updateInstrumentDTO(true,null,deleteAccountNumber);

        instrumentUpdateDTO.setInstrumentNumber("1234567890");
        Mono<Instrument> instrumentDtoMono = instrumentService.updateInstrument(instrumentUpdateDTO);

        StepVerifier
                .create(instrumentDtoMono.log())
                .expectError(NotFoundException.class)
                .verify();

    }

    @Test
    void fetchInstrument(){

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");

        InstrumentAddDTO instrumentAddDTO = createAddInstrumentDTO(true,deleteAccountNumber);
        instrumentAddDTO.setCardId(cardsBasic.getCardId());
        Instrument instrumentDto = instrumentService.createNewInstrument(instrumentAddDTO)
                .block();

        Mono<Instrument> instrumentDtoMono = instrumentService.fetchInstrument(instrumentDto.getInstrumentNumber());

        StepVerifier
                .create(instrumentDtoMono)
                .expectNextCount(1)
                .verifyComplete();

    }

    @Test
    void deleteInstrument(){

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");

        InstrumentAddDTO instrumentAddDTO = createAddInstrumentDTO(true,deleteAccountNumber);
        instrumentAddDTO.setCardId(cardsBasic.getCardId());
        Instrument instrumentDto = instrumentService.createNewInstrument(instrumentAddDTO)
                .block();

        Mono<Instrument> instrumentDtoMono = instrumentService.deleteInstrument(instrumentDto.getInstrumentNumber());

        Instrument instrumentDto1 = instrumentDtoMono.block();

        Optional<Instrument> instrumentOptional = instrumentRepository.findById(instrumentDto.getInstrumentNumber());

        assertTrue(instrumentOptional.isEmpty());
    }



    @Test
    void fetchInstrument1(){

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");

        InstrumentAddDTO instrumentAddDTO = createAddInstrumentDTO(true,deleteAccountNumber);
        instrumentAddDTO.setCardId(cardsBasic.getCardId());
        Instrument instrumentDto = instrumentService.createNewInstrument(instrumentAddDTO)
                .block();

        instrumentDto.setInstrumentNumber("12345");
        Mono<Instrument> instrumentDtoMono = instrumentService.fetchInstrument(instrumentDto.getInstrumentNumber());

        StepVerifier
                .create(instrumentDtoMono)
                .expectError(NotFoundException.class)
                .verify();

    }

    @Test
    void deleteInstrument1(){

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");

        InstrumentAddDTO instrumentAddDTO = createAddInstrumentDTO(true,deleteAccountNumber);
        instrumentAddDTO.setCardId(cardsBasic.getCardId());
        Instrument instrumentDto = instrumentService.createNewInstrument(instrumentAddDTO)
                .block();

        Mono<Instrument> instrumentDtoMono = instrumentService.deleteInstrument("123456");

        StepVerifier
                .create(instrumentDtoMono)
                .expectError(NotFoundException.class)
                .verify();

    }


    @Test
    void fetchInstrumentsForCardNumber(){

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");

        String acctNumber = UUID.randomUUID().toString().replace("-", "");
        String customerNumber = UUID.randomUUID().toString().replace("-", "");
        String corporateNumber = UUID.randomUUID().toString().replace("-", "");

        Instrument[] instruments = createInstruments(3,"491652996363189",acctNumber,customerNumber,corporateNumber);
        instrumentRepository.saveAll(Arrays.asList(instruments));

        String cardNumber = Util.generateCardNumberFromStarter("491652996363189");

        Flux<Instrument> instrumentDtoFlux = instrumentService.fetchAllInstrumentsForCard(cardNumber);

        StepVerifier
                .create(instrumentDtoFlux)
                .expectNextCount(3)
                .verifyComplete();

    }

    @Test
    void deleteInstrumentsForCardNumber(){

        String acctNumber = UUID.randomUUID().toString().replace("-", "");
        String customerNumber = UUID.randomUUID().toString().replace("-", "");
        String corporateNumber = UUID.randomUUID().toString().replace("-", "");

        Instrument[] instruments = createInstruments(3,"491652996363189",acctNumber,customerNumber,corporateNumber);
        instrumentRepository.saveAll(Arrays.asList(instruments));

        String cardNumber = Util.generateCardNumberFromStarter("491652996363189");


        instrumentService.deleteAllInstrumentsForCard(cardNumber)
                .blockLast();

        Flux<Instrument> instrumentDtoFlux = instrumentService.deleteAllInstrumentsForCard(cardNumber);


        StepVerifier
                .create(instrumentDtoFlux)
                .expectNextCount(0)
                .verifyComplete();

    }


    @Test
    void fetchInstrumentsForCardNumber1(){

        String acctNumber = UUID.randomUUID().toString().replace("-", "");
        String customerNumber = UUID.randomUUID().toString().replace("-", "");
        String corporateNumber = UUID.randomUUID().toString().replace("-", "");

        Instrument[] instruments = createInstruments(3,"491652996363189",acctNumber,customerNumber,corporateNumber);
        instrumentRepository.saveAll(Arrays.asList(instruments));

        String cardNumber = Util.generateCardNumberFromStarter("491652996363189");

        Flux<Instrument> instrumentDtoFlux = instrumentService.fetchAllInstrumentsForCard("123456");

        StepVerifier
                .create(instrumentDtoFlux)
                .expectNextCount(0)
                .verifyComplete();

    }







    @Test
    void createInstrumentFromAddDTO() {

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");

        InstrumentAddDTO instrumentAddDTO = createAddInstrumentDTO(true,deleteAccountNumber);
        Instrument instrument = instrumentService.createInstrumentFromAddDTO(instrumentAddDTO,cardsBasic);

        assertAll(
                () -> assertEquals(instrumentAddDTO.getInstrumentNumber(), instrument.getInstrumentNumber()),
//                () -> assertEquals(instrumentAddDTO.getAccountNumber(), instrument.getAccountNumber()),
                () -> assertEquals(instrumentAddDTO.getCardId(), instrument.getCardNumber()),
                () -> assertEquals(Util.getBlockType(instrumentAddDTO.getBlockType()), instrument.getBlockType()),
                () -> assertEquals(instrumentAddDTO.getExpiryDate(), instrument.getExpiryDate().format(DateTimeFormatter.BASIC_ISO_DATE)),
                () -> assertEquals(instrumentAddDTO.getOrg(), instrument.getOrg()),
                () -> assertEquals(instrumentAddDTO.getProduct(), instrument.getProduct()),
                () -> assertEquals(instrumentAddDTO.getActive(), instrument.isActive()),
                ()-> assertEquals(Util.getInstrumentType(instrumentAddDTO.getInstrumentType()),instrument.getInstrumentType())

        );

    }

    @Test
    void createInstrumentFromAddDTO1() {

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");

        InstrumentAddDTO instrumentAddDTO = createAddInstrumentDTO(false,deleteAccountNumber);
        Instrument instrument = instrumentService.createInstrumentFromAddDTO(instrumentAddDTO,cardsBasic);
        assertAll(
                () -> assertEquals(instrumentAddDTO.getInstrumentNumber(), instrument.getInstrumentNumber()),
//                () -> assertEquals(instrumentAddDTO.getAccountNumber(), instrument.getAccountNumber()),
                () -> assertEquals(instrumentAddDTO.getCardId(), instrument.getCardNumber()),
                () -> assertNull(instrument.getCorporateNumber()),
                () -> assertNull(instrument.getBlockType()),
                () -> assertNull(instrument.getExpiryDate()),
                () -> assertEquals(instrumentAddDTO.getOrg(), instrument.getOrg()),
                () -> assertEquals(instrumentAddDTO.getProduct(), instrument.getProduct()),
                () -> assertEquals(instrumentAddDTO.getActive(), instrument.isActive()),
                ()-> assertEquals(Util.getInstrumentType(instrumentAddDTO.getInstrumentType()),instrument.getInstrumentType())

        );

    }


    @Test
    void createInstrumentFromUpdateDTO() {

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");

        Instrument instrument = createInstrument(true,deleteAccountNumber);

        InstrumentUpdateDTO instrumentUpdateDTO = updateInstrumentDTO(true,null,deleteAccountNumber);

        Instrument instrument1 = instrumentService.createInstrumentFromUpdateDTO(instrumentUpdateDTO,instrument);

        assertAll(
                () -> assertEquals(instrumentUpdateDTO.getInstrumentNumber(), instrument1.getInstrumentNumber()),
//                () -> assertEquals(instrumentUpdateDTO.getAccountNumber(), instrument1.getAccountNumber()),
                () -> assertEquals(instrumentUpdateDTO.getCardId(), instrument1.getCardNumber()),
                () -> assertEquals(Util.getBlockType(instrumentUpdateDTO.getBlockType()), instrument1.getBlockType()),
                () -> assertEquals(instrumentUpdateDTO.getExpiryDate(), instrument1.getExpiryDate().format(DateTimeFormatter.BASIC_ISO_DATE)),
                () -> assertEquals(instrumentUpdateDTO.getOrg(), instrument1.getOrg()),
                () -> assertEquals(instrumentUpdateDTO.getProduct(), instrument1.getProduct()),
                () -> assertEquals(instrumentUpdateDTO.getActive(), instrument1.isActive()),
                ()-> assertEquals(Util.getInstrumentType(instrumentUpdateDTO.getInstrumentType()),instrument1.getInstrumentType()),
                ()-> assertEquals(cardsBasic.getAccountDefSet().size(), instrument1.getAccountDefSet().size())

        );
    }

    @Test
    void createInstrumentFromUpdateDTO1() {

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");

        Instrument instrument = createInstrument(true,deleteAccountNumber);
        Integer[] numbers = {1,2,3,11};

        InstrumentUpdateDTO instrumentUpdateDTO = updateInstrumentDTO(false,numbers,deleteAccountNumber);

        Instrument instrument1 = instrumentService.createInstrumentFromUpdateDTO(instrumentUpdateDTO,instrument);

        assertAll(
                () -> assertEquals(instrument.getInstrumentNumber(), instrument1.getInstrumentNumber()),
//                () -> assertEquals(instrumentUpdateDTO.getAccountNumber(), instrument1.getAccountNumber()),
                () -> assertEquals(instrumentUpdateDTO.getCardId(), instrument1.getCardNumber()),
                () -> assertEquals(instrument.getCustomerNumber(), instrument1.getCustomerNumber()),
                () -> assertEquals(instrument.getCorporateNumber(), instrument1.getCorporateNumber()),
                () -> assertEquals(instrument.getBlockType(), instrument1.getBlockType()),
                () -> assertEquals(instrument.getExpiryDate(), instrument1.getExpiryDate()),
                () -> assertEquals(instrument.getOrg(), instrument1.getOrg()),
                () -> assertEquals(instrument.getProduct(), instrument1.getProduct()),
                () -> assertEquals(instrumentUpdateDTO.getActive(), instrument1.isActive()),
                ()-> assertEquals(instrument.getInstrumentType(),instrument1.getInstrumentType()),
                ()-> assertEquals(cardsBasic.getAccountDefSet().size(), instrument.getAccountDefSet().size())

        );
    }

    @Test
    void createInstrumentFromUpdateDTO2() {

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");

        Instrument instrument = createInstrument(true,deleteAccountNumber);
        Integer[] numbers = {4,5,6,12};

        InstrumentUpdateDTO instrumentUpdateDTO = updateInstrumentDTO(false,numbers,deleteAccountNumber);

        Instrument instrument1 = instrumentService.createInstrumentFromUpdateDTO(instrumentUpdateDTO,instrument);

        assertAll(
                () -> assertEquals(instrument.getInstrumentNumber(), instrument1.getInstrumentNumber()),
//                () -> assertEquals(instrument.getAccountNumber(), instrument1.getAccountNumber()),
                () -> assertEquals(instrument.getCardNumber(), instrument1.getCardNumber()),
                () -> assertEquals(Util.getBlockType(instrumentUpdateDTO.getBlockType()), instrument1.getBlockType()),
                () -> assertEquals(instrument.getExpiryDate(), instrument1.getExpiryDate()),
                () -> assertEquals(instrument.getOrg(), instrument1.getOrg()),
                () -> assertEquals(instrument.getProduct(), instrument1.getProduct()),
                () -> assertEquals(instrument.isActive(), instrument1.isActive()),
                ()-> assertEquals(instrument.getInstrumentType(),instrument1.getInstrumentType()),
                ()-> assertEquals(cardsBasic.getAccountDefSet().size(),instrument.getAccountDefSet().size())
        );
    }

    @Test
    void createInstrumentFromUpdateDTO3() {

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");


        Instrument instrument = createInstrument(true,deleteAccountNumber);
        Integer[] numbers = {7,8,9,10};

        InstrumentUpdateDTO instrumentUpdateDTO = updateInstrumentDTO(false,numbers,deleteAccountNumber);

        Instrument instrument1 = instrumentService.createInstrumentFromUpdateDTO(instrumentUpdateDTO,instrument);

        assertAll(
                () -> assertEquals(instrument.getInstrumentNumber(), instrument1.getInstrumentNumber()),
//                () -> assertEquals(instrument.getAccountNumber(), instrument1.getAccountNumber()),
                () -> assertEquals(instrument.getCardNumber(), instrument1.getCardNumber()),
                () -> assertEquals(instrument.getCustomerNumber(), instrument1.getCustomerNumber()),
                () -> assertEquals(instrument.getCorporateNumber(), instrument1.getCorporateNumber()),
                () -> assertEquals(instrument.getBlockType(), instrument1.getBlockType()),
                () -> assertEquals(instrumentUpdateDTO.getExpiryDate(), instrument1.getExpiryDate().format(DateTimeFormatter.BASIC_ISO_DATE)),
                () -> assertEquals(instrumentUpdateDTO.getOrg(), instrument1.getOrg()),
                () -> assertEquals(instrumentUpdateDTO.getProduct(), instrument1.getProduct()),
                ()-> assertEquals(Util.getInstrumentType(instrumentUpdateDTO.getInstrumentType()),instrument1.getInstrumentType()),
                () -> assertEquals(instrument.isActive(), instrument1.isActive()),
                ()-> assertEquals(2, instrument1.getAccountDefSet().size())
        );
    }


    @Test
    void createDTOFromInstrument() {

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");

        Instrument instrument = createInstrument(true,deleteAccountNumber);
        InstrumentDto instrumentDto = instrumentService.createDTOFromInstrument(instrument);

        String[] accountNumberArray = (String[]) instrument.getAccountDefSet().stream()
                .map(accountDef -> accountDef.getAccountNumber())
                .toArray(String[]::new);

        Arrays.sort(accountNumberArray);

        String[] accountNumberArray1 = instrumentDto.getAccountDefDTOSet().stream()
                .map(accountDefDTO -> accountDefDTO.getAccountId())
                .toArray(String[]::new);

        Arrays.sort(accountNumberArray1);
        assertAll(
                () -> assertEquals(instrument.getInstrumentNumber(), instrumentDto.getInstrumentNumber()),
//                () -> assertEquals(instrument.getAccountNumber(), instrumentDto.getAccountNumber()),
                () -> assertEquals(instrument.getCardNumber(), instrumentDto.getCardId()),
                () -> assertEquals(instrument.getCustomerNumber(), instrumentDto.getCustomerId()),
                () -> assertEquals(instrument.getCorporateNumber(), instrumentDto.getCorporateNumber()),
                () -> assertEquals(instrument.getBlockType(), Util.getBlockType(instrumentDto.getBlockType())),
                () -> assertEquals(instrument.getExpiryDate().format(DateTimeFormatter.BASIC_ISO_DATE), instrumentDto.getExpiryDate()),
                () -> assertEquals(instrument.getOrg(), instrumentDto.getOrg()),
                () -> assertEquals(instrument.getProduct(), instrumentDto.getProduct()),
                () -> assertEquals(instrument.isActive(), instrumentDto.isActive()),
                ()-> assertEquals(Util.getInstrumentType(instrument.getInstrumentType()),instrumentDto.getInstrumentType()),
                ()-> assertArrayEquals(accountNumberArray,accountNumberArray1)

        );

    }


    @Test
    void createDTOFromInstrument1() {

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");

        Instrument instrument = createInstrument(false,deleteAccountNumber);
        InstrumentDto instrumentDto = instrumentService.createDTOFromInstrument(instrument);

        String[] accountDefs = instrument.getAccountDefSet()
                .stream()
                .map(accountDef -> accountDef.getAccountNumber())
                .toArray(String[]::new);

        Arrays.sort(accountDefs);

        String[] accountDefs1 = instrumentDto.getAccountDefDTOSet().stream()
                .map(accountDefDTO -> accountDefDTO.getAccountId())
                .toArray(String[]::new);

        Arrays.sort(accountDefs1);
        assertAll(
                () -> assertEquals(instrument.getInstrumentNumber(), instrumentDto.getInstrumentNumber()),
//                () -> assertEquals(instrument.getAccountNumber(), instrumentDto.getAccountNumber()),
                () -> assertEquals(instrument.getCardNumber(), instrumentDto.getCardId()),
                () -> assertEquals(instrument.getCustomerNumber(), instrumentDto.getCustomerId()),
                () -> assertNull(instrumentDto.getCorporateNumber()),
                () -> assertNull(instrumentDto.getBlockType()),
                () -> assertNull(instrumentDto.getExpiryDate()),
                () -> assertEquals(instrument.getOrg(), instrumentDto.getOrg()),
                () -> assertEquals(instrument.getProduct(), instrumentDto.getProduct()),
                () -> assertEquals(instrument.isActive(), instrumentDto.isActive()),
                ()-> assertEquals(Util.getInstrumentType(instrument.getInstrumentType()),instrumentDto.getInstrumentType()),
                ()-> assertArrayEquals(accountDefs,accountDefs1)

        );

    }

    private Instrument createInstrument(boolean allFields,String deleteAccountNumber) {

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

    private Instrument[] createInstruments(int count, String starterString,String acctNumber, String customerNumber,
                                           String corporateNumber){

        Long starter = Long.parseLong(starterString);
        String cardNumber = Util.generateCardNumberFromStarter(starter.toString());
        starter = starter + 1;

        String deleteAccountNumber = UUID.randomUUID().toString().replace("-","");


        Instrument[] instruments = new Instrument[count];
        for (int i = 0 ; i < count; i ++ ){
            instruments[i] = createInstrument(cardNumber,starter.toString(),customerNumber,acctNumber,corporateNumber,deleteAccountNumber);
            starter = starter + 1;
        }
        return instruments;
    }

    private Instrument createInstrument(String cardNumber, String instrumentStarter, String customerNumber, String accountNumber,
                                        String corporateNumber,String deleteAccountNumber) {

        Instrument instrument = new Instrument();

        String instrumentNumber = Util.generateCardNumberFromStarter(instrumentStarter);

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
        instrument.setCardNumber(cardNumber);
//        instrument.setAccountNumber(accountNumber);
        instrument.setCustomerNumber(customerNumber);
        instrument.setCorporateNumber(corporateNumber);
        instrument.setBlockType(BlockType.APPROVE);
        instrument.setAccountDefSet(accountDefSet);
        instrument.setExpiryDate(LocalDate.of(2020, 11, 23));

        return instrument;
    }


    private InstrumentAddDTO createAddInstrumentDTO(boolean allFields,String deleteAccountNumber) {


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


        String instrumentNumber = Util.generateCardNumberFromStarter("491652996363189");

        InstrumentAddDTO.InstrumentAddDTOBuilder instrumentBuilder = InstrumentAddDTO.builder()
                .instrumentNumber(instrumentNumber)
                .cardId(Util.generateNextCardNumber(instrumentNumber))
//                .accountNumber(UUID.randomUUID().toString().replace("-", ""))
                .active(true)
                .instrumentType("0")
                .org(001)
                .product(121);
        ;

        if (allFields) {
            instrumentBuilder
                    .expiryDate("20201125")
                    .blockType("2")
            ;
        }

        return instrumentBuilder.build();

    }

    private InstrumentUpdateDTO updateInstrumentDTO(  boolean allFields,Integer[] numbers,String deleteAccountNumber) {

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


        String instrumentNumber = Util.generateCardNumberFromStarter("491652996363189");

        String cardNumber = Util.generateNextCardNumber(instrumentNumber);
        cardNumber = Util.generateNextCardNumber(cardNumber);

        InstrumentUpdateDTO.InstrumentUpdateDTOBuilder builder = InstrumentUpdateDTO.builder()
                .instrumentNumber(instrumentNumber);

        if (allFields) {

            return builder
                    .cardId(cardNumber)
                    .active(false)
//                    .accountNumber(UUID.randomUUID().toString().replace("-", ""))
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



        switch (i) {
            case 1: {
                builder.cardId(cardNumber);
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

