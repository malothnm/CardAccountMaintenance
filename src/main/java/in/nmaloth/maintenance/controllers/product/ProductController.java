package in.nmaloth.maintenance.controllers.product;

import in.nmaloth.maintenance.config.data.ProductTable;
import in.nmaloth.maintenance.controllers.EndPoints;
import in.nmaloth.maintenance.exception.AlreadyPresentException;
import in.nmaloth.maintenance.model.Test;
import in.nmaloth.maintenance.model.dto.product.ProductDefDTO;
import in.nmaloth.maintenance.model.dto.product.ProductDefUpdateDTO;
import in.nmaloth.maintenance.service.product.ProductServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@Slf4j
public class ProductController {

    private final ProductServices productServices;
    private final ProductTable productTable;

    public ProductController(ProductServices productServices, ProductTable productTable) {
        this.productServices = productServices;
        this.productTable = productTable;
    }


    @PostMapping("/test")
    public Mono<String> testMessage(@RequestBody Test test){
        log.info(test.getTestMessage());

        return Mono.just("Test Successful");
    }


    @PostMapping(value = EndPoints.PRODUCTS,consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ProductDefDTO>> createNewProduct(@Valid @RequestBody ProductDefDTO productDefDTO){


        return productServices.fetchProductOptional(productDefDTO.getOrg(),productDefDTO.getProduct())
                .doOnNext(productDefOptional -> {
                    if(productDefOptional.isPresent()){
                        throw  new AlreadyPresentException(" Cannot Add Existing Product");
                    }
                })
                .onErrorMap(throwable -> throwable)
                .flatMap(productDefOptional -> productServices.createNewProductDef(productDefDTO))
                .doOnNext(productDefDTO1 -> productTable.loadMap(productServices.convertDtoToProduct(productDefDTO1)))
                .map(productDefDTO1 -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(productDefDTO1))

                ;
    }

    @GetMapping(EndPoints.PRODUCTS)
    public Flux<ProductDefDTO> fetchAllProducts(){
        return productServices.findAllProducts();
    }

    @GetMapping(EndPoints.PRODUCTS_ORG_PRODUCT)
    public Mono<ProductDefDTO> fetchExistingProduct(@PathVariable Integer org,@PathVariable Integer product){
        return productServices.fetchProductInfo(org, product);
    }

    @DeleteMapping(EndPoints.PRODUCTS_ORG_PRODUCT)
    public Mono<ProductDefDTO> deleteExitingProduct(@PathVariable Integer org,@PathVariable Integer product){
        return productServices.deleteProduct(org,product);
    }

    @PutMapping(EndPoints.PRODUCTS)
    public Mono<ProductDefDTO> updateExistingProduct(@Valid @RequestBody ProductDefUpdateDTO productDefUpdateDTO){
        return productServices.updateProductDef(productDefUpdateDTO)
                .doOnNext(productDefDTO1 -> productTable.loadMap(productServices.convertDtoToProduct(productDefDTO1)))
                ;
    }


}
