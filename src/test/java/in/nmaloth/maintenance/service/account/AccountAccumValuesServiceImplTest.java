package in.nmaloth.maintenance.service.account;

import in.nmaloth.entity.BlockType;
import in.nmaloth.entity.account.AccountAccumValues;
import in.nmaloth.entity.account.AccountBalances;
import in.nmaloth.entity.account.AccountType;
import in.nmaloth.entity.account.BalanceTypes;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.entity.product.ProductId;
import in.nmaloth.maintenance.config.data.ProductTable;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.account.AccountAccumValuesDTO;
import in.nmaloth.maintenance.model.dto.account.AccountBasicAddDTO;
import in.nmaloth.maintenance.model.dto.account.AccountBasicUpdateDTO;
import in.nmaloth.maintenance.model.dto.account.BalanceTypesDTO;
import in.nmaloth.maintenance.repository.account.AccountAccumValuesRepository;
import in.nmaloth.maintenance.repository.product.ProductDefRepository;
import in.nmaloth.maintenance.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AccountAccumValuesServiceImplTest {

    @Autowired
    private AccountAccumValuesService accountAccumValuesService;

    @Autowired
    private AccountAccumValuesRepository accountAccumValuesRepository;

    @Autowired
    private ProductDefRepository productDefRepository;

    @Autowired
    private ProductTable productTable;



    @BeforeEach
    void setup(){


        productDefRepository.findAll()
                .forEach(productDef -> productDefRepository.delete(productDef));

        accountAccumValuesRepository.findAll()
                .forEach(accountAccumValues -> accountAccumValuesRepository.delete(accountAccumValues));

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
    void fetchAccountAccumValuesByAccountNumber() {

        String accountNumber = UUID.randomUUID().toString().replace("-", "");
        AccountAccumValues accountAccumValues = createAccountAccumValues(accountNumber);
        accountAccumValuesRepository.save(accountAccumValues);

        Mono<AccountAccumValues> accountAccumValuesMono = accountAccumValuesService.fetchAccountAccumValuesByAccountId(accountNumber);
        StepVerifier
                .create(accountAccumValuesMono)
                .expectNextCount(1)
                .verifyComplete();

    }

    @Test
    void fetchAccountAccumValuesByAccountNumber1() {

        String accountNumber = UUID.randomUUID().toString().replace("-", "");

        Mono<AccountAccumValues> accountAccumValuesMono = accountAccumValuesService.fetchAccountAccumValuesByAccountId(accountNumber);
        StepVerifier
                .create(accountAccumValuesMono)
                .expectError(NotFoundException.class)
                .verify();

    }

    @Test
    void initializeDb1(){

        AccountBasicAddDTO accountBasicAddDTO = createAccountBasicAddDTO();

        Mono<AccountAccumValues> accountAccumValuesMono = accountAccumValuesService.createNewAccumValues(accountBasicAddDTO.getAccountId(),accountBasicAddDTO.getBalanceTypesDTOList(),
                accountBasicAddDTO.getOrg(),accountBasicAddDTO.getProduct());


        StepVerifier.create(accountAccumValuesMono)
                .consumeNextWith(accountAccumValues -> {

                    Map<BalanceTypes,Long> balanceTypesMap = accountAccumValues.getLimitsMap();

                    Long creditLimit = balanceTypesMap.get(BalanceTypes.CURRENT_BALANCE);
                    Long cashLimit = balanceTypesMap.get(BalanceTypes.CASH_BALANCE);
                    Long internationalLimit = balanceTypesMap.get(BalanceTypes.INTERNATIONAL);
                    Long internationalCashLimit = balanceTypesMap.get(BalanceTypes.INTERNATIONAL_CASH);
                    Long installmentLimit = balanceTypesMap.get(BalanceTypes.INSTALLMENT_BALANCE);
                    Long internationalInstallmentLimit = balanceTypesMap.get(BalanceTypes.INTERNATIONAL_INSTALLMENT);
                    Long internationalCashInstallmentLimit = balanceTypesMap.get(BalanceTypes.INTERNATIONAL_CASH_INSTALLMENT);
                    Long installmentCashLimit = balanceTypesMap.get(BalanceTypes.INSTALLMENT_CASH);

                    assertAll(
                            ()-> assertEquals(accountBasicAddDTO.getAccountId(),accountAccumValues.getAccountId()),
                            ()-> assertNotNull(accountAccumValues.getAccountId()),
                            ()-> assertEquals(500000L, creditLimit),
                            ()-> assertEquals(400000L,cashLimit),
                            ()-> assertEquals(50000L,internationalCashLimit),
                            ()-> assertEquals(150000L,internationalCashInstallmentLimit),
                            ()-> assertEquals(250000L,installmentLimit),
                            ()-> assertNull(internationalLimit),
                            ()-> assertNull(installmentCashLimit),
                            ()-> assertNull(internationalInstallmentLimit),
                            ()-> assertEquals(5,accountAccumValues.getBalancesMap().size()),
                            ()-> assertEquals(0, accountAccumValues.getBalancesMap().get(BalanceTypes.CURRENT_BALANCE).getPostedBalance()),
                            ()-> assertEquals(0, accountAccumValues.getBalancesMap().get(BalanceTypes.CASH_BALANCE).getMemoCr()),
                            ()-> assertEquals(0, accountAccumValues.getBalancesMap().get(BalanceTypes.INSTALLMENT_BALANCE).getMemoDb()),
                            ()-> assertEquals(0, accountAccumValues.getBalancesMap().get(BalanceTypes.INTERNATIONAL_CASH).getMemoDb()),
                            ()-> assertEquals(0, accountAccumValues.getBalancesMap().get(BalanceTypes.INTERNATIONAL_CASH_INSTALLMENT).getPostedBalance()),
                            ()-> assertNull(accountAccumValues.getBalancesMap().get(BalanceTypes.INTERNATIONAL)),
                            ()-> assertNull(accountAccumValues.getBalancesMap().get(BalanceTypes.INSTALLMENT_CASH)),
                            ()-> assertNull(accountAccumValues.getBalancesMap().get(BalanceTypes.INTERNATIONAL_INSTALLMENT))
                    );
                });


    }

    @Test
    void initializeDb2(){

        AccountBasicAddDTO accountBasicAddDTO = createAccountBasicAddDTO();

        Mono<AccountAccumValues> accountAccumValuesMono = accountAccumValuesService.createNewAccumValues(accountBasicAddDTO.getAccountId(),accountBasicAddDTO.getBalanceTypesDTOList(),
                accountBasicAddDTO.getOrg(),accountBasicAddDTO.getProduct());

        accountAccumValuesMono.block();
        Optional<AccountAccumValues> optionalAccountAccumValues = accountAccumValuesRepository.findById(accountBasicAddDTO.getAccountId());

        assertTrue(optionalAccountAccumValues.isPresent());

    }


    @Test
    void saveAccountAccumValues() {

        String accountNumber = UUID.randomUUID().toString().replace("-", "");
        AccountAccumValues accountAccumValues = createAccountAccumValues(accountNumber);

        accountAccumValuesService.saveAccountAccumValues(accountAccumValues).block();

        AccountAccumValues accountAccumValues1 = accountAccumValuesRepository.findById(accountNumber).get();

        assertAll(
                ()-> assertEquals(accountAccumValues.getAccountId() , accountAccumValues1.getAccountId()),
                ()-> assertEquals(accountAccumValues.getAccountId(),accountAccumValues1.getAccountId()),
                ()-> assertEquals(accountAccumValues.getBalancesMap().size(),accountAccumValues1.getBalancesMap().size())

        );

    }

    @Test
    void convertToDTO() {

        AccountAccumValues accountAccumValues = createAccountAccumValues(UUID.randomUUID().toString().replace("-",""));
        AccountAccumValuesDTO accountAccumValuesDTO = accountAccumValuesService.convertToDTO(accountAccumValues);


        assertAll(
                ()-> assertEquals(accountAccumValues.getAccountId(),accountAccumValuesDTO.getAccountId()),
                ()-> assertEquals(accountAccumValues.getBalancesMap().size(),accountAccumValuesDTO.getAccountBalancesDTOList().size()),
                ()-> assertEquals(accountAccumValues.getLimitsMap().size(),accountAccumValuesDTO.getBalanceTypesDTOList().size())
        );
    }

    @Test
    void convertToDTO1() {

        AccountAccumValues accountAccumValues = createAccountAccumValues(UUID.randomUUID().toString().replace("-",""));
        AccountAccumValuesDTO accountAccumValuesDTO = accountAccumValuesService.convertToDTO(accountAccumValues);

        Map<BalanceTypes,AccountBalances> balanceTypesMap = new HashMap<>();
         accountAccumValuesDTO.getAccountBalancesDTOList()
                 .forEach(accountBalancesDTO -> balanceTypesMap.put(Util.getBalanceTypes(accountBalancesDTO.getBalanceType()),AccountBalances.builder()
                 .postedBalance(accountBalancesDTO.getPostedBalance())
                         .memoCr(accountBalancesDTO.getMemoCr())
                         .memoDb(accountBalancesDTO.getMemoDb())
                         .build()
                 ));

        Map<BalanceTypes,Long> limitTypesMap = new HashMap<>();

        accountAccumValuesDTO.getBalanceTypesDTOList()
                 .forEach(balanceTypesDTO -> limitTypesMap.put(Util.getBalanceTypes(balanceTypesDTO.getBalanceType()),balanceTypesDTO.getLimitAmount()));


        Long creditLimit = limitTypesMap.get(BalanceTypes.CURRENT_BALANCE);
        Long cashLimit = limitTypesMap.get(BalanceTypes.CASH_BALANCE);
        Long internationalCashLimit = limitTypesMap.get(BalanceTypes.INTERNATIONAL_CASH);
        Long internationalLimit =limitTypesMap.get(BalanceTypes.INTERNATIONAL);
        Long installmentLimit = limitTypesMap.get(BalanceTypes.INSTALLMENT_BALANCE);
        Long installmentCashLimit  = limitTypesMap.get(BalanceTypes.INSTALLMENT_CASH);
        Long internationalInstallmentLimit = limitTypesMap.get(BalanceTypes.INTERNATIONAL_INSTALLMENT);

         AccountBalances accountBalancesCreditLimit = balanceTypesMap.get(BalanceTypes.CURRENT_BALANCE);
        AccountBalances accountBalancesCashLimit = balanceTypesMap.get(BalanceTypes.CASH_BALANCE);
        AccountBalances accountBalancesInternational = balanceTypesMap.get(BalanceTypes.INTERNATIONAL);
        AccountBalances accountBalancesInstallment = balanceTypesMap.get(BalanceTypes.INSTALLMENT_BALANCE);


        assertAll(
                ()-> assertEquals(4000,accountBalancesCreditLimit.getPostedBalance()),
                ()-> assertEquals(400,accountBalancesCreditLimit.getMemoDb()),
                ()-> assertEquals(40,accountBalancesCreditLimit.getMemoCr()),
                ()-> assertEquals(3000,accountBalancesCashLimit.getPostedBalance()),
                ()-> assertEquals(300,accountBalancesCashLimit.getMemoDb()),
                ()-> assertEquals(30,accountBalancesCashLimit.getMemoCr()),
                ()-> assertEquals(2000,accountBalancesInternational.getPostedBalance()),
                ()-> assertEquals(200,accountBalancesInternational.getMemoDb()),
                ()-> assertEquals(20,accountBalancesInternational.getMemoCr()),
                ()-> assertEquals(1000,accountBalancesInstallment.getPostedBalance()),
                ()-> assertEquals(100,accountBalancesInstallment.getMemoDb()),
                ()-> assertEquals(10,accountBalancesInstallment.getMemoCr()),
                ()-> assertEquals(100000L,creditLimit),
                ()-> assertEquals(50000L,cashLimit),
                ()-> assertNull(internationalCashLimit),
                ()-> assertEquals(70000L,internationalLimit),
                ()-> assertEquals(80000L,installmentLimit),
                ()-> assertNull(installmentCashLimit),
                ()-> assertNull(internationalInstallmentLimit)
        );
    }

    @Test
    void initializeAccumValues() {


        AccountBasicAddDTO accountBasicAddDTO = createAccountBasicAddDTO();

        AccountAccumValues accountAccumValues = accountAccumValuesService.initializeAccumValues(accountBasicAddDTO.getAccountId(),accountBasicAddDTO.getBalanceTypesDTOList(),
                accountBasicAddDTO.getOrg(),accountBasicAddDTO.getProduct());

        Map<BalanceTypes,Long> balanceTypesMap = accountAccumValues.getLimitsMap();

        Long creditLimit = balanceTypesMap.get(BalanceTypes.CURRENT_BALANCE);
        Long cashLimit = balanceTypesMap.get(BalanceTypes.CASH_BALANCE);
        Long internationalLimit = balanceTypesMap.get(BalanceTypes.INTERNATIONAL);
        Long internationalCashLimit = balanceTypesMap.get(BalanceTypes.INTERNATIONAL_CASH);
        Long installmentLimit = balanceTypesMap.get(BalanceTypes.INSTALLMENT_BALANCE);
        Long internationalInstallmentLimit = balanceTypesMap.get(BalanceTypes.INTERNATIONAL_INSTALLMENT);
        Long internationalCashInstallmentLimit = balanceTypesMap.get(BalanceTypes.INTERNATIONAL_CASH_INSTALLMENT);
        Long installmentCashLimit = balanceTypesMap.get(BalanceTypes.INSTALLMENT_CASH);

        assertAll(
                ()-> assertEquals(accountBasicAddDTO.getAccountId(),accountAccumValues.getAccountId()),
                ()-> assertNotNull(accountAccumValues.getAccountId()),
                ()-> assertEquals(500000L, creditLimit),
                ()-> assertEquals(400000L,cashLimit),
                ()-> assertEquals(50000L,internationalCashLimit),
                ()-> assertEquals(150000L,internationalCashInstallmentLimit),
                ()-> assertEquals(250000L,installmentLimit),
                ()-> assertNull(internationalLimit),
                ()-> assertNull(installmentCashLimit),
                ()-> assertNull(internationalInstallmentLimit),
                ()-> assertEquals(5,accountAccumValues.getBalancesMap().size()),
                ()-> assertEquals(0, accountAccumValues.getBalancesMap().get(BalanceTypes.CURRENT_BALANCE).getPostedBalance()),
                ()-> assertEquals(0, accountAccumValues.getBalancesMap().get(BalanceTypes.CASH_BALANCE).getMemoCr()),
                ()-> assertEquals(0, accountAccumValues.getBalancesMap().get(BalanceTypes.INSTALLMENT_BALANCE).getMemoDb()),
                ()-> assertEquals(0, accountAccumValues.getBalancesMap().get(BalanceTypes.INTERNATIONAL_CASH).getMemoDb()),
                ()-> assertEquals(0, accountAccumValues.getBalancesMap().get(BalanceTypes.INTERNATIONAL_CASH_INSTALLMENT).getPostedBalance()),
                ()-> assertNull(accountAccumValues.getBalancesMap().get(BalanceTypes.INTERNATIONAL)),
                ()-> assertNull(accountAccumValues.getBalancesMap().get(BalanceTypes.INSTALLMENT_CASH)),
                ()-> assertNull(accountAccumValues.getBalancesMap().get(BalanceTypes.INTERNATIONAL_INSTALLMENT))

                );
    }

    @Test
    void updateAccumInitial1() {

        AccountAccumValues accountAccumValues = createAccountAccumValues(UUID.randomUUID().toString().replace("-",""));

        AccountBasicUpdateDTO accountBasicUpdateDTO = createAccountUpdateDTO(true,accountAccumValues.getAccountId(),null);
        AccountAccumValues accountAccumValues1 = accountAccumValuesService.updateAccumValues(accountBasicUpdateDTO.getBalanceTypesDTOListAdd(),
                accountBasicUpdateDTO.getBalanceTypesDTOListDelete(),accountAccumValues);


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



        AccountBalances accountBalancesCreditLimit = accountAccumValues1.getBalancesMap().get(BalanceTypes.CURRENT_BALANCE);
        AccountBalances accountBalancesCashLimit = accountAccumValues1.getBalancesMap().get(BalanceTypes.CASH_BALANCE);
        AccountBalances accountBalancesInternational = accountAccumValues1.getBalancesMap().get(BalanceTypes.INTERNATIONAL);
        AccountBalances accountBalancesInstallment = accountAccumValues1.getBalancesMap().get(BalanceTypes.INSTALLMENT_BALANCE);
        AccountBalances accountBalancesInternationalCash = accountAccumValues1.getBalancesMap().get(BalanceTypes.INTERNATIONAL_CASH);



        assertAll(
                ()-> assertEquals(6,accountAccumValues1.getBalancesMap().size()),
                ()-> assertEquals(4000,accountBalancesCreditLimit.getPostedBalance()),
                ()-> assertEquals(400,accountBalancesCreditLimit.getMemoDb()),
                ()-> assertEquals(40,accountBalancesCreditLimit.getMemoCr()),
                ()-> assertEquals(3000,accountBalancesCashLimit.getPostedBalance()),
                ()-> assertEquals(300,accountBalancesCashLimit.getMemoDb()),
                ()-> assertEquals(30,accountBalancesCashLimit.getMemoCr()),
                ()-> assertEquals(2000,accountBalancesInternational.getPostedBalance()),
                ()-> assertEquals(200,accountBalancesInternational.getMemoDb()),
                ()-> assertEquals(20,accountBalancesInternational.getMemoCr()),
                ()-> assertEquals(1000,accountBalancesInstallment.getPostedBalance()),
                ()-> assertEquals(100,accountBalancesInstallment.getMemoDb()),
                ()-> assertEquals(10,accountBalancesInstallment.getMemoCr()),
                ()-> assertNull(accountBalancesInternationalCash),
                ()-> assertEquals(5,accountAccumValues1.getLimitsMap().size()),
                ()-> assertNull(installmentCashLimit),
                ()-> assertEquals(creditLimitValue,creditLimit),
                ()-> assertNull(internationalCashLimit),
                ()-> assertEquals(cashLimitValue,cashLimit),
                ()-> assertNull(internationalLimit),
                ()-> assertEquals(installmentLimitValue,installmentLimit),
                ()-> assertEquals(internationalInstallmentLimitValue, internationalInstallmentLimit)
        );
    }


    @Test
    void updateAccumInitial2() {

        AccountAccumValues accountAccumValues = createAccountAccumValues(UUID.randomUUID().toString().replace("-",""));

        List<Integer> integerList = Arrays.asList(1,3);
        AccountBasicUpdateDTO accountBasicUpdateDTO = createAccountUpdateDTO(false,accountAccumValues.getAccountId(),integerList);
        AccountAccumValues accountAccumValues1 = accountAccumValuesService.updateAccumValues(accountBasicUpdateDTO.getBalanceTypesDTOListAdd(),
                accountBasicUpdateDTO.getBalanceTypesDTOListDelete(),accountAccumValues);


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

        AccountBalances accountBalancesCreditLimit = accountAccumValues1.getBalancesMap().get(BalanceTypes.CURRENT_BALANCE);
        AccountBalances accountBalancesCashLimit = accountAccumValues1.getBalancesMap().get(BalanceTypes.CASH_BALANCE);
        AccountBalances accountBalancesInternational = accountAccumValues1.getBalancesMap().get(BalanceTypes.INTERNATIONAL);
        AccountBalances accountBalancesInstallment = accountAccumValues1.getBalancesMap().get(BalanceTypes.INSTALLMENT_BALANCE);
        AccountBalances accountBalancesInternationalCash = accountAccumValues1.getBalancesMap().get(BalanceTypes.INTERNATIONAL_CASH);

        assertAll(
                ()-> assertEquals(6,accountAccumValues1.getBalancesMap().size()),
                ()-> assertEquals(4000,accountBalancesCreditLimit.getPostedBalance()),
                ()-> assertEquals(400,accountBalancesCreditLimit.getMemoDb()),
                ()-> assertEquals(40,accountBalancesCreditLimit.getMemoCr()),
                ()-> assertEquals(3000,accountBalancesCashLimit.getPostedBalance()),
                ()-> assertEquals(300,accountBalancesCashLimit.getMemoDb()),
                ()-> assertEquals(30,accountBalancesCashLimit.getMemoCr()),
                ()-> assertEquals(2000,accountBalancesInternational.getPostedBalance()),
                ()-> assertEquals(200,accountBalancesInternational.getMemoDb()),
                ()-> assertEquals(20,accountBalancesInternational.getMemoCr()),
                ()-> assertEquals(1000,accountBalancesInstallment.getPostedBalance()),
                ()-> assertEquals(100,accountBalancesInstallment.getMemoDb()),
                ()-> assertEquals(10,accountBalancesInstallment.getMemoCr()),
                ()-> assertEquals(6,accountAccumValues1.getLimitsMap().size()),
                ()-> assertNull(installmentCashLimit),
                ()-> assertEquals(creditLimitValue,creditLimit),
                ()-> assertEquals(cashLimitValue,cashLimit),
                ()-> assertEquals(internationalLimitValue,internationalLimit),
                ()-> assertEquals(installmentLimitValue,installmentLimit),
                ()-> assertEquals(internationalInstallmentLimitValue, internationalInstallmentLimit),
                ()-> assertNull(installmentCashLimit),
                ()-> assertEquals(creditLimitValue,creditLimit),
                ()-> assertNull(internationalCashLimit),
                ()-> assertEquals(cashLimitValue,cashLimit),
                ()-> assertEquals(internationalLimitValue,internationalLimit),
                ()-> assertEquals(installmentLimitValue,installmentLimit),
                ()-> assertEquals(internationalInstallmentLimitValue, internationalInstallmentLimit)
        );
    }

    @Test
    void updateAccumInitial3() {

        AccountAccumValues accountAccumValues = createAccountAccumValues(UUID.randomUUID().toString().replace("-",""));

        List<Integer> integerList = Arrays.asList(2,4,10);
        AccountBasicUpdateDTO accountBasicUpdateDTO = createAccountUpdateDTO(false,accountAccumValues.getAccountId(),integerList);
        AccountAccumValues accountAccumValues1 = accountAccumValuesService.updateAccumValues(accountBasicUpdateDTO.getBalanceTypesDTOListAdd(),
                accountBasicUpdateDTO.getBalanceTypesDTOListDelete(),accountAccumValues);


        Long creditLimitValue = 100000L;
        Long cashLimitValue = 50000L;
        Long internationalCashLimitValue = 30000L;
        Long internationalLimitValue = 70000L;
        Long installmentLimitValue = 80000L;
        Long installmentCashLimitValue  = 20000L;
        Long internationalInstallmentLimitValue = 10000L;

        Long creditLimit = accountAccumValues1.getLimitsMap().get(BalanceTypes.CURRENT_BALANCE);
        Long cashLimit = accountAccumValues1.getLimitsMap().get(BalanceTypes.CASH_BALANCE);
        Long internationalCashLimit = accountAccumValues1.getLimitsMap().get(BalanceTypes.INTERNATIONAL_CASH);
        Long internationalLimit = accountAccumValues1.getLimitsMap().get(BalanceTypes.INTERNATIONAL);
        Long installmentLimit = accountAccumValues1.getLimitsMap().get(BalanceTypes.INSTALLMENT_BALANCE);
        Long installmentCashLimit  = accountAccumValues1.getLimitsMap().get(BalanceTypes.INSTALLMENT_CASH);
        Long internationalInstallmentLimit = accountAccumValues1.getLimitsMap().get(BalanceTypes.INTERNATIONAL_INSTALLMENT);

        AccountBalances accountBalancesCreditLimit = accountAccumValues1.getBalancesMap().get(BalanceTypes.CURRENT_BALANCE);
        AccountBalances accountBalancesCashLimit = accountAccumValues1.getBalancesMap().get(BalanceTypes.CASH_BALANCE);
        AccountBalances accountBalancesInternational = accountAccumValues1.getBalancesMap().get(BalanceTypes.INTERNATIONAL);
        AccountBalances accountBalancesInstallment = accountAccumValues1.getBalancesMap().get(BalanceTypes.INSTALLMENT_BALANCE);
        AccountBalances accountBalancesInternationalCash = accountAccumValues1.getBalancesMap().get(BalanceTypes.INTERNATIONAL_CASH);

        assertAll(
                ()-> assertEquals(4,accountAccumValues1.getBalancesMap().size()),
                ()-> assertEquals(4000,accountBalancesCreditLimit.getPostedBalance()),
                ()-> assertEquals(400,accountBalancesCreditLimit.getMemoDb()),
                ()-> assertEquals(40,accountBalancesCreditLimit.getMemoCr()),
                ()-> assertEquals(3000,accountBalancesCashLimit.getPostedBalance()),
                ()-> assertEquals(300,accountBalancesCashLimit.getMemoDb()),
                ()-> assertEquals(30,accountBalancesCashLimit.getMemoCr()),
                ()-> assertEquals(2000,accountBalancesInternational.getPostedBalance()),
                ()-> assertEquals(200,accountBalancesInternational.getMemoDb()),
                ()-> assertEquals(20,accountBalancesInternational.getMemoCr()),
                ()-> assertEquals(1000,accountBalancesInstallment.getPostedBalance()),
                ()-> assertEquals(100,accountBalancesInstallment.getMemoDb()),
                ()-> assertEquals(10,accountBalancesInstallment.getMemoCr()),
                ()-> assertEquals(3,accountAccumValues.getLimitsMap().size()),
                ()-> assertNull(installmentCashLimit),
                ()-> assertEquals(creditLimitValue,creditLimit),
                ()-> assertNull(internationalCashLimit),
                ()-> assertEquals(cashLimitValue,cashLimit),
                ()-> assertNull(internationalLimit),
                ()-> assertEquals(installmentLimitValue,installmentLimit),
                ()-> assertNull(internationalInstallmentLimit)

        );
    }




    private AccountAccumValues createAccountAccumValues(String accountNumber){

        Map<BalanceTypes,Long> balanceLimitMap = new HashMap<>();

        balanceLimitMap.put(BalanceTypes.CURRENT_BALANCE,100000L);
        balanceLimitMap.put(BalanceTypes.CASH_BALANCE,50000L);
        balanceLimitMap.put(BalanceTypes.INTERNATIONAL,70000L);
        balanceLimitMap.put(BalanceTypes.INSTALLMENT_BALANCE,80000L);


        Map<BalanceTypes, AccountBalances> accountBalancesMap = new HashMap<>();

        AccountBalances accountBalances1 = AccountBalances.builder()
                .postedBalance(1000)
                .memoDb(100)
                .memoCr(10)
                .build()
                ;

        AccountBalances accountBalances2 = AccountBalances.builder()
                .postedBalance(2000)
                .memoDb(200)
                .memoCr(20)
                .build()
                ;
        AccountBalances accountBalances3 = AccountBalances.builder()
                .postedBalance(3000)
                .memoDb(300)
                .memoCr(30)
                .build()
                ;
        AccountBalances accountBalances4 = AccountBalances.builder()
                .postedBalance(4000)
                .memoDb(400)
                .memoCr(40)
                .build()
                ;

        accountBalancesMap.put(BalanceTypes.CURRENT_BALANCE,accountBalances4);
        accountBalancesMap.put(BalanceTypes.CASH_BALANCE,accountBalances3);
        accountBalancesMap.put(BalanceTypes.INTERNATIONAL,accountBalances2);
        accountBalancesMap.put(BalanceTypes.INSTALLMENT_BALANCE,accountBalances1);


        return AccountAccumValues.builder()
                .accountId(UUID.randomUUID().toString().replace("-",""))
                .accountId(accountNumber)
                .balancesMap(accountBalancesMap)
                .limitsMap(balanceLimitMap)
                .org(1)
                .product(201)
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
        balanceTypesDTOListDelete.add(BalanceTypesDTO.builder()
                .balanceType(Util.getBalanceTypes(BalanceTypes.INTERNATIONAL))
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
                .build();

    }
}