package in.nmaloth.maintenance.service.product;

import in.nmaloth.entity.card.LimitType;
import in.nmaloth.entity.card.PeriodicCardAmount;
import in.nmaloth.entity.card.PeriodicType;
import in.nmaloth.entity.product.ProductId;
import in.nmaloth.entity.product.ProductLimitsDef;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.product.PeriodicLimitDTO;
import in.nmaloth.maintenance.model.dto.product.ProductLimitDefDTO;
import in.nmaloth.maintenance.model.dto.product.ProductLimitDefUpdateDTO;
import in.nmaloth.maintenance.repository.product.ProductLimitsDefRepository;
import in.nmaloth.maintenance.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductLimitServiceImplTest {


    @Autowired
    private ProductLimitService productLimitService;

    @Autowired
    private ProductLimitsDefRepository productLimitsDefRepository;


    @BeforeEach
    void cleanup(){
        productLimitsDefRepository.findAll()
                .forEach(productLimitsDef -> productLimitsDefRepository.delete(productLimitsDef));
    }


    @Test
    void createNewProductLimitsDef(){

        ProductLimitDefDTO productLimitDefDTO = createProductLimitDTOForAdd();

        ProductLimitDefDTO productLimitDefDTO1 = productLimitService.createNewProductLimitsDef(productLimitDefDTO).block();


        ProductLimitsDef productLimitsDef = productLimitsDefRepository
                .findById(new ProductId(productLimitDefDTO.getOrg(),productLimitDefDTO.getProduct())).get();

        List<PeriodicCardAmount> periodicSingleList = productLimitsDef.getCardLimitMap().get(PeriodicType.SINGLE);
        List<PeriodicCardAmount> periodicDailyList = productLimitsDef.getCardLimitMap().get(PeriodicType.DAILY);
        List<PeriodicCardAmount> periodicMonthlyList = productLimitsDef.getCardLimitMap().get(PeriodicType.MONTHLY);
        int totalSize = periodicDailyList.size() + periodicMonthlyList.size() + periodicSingleList.size();

        Map<LimitType,Long> limitDailyAmountMap = new HashMap<>();
        Map<LimitType,Integer> limitDailyIntegerMap = new HashMap<>();

        Map<LimitType,Long> limitSingleAmountMap = new HashMap<>();
        Map<LimitType,Integer> limitSingleIntegerMap = new HashMap<>();

        Map<LimitType,Long> limitMonthlyAmountMap = new HashMap<>();
        Map<LimitType,Integer> limitMonthlyIntegerMap = new HashMap<>();

        populateLimitTypeAmount(limitSingleAmountMap,limitSingleIntegerMap,periodicSingleList);
        populateLimitTypeAmount(limitDailyAmountMap,limitDailyIntegerMap,periodicDailyList);
        populateLimitTypeAmount(limitMonthlyAmountMap,limitMonthlyIntegerMap,periodicMonthlyList);



        assertAll(
                ()-> assertEquals(productLimitDefDTO.getOrg(),productLimitsDef.getProductId().getOrg()),
                ()-> assertEquals(productLimitDefDTO.getProduct(),productLimitsDef.getProductId().getProduct()),
                ()-> assertEquals(productLimitDefDTO.getPeriodicLimitDTOList().size(),totalSize),
                ()-> assertEquals(1000,limitSingleIntegerMap.get(LimitType.NO_SPECIFIC)),
                ()-> assertEquals(500000L,limitSingleAmountMap.get(LimitType.NO_SPECIFIC)),
                ()-> assertEquals(4000,limitSingleIntegerMap.get(LimitType.OTC)),
                ()-> assertEquals(900000L,limitSingleAmountMap.get(LimitType.OTC)),
                ()-> assertEquals(5000,limitDailyIntegerMap.get(LimitType.NO_SPECIFIC)),
                ()-> assertEquals(1000000L,limitDailyAmountMap.get(LimitType.NO_SPECIFIC)),
                ()-> assertEquals(7000, limitDailyIntegerMap.get(LimitType.RETAIL)),
                ()-> assertEquals(1300000L,limitDailyAmountMap.get(LimitType.OTC)),
                ()-> assertEquals(1500000L, limitMonthlyAmountMap.get(LimitType.RETAIL)),
                ()-> assertEquals(11000,limitMonthlyIntegerMap.get(LimitType.CASH))
        );


    }

    @Test
    void updateProductDef(){


        ProductLimitDefDTO productLimitDefDTO = createProductLimitDTOForAdd();

        productLimitService.createNewProductLimitsDef(productLimitDefDTO).block();


        ProductLimitDefUpdateDTO productLimitDefUpdateDTO = createProductLimitDTOForUpdate(true,false);
        productLimitDefUpdateDTO.setOrg(productLimitDefDTO.getOrg());
        productLimitDefUpdateDTO.setProduct(productLimitDefDTO.getProduct());
        productLimitService.updateProductDef(productLimitDefUpdateDTO).block();


        ProductLimitsDef productLimitsDef1 = productLimitsDefRepository
                .findById(new ProductId(productLimitDefDTO.getOrg(),productLimitDefDTO.getProduct())).get();


        List<PeriodicCardAmount> periodicCardAmountListSingle = productLimitsDef1.getCardLimitMap().get(PeriodicType.SINGLE);
        List<PeriodicCardAmount> periodicCardAmountListDaily = productLimitsDef1.getCardLimitMap().get(PeriodicType.DAILY);
        List<PeriodicCardAmount> periodicCardAmountListMonthly = productLimitsDef1.getCardLimitMap().get(PeriodicType.MONTHLY);
        List<PeriodicCardAmount> periodicCardAmountListYearly = productLimitsDef1.getCardLimitMap().get(PeriodicType.YEARLY);

        PeriodicCardAmount periodicCardAmountSingleNoSpecific = extractPeriodCardAmount(periodicCardAmountListSingle,LimitType.NO_SPECIFIC);
        PeriodicCardAmount periodicCardAmountSingleQuasiCash = extractPeriodCardAmount(periodicCardAmountListSingle,LimitType.QUASI_CASH);
        PeriodicCardAmount periodicCardAmountSingleATM = extractPeriodCardAmount(periodicCardAmountListSingle,LimitType.ATM);
        PeriodicCardAmount periodicCardAmountSingleRetail = extractPeriodCardAmount(periodicCardAmountListSingle,LimitType.RETAIL);
        PeriodicCardAmount periodicCardAmountSingleOTC = extractPeriodCardAmount(periodicCardAmountListSingle,LimitType.OTC);

        PeriodicCardAmount periodicCardAmountYearNoSpecific = extractPeriodCardAmount(periodicCardAmountListYearly,LimitType.NO_SPECIFIC);
        PeriodicCardAmount periodicCardAmountYearCash = extractPeriodCardAmount(periodicCardAmountListYearly,LimitType.CASH);
        PeriodicCardAmount periodicCardAmountYearRetail = extractPeriodCardAmount(periodicCardAmountListYearly,LimitType.RETAIL);

        PeriodicCardAmount periodicCardAmountMonthOTC = extractPeriodCardAmount(periodicCardAmountListMonthly,LimitType.OTC);







        assertAll(
                ()-> assertEquals(5, periodicCardAmountListSingle.size()),
                ()-> assertEquals(4,periodicCardAmountListDaily.size()),
                ()-> assertEquals(4,periodicCardAmountListMonthly.size()),
                ()-> assertEquals(3,periodicCardAmountListYearly.size()),
                ()-> assertEquals(12000,periodicCardAmountSingleNoSpecific.getTransactionNumber()),
                ()-> assertEquals(1500000L,periodicCardAmountSingleNoSpecific.getTransactionAmount()),
                ()-> assertEquals(13000, periodicCardAmountSingleQuasiCash.getTransactionNumber()),
                ()-> assertEquals(600000L,periodicCardAmountSingleQuasiCash.getTransactionAmount()),
                ()-> assertEquals(2000,periodicCardAmountSingleATM.getTransactionNumber()),
                ()->assertEquals(600000L,periodicCardAmountSingleATM.getTransactionAmount()),
                ()-> assertEquals(3000,periodicCardAmountSingleRetail.getTransactionNumber()),
                ()-> assertEquals(800000L,periodicCardAmountSingleRetail.getTransactionAmount()),
                ()-> assertEquals(4000,periodicCardAmountSingleOTC.getTransactionNumber()),
                ()-> assertEquals(900000L,periodicCardAmountSingleOTC.getTransactionAmount()),
                ()-> assertEquals(9000,periodicCardAmountYearNoSpecific.getTransactionNumber()),
                ()-> assertEquals(1400000L,periodicCardAmountYearNoSpecific.getTransactionAmount()),
                ()-> assertEquals(10000,periodicCardAmountYearRetail.getTransactionNumber()),
                ()-> assertEquals(1500000L,periodicCardAmountYearRetail.getTransactionAmount()),
                ()-> assertEquals(11000,periodicCardAmountYearCash.getTransactionNumber()),
                ()-> assertEquals(1600000L,periodicCardAmountYearCash.getTransactionAmount()),
                ()-> assertEquals(1700000L, periodicCardAmountMonthOTC.getTransactionAmount())
        );


    }

    @Test
    void fetchProductInfo(){

        ProductLimitDefDTO productLimitDefDTO = createProductLimitDTOForAdd();

        productLimitService.createNewProductLimitsDef(productLimitDefDTO).block();

        Mono<ProductLimitDefDTO> limitDTOMono = productLimitService.fetchProductInfo(productLimitDefDTO.getOrg(), productLimitDefDTO.getProduct());

        StepVerifier.create(limitDTOMono)
                .expectNextCount(1)
                .verifyComplete();

    }

    @Test
    void fetchProductInfo1(){

        ProductLimitDefDTO productLimitDefDTO = createProductLimitDTOForAdd();

        productLimitService.createNewProductLimitsDef(productLimitDefDTO).block();

        Mono<ProductLimitDefDTO> limitDTOMono = productLimitService.fetchProductInfo(2, 3);

        StepVerifier.create(limitDTOMono)
                .expectError(NotFoundException.class)
                .verify();

    }

    @Test
    void deleteProduct(){

        ProductLimitDefDTO productLimitDefDTO = createProductLimitDTOForAdd();

        productLimitService.createNewProductLimitsDef(productLimitDefDTO).block();

        productLimitService.deleteProduct(productLimitDefDTO.getOrg(), productLimitDefDTO.getProduct()).block();

        Optional<ProductLimitsDef> productLimitsDefOptional = productLimitsDefRepository
                .findById(new ProductId(productLimitDefDTO.getOrg(), productLimitDefDTO.getProduct()));

        assertTrue(productLimitsDefOptional.isEmpty());


    }

    @Test
    void deleteProduct1(){

        ProductLimitDefDTO productLimitDefDTO = createProductLimitDTOForAdd();

        productLimitService.createNewProductLimitsDef(productLimitDefDTO).block();

        Mono<ProductLimitDefDTO> productLimitDefDTOMono = productLimitService.deleteProduct(1,3);

        StepVerifier.create(productLimitDefDTOMono)
                .expectError(NotFoundException.class)
                .verify();

    }

    @Test
    void findAllProducts(){

        ProductLimitDefDTO productLimitDefDTO = createProductLimitDTOForAdd();
        productLimitService.createNewProductLimitsDef(productLimitDefDTO).block();

        productLimitDefDTO.setOrg(1);
        productLimitDefDTO.setProduct(301);
        productLimitService.createNewProductLimitsDef(productLimitDefDTO).block();

        productLimitDefDTO.setOrg(501);
        productLimitDefDTO.setProduct(307);
        productLimitService.createNewProductLimitsDef(productLimitDefDTO).block();

        StepVerifier.create(productLimitService.findAllProducts())
                .expectNextCount(3)
                .verifyComplete();

    }


    @Test
    void convertDTOToProductLimitsDef() {

        ProductLimitDefDTO productLimitDefDTO = createProductLimitDTOForAdd();

        ProductLimitsDef productLimitsDef = productLimitService.convertDTOToProductLimitsDef(productLimitDefDTO);

        List<PeriodicCardAmount> periodicSingleList = productLimitsDef.getCardLimitMap().get(PeriodicType.SINGLE);
        List<PeriodicCardAmount> periodicDailyList = productLimitsDef.getCardLimitMap().get(PeriodicType.DAILY);
        List<PeriodicCardAmount> periodicMonthlyList = productLimitsDef.getCardLimitMap().get(PeriodicType.MONTHLY);
        int totalSize = periodicDailyList.size() + periodicMonthlyList.size() + periodicSingleList.size();

        Map<LimitType,Long> limitDailyAmountMap = new HashMap<>();
        Map<LimitType,Integer> limitDailyIntegerMap = new HashMap<>();

        Map<LimitType,Long> limitSingleAmountMap = new HashMap<>();
        Map<LimitType,Integer> limitSingleIntegerMap = new HashMap<>();

        Map<LimitType,Long> limitMonthlyAmountMap = new HashMap<>();
        Map<LimitType,Integer> limitMonthlyIntegerMap = new HashMap<>();

        populateLimitTypeAmount(limitSingleAmountMap,limitSingleIntegerMap,periodicSingleList);
        populateLimitTypeAmount(limitDailyAmountMap,limitDailyIntegerMap,periodicDailyList);
        populateLimitTypeAmount(limitMonthlyAmountMap,limitMonthlyIntegerMap,periodicMonthlyList);



        assertAll(
                ()-> assertEquals(productLimitDefDTO.getOrg(),productLimitsDef.getProductId().getOrg()),
                ()-> assertEquals(productLimitDefDTO.getProduct(),productLimitsDef.getProductId().getProduct()),
                ()-> assertEquals(productLimitDefDTO.getPeriodicLimitDTOList().size(),totalSize),
                ()-> assertEquals(1000,limitSingleIntegerMap.get(LimitType.NO_SPECIFIC)),
                ()-> assertEquals(500000L,limitSingleAmountMap.get(LimitType.NO_SPECIFIC)),
                ()-> assertEquals(4000,limitSingleIntegerMap.get(LimitType.OTC)),
                ()-> assertEquals(900000L,limitSingleAmountMap.get(LimitType.OTC)),
                ()-> assertEquals(5000,limitDailyIntegerMap.get(LimitType.NO_SPECIFIC)),
                ()-> assertEquals(1000000L,limitDailyAmountMap.get(LimitType.NO_SPECIFIC)),
                ()-> assertEquals(7000, limitDailyIntegerMap.get(LimitType.RETAIL)),
                ()-> assertEquals(1300000L,limitDailyAmountMap.get(LimitType.OTC)),
                ()-> assertEquals(1500000L, limitMonthlyAmountMap.get(LimitType.RETAIL)),
                ()-> assertEquals(11000,limitMonthlyIntegerMap.get(LimitType.CASH))
        );


    }

    private void populateLimitTypeAmount(Map<LimitType, Long> limitTypeAmountMap, Map<LimitType, Integer> limitTypeIntegerMap,
                                         List<PeriodicCardAmount> periodicDailyList) {

        periodicDailyList.forEach(periodicCardAmount -> {
            limitTypeAmountMap.put(periodicCardAmount.getLimitType(),periodicCardAmount.getTransactionAmount());
            limitTypeIntegerMap.put(periodicCardAmount.getLimitType(),periodicCardAmount.getTransactionNumber());
        });
    }

    @Test
    void convertToDTO() {

        ProductLimitsDef productLimitsDef = createProductLimitsDef();
        ProductLimitDefDTO productLimitDefDTO = productLimitService.convertToDTO(productLimitsDef);

        List<PeriodicLimitDTO> periodicLimitDTOList = productLimitDefDTO.getPeriodicLimitDTOList();

        PeriodicLimitDTO periodicLimitDTOSingleNoSpecfic = extractPeriodicLimitDTO(periodicLimitDTOList,PeriodicType.SINGLE,LimitType.NO_SPECIFIC);
        PeriodicLimitDTO periodicLimitDTOSingleATM = extractPeriodicLimitDTO(periodicLimitDTOList,PeriodicType.SINGLE,LimitType.ATM);
        PeriodicLimitDTO periodicLimitDTOSingleQuasiCash = extractPeriodicLimitDTO(periodicLimitDTOList,PeriodicType.SINGLE,LimitType.QUASI_CASH);
        PeriodicLimitDTO periodicLimitDTODailyOTC = extractPeriodicLimitDTO(periodicLimitDTOList,PeriodicType.DAILY,LimitType.OTC);
        PeriodicLimitDTO periodicLimitDTODailyQuasiCash = extractPeriodicLimitDTO(periodicLimitDTOList,PeriodicType.DAILY,LimitType.QUASI_CASH);
        assertAll(
                ()-> assertEquals(productLimitsDef.getProductId().getOrg(),productLimitDefDTO.getOrg()),
                ()-> assertEquals(productLimitsDef.getProductId().getProduct(),productLimitDefDTO.getProduct()),
                ()-> assertEquals(5,productLimitDefDTO.getPeriodicLimitDTOList().size()),
                ()-> assertEquals(Util.getPeriodicType(PeriodicType.SINGLE),periodicLimitDTOSingleATM.getPeriodicType()),
                ()-> assertEquals(Util.getPeriodicType(PeriodicType.SINGLE),periodicLimitDTOSingleNoSpecfic.getPeriodicType()),
                ()-> assertEquals(Util.getPeriodicType(PeriodicType.SINGLE),periodicLimitDTOSingleQuasiCash.getPeriodicType()),
                ()-> assertEquals(Util.getPeriodicType(PeriodicType.DAILY),periodicLimitDTODailyOTC.getPeriodicType()),
                ()-> assertEquals(Util.getPeriodicType(PeriodicType.DAILY),periodicLimitDTODailyQuasiCash.getPeriodicType()),
                ()-> assertEquals(Util.getLimitType(LimitType.ATM),periodicLimitDTOSingleATM.getLimitType()),
                ()-> assertEquals(Util.getLimitType(LimitType.NO_SPECIFIC),periodicLimitDTOSingleNoSpecfic.getLimitType()),
                ()-> assertEquals(Util.getLimitType(LimitType.QUASI_CASH),periodicLimitDTOSingleQuasiCash.getLimitType()),
                ()-> assertEquals(Util.getLimitType(LimitType.OTC),periodicLimitDTODailyOTC.getLimitType()),
                ()-> assertEquals(Util.getLimitType(LimitType.QUASI_CASH),periodicLimitDTODailyQuasiCash.getLimitType()),
                ()-> assertEquals(100000L,periodicLimitDTOSingleNoSpecfic.getLimitAmount()),
                ()-> assertEquals(10,periodicLimitDTOSingleNoSpecfic.getLimitNumber()),
                ()-> assertEquals(200000L,periodicLimitDTOSingleATM.getLimitAmount()),
                ()-> assertEquals(20,periodicLimitDTOSingleATM.getLimitNumber()),
                ()-> assertEquals(300000L,periodicLimitDTOSingleQuasiCash.getLimitAmount()),
                ()-> assertEquals(30,periodicLimitDTOSingleQuasiCash.getLimitNumber()),
                ()-> assertEquals(400000L,periodicLimitDTODailyOTC.getLimitAmount()),
                ()-> assertEquals(40,periodicLimitDTODailyOTC.getLimitNumber()),
                ()-> assertEquals(500000L,periodicLimitDTODailyQuasiCash.getLimitAmount()),
                ()-> assertEquals(50,periodicLimitDTODailyQuasiCash.getLimitNumber())

                );

    }

    private PeriodicLimitDTO extractPeriodicLimitDTO(List<PeriodicLimitDTO> periodicLimitDTOList, PeriodicType periodicType,LimitType limitType){

        return periodicLimitDTOList
                .stream()
                .filter(periodicLimitDTO -> periodicLimitDTO.getLimitType().equals(Util.getLimitType(limitType)) &&
                        periodicLimitDTO.getPeriodicType().equals(Util.getPeriodicType(periodicType)))
                .findFirst().get();
    }

    @Test
    void updateProductLimitDef() {

        ProductLimitDefDTO productLimitDefDTO = createProductLimitDTOForAdd();
        ProductLimitDefUpdateDTO productLimitDefUpdateDTO = createProductLimitDTOForUpdate(true,false);

        ProductLimitsDef productLimitsDef = productLimitService.convertDTOToProductLimitsDef(productLimitDefDTO);
        ProductLimitsDef productLimitsDef1 = productLimitService.convertDTOToProductLimitsDef(productLimitDefDTO);

        productLimitService.updateProductLimitDef(productLimitDefUpdateDTO,productLimitsDef1);

        List<PeriodicCardAmount> periodicCardAmountListSingle = productLimitsDef1.getCardLimitMap().get(PeriodicType.SINGLE);
        List<PeriodicCardAmount> periodicCardAmountListDaily = productLimitsDef1.getCardLimitMap().get(PeriodicType.DAILY);
        List<PeriodicCardAmount> periodicCardAmountListMonthly = productLimitsDef1.getCardLimitMap().get(PeriodicType.MONTHLY);
        List<PeriodicCardAmount> periodicCardAmountListYearly = productLimitsDef1.getCardLimitMap().get(PeriodicType.YEARLY);

        PeriodicCardAmount periodicCardAmountSingleNoSpecific = extractPeriodCardAmount(periodicCardAmountListSingle,LimitType.NO_SPECIFIC);
        PeriodicCardAmount periodicCardAmountSingleQuasiCash = extractPeriodCardAmount(periodicCardAmountListSingle,LimitType.QUASI_CASH);
        PeriodicCardAmount periodicCardAmountSingleATM = extractPeriodCardAmount(periodicCardAmountListSingle,LimitType.ATM);
        PeriodicCardAmount periodicCardAmountSingleRetail = extractPeriodCardAmount(periodicCardAmountListSingle,LimitType.RETAIL);
        PeriodicCardAmount periodicCardAmountSingleOTC = extractPeriodCardAmount(periodicCardAmountListSingle,LimitType.OTC);

        PeriodicCardAmount periodicCardAmountYearNoSpecific = extractPeriodCardAmount(periodicCardAmountListYearly,LimitType.NO_SPECIFIC);
        PeriodicCardAmount periodicCardAmountYearCash = extractPeriodCardAmount(periodicCardAmountListYearly,LimitType.CASH);
        PeriodicCardAmount periodicCardAmountYearRetail = extractPeriodCardAmount(periodicCardAmountListYearly,LimitType.RETAIL);

        PeriodicCardAmount periodicCardAmountMonthOTC = extractPeriodCardAmount(periodicCardAmountListMonthly,LimitType.OTC);







        assertAll(
                ()-> assertEquals(productLimitsDef.getProductId().getOrg(),productLimitsDef1.getProductId().getOrg()),
                ()-> assertEquals(productLimitsDef.getProductId().getProduct(),productLimitsDef1.getProductId().getProduct()),
                ()-> assertEquals(5, periodicCardAmountListSingle.size()),
                ()-> assertEquals(4,periodicCardAmountListDaily.size()),
                ()-> assertEquals(4,periodicCardAmountListMonthly.size()),
                ()-> assertEquals(3,periodicCardAmountListYearly.size()),
                ()-> assertEquals(12000,periodicCardAmountSingleNoSpecific.getTransactionNumber()),
                ()-> assertEquals(1500000L,periodicCardAmountSingleNoSpecific.getTransactionAmount()),
                ()-> assertEquals(13000, periodicCardAmountSingleQuasiCash.getTransactionNumber()),
                ()-> assertEquals(600000L,periodicCardAmountSingleQuasiCash.getTransactionAmount()),
                ()-> assertEquals(2000,periodicCardAmountSingleATM.getTransactionNumber()),
                ()->assertEquals(600000L,periodicCardAmountSingleATM.getTransactionAmount()),
                ()-> assertEquals(3000,periodicCardAmountSingleRetail.getTransactionNumber()),
                ()-> assertEquals(800000L,periodicCardAmountSingleRetail.getTransactionAmount()),
                ()-> assertEquals(4000,periodicCardAmountSingleOTC.getTransactionNumber()),
                ()-> assertEquals(900000L,periodicCardAmountSingleOTC.getTransactionAmount()),
                ()-> assertEquals(9000,periodicCardAmountYearNoSpecific.getTransactionNumber()),
                ()-> assertEquals(1400000L,periodicCardAmountYearNoSpecific.getTransactionAmount()),
                ()-> assertEquals(10000,periodicCardAmountYearRetail.getTransactionNumber()),
                ()-> assertEquals(1500000L,periodicCardAmountYearRetail.getTransactionAmount()),
                ()-> assertEquals(11000,periodicCardAmountYearCash.getTransactionNumber()),
                ()-> assertEquals(1600000L,periodicCardAmountYearCash.getTransactionAmount()),
                ()-> assertEquals(1700000L, periodicCardAmountMonthOTC.getTransactionAmount())

        );


    }

    @Test
    void updateProductLimitDef1() {

        ProductLimitDefDTO productLimitDefDTO = createProductLimitDTOForAdd();
        ProductLimitDefUpdateDTO productLimitDefUpdateDTO = createProductLimitDTOForUpdate(false,true);

        ProductLimitsDef productLimitsDef = productLimitService.convertDTOToProductLimitsDef(productLimitDefDTO);
        ProductLimitsDef productLimitsDef1 = productLimitService.convertDTOToProductLimitsDef(productLimitDefDTO);

        productLimitService.updateProductLimitDef(productLimitDefUpdateDTO,productLimitsDef1);

        List<PeriodicCardAmount> periodicCardAmountListSingle = productLimitsDef1.getCardLimitMap().get(PeriodicType.SINGLE);
        List<PeriodicCardAmount> periodicCardAmountListDaily = productLimitsDef1.getCardLimitMap().get(PeriodicType.DAILY);
        List<PeriodicCardAmount> periodicCardAmountListMonthly = productLimitsDef1.getCardLimitMap().get(PeriodicType.MONTHLY);
        List<PeriodicCardAmount> periodicCardAmountListYearly = productLimitsDef1.getCardLimitMap().get(PeriodicType.YEARLY);






        assertAll(
                ()-> assertEquals(productLimitsDef.getProductId().getOrg(),productLimitsDef1.getProductId().getOrg()),
                ()-> assertEquals(productLimitsDef.getProductId().getProduct(),productLimitsDef1.getProductId().getProduct()),
                ()-> assertEquals(2, periodicCardAmountListSingle.size()),
                ()-> assertEquals(4,periodicCardAmountListDaily.size()),
                ()-> assertEquals(4,periodicCardAmountListMonthly.size()),
                ()-> assertNull(periodicCardAmountListYearly)

        );

    }

    @Test
    void updateProductLimitDef2() {

        ProductLimitDefDTO productLimitDefDTO = createProductLimitDTOForAdd();
        ProductLimitDefUpdateDTO productLimitDefUpdateDTO = createProductLimitDTOForUpdate(true,true);

        ProductLimitsDef productLimitsDef = productLimitService.convertDTOToProductLimitsDef(productLimitDefDTO);
        ProductLimitsDef productLimitsDef1 = productLimitService.convertDTOToProductLimitsDef(productLimitDefDTO);

        productLimitService.updateProductLimitDef(productLimitDefUpdateDTO,productLimitsDef1);

        List<PeriodicCardAmount> periodicCardAmountListSingle = productLimitsDef1.getCardLimitMap().get(PeriodicType.SINGLE);
        List<PeriodicCardAmount> periodicCardAmountListDaily = productLimitsDef1.getCardLimitMap().get(PeriodicType.DAILY);
        List<PeriodicCardAmount> periodicCardAmountListMonthly = productLimitsDef1.getCardLimitMap().get(PeriodicType.MONTHLY);
        List<PeriodicCardAmount> periodicCardAmountListYearly = productLimitsDef1.getCardLimitMap().get(PeriodicType.YEARLY);






        assertAll(
                ()-> assertEquals(productLimitsDef.getProductId().getOrg(),productLimitsDef1.getProductId().getOrg()),
                ()-> assertEquals(productLimitsDef.getProductId().getProduct(),productLimitsDef1.getProductId().getProduct()),
                ()-> assertEquals(3, periodicCardAmountListSingle.size()),
                ()-> assertEquals(4,periodicCardAmountListDaily.size()),
                ()-> assertEquals(4,periodicCardAmountListMonthly.size()),
                ()-> assertEquals(3,periodicCardAmountListYearly.size())

        );


    }

    private PeriodicCardAmount extractPeriodCardAmount(List<PeriodicCardAmount> periodicCardAmountList, LimitType limitType){

        return periodicCardAmountList.stream()
                .filter(periodicCardAmount -> periodicCardAmount.getLimitType().equals(limitType))
                .findFirst()
                .get();


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
}