package in.nmaloth.maintenance.service.product;

import in.nmaloth.entity.product.ProductCardGenDef;
import in.nmaloth.entity.product.ProductId;
import in.nmaloth.maintenance.model.dto.product.ProductCardGenDTO;
import in.nmaloth.maintenance.model.dto.product.ProductCardGenUpdateDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface ProductCardGenService {

    Mono<ProductCardGenDef> createNewProductCardGenDef(ProductCardGenDTO productCardGenDTO);
    Mono<ProductCardGenDef> updateProductCardGenDef(ProductCardGenUpdateDTO productCardGenUpdateDTO);
    Mono<ProductCardGenDef> fetchProductCardGenInfo(Integer org, Integer product);
    Mono<Optional<ProductCardGenDef>> fetchProductCardGenOptional(Integer org, Integer product);

    Mono<ProductCardGenDef> deleteProductCardGen(Integer org, Integer product);
    Flux<ProductCardGenDef> findAllProductCardGen();



    ProductCardGenDef convertDTOToProductCardGen(ProductCardGenDTO productCardGenDTO);
    ProductCardGenDTO convertToDTO(ProductCardGenDef productCardGenDef);
    ProductCardGenDef updateProductCardGen(ProductCardGenUpdateDTO productCardGenUpdateDTO, ProductCardGenDef productCardGenDef);
}
