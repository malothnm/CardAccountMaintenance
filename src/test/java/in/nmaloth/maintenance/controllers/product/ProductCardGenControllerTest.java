package in.nmaloth.maintenance.controllers.product;

import in.nmaloth.entity.account.AccountType;
import in.nmaloth.entity.account.BalanceTypes;
import in.nmaloth.entity.product.ProductCardGenDef;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.entity.product.ProductId;
import in.nmaloth.maintenance.controllers.EndPoints;
import in.nmaloth.maintenance.model.dto.product.ProductCardGenDTO;
import in.nmaloth.maintenance.model.dto.product.ProductCardGenUpdateDTO;
import in.nmaloth.maintenance.repository.product.ProductCardGenRepository;
import in.nmaloth.maintenance.repository.product.ProductDefRepository;
import in.nmaloth.maintenance.repository.product.ProductLimitsDefRepository;
import in.nmaloth.maintenance.service.product.ProductCardGenService;
import in.nmaloth.maintenance.service.product.ProductLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@DirtiesContext
@AutoConfigureWebTestClient
class ProductCardGenControllerTest {

    @Autowired
    private ProductDefRepository productDefRepository;

    @Autowired
    private ProductCardGenRepository productCardGenRepository;

    @Autowired
    private ProductCardGenService productCardGenService;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setup(){

        productDefRepository.findAll()
                .forEach(productDef -> productDefRepository.delete(productDef));

        productCardGenRepository.findAll()
                .forEach(productCardGenDef -> productCardGenRepository.delete(productCardGenDef));
    }


    @Test
    void createNeWProductCardGen() {

        ProductDef productDef = createProductDef();
        productDefRepository.save(productDef);

        ProductCardGenDTO productCardGenDTO = createProductCardGenDTO();
        productCardGenDTO.setProduct(productDef.getProductId().getProduct());
        productCardGenDTO.setOrg(productDef.getProductId().getOrg());

        webTestClient.post()
                .uri(EndPoints.PRODUCT_CARD_GEN)
                .body(Mono.just(productCardGenDTO),ProductCardGenDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductCardGenDTO.class);
    }


    @Test
    void createNeWProductCardGen1() {

        ProductDef productDef = createProductDef();

        ProductCardGenDTO productCardGenDTO = createProductCardGenDTO();
        productCardGenDTO.setProduct(productDef.getProductId().getProduct());
        productCardGenDTO.setOrg(productDef.getProductId().getOrg());

        webTestClient.post()
                .uri(EndPoints.PRODUCT_CARD_GEN)
                .body(Mono.just(productCardGenDTO),ProductCardGenDTO.class)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class);
    }

    @Test
    void createNeWProductCardGen2() {

        ProductDef productDef = createProductDef();

        productDefRepository.save(productDef);

        ProductCardGenDef productCardGenDef = createProductCardGenDef();
        productCardGenDef.setProductId(productDef.getProductId());

        ProductCardGenDTO productCardGenDTO = createProductCardGenDTO();
        productCardGenDTO.setProduct(productDef.getProductId().getProduct());
        productCardGenDTO.setOrg(productDef.getProductId().getOrg());

        productCardGenRepository.save(productCardGenDef);

        webTestClient.post()
                .uri(EndPoints.PRODUCT_CARD_GEN)
                .body(Mono.just(productCardGenDTO),ProductCardGenDTO.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class);
    }

    @Test
    void fetchProductCardGen() {

        ProductDef productDef = createProductDef();
        productDefRepository.save(productDef);

        ProductCardGenDef productCardGenDef = createProductCardGenDef();
        productCardGenDef.setProductId(productDef.getProductId());
        productCardGenRepository.save(productCardGenDef);

        Integer org = productDef.getProductId().getOrg();
        Integer product = productDef.getProductId().getProduct();

        String uri = EndPoints.PRODUCTS_ORG_PRODUCT.replace("{org}",org.toString())
                .replace("{product}",product.toString());
        
        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductCardGenDTO.class)
        ;

    }

    @Test
    void fetchProductCardGen1() {

        ProductDef productDef = createProductDef();
        productDefRepository.save(productDef);

        ProductCardGenDef productCardGenDef = createProductCardGenDef();
        productCardGenDef.setProductId(productDef.getProductId());
        productCardGenRepository.save(productCardGenDef);

        Integer org = 999;
        Integer product = 999;

        String uri = EndPoints.PRODUCTS_ORG_PRODUCT.replace("{org}",org.toString())
                .replace("{product}",product.toString());

        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
        ;

    }


    @Test
    void fetchAllProductCardGen() {

        ProductCardGenDef productCardGenDef = createProductCardGenDef();
        productCardGenRepository.save(productCardGenDef);

        productCardGenDef.getProductId().setProduct(101);
        productCardGenRepository.save(productCardGenDef);

        productCardGenDef.getProductId().setProduct(501);
        productCardGenRepository.save(productCardGenDef);

        webTestClient.get()
                .uri(EndPoints.PRODUCT_CARD_GEN)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductCardGenDTO.class)
                .hasSize(3);
    }

    @Test
    void updateProductCardGen() {

        ProductDef productDef = createProductDef();
        productDefRepository.save(productDef);

        ProductCardGenDef productCardGenDef = createProductCardGenDef();
        productCardGenDef.setProductId(productDef.getProductId());
        productCardGenRepository.save(productCardGenDef);

        ProductCardGenUpdateDTO productCardGenUpdateDTO = createProductGenUpdate(true,null);

        productCardGenUpdateDTO.setProduct(productCardGenDef.getProductId().getProduct());
        productCardGenUpdateDTO.setOrg(productCardGenDef.getProductId().getOrg());

        webTestClient.put()
                .uri(EndPoints.PRODUCT_CARD_GEN)
                .body(Mono.just(productCardGenUpdateDTO),ProductCardGenUpdateDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductCardGenDTO.class)
                ;

    }

    @Test
    void updateProductCardGen1() {

        ProductDef productDef = createProductDef();
        productDefRepository.save(productDef);

        ProductCardGenDef productCardGenDef = createProductCardGenDef();
        productCardGenDef.setProductId(productDef.getProductId());
        productCardGenRepository.save(productCardGenDef);

        ProductCardGenUpdateDTO productCardGenUpdateDTO = createProductGenUpdate(true,null);


        webTestClient.put()
                .uri(EndPoints.PRODUCT_CARD_GEN)
                .body(Mono.just(productCardGenUpdateDTO),ProductCardGenUpdateDTO.class)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
        ;

    }

    @Test
    void deleteProductCardGen() {

        ProductDef productDef = createProductDef();
        productDefRepository.save(productDef);

        ProductCardGenDef productCardGenDef = createProductCardGenDef();
        productCardGenDef.setProductId(productDef.getProductId());
        productCardGenRepository.save(productCardGenDef);

        Integer org = productDef.getProductId().getOrg();
        Integer product = productDef.getProductId().getProduct();

        String uri = EndPoints.PRODUCTS_ORG_PRODUCT.replace("{org}",org.toString())
                .replace("{product}",product.toString());

        webTestClient.delete()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductCardGenDTO.class)
        ;
    }

    @Test
    void deleteProductCardGen1() {

        ProductDef productDef = createProductDef();
        productDefRepository.save(productDef);

        ProductCardGenDef productCardGenDef = createProductCardGenDef();
        productCardGenDef.setProductId(productDef.getProductId());
        productCardGenRepository.save(productCardGenDef);

        Integer org = 999;
        Integer product = 999;

        String uri = EndPoints.PRODUCTS_ORG_PRODUCT.replace("{org}",org.toString())
                .replace("{product}",product.toString());

        webTestClient.delete()
                .uri(uri)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
        ;
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

    private ProductDef createProductDef(){

        Map<BalanceTypes,Long> percentMap = new HashMap<>();
        percentMap.put(BalanceTypes.CASH_BALANCE,2000000L);
        percentMap.put(BalanceTypes.INTERNATIONAL_CASH,1000000L);
        percentMap.put(BalanceTypes.INTERNATIONAL_CASH_INSTALLMENT,3000000L);


        return ProductDef.builder()
                .serviceCode(301)
                .productId(new ProductId(101,251))
                .daysToCardsValid(11)
                .dateRangeNewExpDate(10)
                .cardsWaiverActivationDays(5)
                .cardsValidityMonthReplace(35)
                .cardsValidityMonthReIssue(40)
                .cardsValidityMonthNew(44)
                .cardsActivationRequired(false)
                .limitPercents(percentMap)
                .cardsReturn(10)
                .billingCurrencyCode("840")
                .primaryAccountType(AccountType.PREPAID)
                .build();

    }

}