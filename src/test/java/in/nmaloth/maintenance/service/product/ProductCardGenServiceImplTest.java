package in.nmaloth.maintenance.service.product;

import in.nmaloth.entity.product.ProductCardGenDef;
import in.nmaloth.entity.product.ProductId;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.product.ProductCardGenDTO;
import in.nmaloth.maintenance.model.dto.product.ProductCardGenUpdateDTO;
import in.nmaloth.maintenance.repository.product.ProductCardGenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductCardGenServiceImplTest {

    @Autowired
    private ProductCardGenService productCardGenService;

    @Autowired
    private ProductCardGenRepository productCardGenRepository;

    @BeforeEach
    void setup(){

        productCardGenRepository.findAll()
                .forEach(productCardGenDef -> productCardGenRepository.delete(productCardGenDef));

    }

    @Test
    void createNewProductCardGenDef() {

        ProductCardGenDTO productCardGenDTO = createProductCardGenDTO();

        productCardGenService.createNewProductCardGenDef(productCardGenDTO).block();

        ProductCardGenDef productCardGenDef = productCardGenRepository
                .findById(new ProductId(productCardGenDTO.getOrg(),productCardGenDTO.getProduct())).get();

        assertAll(
                ()-> assertEquals(productCardGenDTO.getOrg(),productCardGenDef.getProductId().getOrg()),
                ()-> assertEquals(productCardGenDTO.getProduct(),productCardGenDef.getProductId().getProduct()),
                ()-> assertEquals(productCardGenDTO.getEndingGeneratedCardNumber(),productCardGenDef.getEndingGeneratedCardNumber()),
                ()-> assertEquals(productCardGenDTO.getLastGeneratedCardNumber(),productCardGenDef.getLastGeneratedCardNumber()),
                ()-> assertEquals(productCardGenDTO.getNumberIncrementBy(),productCardGenDef.getNumberIncrementBy()),
                ()-> assertEquals(productCardGenDTO.getStartingCardNumber(),productCardGenDef.getStartingCardNumber())
        );

    }

    @Test
    void updateProductCardGenDef() {

        ProductCardGenDef productCardGenDef = createProductCardGenDef();
        productCardGenRepository.save(productCardGenDef);
        ProductCardGenUpdateDTO productCardGenUpdateDTO = createProductGenUpdate(true,null);
        productCardGenUpdateDTO.setOrg(productCardGenDef.getProductId().getOrg());
        productCardGenUpdateDTO.setProduct(productCardGenDef.getProductId().getProduct());

        productCardGenService.updateProductCardGenDef(productCardGenUpdateDTO).block();

        ProductCardGenDef productCardGenDef1 = productCardGenRepository.findById(productCardGenDef.getProductId()).get();

        assertAll(
                ()-> assertEquals(productCardGenUpdateDTO.getEndingGeneratedCardNumber(),productCardGenDef1.getEndingGeneratedCardNumber()),
                ()-> assertEquals(productCardGenUpdateDTO.getLastGeneratedCardNumber(),productCardGenDef1.getLastGeneratedCardNumber()),
                ()-> assertEquals(productCardGenUpdateDTO.getNumberIncrementBy(),productCardGenDef1.getNumberIncrementBy()),
                ()-> assertEquals(productCardGenUpdateDTO.getStartingCardNumber(),productCardGenDef1.getStartingCardNumber())
        );
    }

    @Test
    void fetchProductCardGenInfo() {

        ProductCardGenDef productCardGenDef = createProductCardGenDef();
        productCardGenRepository.save(productCardGenDef);

        Mono<ProductCardGenDef> productCardGenDefMono = productCardGenService
                .fetchProductCardGenInfo(productCardGenDef.getProductId().getOrg(),productCardGenDef.getProductId().getProduct());

        StepVerifier.create(productCardGenDefMono)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void fetchProductCardGenInfo1() {

        ProductCardGenDef productCardGenDef = createProductCardGenDef();
        productCardGenRepository.save(productCardGenDef);

        Mono<ProductCardGenDef> productCardGenDefMono = productCardGenService
                .fetchProductCardGenInfo(999,999);

        StepVerifier.create(productCardGenDefMono)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void deleteProductCardGen() {


        ProductCardGenDef productCardGenDef = createProductCardGenDef();
        productCardGenRepository.save(productCardGenDef);

        productCardGenService
                .deleteProductCardGen(productCardGenDef.getProductId().getOrg(),productCardGenDef.getProductId().getProduct()).block();

        Optional<ProductCardGenDef>  optionalProductCardGenDef = productCardGenRepository
                .findById(new ProductId(productCardGenDef.getProductId().getOrg(),productCardGenDef.getProductId().getProduct()));

        assertTrue(optionalProductCardGenDef.isEmpty());
    }

    @Test
    void deleteProductCardGenInfo1() {

        ProductCardGenDef productCardGenDef = createProductCardGenDef();
        productCardGenRepository.save(productCardGenDef);

        Mono<ProductCardGenDef> productCardGenDefMono = productCardGenService
                .deleteProductCardGen(999,999);

        StepVerifier.create(productCardGenDefMono)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void findAllProductCardGen() {

        ProductCardGenDef productCardGenDef = createProductCardGenDef();
        productCardGenRepository.save(productCardGenDef);
        productCardGenDef.getProductId().setOrg(2);
        productCardGenDef.getProductId().setProduct(3);
        productCardGenRepository.save(productCardGenDef);

        productCardGenDef.getProductId().setOrg(2);
        productCardGenDef.getProductId().setProduct(4);
        productCardGenRepository.save(productCardGenDef);

        Flux<ProductCardGenDef> productCardGenDefFlux = productCardGenService.findAllProductCardGen();

        StepVerifier
                .create(productCardGenDefFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void convertDTOToProductCardGen() {

        ProductCardGenDTO productCardGenDTO = createProductCardGenDTO();
        ProductCardGenDef productCardGenDef = productCardGenService.convertDTOToProductCardGen(productCardGenDTO);

        assertAll(
                ()-> assertEquals(productCardGenDTO.getOrg(),productCardGenDef.getProductId().getOrg()),
                ()-> assertEquals(productCardGenDTO.getProduct(),productCardGenDef.getProductId().getProduct()),
                ()-> assertEquals(productCardGenDTO.getEndingGeneratedCardNumber(),productCardGenDef.getEndingGeneratedCardNumber()),
                ()-> assertEquals(productCardGenDTO.getLastGeneratedCardNumber(),productCardGenDef.getLastGeneratedCardNumber()),
                ()-> assertEquals(productCardGenDTO.getNumberIncrementBy(),productCardGenDef.getNumberIncrementBy()),
                ()-> assertEquals(productCardGenDTO.getStartingCardNumber(),productCardGenDef.getStartingCardNumber())
        );

    }

    @Test
    void convertToDTO() {

        ProductCardGenDef productCardGenDef = createProductCardGenDef();
        ProductCardGenDTO productCardGenDTO = productCardGenService.convertToDTO(productCardGenDef);

        assertAll(
                ()-> assertEquals(productCardGenDTO.getOrg(),productCardGenDef.getProductId().getOrg()),
                ()-> assertEquals(productCardGenDTO.getProduct(),productCardGenDef.getProductId().getProduct()),
                ()-> assertEquals(productCardGenDTO.getEndingGeneratedCardNumber(),productCardGenDef.getEndingGeneratedCardNumber()),
                ()-> assertEquals(productCardGenDTO.getLastGeneratedCardNumber(),productCardGenDef.getLastGeneratedCardNumber()),
                ()-> assertEquals(productCardGenDTO.getNumberIncrementBy(),productCardGenDef.getNumberIncrementBy()),
                ()-> assertEquals(productCardGenDTO.getStartingCardNumber(),productCardGenDef.getStartingCardNumber())
        );

    }

    @Test
    void updateProductCardGen() {

        ProductCardGenDef productCardGenDef = createProductCardGenDef();
        ProductCardGenUpdateDTO productCardGenUpdateDTO = createProductGenUpdate(true,null);
        productCardGenService.updateProductCardGen(productCardGenUpdateDTO,productCardGenDef);

        assertAll(
                ()-> assertEquals(productCardGenUpdateDTO.getEndingGeneratedCardNumber(),productCardGenDef.getEndingGeneratedCardNumber()),
                ()-> assertEquals(productCardGenUpdateDTO.getLastGeneratedCardNumber(),productCardGenDef.getLastGeneratedCardNumber()),
                ()-> assertEquals(productCardGenUpdateDTO.getNumberIncrementBy(),productCardGenDef.getNumberIncrementBy()),
                ()-> assertEquals(productCardGenUpdateDTO.getStartingCardNumber(),productCardGenDef.getStartingCardNumber())
        );

    }

    @Test
    void updateProductCardGen1() {

        ProductCardGenDef productCardGenDef = createProductCardGenDef();
        String prevEndingCardNumber = productCardGenDef.getEndingGeneratedCardNumber();
        String lastGeneratedCardNumber = productCardGenDef.getLastGeneratedCardNumber();

        List<Integer> integerList = Arrays.asList(1,2);
        ProductCardGenUpdateDTO productCardGenUpdateDTO = createProductGenUpdate(false,integerList);
        productCardGenService.updateProductCardGen(productCardGenUpdateDTO,productCardGenDef);

        assertAll(
                ()-> assertEquals(prevEndingCardNumber,productCardGenDef.getEndingGeneratedCardNumber()),
                ()-> assertEquals(lastGeneratedCardNumber,productCardGenDef.getLastGeneratedCardNumber()),
                ()-> assertEquals(productCardGenUpdateDTO.getNumberIncrementBy(),productCardGenDef.getNumberIncrementBy()),
                ()-> assertEquals(productCardGenUpdateDTO.getStartingCardNumber(),productCardGenDef.getStartingCardNumber())
        );

    }

    @Test
    void updateProductCardGen2() {

        ProductCardGenDef productCardGenDef = createProductCardGenDef();
        Integer numberIncrementBy = productCardGenDef.getNumberIncrementBy();
        String startingCardNumber = productCardGenDef.getStartingCardNumber();

        List<Integer> integerList = Arrays.asList(3,4);
        ProductCardGenUpdateDTO productCardGenUpdateDTO = createProductGenUpdate(false,integerList);
        productCardGenService.updateProductCardGen(productCardGenUpdateDTO,productCardGenDef);

        assertAll(
                ()-> assertEquals(productCardGenUpdateDTO.getEndingGeneratedCardNumber(),productCardGenDef.getEndingGeneratedCardNumber()),
                ()-> assertEquals(productCardGenDef.getLastGeneratedCardNumber(),productCardGenDef.getLastGeneratedCardNumber()),
                ()-> assertEquals(numberIncrementBy,productCardGenDef.getNumberIncrementBy()),
                ()-> assertEquals(startingCardNumber,productCardGenDef.getStartingCardNumber())
        );

    }


    private ProductCardGenDef createProductCardGenDef(){

        return ProductCardGenDef.builder()
                .productId(new ProductId(1,201))
                .startingCardNumber("5500000000000004")
                .numberIncrementBy(1)
                .lastGeneratedCardNumber("5500000000000005")
                .endingGeneratedCardNumber("5500000099999999")
                .build();
    }

    private ProductCardGenDTO createProductCardGenDTO(){

        return ProductCardGenDTO.builder()
                .org(2)
                .product(202)
                .startingCardNumber("4111111111111111")
                .numberIncrementBy(2)
                .lastGeneratedCardNumber("411111111111141")
                .endingGeneratedCardNumber("4111111199999999")
                .build();
    }

    private ProductCardGenUpdateDTO createProductGenUpdate(boolean allFields, List<Integer> fieldList){

        ProductCardGenUpdateDTO.ProductCardGenUpdateDTOBuilder builder = ProductCardGenUpdateDTO.builder()
                .org(1)
                .product(201);

        if(allFields){
           return builder
                    .startingCardNumber("4111111111111111")
                    .numberIncrementBy(2)
                    .lastGeneratedCardNumber("411111111111141")
                    .endingGeneratedCardNumber("4111111199999999")
                    .build();
        }

        fieldList.forEach(integer -> updateBuilder(builder,integer));

        return builder.build();

    }

    private void updateBuilder(ProductCardGenUpdateDTO.ProductCardGenUpdateDTOBuilder builder, Integer integer) {

        switch (integer){
            case 1: {
                builder.startingCardNumber("4111111111111111");
                break;
            }
            case 2: {
                builder.numberIncrementBy(2);
                break;
            }
            case 3: {
                builder.lastGeneratedCardNumber("411111111111141");
                break;
            }
            case 4: {
                builder.endingGeneratedCardNumber("4111111199999999");
                break;
            }
        }
    }


}