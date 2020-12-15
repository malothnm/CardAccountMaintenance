package in.nmaloth.maintenance.dataService.product;

import in.nmaloth.entity.product.ProductId;
import in.nmaloth.entity.product.ProductLimitsDef;
import in.nmaloth.maintenance.repository.product.ProductLimitsDefRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class ProductLimitsDataServiceImpl implements ProductLimitsDataService {

    private final ProductLimitsDefRepository productLimitsDefRepository;

    public ProductLimitsDataServiceImpl(ProductLimitsDefRepository productLimitsDefRepository) {
        this.productLimitsDefRepository = productLimitsDefRepository;
    }

    @Override
    public Mono<Optional<ProductLimitsDef>> fetchProductLimitsDef(ProductId productId) {

        CompletableFuture<Optional<ProductLimitsDef>> completableFuture = CompletableFuture
                .supplyAsync(() -> productLimitsDefRepository.findById(productId));
        return Mono.fromFuture(completableFuture);
    }

    @Override
    public Mono<ProductLimitsDef> saveProductLimitsDef(ProductLimitsDef productLimitsDef) {

        CompletableFuture<ProductLimitsDef> completableFuture = CompletableFuture
                .supplyAsync(()-> productLimitsDefRepository.save(productLimitsDef));
        return Mono.fromFuture(completableFuture);
    }

    @Override
    public Mono<Optional<ProductLimitsDef>> deleteProductLimitsDef(ProductId productId) {
        return fetchProductLimitsDef(productId)
                .doOnNext(productLimitsDefOptional -> {
                    if(productLimitsDefOptional.isPresent()){
                        productLimitsDefRepository.delete(productLimitsDefOptional.get());
                    }
                } )
                ;
    }

    @Override
    public Flux<ProductLimitsDef> fetchAllProductLimits() {

        CompletableFuture<Iterable<ProductLimitsDef>> completableFuture = CompletableFuture
                .supplyAsync(()-> productLimitsDefRepository.findAll());

        return Mono.fromFuture(completableFuture)
                .flatMapMany(productLimitsDefs -> Flux.fromIterable(productLimitsDefs))
                ;
    }
}
