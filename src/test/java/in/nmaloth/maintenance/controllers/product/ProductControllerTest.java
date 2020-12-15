package in.nmaloth.maintenance.controllers.product;

import in.nmaloth.entity.account.AccountType;
import in.nmaloth.entity.account.BalanceTypes;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.entity.product.ProductId;
import in.nmaloth.maintenance.controllers.EndPoints;
import in.nmaloth.maintenance.model.dto.product.LimitPercentDTO;
import in.nmaloth.maintenance.model.dto.product.ProductDefDTO;
import in.nmaloth.maintenance.model.dto.product.ProductDefUpdateDTO;
import in.nmaloth.maintenance.repository.product.ProductDefRepository;
import in.nmaloth.maintenance.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext
@AutoConfigureWebTestClient
class ProductControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProductDefRepository productDefRepository;


    @BeforeEach
    void setup(){

        productDefRepository.findAll()
            .forEach(productDef -> productDefRepository.delete(productDef));
        ;

    }

    @Test
    void createNewProduct() {

        ProductDefDTO productDefDTO = createProductDefDto();

        webTestClient.post()
                .uri(EndPoints.PRODUCTS)
                .body(Mono.just(productDefDTO),ProductDefDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductDefDTO.class)
                .value(productDefDTO1 -> {
                    assertAll(
                            ()->assertEquals(productDefDTO.getOrg(),productDefDTO1.getOrg()),
                            ()-> assertEquals(productDefDTO.getProduct(),productDefDTO1.getProduct()),
                            ()-> assertEquals(productDefDTO.getPrimaryAccountType(),productDefDTO1.getPrimaryAccountType()),
                            ()-> assertEquals(productDefDTO.getBillingCurrencyCode(),productDefDTO1.getBillingCurrencyCode()),
                            ()-> assertEquals(productDefDTO.getLimitPercents().size(),productDefDTO1.getLimitPercents().size()),
                            ()-> assertEquals(productDefDTO.getCardsReturn(),productDefDTO1.getCardsReturn()),
                            ()-> assertEquals(productDefDTO.getCardsActivationRequired(),productDefDTO1.getCardsActivationRequired()),
                            ()-> assertEquals(productDefDTO.getCardsValidityMonthNew(),productDefDTO1.getCardsValidityMonthNew()),
                            ()-> assertEquals(productDefDTO.getCardsValidityMonthReIssue(),productDefDTO1.getCardsValidityMonthReIssue()),
                            ()-> assertEquals(productDefDTO.getCardsValidityMonthReplace(),productDefDTO1.getCardsValidityMonthReplace()),
                            ()-> assertEquals(productDefDTO.getCardsWaiverActivationDays(),productDefDTO1.getCardsWaiverActivationDays()),
                            ()-> assertEquals(productDefDTO.getDateRangeNewExpDate(),productDefDTO1.getDateRangeNewExpDate()),
                            ()-> assertEquals(productDefDTO.getDaysToCardsValid(),productDefDTO1.getDaysToCardsValid()),
                            ()-> assertEquals(productDefDTO.getServiceCode(),productDefDTO1.getServiceCode())
                    );
                })
                ;

    }

    @Test
    void createNewProduct1() {

        ProductDef productDef = createProductDef();

        productDefRepository.save(productDef);
        ProductDefDTO productDefDTO = createProductDefDto();
        productDefDTO.setOrg(productDef.getProductId().getOrg());
        productDefDTO.setProduct(productDef.getProductId().getProduct());

        webTestClient.post()
                .uri(EndPoints.PRODUCTS)
                .body(Mono.just(productDefDTO),ProductDefDTO.class)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                .expectBody(String.class)

        ;

    }

    @Test
    void fetchProduct(){

        ProductDef productDef = createProductDef();
        productDefRepository.save(productDef);

        Integer org = productDef.getProductId().getOrg();
        Integer product = productDef.getProductId().getProduct();

        String uri = EndPoints.PRODUCTS_ORG_PRODUCT.replace("{org}",org.toString())
                .replace("{product}",product.toString());

        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductDefDTO.class)
                .value(productDefDTO -> {

                    assertAll(
                            ()->assertEquals(productDef.getProductId().getOrg(),productDefDTO.getOrg()),
                            ()-> assertEquals(productDef.getProductId().getProduct(),productDefDTO.getProduct()),
                            ()-> assertEquals(productDef.getPrimaryAccountType(),Util.getAccountType(productDefDTO.getPrimaryAccountType())),
                            ()-> assertEquals(productDef.getBillingCurrencyCode(),productDefDTO.getBillingCurrencyCode()),
                            ()-> assertEquals(productDef.getLimitPercents().size(),productDefDTO.getLimitPercents().size()),
                            ()-> assertEquals(productDef.getCardsReturn(),productDefDTO.getCardsReturn()),
                            ()-> assertEquals(productDef.getCardsActivationRequired(),productDefDTO.getCardsActivationRequired()),
                            ()-> assertEquals(productDef.getCardsValidityMonthNew(),productDefDTO.getCardsValidityMonthNew()),
                            ()-> assertEquals(productDef.getCardsValidityMonthReIssue(),productDefDTO.getCardsValidityMonthReIssue()),
                            ()-> assertEquals(productDef.getCardsValidityMonthReplace(),productDefDTO.getCardsValidityMonthReplace()),
                            ()-> assertEquals(productDef.getCardsWaiverActivationDays(),productDefDTO.getCardsWaiverActivationDays()),
                            ()-> assertEquals(productDef.getDateRangeNewExpDate(),productDefDTO.getDateRangeNewExpDate()),
                            ()-> assertEquals(productDef.getDaysToCardsValid(),productDefDTO.getDaysToCardsValid()),
                            ()-> assertEquals(productDef.getServiceCode(),productDefDTO.getServiceCode())
                    );

                });

    }

    @Test
    void fetchProduct1(){

        ProductDef productDef = createProductDef();

        Integer org = productDef.getProductId().getOrg();
        Integer product = productDef.getProductId().getProduct();

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
    void deleteProduct(){

        ProductDef productDef = createProductDef();
        productDefRepository.save(productDef);

        Integer org = productDef.getProductId().getOrg();
        Integer product = productDef.getProductId().getProduct();

        String uri = EndPoints.PRODUCTS_ORG_PRODUCT.replace("{org}",org.toString())
                .replace("{product}",product.toString());

        webTestClient.delete()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductDefDTO.class)
                .value(productDefDTO -> {

                    Optional<ProductDef> productDefOptional =  productDefRepository.findById(new ProductId(org,product));

                    assertAll(
                            ()->assertEquals(productDef.getProductId().getOrg(),productDefDTO.getOrg()),
                            ()-> assertEquals(productDef.getProductId().getProduct(),productDefDTO.getProduct()),
                            ()-> assertEquals(productDef.getPrimaryAccountType(),Util.getAccountType(productDefDTO.getPrimaryAccountType())),
                            ()-> assertEquals(productDef.getBillingCurrencyCode(),productDefDTO.getBillingCurrencyCode()),
                            ()-> assertEquals(productDef.getLimitPercents().size(),productDefDTO.getLimitPercents().size()),
                            ()-> assertEquals(productDef.getCardsReturn(),productDefDTO.getCardsReturn()),
                            ()-> assertEquals(productDef.getCardsActivationRequired(),productDefDTO.getCardsActivationRequired()),
                            ()-> assertEquals(productDef.getCardsValidityMonthNew(),productDefDTO.getCardsValidityMonthNew()),
                            ()-> assertEquals(productDef.getCardsValidityMonthReIssue(),productDefDTO.getCardsValidityMonthReIssue()),
                            ()-> assertEquals(productDef.getCardsValidityMonthReplace(),productDefDTO.getCardsValidityMonthReplace()),
                            ()-> assertEquals(productDef.getCardsWaiverActivationDays(),productDefDTO.getCardsWaiverActivationDays()),
                            ()-> assertEquals(productDef.getDateRangeNewExpDate(),productDefDTO.getDateRangeNewExpDate()),
                            ()-> assertEquals(productDef.getDaysToCardsValid(),productDefDTO.getDaysToCardsValid()),
                            ()-> assertEquals(productDef.getServiceCode(),productDefDTO.getServiceCode()),
                            ()-> assertTrue(productDefOptional.isEmpty())
                    );

                });

    }


    @Test
    void deleteProduct1(){

        ProductDef productDef = createProductDef();

        Integer org = productDef.getProductId().getOrg();
        Integer product = productDef.getProductId().getProduct();

        String uri = EndPoints.PRODUCTS_ORG_PRODUCT.replace("{org}",org.toString())
                .replace("{product}",product.toString());

        webTestClient.delete()
                .uri(uri)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                ;


    }


    @Test
    void UpdateProduct(){

        ProductDef productDef = createProductDef();
        productDefRepository.save(productDef);

        Integer org = productDef.getProductId().getOrg();
        Integer product = productDef.getProductId().getProduct();

        ProductDefUpdateDTO productDefUpdateDTO = createProductDefUpdateDTO(true,null);

        productDefUpdateDTO.setOrg(org);
        productDefUpdateDTO.setProduct(product);

        webTestClient.put()
                .uri(EndPoints.PRODUCTS)
                .body(Mono.just(productDefUpdateDTO),ProductDefUpdateDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductDefDTO.class)
                .value(productDefDTO -> {

                    assertAll(
                            ()->assertEquals(productDef.getProductId().getOrg(),productDefDTO.getOrg()),
                            ()-> assertEquals(productDef.getProductId().getProduct(),productDefDTO.getProduct()),
                            ()-> assertEquals(productDefUpdateDTO.getPrimaryAccountType(),productDefDTO.getPrimaryAccountType()),
                            ()-> assertEquals(productDefUpdateDTO.getBillingCurrencyCode(),productDefDTO.getBillingCurrencyCode()),
                            ()-> assertEquals(3,productDefDTO.getLimitPercents().size()),
                            ()-> assertEquals(productDefUpdateDTO.getCardsReturn(),productDefDTO.getCardsReturn()),
                            ()-> assertEquals(productDefUpdateDTO.getCardsActivationRequired(),productDefDTO.getCardsActivationRequired()),
                            ()-> assertEquals(productDefUpdateDTO.getCardsValidityMonthNew(),productDefDTO.getCardsValidityMonthNew()),
                            ()-> assertEquals(productDefUpdateDTO.getCardsValidityMonthReIssue(),productDefDTO.getCardsValidityMonthReIssue()),
                            ()-> assertEquals(productDefUpdateDTO.getCardsValidityMonthReplace(),productDefDTO.getCardsValidityMonthReplace()),
                            ()-> assertEquals(productDefUpdateDTO.getCardsWaiverActivationDays(),productDefDTO.getCardsWaiverActivationDays()),
                            ()-> assertEquals(productDefUpdateDTO.getDateRangeNewExpDate(),productDefDTO.getDateRangeNewExpDate()),
                            ()-> assertEquals(productDefUpdateDTO.getDaysToCardsValid(),productDefDTO.getDaysToCardsValid()),
                            ()-> assertEquals(productDefUpdateDTO.getServiceCode(),productDefDTO.getServiceCode())
                    );

                });

    }


    @Test
    void  fetchAllProducts(){

        ProductDef productDef = createProductDef();
        productDefRepository.save(productDef);
        productDef.getProductId().setProduct(501);
        productDefRepository.save(productDef);
        productDef.getProductId().setProduct(601);
        productDefRepository.save(productDef);

        webTestClient.get()
                .uri(EndPoints.PRODUCTS)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductDefDTO.class)
                .hasSize(3)
                ;
    }

    @Test
    void UpdateProduct1(){

        ProductDef productDef = createProductDef();

        Integer org = productDef.getProductId().getOrg();
        Integer product = productDef.getProductId().getProduct();

        ProductDefUpdateDTO productDefUpdateDTO = createProductDefUpdateDTO(true,null);

        productDefUpdateDTO.setOrg(org);
        productDefUpdateDTO.setProduct(product);

        webTestClient.put()
                .uri(EndPoints.PRODUCTS)
                .body(Mono.just(productDefUpdateDTO),ProductDefUpdateDTO.class)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class);

    }



    private ProductDefDTO createProductDefDto(){

        LimitPercentDTO limitPercentDTO1 = LimitPercentDTO.builder()
                .percent(500000L)
                .balanceTypes(Util.getBalanceTypes(BalanceTypes.CASH_BALANCE))
                .build();

        LimitPercentDTO limitPercentDTO2 = LimitPercentDTO.builder()
                .percent(600000L)
                .balanceTypes(Util.getBalanceTypes(BalanceTypes.INTERNATIONAL))
                .build();

        LimitPercentDTO limitPercentDTO3 = LimitPercentDTO.builder()
                .percent(300000L)
                .balanceTypes(Util.getBalanceTypes(BalanceTypes.INSTALLMENT_BALANCE))
                .build();

        List<LimitPercentDTO> limitPercentDTOList = new ArrayList<>();
        limitPercentDTOList.add(limitPercentDTO1);
        limitPercentDTOList.add(limitPercentDTO2);
        limitPercentDTOList.add(limitPercentDTO3);

        return ProductDefDTO.builder()
                .serviceCode(201)
                .product(101)
                .org(001)
                .daysToCardsValid(10)
                .dateRangeNewExpDate(48)
                .cardsWaiverActivationDays(5)
                .cardsValidityMonthReplace(40)
                .cardsValidityMonthReIssue(44)
                .cardsValidityMonthNew(48)
                .cardsActivationRequired(true)
                .limitPercents(limitPercentDTOList)
                .cardsReturn(20)
                .billingCurrencyCode("USA")
                .primaryAccountType(Util.getAccountType(AccountType.SAVINGS))
                .build();

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

    private ProductDefUpdateDTO createProductDefUpdateDTO(boolean allFields, List<Integer> integerList){

        Map<BalanceTypes,Long>  percentMap = new HashMap<>();
        percentMap.put(BalanceTypes.CASH_BALANCE,2000000L);
        percentMap.put(BalanceTypes.INTERNATIONAL_CASH,1000000L);
        percentMap.put(BalanceTypes.INTERNATIONAL_CASH_INSTALLMENT,3000000L);


        LimitPercentDTO limitPercentDTO1 = LimitPercentDTO.builder()
                .percent(500000L)
                .balanceTypes(Util.getBalanceTypes(BalanceTypes.INTERNATIONAL_CASH))
                .build();

        LimitPercentDTO limitPercentDTO2 = LimitPercentDTO.builder()
                .percent(600000L)
                .balanceTypes(Util.getBalanceTypes(BalanceTypes.INSTALLMENT_BALANCE))
                .build();

        LimitPercentDTO limitPercentDTO3 = LimitPercentDTO.builder()
                .percent(300000L)
                .balanceTypes(Util.getBalanceTypes(BalanceTypes.INTERNATIONAL_CASH_INSTALLMENT))
                .build();

        List<LimitPercentDTO> limitPercentDTOListAdd = new ArrayList<>();
        List<LimitPercentDTO> limitPercentDTOListDelete = new ArrayList<>();

        limitPercentDTOListAdd.add(limitPercentDTO1);
        limitPercentDTOListAdd.add(limitPercentDTO2);
        limitPercentDTOListDelete.add(limitPercentDTO3);

        ProductDefUpdateDTO.ProductDefUpdateDTOBuilder builder = ProductDefUpdateDTO.builder()
                .product(101)
                .org(001);

        if(allFields){
            return  builder
                    .serviceCode(401)
                    .daysToCardsValid(11)
                    .dateRangeNewExpDate(49)
                    .cardsWaiverActivationDays(8)
                    .cardsValidityMonthReplace(42)
                    .cardsValidityMonthReIssue(45)
                    .cardsValidityMonthNew(49)
                    .cardsActivationRequired(false)
                    .limitPercentListAdd(limitPercentDTOListAdd)
                    .limitPercentListDelete(limitPercentDTOListDelete)
                    .cardsReturn(15)
                    .primaryAccountType(Util.getAccountType(AccountType.CURRENT))
                    .billingCurrencyCode("124")
                    .build();

        }

        integerList.forEach(integer -> evaluateUpdateDTO(builder, integer));

        return builder.build();

    }

    private void evaluateUpdateDTO(ProductDefUpdateDTO.ProductDefUpdateDTOBuilder builder, Integer integer) {

        switch (integer){
            case 1: {
                builder
                        .serviceCode(501);
                break;
            }
            case 2:{
                builder.primaryAccountType(Util.getAccountType(AccountType.CREDIT));
                break;
            }
            case 3:{
                builder.billingCurrencyCode("484");
            }
            case 4:
            case 5: {
                break;
            }
            case 6: {
                builder
                        .daysToCardsValid(14);
                break;
            }
            case 7: {
                builder
                        .dateRangeNewExpDate(15);
                break;
            }
            case 8: {
                builder
                        .cardsWaiverActivationDays(25);
                break;
            }
            case 9: {
                builder
                        .cardsValidityMonthReplace(52);
                break;
            }
            case 10: {
                builder
                        .cardsValidityMonthReIssue(63);
                break;
            }
            case 11: {
                builder
                        .cardsValidityMonthNew(72);
                break;
            }
            case 12: {
                builder
                        .cardsActivationRequired(false);
                break;
            }
            case 13: {
                LimitPercentDTO limitPercentDTO1 = LimitPercentDTO.builder()
                        .percent(500000L)
                        .balanceTypes(Util.getBalanceTypes(BalanceTypes.INTERNATIONAL_CASH))
                        .build();

                LimitPercentDTO limitPercentDTO2 = LimitPercentDTO.builder()
                        .percent(600000L)
                        .balanceTypes(Util.getBalanceTypes(BalanceTypes.INTERNATIONAL_CASH_INSTALLMENT))
                        .build();
                List<LimitPercentDTO> limitPercentDTOListAdd = new ArrayList<>();
                limitPercentDTOListAdd.add(limitPercentDTO1);
                limitPercentDTOListAdd.add(limitPercentDTO2);

                builder
                        .limitPercentListAdd(limitPercentDTOListAdd);
                break;
            }
            case 14: {

                LimitPercentDTO limitPercentDTO3 = LimitPercentDTO.builder()
                        .percent(300000L)
                        .balanceTypes(Util.getBalanceTypes(BalanceTypes.CASH_BALANCE))
                        .build();

                List<LimitPercentDTO> limitPercentDTOListDelete = new ArrayList<>();
                limitPercentDTOListDelete.add(limitPercentDTO3);

                builder
                        .limitPercentListDelete(limitPercentDTOListDelete);
                break;

            }
            case 15: {
                builder.daysToCardsValid(30);
                break;
            }
            case 16: {
                builder.cardsReturn(17);
            }


        }
    }
}