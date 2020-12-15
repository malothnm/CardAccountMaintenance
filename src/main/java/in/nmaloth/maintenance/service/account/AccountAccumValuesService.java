package in.nmaloth.maintenance.service.account;

import in.nmaloth.entity.account.AccountAccumValues;
import in.nmaloth.entity.account.BalanceTypes;
import in.nmaloth.maintenance.model.dto.account.AccountAccumValuesDTO;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public interface AccountAccumValuesService {



    Mono<AccountAccumValues> fetchAccountAccumValuesByAccountNumber(String accountNumber);
    Mono<AccountAccumValues> saveAccountAccumValues(AccountAccumValues accountAccumValues);

    AccountAccumValuesDTO convertToDTO(AccountAccumValues accountAccumValues);
    AccountAccumValues initializeAccumValues(String accountNumber, Set<BalanceTypes> balanceTypesList);
    AccountAccumValues updateNewAccumValues(String accountNumber, Set<BalanceTypes> balanceTypesList,AccountAccumValues accountAccumValues);




}
