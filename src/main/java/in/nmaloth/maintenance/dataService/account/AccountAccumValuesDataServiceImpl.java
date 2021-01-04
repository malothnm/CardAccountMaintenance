package in.nmaloth.maintenance.dataService.account;

import in.nmaloth.entity.account.AccountAccumValues;
import in.nmaloth.maintenance.repository.account.AccountAccumValuesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class AccountAccumValuesDataServiceImpl implements AccountAccumValuesDataService {

    private final AccountAccumValuesRepository accountAccumValuesRepository;


    public AccountAccumValuesDataServiceImpl(AccountAccumValuesRepository accountAccumValuesRepository) {
        this.accountAccumValuesRepository = accountAccumValuesRepository;
    }


    @Override
    public Mono<AccountAccumValues> saveAccountAccumValues(AccountAccumValues accountAccumValues) {

        CompletableFuture<AccountAccumValues> completableFuture = CompletableFuture
                .supplyAsync(()-> accountAccumValuesRepository.save(accountAccumValues));



        return Mono.fromFuture(completableFuture);
    }

    @Override
    public Mono<Optional<AccountAccumValues>> findAccountAccumValuesById(String id) {

        CompletableFuture<Optional<AccountAccumValues>> completableFuture = CompletableFuture
                .supplyAsync(()-> accountAccumValuesRepository.findById(id));

        return Mono.fromFuture(completableFuture);
    }


}
