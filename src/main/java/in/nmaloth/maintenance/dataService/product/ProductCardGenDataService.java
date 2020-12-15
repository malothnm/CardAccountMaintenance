package in.nmaloth.maintenance.dataService.product;

import in.nmaloth.entity.product.ProductCardGenDef;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.entity.product.ProductId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface ProductCardGenDataService {

    Mono<Optional<ProductCardGenDef>> fetchProductDef(ProductId productId);
    Mono<ProductCardGenDef> saveProductDef(ProductCardGenDef productCardGenDef);
    Mono<Optional<ProductCardGenDef>> deleteProductDef(ProductId productId);
    Flux<ProductCardGenDef> fetchAllProducts();
}
