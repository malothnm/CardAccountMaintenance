package in.nmaloth.maintenance.service.account;

import in.nmaloth.entity.account.AccountAccumValues;
import in.nmaloth.entity.account.BalanceTypes;
import in.nmaloth.maintenance.model.dto.account.AccountAccumValuesDTO;
import in.nmaloth.maintenance.model.dto.account.BalanceTypesDTO;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public interface AccountAccumValuesService {



    Mono<AccountAccumValues> fetchAccountAccumValuesByAccountId(String accountId);
    Mono<AccountAccumValues> saveAccountAccumValues(AccountAccumValues accountAccumValues);

    Mono<AccountAccumValues> createNewAccumValues(String accountId, List<BalanceTypesDTO> balanceTypesDTOList,
                                                  int org, int product);

    Mono<AccountAccumValues> updateAccumValues(List<BalanceTypesDTO> balanceTypesDTOListAdd,
                                               List<BalanceTypesDTO> balanceTypesDTOListDelete,String accountId);

    AccountAccumValuesDTO convertToDTO(AccountAccumValues accountAccumValues);
    AccountAccumValues initializeAccumValues(String accountId, List<BalanceTypesDTO> balanceTypesDTOList,int org, int product);
    AccountAccumValues updateAccumValues(List<BalanceTypesDTO> balanceTypesDTOListAdd,
            List<BalanceTypesDTO> balanceTypesDTOListDelete,AccountAccumValues accountAccumValues);




}
