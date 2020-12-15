package in.nmaloth.maintenance.dataService.account;

import in.nmaloth.entity.account.AccountBasic;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface AccountBasicDataService {

    Mono<AccountBasic> saveAccountBasic(AccountBasic accountBasic);
    Mono<Optional<AccountBasic>> findAccountBasic(String accountNumber);
    Mono<Optional<AccountBasic>> deleteAccountBasicByAcctNumber(String accountNumber);
}
