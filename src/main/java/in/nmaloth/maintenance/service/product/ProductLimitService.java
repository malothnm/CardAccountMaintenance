package in.nmaloth.maintenance.service.product;

import in.nmaloth.entity.product.ProductLimitsDef;
import in.nmaloth.maintenance.model.dto.product.ProductLimitDefDTO;
import in.nmaloth.maintenance.model.dto.product.ProductLimitDefUpdateDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface ProductLimitService {

    Mono<ProductLimitDefDTO> createNewProductLimitsDef(ProductLimitDefDTO productLimitDefDTO);
    Mono<ProductLimitDefDTO> updateProductDef(ProductLimitDefUpdateDTO productLimitDefUpdateDTO);
    Mono<ProductLimitDefDTO> fetchProductInfo(Integer org, Integer product);
    Mono<Optional<ProductLimitsDef>> fetchProductOptionalInfo(Integer org, Integer product);
    Mono<ProductLimitDefDTO> deleteProduct(Integer org, Integer product);
    Flux<ProductLimitDefDTO> findAllProducts();


    ProductLimitsDef convertDTOToProductLimitsDef(ProductLimitDefDTO productLimitDefDTO);
    ProductLimitDefDTO convertToDTO(ProductLimitsDef productLimitsDef);
    ProductLimitsDef updateProductLimitDef(ProductLimitDefUpdateDTO productLimitDefUpdateDTO, ProductLimitsDef productLimitsDef);



}
