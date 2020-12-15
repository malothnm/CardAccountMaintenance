package in.nmaloth.maintenance.dataService.account;

import in.nmaloth.entity.account.AccountAccumValues;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface AccountAccumValuesDataService {

    Mono<AccountAccumValues> saveAccountAccumValues(AccountAccumValues accountAccumValues);
    Mono<Optional<AccountAccumValues>> findAccountAccumValuesById(String id);
    Mono<Optional<AccountAccumValues>> findAccountAccumValuesByAcctNumber(String acctNumber);
    Mono<Optional<AccountAccumValues>> deleteAccountAccumValuesByAcctNumber(String acctNumber);
}
