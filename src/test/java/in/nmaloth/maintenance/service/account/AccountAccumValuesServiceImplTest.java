package in.nmaloth.maintenance.service.account;

import in.nmaloth.entity.account.AccountAccumValues;
import in.nmaloth.entity.account.AccountBalances;
import in.nmaloth.entity.account.BalanceTypes;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.account.AccountAccumValuesDTO;
import in.nmaloth.maintenance.repository.account.AccountAccumValuesRepository;
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
class AccountAccumValuesServiceImplTest {

    @Autowired
    private AccountAccumValuesService accountAccumValuesService;

    @Autowired
    private AccountAccumValuesRepository accountAccumValuesRepository;



    @BeforeEach
    void setup(){

        accountAccumValuesRepository.findAll()
                .forEach(accountAccumValues -> accountAccumValuesRepository.delete(accountAccumValues));
    }


    @Test
    void fetchAccountAccumValuesByAccountNumber() {

        String accountNumber = UUID.randomUUID().toString().replace("-", "");
        AccountAccumValues accountAccumValues = createAccountAccumValues(accountNumber);
        accountAccumValuesRepository.save(accountAccumValues);

        Mono<AccountAccumValues> accountAccumValuesMono = accountAccumValuesService.fetchAccountAccumValuesByAccountNumber(accountNumber);
        StepVerifier
                .create(accountAccumValuesMono)
                .expectNextCount(1)
                .verifyComplete();

    }

    @Test
    void fetchAccountAccumValuesByAccountNumber1() {

        String accountNumber = UUID.randomUUID().toString().replace("-", "");

        Mono<AccountAccumValues> accountAccumValuesMono = accountAccumValuesService.fetchAccountAccumValuesByAccountNumber(accountNumber);
        StepVerifier
                .create(accountAccumValuesMono)
                .expectError(NotFoundException.class)
                .verify();

    }

    @Test
    void saveAccountAccumValues() {

        String accountNumber = UUID.randomUUID().toString().replace("-", "");
        AccountAccumValues accountAccumValues = createAccountAccumValues(accountNumber);

        accountAccumValuesService.saveAccountAccumValues(accountAccumValues).block();

        AccountAccumValues accountAccumValues1 = accountAccumValuesRepository.findByAccountNumber(accountNumber).get();

        assertAll(
                ()-> assertEquals(accountAccumValues.getAccountId() , accountAccumValues1.getAccountId()),
                ()-> assertEquals(accountAccumValues.getAccountNumber(),accountAccumValues1.getAccountNumber()),
                ()-> assertEquals(accountAccumValues.getBalancesMap().size(),accountAccumValues1.getBalancesMap().size())

        );


    }

    @Test
    void convertToDTO() {

        AccountAccumValues accountAccumValues = createAccountAccumValues(UUID.randomUUID().toString().replace("-",""));
        AccountAccumValuesDTO accountAccumValuesDTO = accountAccumValuesService.convertToDTO(accountAccumValues);



        assertAll(
                ()-> assertEquals(accountAccumValues.getAccountId(),accountAccumValuesDTO.getAccountId()),
                ()-> assertEquals(accountAccumValues.getAccountNumber(),accountAccumValuesDTO.getAccountNumber()),
                ()-> assertEquals(accountAccumValues.getBalancesMap().size(),accountAccumValuesDTO.getAccountBalancesDTOList().size())
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
                ()-> assertEquals(10,accountBalancesInstallment.getMemoCr())
        );
    }

    @Test
    void initializeAccumValues() {

        Set<BalanceTypes> balanceTypesList = new HashSet<>();
        balanceTypesList.add(BalanceTypes.CURRENT_BALANCE);
        balanceTypesList.add(BalanceTypes.CASH_BALANCE);
        balanceTypesList.add(BalanceTypes.INTERNATIONAL);
        balanceTypesList.add(BalanceTypes.INSTALLMENT_BALANCE);



        String accountNumber = UUID.randomUUID().toString().replace("-","");
        AccountAccumValues accountAccumValues = accountAccumValuesService.initializeAccumValues(accountNumber,balanceTypesList);

        assertAll(
                ()-> assertEquals(accountNumber,accountAccumValues.getAccountNumber()),
                ()-> assertNotNull(accountAccumValues.getAccountId()),
                ()-> assertEquals(4,accountAccumValues.getBalancesMap().size()),
                ()-> assertEquals(0, accountAccumValues.getBalancesMap().get(BalanceTypes.CURRENT_BALANCE).getPostedBalance()),
                ()-> assertEquals(0, accountAccumValues.getBalancesMap().get(BalanceTypes.CASH_BALANCE).getMemoCr()),
                ()-> assertEquals(0, accountAccumValues.getBalancesMap().get(BalanceTypes.INSTALLMENT_BALANCE).getMemoDb())

        );
    }


    @Test
    void updateAccumInitial1() {

        AccountAccumValues accountAccumValues = createAccountAccumValues(UUID.randomUUID().toString().replace("-",""));

        Set<BalanceTypes> balanceTypesSet = new HashSet<>();
        balanceTypesSet.add(BalanceTypes.CURRENT_BALANCE);
        balanceTypesSet.add(BalanceTypes.INTERNATIONAL_CASH);

        AccountAccumValues accountAccumValues1 = accountAccumValuesService.updateNewAccumValues(accountAccumValues.getAccountNumber(),balanceTypesSet,accountAccumValues);





        AccountBalances accountBalancesCreditLimit = accountAccumValues1.getBalancesMap().get(BalanceTypes.CURRENT_BALANCE);
        AccountBalances accountBalancesCashLimit = accountAccumValues1.getBalancesMap().get(BalanceTypes.CASH_BALANCE);
        AccountBalances accountBalancesInternational = accountAccumValues1.getBalancesMap().get(BalanceTypes.INTERNATIONAL);
        AccountBalances accountBalancesInstallment = accountAccumValues1.getBalancesMap().get(BalanceTypes.INSTALLMENT_BALANCE);
        AccountBalances accountBalancesInternationalCash = accountAccumValues1.getBalancesMap().get(BalanceTypes.INTERNATIONAL_CASH);



        assertAll(
                ()-> assertEquals(5,accountAccumValues1.getBalancesMap().size()),
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
                ()-> assertEquals(0,accountBalancesInternationalCash.getPostedBalance()),
                ()-> assertEquals(0,accountBalancesInternationalCash.getMemoDb()),
                ()-> assertEquals(0,accountBalancesInternationalCash.getMemoCr())
        );
    }
    private AccountAccumValues createAccountAccumValues(String accountNumber){

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
                .accountNumber(accountNumber)
                .balancesMap(accountBalancesMap)
                .build();


    }
}