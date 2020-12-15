package in.nmaloth.maintenance.service.account;

import in.nmaloth.entity.account.AccountAccumValues;
import in.nmaloth.entity.account.AccountBalances;
import in.nmaloth.entity.account.BalanceTypes;
import in.nmaloth.maintenance.dataService.account.AccountAccumValuesDataService;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.account.AccountAccumValuesDTO;
import in.nmaloth.maintenance.model.dto.account.AccountBalancesDTO;
import in.nmaloth.maintenance.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AccountAccumValuesServiceImpl implements AccountAccumValuesService {

    private final AccountAccumValuesDataService accountAccumValuesDataService;

    public AccountAccumValuesServiceImpl(AccountAccumValuesDataService accountAccumValuesDataService) {
        this.accountAccumValuesDataService = accountAccumValuesDataService;
    }


    @Override
    public Mono<AccountAccumValues> fetchAccountAccumValuesByAccountNumber(String accountNumber) {
        return accountAccumValuesDataService.findAccountAccumValuesByAcctNumber(accountNumber)
                .map(accountAccumValuesOptional -> {
                    if(accountAccumValuesOptional.isPresent()){
                        return accountAccumValuesOptional.get();
                    }
                    throw  new NotFoundException("Invalid Account Number " + accountNumber);
                })
                ;
    }

    @Override
    public Mono<AccountAccumValues> saveAccountAccumValues(AccountAccumValues accountAccumValues) {

        return accountAccumValuesDataService.saveAccountAccumValues(accountAccumValues);
    }

    @Override
    public AccountAccumValuesDTO convertToDTO(AccountAccumValues accountAccumValues) {

        List<AccountBalancesDTO> accountBalancesDTOList = accountAccumValues.getBalancesMap().entrySet()
                .stream()
                .map(accountBalancesEntry ->  AccountBalancesDTO.builder()
                        .balanceType(Util.getBalanceTypes(accountBalancesEntry.getKey()))
                        .postedBalance(accountBalancesEntry.getValue().getPostedBalance())
                        .memoCr(accountBalancesEntry.getValue().getMemoCr())
                        .memoDb(accountBalancesEntry.getValue().getMemoDb())
                        .build()
                )
                .collect(Collectors.toList());


        return AccountAccumValuesDTO.builder()
                .accountId(accountAccumValues.getAccountId())
                .accountNumber(accountAccumValues.getAccountNumber())
                .accountBalancesDTOList(accountBalancesDTOList)
                .build()
                ;
    }

    @Override
    public AccountAccumValues initializeAccumValues(String accountNumber, Set<BalanceTypes> balanceTypesList) {

        Map<BalanceTypes, AccountBalances> accountBalancesMap = new HashMap<>();
        balanceTypesList.stream()
                .forEach(balanceTypes -> accountBalancesMap.put(balanceTypes,AccountBalances.builder()
                        .memoCr(0)
                        .memoDb(0)
                        .postedBalance(0)
                        .build())

                );

        return AccountAccumValues.builder()
                .accountId(UUID.randomUUID().toString().replace("-",""))
                .accountNumber(accountNumber)
                .balancesMap(accountBalancesMap)
                .build()
                ;

    }

    @Override
    public AccountAccumValues updateNewAccumValues(String accountNumber, Set<BalanceTypes> balanceTypesSet,
                                                   AccountAccumValues accountAccumValues) {

        Map<BalanceTypes,AccountBalances> accountBalancesMap = accountAccumValues.getBalancesMap();
        balanceTypesSet.stream()
                .filter(balanceTypes -> accountBalancesMap.get(balanceTypes)==null)
                .forEach(balanceTypes -> accountBalancesMap.put(balanceTypes,AccountBalances.builder()
                        .postedBalance(0L)
                        .memoDb(0L)
                        .memoCr(0L)
                        .build()
                ));
        return accountAccumValues;
    }


}
