package in.nmaloth.maintenance.service.product;

import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.maintenance.model.dto.product.DeclineReasonDefDTO;
import in.nmaloth.maintenance.model.dto.product.DeclineReasonUpdateDefDTO;
import in.nmaloth.maintenance.model.dto.product.ProductDefDTO;
import in.nmaloth.maintenance.model.dto.product.ProductDefUpdateDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface ProductServices {

    ProductDef convertDtoToProduct(ProductDefDTO productDefDTO);
    ProductDefDTO convertProductToDto(ProductDef productDef);
    ProductDef updateProductDefFromDto(ProductDefUpdateDTO productDefDTO, ProductDef productDef);


    Mono<ProductDefDTO> createNewProductDef(ProductDefDTO productDefDTO);
    Mono<ProductDefDTO> updateProductDef(ProductDefUpdateDTO productDefUpdateDTO);
    Mono<ProductDefDTO> fetchProductInfo(Integer org, Integer product);
    Mono<Optional<ProductDef>> fetchProductOptional(Integer org, Integer product);
    Mono<ProductDefDTO> deleteProduct(Integer org, Integer product);
    Flux<ProductDefDTO> findAllProducts();



}
