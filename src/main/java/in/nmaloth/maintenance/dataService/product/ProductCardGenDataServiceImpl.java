package in.nmaloth.maintenance.dataService.product;

import in.nmaloth.entity.product.ProductCardGenDef;
import in.nmaloth.entity.product.ProductId;
import in.nmaloth.maintenance.repository.product.ProductCardGenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class ProductCardGenDataServiceImpl implements ProductCardGenDataService {

    private final ProductCardGenRepository productCardGenRepository;


    public ProductCardGenDataServiceImpl(ProductCardGenRepository productCardGenRepository) {
        this.productCardGenRepository = productCardGenRepository;
    }


    @Override
    public Mono<Optional<ProductCardGenDef>> fetchProductDef(ProductId productId) {

        CompletableFuture<Optional<ProductCardGenDef>> completableFuture = CompletableFuture
                .supplyAsync(() -> productCardGenRepository.findById(productId));

        return Mono.fromFuture(completableFuture);
    }

    @Override
    public Mono<ProductCardGenDef> saveProductDef(ProductCardGenDef productCardGenDef) {

        CompletableFuture<ProductCardGenDef> completableFuture = CompletableFuture
                .supplyAsync(() -> productCardGenRepository.save(productCardGenDef));
        return Mono.fromFuture(completableFuture);
    }

    @Override
    public Mono<Optional<ProductCardGenDef>> deleteProductDef(ProductId productId) {
        return fetchProductDef(productId)
                .doOnNext(productCardGenDef -> {
                    if(productCardGenDef.isPresent()){
                        productCardGenRepository.delete(productCardGenDef.get());
                    }
                })
                ;
    }

    @Override
    public Flux<ProductCardGenDef> fetchAllProducts() {

        CompletableFuture<Iterable<ProductCardGenDef>> completableFuture = CompletableFuture
                .supplyAsync(() -> productCardGenRepository.findAll());
        return Mono.fromFuture(completableFuture)
                .flatMapMany(productCardGenDefs -> Flux.fromIterable(productCardGenDefs))
                ;
    }
}
