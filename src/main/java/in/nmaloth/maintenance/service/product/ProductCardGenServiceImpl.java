package in.nmaloth.maintenance.service.product;

import in.nmaloth.entity.product.ProductCardGenDef;
import in.nmaloth.entity.product.ProductId;
import in.nmaloth.maintenance.dataService.product.ProductCardGenDataService;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.product.ProductCardGenDTO;
import in.nmaloth.maintenance.model.dto.product.ProductCardGenUpdateDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@Slf4j
public class ProductCardGenServiceImpl implements ProductCardGenService {

    private final ProductCardGenDataService productCardGenDataService;

    public ProductCardGenServiceImpl(ProductCardGenDataService productCardGenDataService) {
        this.productCardGenDataService = productCardGenDataService;
    }


    @Override
    public Mono<ProductCardGenDef> createNewProductCardGenDef(ProductCardGenDTO productCardGenDTO) {


        return productCardGenDataService.saveProductDef(convertDTOToProductCardGen(productCardGenDTO));
    }

    @Override
    public Mono<ProductCardGenDef> updateProductCardGenDef(ProductCardGenUpdateDTO productCardGenUpdateDTO) {
        return productCardGenDataService.fetchProductDef(new ProductId(productCardGenUpdateDTO.getOrg(),productCardGenUpdateDTO.getProduct()))
                .map(productCardGenDefOptional ->{
                    if(productCardGenDefOptional.isPresent()){
                        return productCardGenDefOptional.get();
                    }
                    throw new NotFoundException("Invalid Org " + productCardGenUpdateDTO.getOrg() + " product " + productCardGenUpdateDTO.getProduct());
                } )
                .map(productCardGenDef -> updateProductCardGen(productCardGenUpdateDTO,productCardGenDef))
                .flatMap(productCardGenDef -> productCardGenDataService.saveProductDef(productCardGenDef))
                ;
    }

    @Override
    public Mono<ProductCardGenDef> fetchProductCardGenInfo(Integer org, Integer product) {

        return productCardGenDataService.fetchProductDef(new ProductId(org,product))
                .map(productCardGenDefOptional -> {
                    if(productCardGenDefOptional.isPresent()){
                        return productCardGenDefOptional.get();
                    }
                    throw  new NotFoundException(" Invalid Org " + org + " product " + product);
                })
                ;
    }

    @Override
    public Mono<Optional<ProductCardGenDef>> fetchProductCardGenOptional(Integer org, Integer product) {
        return productCardGenDataService.fetchProductDef(new ProductId(org,product));
    }

    @Override
    public Mono<ProductCardGenDef> deleteProductCardGen(Integer org, Integer product) {
        return productCardGenDataService.deleteProductDef(new ProductId(org,product))
                .map(productCardGenDefOptional -> {
                    if(productCardGenDefOptional.isPresent()){
                        return productCardGenDefOptional.get();
                    }
                    throw  new NotFoundException(" Invalid Org " + org + " product " + product);
                })
                ;
    }

    @Override
    public Flux<ProductCardGenDef> findAllProductCardGen() {
        return productCardGenDataService.fetchAllProducts();
    }

    @Override
    public ProductCardGenDef convertDTOToProductCardGen(ProductCardGenDTO productCardGenDTO) {

        return ProductCardGenDef.builder()
                .endingGeneratedCardNumber(productCardGenDTO.getEndingGeneratedCardNumber())
                .lastGeneratedCardNumber(productCardGenDTO.getLastGeneratedCardNumber())
                .numberIncrementBy(productCardGenDTO.getNumberIncrementBy())
                .startingCardNumber(productCardGenDTO.getStartingCardNumber())
                .productId(new ProductId(productCardGenDTO.getOrg(),productCardGenDTO.getProduct()))
                .build();
    }

    @Override
    public ProductCardGenDTO convertToDTO(ProductCardGenDef productCardGenDef) {

        return ProductCardGenDTO.builder()
                .endingGeneratedCardNumber(productCardGenDef.getEndingGeneratedCardNumber())
                .lastGeneratedCardNumber(productCardGenDef.getLastGeneratedCardNumber())
                .numberIncrementBy(productCardGenDef.getNumberIncrementBy())
                .startingCardNumber(productCardGenDef.getStartingCardNumber())
                .org(productCardGenDef.getProductId().getOrg())
                .product(productCardGenDef.getProductId().getProduct())
                .build()

                ;
    }

    @Override
    public ProductCardGenDef updateProductCardGen(ProductCardGenUpdateDTO productCardGenUpdateDTO, ProductCardGenDef productCardGenDef) {

        if(productCardGenUpdateDTO.getEndingGeneratedCardNumber() != null){
            productCardGenDef.setEndingGeneratedCardNumber(productCardGenUpdateDTO.getEndingGeneratedCardNumber());
        }
        if(productCardGenUpdateDTO.getLastGeneratedCardNumber() != null){
            productCardGenDef.setLastGeneratedCardNumber(productCardGenUpdateDTO.getLastGeneratedCardNumber());
        }
        if(productCardGenUpdateDTO.getNumberIncrementBy() != null){
            productCardGenDef.setNumberIncrementBy(productCardGenUpdateDTO.getNumberIncrementBy());
        }
        if(productCardGenUpdateDTO.getStartingCardNumber() != null){
            productCardGenDef.setStartingCardNumber(productCardGenUpdateDTO.getStartingCardNumber());
        }

        return productCardGenDef;
    }
}
