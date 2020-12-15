package in.nmaloth.maintenance.dataService.product;

import in.nmaloth.entity.product.DeclineReasonDef;
import in.nmaloth.maintenance.repository.product.DeclineReasonDefRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class DeclineReasonDataServiceImpl implements DeclineReasonDataService {

    private final DeclineReasonDefRepository declineReasonDefRepository;

    public DeclineReasonDataServiceImpl(DeclineReasonDefRepository declineReasonDefRepository) {
        this.declineReasonDefRepository = declineReasonDefRepository;
    }

    @Override
    public Mono<Optional<DeclineReasonDef>> fetchDeclineReason(String serviceName) {

        CompletableFuture<Optional<DeclineReasonDef>> completableFuture = CompletableFuture
                .supplyAsync(() -> declineReasonDefRepository.findById(serviceName));

        return Mono.fromFuture(completableFuture);
    }

    @Override
    public Mono<DeclineReasonDef> saveDeclineReason(DeclineReasonDef declineReasonDef) {

        CompletableFuture completableFuture = CompletableFuture
                .supplyAsync(()-> declineReasonDefRepository.save(declineReasonDef));

        return Mono.fromFuture(completableFuture);
    }

    @Override
    public Mono<Optional<DeclineReasonDef>> deleteDeclineReasonDef(String serviceName) {

        return fetchDeclineReason(serviceName)
                .doOnNext(declineReasonDef -> {
                    if(declineReasonDef.isPresent()){
                        declineReasonDefRepository.delete(declineReasonDef.get());
                    }
                });
    }

    @Override
    public Flux<DeclineReasonDef> findAllDeclineReasonDef() {

        CompletableFuture<Iterable<DeclineReasonDef>> completableFuture = CompletableFuture
                .supplyAsync(()-> declineReasonDefRepository.findAll());

        return Mono.fromFuture(completableFuture)
                .flatMapMany(declineReasonDefs -> Flux.fromIterable(declineReasonDefs))
                ;
    }
}
