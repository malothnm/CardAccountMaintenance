package in.nmaloth.maintenance.dataService.product;

import in.nmaloth.entity.product.ProductId;
import in.nmaloth.entity.product.ProductLimitsDef;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface ProductLimitsDataService {

    Mono<Optional<ProductLimitsDef>> fetchProductLimitsDef(ProductId productId);
    Mono<ProductLimitsDef> saveProductLimitsDef(ProductLimitsDef productLimitsDef);
    Mono<Optional<ProductLimitsDef>> deleteProductLimitsDef(ProductId productId);
    Flux<ProductLimitsDef> fetchAllProductLimits();
}
