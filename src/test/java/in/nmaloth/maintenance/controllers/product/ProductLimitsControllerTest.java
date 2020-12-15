package in.nmaloth.maintenance.controllers.product;

import in.nmaloth.entity.account.AccountType;
import in.nmaloth.entity.account.BalanceTypes;
import in.nmaloth.entity.card.LimitType;
import in.nmaloth.entity.card.PeriodicCardAmount;
import in.nmaloth.entity.card.PeriodicType;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.entity.product.ProductId;
import in.nmaloth.entity.product.ProductLimitsDef;
import in.nmaloth.maintenance.controllers.EndPoints;
import in.nmaloth.maintenance.model.dto.product.PeriodicLimitDTO;
import in.nmaloth.maintenance.model.dto.product.ProductDefUpdateDTO;
import in.nmaloth.maintenance.model.dto.product.ProductLimitDefDTO;
import in.nmaloth.maintenance.model.dto.product.ProductLimitDefUpdateDTO;
import in.nmaloth.maintenance.repository.product.ProductDefRepository;
import in.nmaloth.maintenance.repository.product.ProductLimitsDefRepository;
import in.nmaloth.maintenance.service.product.ProductLimitService;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext
@AutoConfigureWebTestClient
class ProductLimitsControllerTest {

    @Autowired
    private ProductDefRepository productDefRepository;

    @Autowired
    private ProductLimitsDefRepository productLimitsDefRepository;

    @Autowired
    private ProductLimitService productLimitService;

    @Autowired
    private WebTestClient webTestClient;


    @BeforeEach
    void  setup(){

        productDefRepository.findAll()
                .forEach(productDef -> productDefRepository.delete(productDef));

        productLimitsDefRepository.findAll()
                .forEach(productLimitsDef -> productLimitsDefRepository.delete(productLimitsDef));

    }

    @Test
    void createNeWProductLimit() {

        ProductDef productDef = createProductDef();
        productDefRepository.save(productDef);

        ProductLimitDefDTO productLimitDefDTO = createProductLimitDTOForAdd();
        productLimitDefDTO.setOrg(productDef.getProductId().getOrg());
        productLimitDefDTO.setProduct(productDef.getProductId().getProduct());

        webTestClient.post()
                .uri(EndPoints.PRODUCT_LIMITS)
                .body(Mono.just(productLimitDefDTO),ProductLimitDefDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductLimitDefDTO.class)
                ;
    }

    @Test
    void createNeWProductLimit1() {

        ProductDef productDef = createProductDef();
        productDefRepository.save(productDef);

        ProductLimitDefDTO productLimitDefDTO = createProductLimitDTOForAdd();
        productLimitDefDTO.setOrg(productDef.getProductId().getOrg());
        productLimitDefDTO.setProduct(999);

        webTestClient.post()
                .uri(EndPoints.PRODUCT_LIMITS)
                .body(Mono.just(productLimitDefDTO),ProductLimitDefDTO.class)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
                .expectBody(String.class)
        ;
    }

    @Test
    void createNeWProductLimit2() {

        ProductDef productDef = createProductDef();
        productDefRepository.save(productDef);

        ProductLimitsDef productLimitsDef = createProductLimitsDef();
        productLimitsDef.setProductId(productDef.getProductId());
        productLimitsDefRepository.save(productLimitsDef);


        ProductLimitDefDTO productLimitDefDTO = createProductLimitDTOForAdd();
        productLimitDefDTO.setOrg(productDef.getProductId().getOrg());
        productLimitDefDTO.setProduct(productDef.getProductId().getProduct());

        webTestClient.post()
                .uri(EndPoints.PRODUCT_LIMITS)
                .body(Mono.just(productLimitDefDTO),ProductLimitDefDTO.class)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                .expectBody(String.class)
        ;
    }

    @Test
    void fetchProductLimit() {

        ProductDef productDef = createProductDef();
        productDefRepository.save(productDef);

        ProductLimitsDef productLimitsDef = createProductLimitsDef();
        productLimitsDef.setProductId(productDef.getProductId());
        productLimitsDefRepository.save(productLimitsDef);

        Integer org = productDef.getProductId().getOrg();
        Integer product = productDef.getProductId().getProduct();

        String uri = EndPoints.PRODUCTS_ORG_PRODUCT.replace("{org}",org.toString())
                .replace("{product}",product.toString());

        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductLimitDefDTO.class)
                ;
    }

    @Test
    void fetchAllProductLimit() {

        ProductLimitsDef productLimitsDef = createProductLimitsDef();
        productLimitsDefRepository.save(productLimitsDef);
        productLimitsDef.getProductId().setProduct(777);
        productLimitsDefRepository.save(productLimitsDef);
        productLimitsDef.getProductId().setProduct(888);
        productLimitsDefRepository.save(productLimitsDef);

        webTestClient
                .get()
                .uri(EndPoints.PRODUCT_LIMITS)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductLimitDefDTO.class)
                .hasSize(3)
                ;
    }

    @Test
    void updateProduct() {

        ProductDef productDef = createProductDef();
        productDefRepository.save(productDef);

        ProductLimitsDef productLimitsDef = createProductLimitsDef();
        productLimitsDef.setProductId(productDef.getProductId());
        productLimitsDefRepository.save(productLimitsDef);

        ProductLimitDefUpdateDTO productLimitDefUpdateDTO = createProductLimitDTOForUpdate(true, true);
        productLimitDefUpdateDTO.setProduct(productDef.getProductId().getProduct());
        productLimitDefUpdateDTO.setOrg(productDef.getProductId().getOrg());

        webTestClient.put()
                .uri(EndPoints.PRODUCT_LIMITS)
                .body(Mono.just(productLimitDefUpdateDTO),ProductDefUpdateDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductDef.class)
                ;


    }

    @Test
    void updateProduct1() {

        ProductDef productDef = createProductDef();
        productDefRepository.save(productDef);

        ProductLimitsDef productLimitsDef = createProductLimitsDef();
        productLimitsDef.setProductId(productDef.getProductId());
        productLimitsDefRepository.save(productLimitsDef);

        ProductLimitDefUpdateDTO productLimitDefUpdateDTO = createProductLimitDTOForUpdate(true, true);

        webTestClient.put()
                .uri(EndPoints.PRODUCT_LIMITS)
                .body(Mono.just(productLimitDefUpdateDTO),ProductDefUpdateDTO.class)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
                .expectBody(String.class)
        ;


    }

    @Test
    void deleteProductLimit() {

        ProductDef productDef = createProductDef();
        productDefRepository.save(productDef);

        ProductLimitsDef productLimitsDef = createProductLimitsDef();
        productLimitsDef.setProductId(productDef.getProductId());
        productLimitsDefRepository.save(productLimitsDef);

        Integer org = productDef.getProductId().getOrg();
        Integer product = productDef.getProductId().getProduct();

        String uri = EndPoints.PRODUCTS_ORG_PRODUCT.replace("{org}",org.toString())
                .replace("{product}",product.toString());

        webTestClient.delete()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductLimitDefDTO.class)
                ;

    }

    @Test
    void deleteProductLimit1() {

        ProductDef productDef = createProductDef();
        productDefRepository.save(productDef);

        ProductLimitsDef productLimitsDef = createProductLimitsDef();
        productLimitsDef.setProductId(productDef.getProductId());
        productLimitsDefRepository.save(productLimitsDef);

        Integer org = 301;
        Integer product = 401;

        String uri = EndPoints.PRODUCTS_ORG_PRODUCT.replace("{org}",org.toString())
                .replace("{product}",product.toString());

        webTestClient.delete()
                .uri(uri)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
        ;

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

    private ProductLimitDefDTO createProductLimitDTOForAdd(){


        List<PeriodicLimitDTO> periodicLimitDTOList = new ArrayList<>();

        PeriodicLimitDTO periodicLimitDTOSingle1 = PeriodicLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.SINGLE))
                .limitType(Util.getLimitType(LimitType.NO_SPECIFIC))
                .limitNumber(1000)
                .limitAmount(500000L)
                .build();
        PeriodicLimitDTO periodicLimitDTOSingle2 = PeriodicLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.SINGLE))
                .limitType(Util.getLimitType(LimitType.ATM))
                .limitNumber(2000)
                .limitAmount(600000L)
                .build();
        PeriodicLimitDTO periodicLimitDTOSingle3 = PeriodicLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.SINGLE))
                .limitType(Util.getLimitType(LimitType.RETAIL))
                .limitNumber(3000)
                .limitAmount(800000L)
                .build();
        PeriodicLimitDTO periodicLimitDTOSingle4 = PeriodicLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.SINGLE))
                .limitType(Util.getLimitType(LimitType.OTC))
                .limitNumber(4000)
                .limitAmount(900000L)
                .build();

        PeriodicLimitDTO periodicLimitDTODaily1 = PeriodicLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.DAILY))
                .limitType(Util.getLimitType(LimitType.NO_SPECIFIC))
                .limitNumber(5000)
                .limitAmount(1000000L)
                .build();

        PeriodicLimitDTO periodicLimitDTODaily2 = PeriodicLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.DAILY))
                .limitType(Util.getLimitType(LimitType.CASH))
                .limitNumber(6000)
                .limitAmount(1100000L)
                .build();

        PeriodicLimitDTO periodicLimitDTODaily3 = PeriodicLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.DAILY))
                .limitType(Util.getLimitType(LimitType.RETAIL))
                .limitNumber(7000)
                .limitAmount(1200000L)
                .build();

        PeriodicLimitDTO periodicLimitDTODaily4 = PeriodicLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.DAILY))
                .limitType(Util.getLimitType(LimitType.OTC))
                .limitNumber(8000)
                .limitAmount(1300000L)
                .build();

        PeriodicLimitDTO periodicLimitDTOMonthly1 = PeriodicLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.MONTHLY))
                .limitType(Util.getLimitType(LimitType.NO_SPECIFIC))
                .limitNumber(9000)
                .limitAmount(1400000L)
                .build();

        PeriodicLimitDTO periodicLimitDTOMonthly2 = PeriodicLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.MONTHLY))
                .limitType(Util.getLimitType(LimitType.RETAIL))
                .limitNumber(10000)
                .limitAmount(1500000L)
                .build();

        PeriodicLimitDTO periodicLimitDTOMonthly3 = PeriodicLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.MONTHLY))
                .limitType(Util.getLimitType(LimitType.CASH))
                .limitNumber(11000)
                .limitAmount(1600000L)
                .build();

        PeriodicLimitDTO periodicLimitDTOMonthly4 = PeriodicLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.MONTHLY))
                .limitType(Util.getLimitType(LimitType.OTC))
                .limitNumber(12000)
                .limitAmount(1700000L)
                .build();


        periodicLimitDTOList.add(periodicLimitDTOSingle1);
        periodicLimitDTOList.add(periodicLimitDTOSingle2);
        periodicLimitDTOList.add(periodicLimitDTOSingle3);
        periodicLimitDTOList.add(periodicLimitDTOSingle4);
        periodicLimitDTOList.add(periodicLimitDTODaily1);
        periodicLimitDTOList.add(periodicLimitDTODaily2);
        periodicLimitDTOList.add(periodicLimitDTODaily3);
        periodicLimitDTOList.add(periodicLimitDTODaily4);
        periodicLimitDTOList.add(periodicLimitDTOMonthly1);
        periodicLimitDTOList.add(periodicLimitDTOMonthly2);
        periodicLimitDTOList.add(periodicLimitDTOMonthly3);
        periodicLimitDTOList.add(periodicLimitDTOMonthly4);

        return ProductLimitDefDTO.builder()
                .org(101)
                .product(201)
                .periodicLimitDTOList(periodicLimitDTOList)
                .build()
                ;



    }


    private ProductLimitDefUpdateDTO createProductLimitDTOForUpdate(boolean add, boolean delete){


        List<PeriodicLimitDTO> periodicLimitDTOAddList = new ArrayList<>();
        List<PeriodicLimitDTO> periodicLimitDTODeleteList = new ArrayList<>();


        PeriodicLimitDTO periodicLimitDTOSingle1 = PeriodicLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.SINGLE))
                .limitType(Util.getLimitType(LimitType.NO_SPECIFIC))
                .limitNumber(12000)
                .limitAmount(1500000L)
                .build();
        PeriodicLimitDTO periodicLimitDTOSingle2 = PeriodicLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.SINGLE))
                .limitType(Util.getLimitType(LimitType.QUASI_CASH))
                .limitNumber(13000)
                .limitAmount(600000L)
                .build();


        PeriodicLimitDTO periodicLimitDTODaily1 = PeriodicLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.DAILY))
                .limitType(Util.getLimitType(LimitType.NO_SPECIFIC))
                .limitNumber(500)
                .limitAmount(21000000L)
                .build();

        PeriodicLimitDTO periodicLimitDTODaily2 = PeriodicLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.DAILY))
                .limitType(Util.getLimitType(LimitType.CASH))
                .limitNumber(600)
                .limitAmount(2200000L)
                .build();

        PeriodicLimitDTO periodicLimitDTODaily3 = PeriodicLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.DAILY))
                .limitType(Util.getLimitType(LimitType.RETAIL))
                .limitNumber(800)
                .limitAmount(2300000L)
                .build();


        PeriodicLimitDTO periodicLimitDTOYearly1 = PeriodicLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.YEARLY))
                .limitType(Util.getLimitType(LimitType.NO_SPECIFIC))
                .limitNumber(9000)
                .limitAmount(1400000L)
                .build();

        PeriodicLimitDTO periodicLimitDTOYearly2 = PeriodicLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.YEARLY))
                .limitType(Util.getLimitType(LimitType.RETAIL))
                .limitNumber(10000)
                .limitAmount(1500000L)
                .build();

        PeriodicLimitDTO periodicLimitDTOYearly3 = PeriodicLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.YEARLY))
                .limitType(Util.getLimitType(LimitType.CASH))
                .limitNumber(11000)
                .limitAmount(1600000L)
                .build();

        PeriodicLimitDTO periodicLimitDTOSingle3 = PeriodicLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.SINGLE))
                .limitType(Util.getLimitType(LimitType.RETAIL))
                .limitNumber(3000)
                .limitAmount(800000L)
                .build();
        PeriodicLimitDTO periodicLimitDTOSingle4 = PeriodicLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.SINGLE))
                .limitType(Util.getLimitType(LimitType.OTC))
                .limitNumber(4000)
                .limitAmount(900000L)
                .build();



        if(add){

            periodicLimitDTOAddList.add(periodicLimitDTOSingle1);
            periodicLimitDTOAddList.add(periodicLimitDTOSingle2);
            periodicLimitDTOAddList.add(periodicLimitDTODaily1);
            periodicLimitDTOAddList.add(periodicLimitDTODaily2);
            periodicLimitDTOAddList.add(periodicLimitDTODaily3);
            periodicLimitDTOAddList.add(periodicLimitDTOYearly1);
            periodicLimitDTOAddList.add(periodicLimitDTOYearly2);
            periodicLimitDTOAddList.add(periodicLimitDTOYearly3);

        }

        if(delete){
            periodicLimitDTODeleteList.add(periodicLimitDTOSingle3);
            periodicLimitDTODeleteList.add(periodicLimitDTOSingle4);


        }


        ProductLimitDefUpdateDTO.ProductLimitDefUpdateDTOBuilder builder = ProductLimitDefUpdateDTO.builder()
                .org(101)
                .product(201);

        if(add){
            builder
                    .periodicLimitDTOListAdd(periodicLimitDTOAddList);
        }
        if(delete){
            builder.periodicLimitDTOListDelete(periodicLimitDTODeleteList);
        }
        return builder.build();
    }

    private ProductLimitsDef createProductLimitsDef(){

        PeriodicCardAmount periodicCardAmount1 = PeriodicCardAmount.builder()
                .limitType(LimitType.NO_SPECIFIC)
                .transactionAmount(100000L)
                .transactionNumber(10)
                .build();

        PeriodicCardAmount periodicCardAmount2 = PeriodicCardAmount.builder()
                .limitType(LimitType.ATM)
                .transactionAmount(200000L)
                .transactionNumber(20)
                .build();

        PeriodicCardAmount periodicCardAmount3 = PeriodicCardAmount.builder()
                .limitType(LimitType.QUASI_CASH)
                .transactionAmount(300000L)
                .transactionNumber(30)
                .build();

        PeriodicCardAmount periodicCardAmount4 = PeriodicCardAmount.builder()
                .limitType(LimitType.OTC)
                .transactionAmount(400000L)
                .transactionNumber(40)
                .build();

        PeriodicCardAmount periodicCardAmount5 = PeriodicCardAmount.builder()
                .limitType(LimitType.QUASI_CASH)
                .transactionAmount(500000L)
                .transactionNumber(50)
                .build();
        List<PeriodicCardAmount> periodicCardAmountList1 = new ArrayList<>();
        List<PeriodicCardAmount> periodicCardAmountList2 = new ArrayList<>();

        periodicCardAmountList1.add(periodicCardAmount1);
        periodicCardAmountList1.add(periodicCardAmount2);
        periodicCardAmountList1.add(periodicCardAmount3);

        periodicCardAmountList2.add(periodicCardAmount4);
        periodicCardAmountList2.add(periodicCardAmount5);


        Map<PeriodicType,List<PeriodicCardAmount>> periodicCardAmountMap = new HashMap<>();
        periodicCardAmountMap.put(PeriodicType.SINGLE,periodicCardAmountList1);
        periodicCardAmountMap.put(PeriodicType.DAILY,periodicCardAmountList2);


        return ProductLimitsDef.builder()
                .productId(new ProductId(001,101))
                .cardLimitMap(periodicCardAmountMap)
                .build();
    }

    private PeriodicCardAmount extractPeriodCardAmount(List<PeriodicCardAmount> periodicCardAmountList, LimitType limitType){

        return periodicCardAmountList.stream()
                .filter(periodicCardAmount -> periodicCardAmount.getLimitType().equals(limitType))
                .findFirst()
                .get();


    }


}