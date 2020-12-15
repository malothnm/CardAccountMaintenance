package in.nmaloth.maintenance.controllers.product;

import in.nmaloth.maintenance.controllers.EndPoints;
import in.nmaloth.maintenance.exception.AlreadyPresentException;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.product.ProductDefUpdateDTO;
import in.nmaloth.maintenance.model.dto.product.ProductLimitDefDTO;
import in.nmaloth.maintenance.model.dto.product.ProductLimitDefUpdateDTO;
import in.nmaloth.maintenance.service.product.ProductLimitService;
import in.nmaloth.maintenance.service.product.ProductServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
public class ProductLimitsController {

    private final ProductServices productServices;
    private final ProductLimitService productLimitService;

    public ProductLimitsController(ProductServices productServices, ProductLimitService productLimitService) {
        this.productServices = productServices;
        this.productLimitService = productLimitService;
    }

    @PostMapping(EndPoints.PRODUCT_LIMITS)
    public Mono<ResponseEntity<ProductLimitDefDTO>> createNeWProductLimit(@Valid @RequestBody ProductLimitDefDTO productLimitDefDTO){

        return productServices.fetchProductOptional(productLimitDefDTO.getOrg(),productLimitDefDTO.getProduct())
                .doOnNext(productDefOptional -> {
                    if(productDefOptional.isEmpty()){
                        throw  new NotFoundException("product id not found in product def " );
                    }
                })
                .flatMap(productDefOptional -> productLimitService.fetchProductOptionalInfo(productLimitDefDTO.getOrg(),productLimitDefDTO.getProduct()))
                .doOnNext(productLimitsDefOptional -> {
                    if(productLimitsDefOptional.isPresent()){
                        throw new AlreadyPresentException(" product id Already Exists. Cannot create");
                    }
                })
                .flatMap(productLimitsDefOptional -> productLimitService.createNewProductLimitsDef(productLimitDefDTO))
                .map(productLimitDefDTO1 -> ResponseEntity.status(HttpStatus.CREATED)
                                            .body(productLimitDefDTO1)
                );
    }
    
    @GetMapping(EndPoints.PRODUCT_LIMITS_ORG_PRODUCT)
    public Mono<ProductLimitDefDTO> fetchProductLimit(@PathVariable  Integer org , @PathVariable Integer product){
        
        return productLimitService.fetchProductInfo(org,product);
    }
    
    @GetMapping(EndPoints.PRODUCT_LIMITS)
    public Flux<ProductLimitDefDTO> fetchAllProductLimit(){
        return productLimitService.findAllProducts();
    }

    @PutMapping(EndPoints.PRODUCT_LIMITS)
    public Mono<ProductLimitDefDTO> updateProductLimit(@Valid @RequestBody ProductLimitDefUpdateDTO productLimitDefUpdateDTO){
        return productLimitService.updateProductDef(productLimitDefUpdateDTO);
    }

    @DeleteMapping(EndPoints.PRODUCT_LIMITS_ORG_PRODUCT)
    public Mono<ProductLimitDefDTO> deleteProductLimit(@PathVariable  Integer org , @PathVariable Integer product){
        return productLimitService.deleteProduct(org, product);
    }
            
}
