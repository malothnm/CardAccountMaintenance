package in.nmaloth.maintenance.controllers.product;

import in.nmaloth.maintenance.controllers.EndPoints;
import in.nmaloth.maintenance.exception.AlreadyPresentException;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.product.ProductCardGenDTO;
import in.nmaloth.maintenance.model.dto.product.ProductCardGenUpdateDTO;
import in.nmaloth.maintenance.model.dto.product.ProductLimitDefDTO;
import in.nmaloth.maintenance.model.dto.product.ProductLimitDefUpdateDTO;
import in.nmaloth.maintenance.service.product.ProductCardGenService;
import in.nmaloth.maintenance.service.product.ProductLimitService;
import in.nmaloth.maintenance.service.product.ProductServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
public class ProductCardGenController {

    private final ProductServices productServices;
    private final ProductCardGenService productCardGenService;

    public ProductCardGenController(ProductServices productServices, ProductCardGenService productCardGenService) {
        this.productServices = productServices;
        this.productCardGenService = productCardGenService;
    }

    @PostMapping(EndPoints.PRODUCT_CARD_GEN)
    public Mono<ResponseEntity<ProductCardGenDTO>> createNeWProductCardGen(@Valid @RequestBody ProductCardGenDTO productCardGenDTO){

        return productServices.fetchProductOptional(productCardGenDTO.getOrg(),productCardGenDTO.getProduct())
                .doOnNext(productDefOptional -> {
                    if(productDefOptional.isEmpty()){
                        throw  new NotFoundException("product id not found in product def " );
                    }
                })
                .flatMap(productDefOptional -> productCardGenService.fetchProductCardGenOptional(productCardGenDTO.getOrg(),productCardGenDTO.getProduct()))
                .doOnNext(productCardGenDefOptional -> {
                    if(productCardGenDefOptional.isPresent()){
                        throw new AlreadyPresentException(" product id Already Exists. Cannot create");
                    }
                })
                .flatMap(productCardGenDefOptional -> productCardGenService.createNewProductCardGenDef(productCardGenDTO))
                .map(productCardGenDef -> productCardGenService.convertToDTO(productCardGenDef))
                .map(productCardGenDTO1 -> ResponseEntity.status(HttpStatus.CREATED)
                                            .body(productCardGenDTO1)
                );
    }
    
    @GetMapping(EndPoints.PRODUCT_CARD_GEN_ORG_PRODUCT)
    public Mono<ProductCardGenDTO> fetchProductCardGen(@PathVariable  Integer org , @PathVariable Integer product){
        
        return productCardGenService.fetchProductCardGenInfo(org,product)
                .map(productCardGenDef -> productCardGenService.convertToDTO(productCardGenDef))
                ;
    }
    
    @GetMapping(EndPoints.PRODUCT_CARD_GEN)
    public Flux<ProductCardGenDTO> fetchAllProductCardGen(){
        return productCardGenService.findAllProductCardGen()
                .map(productCardGenDef -> productCardGenService.convertToDTO(productCardGenDef))
                ;
    }

    @PutMapping(EndPoints.PRODUCT_CARD_GEN)
    public Mono<ProductCardGenDTO> updateProductCardGen(@Valid @RequestBody ProductCardGenUpdateDTO productCardGenUpdateDTO){
        return productCardGenService.updateProductCardGenDef(productCardGenUpdateDTO)
                .map(productCardGenDef -> productCardGenService.convertToDTO(productCardGenDef))
                ;
    }

    @DeleteMapping(EndPoints.PRODUCT_CARD_GEN_ORG_PRODUCT)
    public Mono<ProductCardGenDTO> deleteProductCardGen(@PathVariable  Integer org , @PathVariable Integer product){
        return productCardGenService.deleteProductCardGen(org, product)
                .map(productCardGenDef -> productCardGenService.convertToDTO(productCardGenDef))
                ;
    }
            
}
