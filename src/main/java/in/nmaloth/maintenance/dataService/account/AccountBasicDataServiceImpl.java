package in.nmaloth.maintenance.dataService.account;

import in.nmaloth.entity.account.AccountBasic;
import in.nmaloth.maintenance.repository.account.AccountBasicRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class AccountBasicDataServiceImpl implements AccountBasicDataService {

    private final AccountBasicRepository accountBasicRepository;

    public AccountBasicDataServiceImpl(AccountBasicRepository accountBasicRepository) {
        this.accountBasicRepository = accountBasicRepository;
    }


    @Override
    public Mono<AccountBasic> saveAccountBasic(AccountBasic accountBasic) {

        CompletableFuture<AccountBasic> completableFuture = CompletableFuture
                .supplyAsync(()-> accountBasicRepository.save(accountBasic));

        return Mono.fromFuture(completableFuture);
    }

    @Override
    public Mono<Optional<AccountBasic>> findAccountBasic(String accountNumber) {
        CompletableFuture<Optional<AccountBasic>> completableFuture = CompletableFuture
                .supplyAsync(()-> accountBasicRepository.findById(accountNumber));

        return Mono.fromFuture(completableFuture);
    }

    @Override
    public Mono<Optional<AccountBasic>> deleteAccountBasicByAcctNumber(String accountNumber) {

        return findAccountBasic(accountNumber)
                .doOnNext(accountBasicOptional ->{
                    if(accountBasicOptional.isPresent()){
                        accountBasicRepository.delete(accountBasicOptional.get());
                    }
                } );
    }
}
