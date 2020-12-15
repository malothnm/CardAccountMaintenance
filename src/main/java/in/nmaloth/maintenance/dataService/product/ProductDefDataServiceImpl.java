package in.nmaloth.maintenance.dataService.product;

import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.entity.product.ProductId;
import in.nmaloth.maintenance.repository.product.ProductDefRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class ProductDefDataServiceImpl implements ProductDefDataService {

    private final ProductDefRepository productDefRepository;

    public ProductDefDataServiceImpl(ProductDefRepository productDefRepository) {
        this.productDefRepository = productDefRepository;
    }


    @Override
    public Mono<Optional<ProductDef>> fetchProductDef(ProductId productId) {

        CompletableFuture<Optional<ProductDef>> completableFuture = CompletableFuture
                .supplyAsync(() -> productDefRepository.findById(productId));
        return Mono.fromFuture(completableFuture);
    }

    @Override
    public Mono<ProductDef> saveProductDef(ProductDef productDef) {

        CompletableFuture<ProductDef> completableFuture = CompletableFuture
                .supplyAsync(() -> productDefRepository.save(productDef));
        return Mono.fromFuture(completableFuture);
    }

    @Override
    public Mono<Optional<ProductDef>> deleteProductDef(ProductId productId) {
        return fetchProductDef(productId)
                .doOnNext(productDef -> {
                    if(productDef.isPresent()){
                        productDefRepository.delete(productDef.get());
                    }
                })
                ;
    }

    @Override
    public Flux<ProductDef> fetchAllProducts() {

        CompletableFuture<Iterable<ProductDef>> completableFuture = CompletableFuture
                .supplyAsync(()-> productDefRepository.findAll());
        return Mono.fromFuture(completableFuture)
                .flatMapMany(productDefs -> Flux.fromIterable(productDefs))
                ;
    }
}
