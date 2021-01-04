package in.nmaloth.maintenance.dataService.account;

import in.nmaloth.entity.account.AccountAccumValues;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface AccountAccumValuesDataService {

    Mono<AccountAccumValues> saveAccountAccumValues(AccountAccumValues accountAccumValues);
    Mono<Optional<AccountAccumValues>> findAccountAccumValuesById(String id);
}
