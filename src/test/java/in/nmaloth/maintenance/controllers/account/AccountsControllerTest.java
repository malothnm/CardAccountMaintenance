package in.nmaloth.maintenance.controllers.account;

import in.nmaloth.entity.BlockType;
import in.nmaloth.entity.account.*;
import in.nmaloth.entity.customer.AddressType;
import in.nmaloth.entity.customer.CustomerDef;
import in.nmaloth.entity.customer.CustomerIDType;
import in.nmaloth.entity.customer.CustomerType;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.entity.product.ProductId;
import in.nmaloth.maintenance.config.data.ProductTable;
import in.nmaloth.maintenance.controllers.EndPoints;
import in.nmaloth.maintenance.model.combined.AccountsCombinedDTO;
import in.nmaloth.maintenance.model.dto.account.*;
import in.nmaloth.maintenance.repository.account.AccountAccumValuesRepository;
import in.nmaloth.maintenance.repository.account.AccountBasicRepository;
import in.nmaloth.maintenance.repository.customer.CustomerRepository;
import in.nmaloth.maintenance.repository.product.ProductDefRepository;
import in.nmaloth.maintenance.service.account.AccountAccumValuesService;
import in.nmaloth.maintenance.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext
@AutoConfigureWebTestClient
class AccountsControllerTest {

    @Autowired
    private WebTestClient webTestClient;


    @Autowired
    private AccountBasicRepository accountBasicRepository;

    @Autowired
    private AccountAccumValuesRepository accountAccumValuesRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductDefRepository productDefRepository;

    private CustomerDef customerDef;

    @Autowired
    private ProductTable productTable;


    @Autowired
    private AccountAccumValuesService accountAccumValuesService;


    @BeforeEach
    void setup(){
        setupProductTable();
        setupCustomer();

        accountBasicRepository.findAll()
                .forEach(accountBasic -> accountBasicRepository.delete(accountBasic));

        accountAccumValuesRepository.findAll()
                .forEach(accountAccumValues -> accountAccumValuesRepository.delete(accountAccumValues));

    }




    @Test
    void addNewAccountsRecord() {

        AccountBasicAddDTO accountBasicAddDTO = createAccountBasicAddDTO();
        accountBasicAddDTO.setCustomerNumber(customerDef.getCustomerId());

        webTestClient.post()
                .uri(EndPoints.ACCOUNTS)
                .body(Mono.just(accountBasicAddDTO),AccountBasicAddDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AccountsCombinedDTO.class)
                .value(accountsCombinedDTO -> {

                    AccountAccumValues accountAccumValues =accountAccumValuesRepository
                            .findById(accountsCombinedDTO.getAccountAccumValuesDTO().getAccountId()).get();

                    Optional<AccountBasic> accountBasicOptional = accountBasicRepository.findById(accountsCombinedDTO.getAccountBasicDTO().getAccountId());

                    int accountAccumValuesBalanceSize = accountAccumValues.getBalancesMap().size();


                    assertAll(
                            ()-> assertEquals(5,accountAccumValuesBalanceSize),
                            ()-> assertTrue(accountBasicOptional.isPresent())
                    );

                })

                ;


    }

    @Test
    void addNewAccountsRecord1() {

        AccountBasicAddDTO accountBasicAddDTO = createAccountBasicAddDTO();
        accountBasicAddDTO.setCustomerNumber(customerDef.getCustomerId());

        accountBasicAddDTO.setOrg(999);

        webTestClient.post()
                .uri(EndPoints.ACCOUNTS)
                .body(Mono.just(accountBasicAddDTO),AccountBasicAddDTO.class)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
        ;
    }

    @Test
    void addNewAccountsRecord2() {

        AccountBasicAddDTO accountBasicAddDTO = createAccountBasicAddDTO();

        webTestClient.post()
                .uri(EndPoints.ACCOUNTS)
                .body(Mono.just(accountBasicAddDTO),AccountBasicAddDTO.class)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
        ;
    }

    @Test
    void addNewAccountsRecord3() {

        AccountBasicAddDTO accountBasicAddDTO = createAccountBasicAddDTO();
        accountBasicAddDTO.setCustomerNumber(customerDef.getCustomerId());
        accountBasicAddDTO.setAccountId(null);

        webTestClient.post()
                .uri(EndPoints.ACCOUNTS)
                .body(Mono.just(accountBasicAddDTO),AccountBasicAddDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AccountsCombinedDTO.class)
                .value(accountsCombinedDTO -> {

                    AccountAccumValues accountAccumValues =accountAccumValuesRepository
                            .findById(accountsCombinedDTO.getAccountAccumValuesDTO().getAccountId()).get();

                    Optional<AccountBasic> accountBasicOptional = accountBasicRepository.findById(accountsCombinedDTO.getAccountBasicDTO().getAccountId());

                    int accountAccumValuesBalanceSize = accountAccumValues.getBalancesMap().size();


                    assertAll(
                            ()-> assertEquals(5,accountAccumValuesBalanceSize),
                            ()-> assertTrue(accountBasicOptional.isPresent())
                    );

                })
        ;
    }


    @Test
    void getAccounts() {

        AccountBasic accountBasic = createAccountBasic();
        accountBasic.setCustomerNumber(customerDef.getCustomerId());
        accountBasicRepository.save(accountBasic);

        String uri = EndPoints.ACCOUNTS_ACCOUNT_NBR.replace("{accountNumber}",accountBasic.getAccountId());
        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountBasicDTO.class)
                ;
    }

    @Test
    void getAccounts1() {

        AccountBasic accountBasic = createAccountBasic();
        accountBasic.setCustomerNumber(customerDef.getCustomerId());
//        accountBasicRepository.save(accountBasic);

        String uri = EndPoints.ACCOUNTS_ACCOUNT_NBR.replace("{accountNumber}",accountBasic.getAccountId());
        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
        ;
    }

    @Test
    void deleteAccounts() {

        AccountBasic accountBasic = createAccountBasic();
        accountBasic.setCustomerNumber(customerDef.getCustomerId());
        accountBasicRepository.save(accountBasic);

        String uri = EndPoints.ACCOUNTS_ACCOUNT_NBR.replace("{accountNumber}",accountBasic.getAccountId());
        webTestClient.delete()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountBasicDTO.class)
                .value(accountBasicDTO -> {

                    Optional<AccountBasic> accountBasicOptional = accountBasicRepository.findById(accountBasicDTO.getAccountId());

                    assertTrue(accountBasicOptional.isEmpty());

                })
        ;

    }

    @Test
    void deleteAccounts1() {

        AccountBasic accountBasic = createAccountBasic();
        accountBasic.setCustomerNumber(customerDef.getCustomerId());
        accountBasicRepository.save(accountBasic);

        String uri = EndPoints.ACCOUNTS_ACCOUNT_NBR.replace("{accountNumber}","12345");
        webTestClient.delete()
                .uri(uri)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
        ;

    }

    @Test
    void updateAccounts() {

        AccountBasic accountBasic = createAccountBasic();
        accountBasic.setCustomerNumber(customerDef.getCustomerId());
        accountBasicRepository.save(accountBasic);

        AccountAccumValues accountAccumValues = createAccountAccum(accountBasic.getAccountId());
        accountAccumValuesRepository.save(accountAccumValues);

        AccountBasicUpdateDTO accountBasicUpdateDTO = createAccountUpdateDTO(true,accountBasic.getAccountId(),null);

        webTestClient
                .put()
                .uri(EndPoints.ACCOUNTS)
                .body(Mono.just(accountBasicUpdateDTO),AccountBasicUpdateDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountsCombinedDTO.class)
                .value(accountsCombinedDTO -> {

                    AccountBasic accountBasic1 = accountBasicRepository.findById(accountBasic.getAccountId()).get();
                    AccountAccumValues accountAccumValues1 = accountAccumValuesRepository.findById(accountBasic.getAccountId()).get();

                    Long creditLimitValue = 100000L;
                    Long cashLimitValue = 60000L;
                    Long internationalCashLimitValue = 30000L;
                    Long internationalLimitValue = 70000L;
                    Long installmentLimitValue = 80000L;
                    Long installmentCashLimitValue  = 20000L;
                    Long internationalInstallmentLimitValue = 500000L;




                    Long creditLimit = accountAccumValues1.getLimitsMap().get(BalanceTypes.CURRENT_BALANCE);
                    Long cashLimit = accountAccumValues1.getLimitsMap().get(BalanceTypes.CASH_BALANCE);
                    Long internationalCashLimit = accountAccumValues1.getLimitsMap().get(BalanceTypes.INTERNATIONAL_CASH);
                    Long internationalLimit = accountAccumValues1.getLimitsMap().get(BalanceTypes.INTERNATIONAL);
                    Long installmentLimit = accountAccumValues1.getLimitsMap().get(BalanceTypes.INSTALLMENT_BALANCE);
                    Long installmentCashLimit  = accountAccumValues1.getLimitsMap().get(BalanceTypes.INSTALLMENT_CASH);
                    Long internationalInstallmentLimit = accountAccumValues1.getLimitsMap().get(BalanceTypes.INTERNATIONAL_INSTALLMENT);


                    assertAll(
                            ()-> assertEquals(accountBasicUpdateDTO.getAccountId(),accountBasic1.getAccountId()),
                            ()-> assertEquals(Util.getBlockType(accountBasicUpdateDTO.getBlockType()),accountBasic1.getBlockType()),
                            ()-> assertEquals(accountBasicUpdateDTO.getBillingCurrencyCode(),accountBasic1.getBillingCurrencyCode()),
                            ()-> assertEquals(accountBasic.getBlockType(),accountBasic1.getPreviousBlockType()),
                            ()-> assertEquals(accountBasic.getDateBlockApplied(),accountBasic1.getDatePreviousBLockType()),
                            ()-> assertNotNull(accountBasic1.getDateBlockApplied()),
                            ()-> assertEquals(7,accountAccumValues1.getLimitsMap().size()),
                            ()-> assertNull(installmentCashLimit),
                            ()-> assertEquals(creditLimitValue,creditLimit),
                            ()-> assertEquals(internationalCashLimitValue,internationalCashLimit),
                            ()-> assertEquals(cashLimitValue,cashLimit),
                            ()-> assertEquals(internationalLimitValue,internationalLimit),
                            ()-> assertEquals(installmentLimitValue,installmentLimit),
                            ()-> assertEquals(internationalInstallmentLimitValue, internationalInstallmentLimit)
                    );


                })
                ;

    }

    @Test
    void updateAccounts1() {

        AccountBasic accountBasic = createAccountBasic();
        accountBasic.setCustomerNumber(customerDef.getCustomerId());
        accountBasicRepository.save(accountBasic);

        AccountAccumValues accountAccumValues = createAccountAccum(accountBasic.getAccountId());
        accountAccumValuesRepository.save(accountAccumValues);

        AccountBasicUpdateDTO accountBasicUpdateDTO = createAccountUpdateDTO(true,"123456789",null);

        webTestClient
                .put()
                .uri(EndPoints.ACCOUNTS)
                .body(Mono.just(accountBasicUpdateDTO),AccountBasicUpdateDTO.class)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)

        ;

    }

    @Test
    void fetchAccountLimits() {

        AccountBasic accountBasic = createAccountBasic();
        accountBasic.setCustomerNumber(customerDef.getCustomerId());
        accountBasicRepository.save(accountBasic);

        AccountAccumValues accountAccumValues = createAccountAccum(accountBasic.getAccountId());
        accountAccumValuesRepository.save(accountAccumValues);

        String url = EndPoints.ACCOUNTS_LIMITS_ACCOUNT_NBR.replace("{accountNumber}",accountAccumValues.getAccountId());

        webTestClient
                .get()
                .uri(url)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountAccumValuesDTO.class)

        ;


    }

    @Test
    void fetchAccountLimits1() {

        AccountBasic accountBasic = createAccountBasic();
        accountBasic.setCustomerNumber(customerDef.getCustomerId());
        accountBasicRepository.save(accountBasic);

        AccountAccumValues accountAccumValues = createAccountAccum(accountBasic.getAccountId());
//        accountAccumValuesRepository.save(accountAccumValues);

        String url = EndPoints.ACCOUNTS_LIMITS_ACCOUNT_NBR.replace("{accountNumber}",accountAccumValues.getAccountId());

        webTestClient
                .get()
                .uri(url)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)

        ;


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


    private AccountAccumValues createAccountAccum(String accountId){

        Map<BalanceTypes,Long> balanceTypesMap = new HashMap<>();

        balanceTypesMap.put(BalanceTypes.CURRENT_BALANCE,100000L);
        balanceTypesMap.put(BalanceTypes.CASH_BALANCE,50000L);
        balanceTypesMap.put(BalanceTypes.INTERNATIONAL_CASH,30000L);
        balanceTypesMap.put(BalanceTypes.INTERNATIONAL,70000L);
        balanceTypesMap.put(BalanceTypes.INSTALLMENT_BALANCE,80000L);
        balanceTypesMap.put(BalanceTypes.INSTALLMENT_CASH,20000L);
        balanceTypesMap.put(BalanceTypes.INTERNATIONAL_INSTALLMENT,10000L);


        AccountBalances accountBalance = AccountBalances.builder()
                .memoCr(0L)
                .memoDb(0L)
                .postedBalance(0L)
                .build();

        Map<BalanceTypes, AccountBalances> accountBalancesMap = new HashMap<>();

        accountBalancesMap.put(BalanceTypes.CURRENT_BALANCE,accountBalance);
        accountBalancesMap.put(BalanceTypes.CASH_BALANCE,accountBalance);
        accountBalancesMap.put(BalanceTypes.INTERNATIONAL_CASH,accountBalance);
        accountBalancesMap.put(BalanceTypes.INTERNATIONAL,accountBalance);
        accountBalancesMap.put(BalanceTypes.INSTALLMENT_BALANCE,accountBalance);
        accountBalancesMap.put(BalanceTypes.INSTALLMENT_CASH,accountBalance);
        accountBalancesMap.put(BalanceTypes.INTERNATIONAL_INSTALLMENT,accountBalance);



        return AccountAccumValues.builder()
                .org(1)
                .product(201)
                .accountId(accountId)
                .limitsMap(balanceTypesMap)
                .balancesMap(accountBalancesMap)
                .build();

    }


    private AccountBasic createAccountBasic(){




        return AccountBasic.builder()
                .org(001)
                .product(201)
                .accountId(UUID.randomUUID().toString().replace("-",""))
                .blockType(BlockType.BLOCK_DECLINE)
                .dateBlockApplied(LocalDateTime.now())
                .billingCurrencyCode("840")
                .datePreviousBLockType(LocalDateTime.of(2020,12,23,11,24,30))
                .previousBlockType(BlockType.BLOCK_SUSPECTED_FRAUD)
                .accountType(AccountType.CREDIT)
                .customerNumber(UUID.randomUUID().toString().replace("-",""))
                .corporateNumber(UUID.randomUUID().toString().replace("-",""))
                .previousAccountNumber(UUID.randomUUID().toString().replace("-",""))
                .dateTransfer(LocalDateTime.now())
                .build();

    }

    private AccountBasicAddDTO createAccountBasicAddDTO(){

        List<BalanceTypesDTO> balanceTypesDTOList = new ArrayList<>();
        balanceTypesDTOList.add(BalanceTypesDTO.builder()
                .balanceType(Util.getBalanceTypes(BalanceTypes.CURRENT_BALANCE))
                .limitAmount(500000L)
                .build()
        );

        balanceTypesDTOList.add(BalanceTypesDTO.builder()
                .balanceType(Util.getBalanceTypes(BalanceTypes.CASH_BALANCE))
                .limitAmount(400000L)
                .build()
        );

        return AccountBasicAddDTO.builder()
                .accountId(UUID.randomUUID().toString().replace("-",""))
                .org(001)
                .product(201)
                .billingCurrencyCode("840")
                .blockType(Util.getBlockType(BlockType.APPROVE))
                .balanceTypesDTOList(balanceTypesDTOList)
                .accountType(Util.getAccountType(AccountType.SAVINGS))
                .customerNumber(UUID.randomUUID().toString().replace("-",""))
                .build();

    }

    private ProductDef createProductDef(){

        Map<BalanceTypes,Long> percentMap = new HashMap<>();
        percentMap.put(BalanceTypes.CASH_BALANCE,200000L);
        percentMap.put(BalanceTypes.INTERNATIONAL_CASH,100000L);
        percentMap.put(BalanceTypes.INTERNATIONAL_CASH_INSTALLMENT,300000L);
        percentMap.put(BalanceTypes.INSTALLMENT_BALANCE,500000L);



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
                .billingCurrencyCode("840")
                .primaryAccountType(AccountType.CREDIT)
                .build();

    }

    private AccountBasicUpdateDTO createAccountUpdateDTO(boolean allfields , String accountNumber, List<Integer> fieldList){

        AccountBasicUpdateDTO.AccountBasicUpdateDTOBuilder builder = AccountBasicUpdateDTO.builder()
                .accountId(accountNumber);

        List<BalanceTypesDTO> balanceTypesDTOListAdd = new ArrayList<>();
        List<BalanceTypesDTO> balanceTypesDTOListDelete = new ArrayList<>();

        balanceTypesDTOListAdd.add(BalanceTypesDTO.builder()
                .balanceType(Util.getBalanceTypes(BalanceTypes.INTERNATIONAL_INSTALLMENT))
                .limitAmount(500000L)
                .build()
        );
        balanceTypesDTOListAdd.add(BalanceTypesDTO.builder()
                .balanceType(Util.getBalanceTypes(BalanceTypes.INTERNATIONAL_CASH_INSTALLMENT))
                .limitAmount(450000L)
                .build()
        );

        balanceTypesDTOListAdd.add(BalanceTypesDTO.builder()
                .balanceType(Util.getBalanceTypes(BalanceTypes.CASH_BALANCE))
                .limitAmount(60000L)
                .build()
        );

        balanceTypesDTOListDelete.add(BalanceTypesDTO.builder()
                .balanceType(Util.getBalanceTypes(BalanceTypes.INSTALLMENT_CASH))
                .build()
        );




        if(allfields){
            return builder
                    .billingCurrencyCode("484")
                    .blockType(Util.getBlockType(BlockType.BLOCK_FRAUD))
                    .balanceTypesDTOListAdd(balanceTypesDTOListAdd)
                    .balanceTypesDTOListDelete(balanceTypesDTOListDelete)
                    .corporateNumber(UUID.randomUUID().toString().replace("-",""))
                    .customerNumber(UUID.randomUUID().toString().replace("-",""))
                    .accountType(Util.getAccountType(AccountType.CURRENT))
                    .build();
        }

        fieldList.forEach(integer -> populateBuilder(builder,integer,balanceTypesDTOListAdd,balanceTypesDTOListDelete));

        return builder.build();
    }

    private void populateBuilder(AccountBasicUpdateDTO.AccountBasicUpdateDTOBuilder builder, Integer integer,
                                 List<BalanceTypesDTO> balanceTypesDTOListAdd, List<BalanceTypesDTO> balanceTypesDTOListDelete) {

        switch (integer){
            case 1: {
                builder.billingCurrencyCode("124");
                break;
            }
            case 2: {
                builder.blockType(Util.getBlockType(BlockType.BLOCK_TEMP));
                break;
            }
            case 3: {
                builder.balanceTypesDTOListAdd(balanceTypesDTOListAdd);
                break;
            }
            case 4: {
                builder.balanceTypesDTOListDelete(balanceTypesDTOListDelete);
                break;
            }
            case 6: {
                balanceTypesDTOListAdd.add(BalanceTypesDTO.builder()
                        .balanceType(Util.getBalanceTypes(BalanceTypes.CURRENT_BALANCE))
                        .limitAmount(200000L)
                        .build()
                );
                builder.balanceTypesDTOListAdd(balanceTypesDTOListAdd);
            }
            case 7: {
                balanceTypesDTOListDelete.add(BalanceTypesDTO.builder()
                        .balanceType(Util.getBalanceTypes(BalanceTypes.CURRENT_BALANCE))
                        .limitAmount(0L)
                        .build()
                );
                builder.balanceTypesDTOListDelete(balanceTypesDTOListDelete);
            }
            case 8: {
                builder.accountType(Util.getAccountType(AccountType.UNIVERSAL));
            }
            case 9 : {
                builder.customerNumber(UUID.randomUUID().toString().replace("-",""));
            }
            case 10: {
                builder.corporateNumber(UUID.randomUUID().toString().replace("-",""));
            }
        }
    }

    private CustomerDef createCustomerDef(boolean allFields){

        Map<CustomerIDType,String> customerIDMap = new HashMap<>();
        customerIDMap.put(CustomerIDType.SSN_OR_NATIONAL_ID,"12345678");
        customerIDMap.put(CustomerIDType.DRIVERS_LICENCE,"ABCDEFGHIJ");


        CustomerDef.CustomerDefBuilder builder = CustomerDef.builder()
                .customerId(UUID.randomUUID().toString().replace("-",""))
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

}