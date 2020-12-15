package in.nmaloth.maintenance.dataService.product;

import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.entity.product.ProductId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface ProductDefDataService {

    Mono<Optional<ProductDef>> fetchProductDef(ProductId productId);
    Mono<ProductDef> saveProductDef(ProductDef productDef);
    Mono<Optional<ProductDef>> deleteProductDef(ProductId productId);
    Flux<ProductDef> fetchAllProducts();

}
