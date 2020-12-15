package in.nmaloth.maintenance.service.product;

import in.nmaloth.entity.account.AccountType;
import in.nmaloth.entity.account.BalanceTypes;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.entity.product.ProductId;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.product.LimitPercentDTO;
import in.nmaloth.maintenance.model.dto.product.ProductDefDTO;
import in.nmaloth.maintenance.model.dto.product.ProductDefUpdateDTO;
import in.nmaloth.maintenance.repository.product.ProductDefRepository;
import in.nmaloth.maintenance.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductServicesImplTest {

    @Autowired
    private ProductServices productServices;


    @Autowired
    private ProductDefRepository productDefRepository;


    @BeforeEach
    void cleanDeclineReason(){

        productDefRepository.findAll()
                .forEach(declineReasonDef -> productDefRepository.delete(declineReasonDef));
    }



    @Test
    void createNewProductDef(){

        ProductDefDTO productDefDTO = createProductDefDto();
        LimitPercentDTO[] limitPercentDTOArray = productDefDTO.getLimitPercents()
                .toArray(new LimitPercentDTO[0]);

        Arrays.sort(limitPercentDTOArray);

        ProductDefDTO productDefDTO1 = productServices.createNewProductDef(productDefDTO).block();

        ProductDef productDef = productDefRepository.findById(new ProductId(productDefDTO.getOrg(),productDefDTO.getProduct())).get();

        LimitPercentDTO[] limitPercentDTOArray1 = productDef.getLimitPercents()
                .entrySet()
                .stream()
                .map(balanceTypesLongEntry -> LimitPercentDTO.builder()
                        .balanceTypes(Util.getBalanceTypes(balanceTypesLongEntry.getKey()))
                        .percent(balanceTypesLongEntry.getValue())
                        .build()
                )
                .toArray(LimitPercentDTO[]::new);

        Arrays.sort(limitPercentDTOArray1);


        assertAll(
                ()-> assertEquals(productDefDTO.getOrg(),productDef.getProductId().getOrg()),
                ()-> assertEquals(productDefDTO.getProduct(),productDef.getProductId().getProduct()),
                ()-> assertEquals(productDefDTO.getCardsValidityMonthNew(),productDef.getCardsValidityMonthNew()),
                ()-> assertEquals(productDefDTO.getCardsValidityMonthReplace(),productDef.getCardsValidityMonthReplace()),
                ()-> assertEquals(productDefDTO.getCardsValidityMonthReIssue(),productDef.getCardsValidityMonthReIssue()),
                ()-> assertEquals(productDefDTO.getDateRangeNewExpDate(),productDef.getDateRangeNewExpDate()),
                ()-> assertEquals(productDefDTO.getCardsWaiverActivationDays(),productDef.getCardsWaiverActivationDays()),
                ()-> assertEquals(productDefDTO.getDaysToCardsValid(),productDef.getDaysToCardsValid()),
                ()-> assertEquals(productDefDTO.getCardsActivationRequired(),productDef.getCardsActivationRequired()),
                ()-> assertEquals(productDefDTO.getServiceCode(),productDef.getServiceCode()),
                ()-> assertEquals(productDefDTO.getBillingCurrencyCode(),productDef.getBillingCurrencyCode()),
                ()-> assertEquals(productDefDTO.getPrimaryAccountType(),Util.getAccountType(productDef.getPrimaryAccountType())),

                ()-> assertArrayEquals(limitPercentDTOArray,limitPercentDTOArray1)
        );

    }

    @Test
    void updateProductDef(){


        ProductDefDTO productDefDTO2 = createProductDefDto();
        productServices.createNewProductDef(productDefDTO2).block();


        ProductDefUpdateDTO productDefDTO = createProductDefUpdateDTO(true,null);

        productDefDTO.setOrg(productDefDTO2.getOrg());
        productDefDTO.setProduct(productDefDTO.getProduct());

        ProductDefDTO productDefDTO1 = productServices.updateProductDef(productDefDTO).block();

        ProductDef productDef = productDefRepository.findById(new ProductId(productDefDTO.getOrg(),productDefDTO.getProduct())).get();

        Long cashBalancePercent = productDef.getLimitPercents().get(BalanceTypes.CASH_BALANCE);
        Long internationalCashPercent = productDef.getLimitPercents().get(BalanceTypes.INTERNATIONAL_CASH);
        Long internationalCashInstPercent = productDef.getLimitPercents().get(BalanceTypes.INTERNATIONAL_CASH_INSTALLMENT);
        Long instBalPercent = productDef.getLimitPercents().get(BalanceTypes.INSTALLMENT_BALANCE);


        assertAll(
                ()-> assertEquals(productDefDTO.getCardsValidityMonthNew(),productDef.getCardsValidityMonthNew()),
                ()-> assertEquals(productDefDTO.getCardsValidityMonthReplace(),productDef.getCardsValidityMonthReplace()),
                ()-> assertEquals(productDefDTO.getCardsValidityMonthReIssue(),productDef.getCardsValidityMonthReIssue()),
                ()-> assertEquals(productDefDTO.getDateRangeNewExpDate(),productDef.getDateRangeNewExpDate()),
                ()-> assertEquals(productDefDTO.getCardsWaiverActivationDays(),productDef.getCardsWaiverActivationDays()),
                ()-> assertEquals(productDefDTO.getDaysToCardsValid(),productDef.getDaysToCardsValid()),
                ()-> assertEquals(productDefDTO.getCardsActivationRequired(),productDef.getCardsActivationRequired()),
                ()-> assertEquals(productDefDTO.getServiceCode(),productDef.getServiceCode()),
                ()-> assertEquals(500000L,cashBalancePercent),
                ()-> assertEquals(500000L,internationalCashPercent),
                ()-> assertEquals(600000L,instBalPercent),
                ()-> assertNull(internationalCashInstPercent)

        );

    }

    @Test
    void updateProductDef1(){

        ProductDefUpdateDTO productDefDTO = createProductDefUpdateDTO(true,null);

        Mono<ProductDefDTO> productDefDTOMono = productServices.updateProductDef(productDefDTO);

        StepVerifier
                .create(productDefDTOMono)
                .expectError(NotFoundException.class)
                .verify();

    }

    @Test
    void fetchProductInfo(){

        ProductDefDTO productDefDTO2 = createProductDefDto();
        productServices.createNewProductDef(productDefDTO2).block();

        ProductDefDTO productDefDTO = productServices.fetchProductInfo(productDefDTO2.getOrg(), productDefDTO2.getProduct()).block();

        LimitPercentDTO[] limitPercentDTOArray = productDefDTO.getLimitPercents()
                .toArray(new LimitPercentDTO[0]);

        Arrays.sort(limitPercentDTOArray);

        ProductDef productDef = productDefRepository.findById(new ProductId(productDefDTO2.getOrg(),productDefDTO2.getProduct())).get();

        LimitPercentDTO[] limitPercentDTOArray1 = productDef.getLimitPercents()
                .entrySet()
                .stream()
                .map(balanceTypesLongEntry -> LimitPercentDTO.builder()
                        .balanceTypes(Util.getBalanceTypes(balanceTypesLongEntry.getKey()))
                        .percent(balanceTypesLongEntry.getValue())
                        .build()
                )
                .toArray(LimitPercentDTO[]::new);

        Arrays.sort(limitPercentDTOArray1);


        assertAll(
                ()-> assertEquals(productDefDTO.getOrg(),productDef.getProductId().getOrg()),
                ()-> assertEquals(productDefDTO.getProduct(),productDef.getProductId().getProduct()),
                ()-> assertEquals(productDefDTO.getCardsValidityMonthNew(),productDef.getCardsValidityMonthNew()),
                ()-> assertEquals(productDefDTO.getCardsValidityMonthReplace(),productDef.getCardsValidityMonthReplace()),
                ()-> assertEquals(productDefDTO.getCardsValidityMonthReIssue(),productDef.getCardsValidityMonthReIssue()),
                ()-> assertEquals(productDefDTO.getDateRangeNewExpDate(),productDef.getDateRangeNewExpDate()),
                ()-> assertEquals(productDefDTO.getCardsWaiverActivationDays(),productDef.getCardsWaiverActivationDays()),
                ()-> assertEquals(productDefDTO.getDaysToCardsValid(),productDef.getDaysToCardsValid()),
                ()-> assertEquals(productDefDTO.getCardsActivationRequired(),productDef.getCardsActivationRequired()),
                ()-> assertEquals(productDefDTO.getServiceCode(),productDef.getServiceCode()),

                ()-> assertArrayEquals(limitPercentDTOArray,limitPercentDTOArray1)
        );



    }


    @Test
    void fetchProductInfo1(){

        Mono<ProductDefDTO> productDefDTOMono = productServices.fetchProductInfo(1, 1);
        StepVerifier
                .create(productDefDTOMono)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void deleteProductDef(){

        ProductDefDTO productDefDTO2 = createProductDefDto();
        productServices.createNewProductDef(productDefDTO2).block();

        ProductDefDTO productDefDTO = productServices.deleteProduct(productDefDTO2.getOrg(), productDefDTO2.getProduct()).block();

        Optional<ProductDef> productDefOptional = productDefRepository.findById(new ProductId(productDefDTO2.getOrg(), productDefDTO2.getProduct()));

        assertTrue(productDefOptional.isEmpty());
    }


    @Test
    void  findAllProducts(){

        ProductDefDTO productDefDTO1 = createProductDefDto();
        productServices.createNewProductDef(productDefDTO1).block();
        productDefDTO1.setOrg(201);
        productDefDTO1.setProduct(202);
        productServices.createNewProductDef(productDefDTO1).block();
        productDefDTO1.setOrg(301);
        productDefDTO1.setProduct(302);
        productServices.createNewProductDef(productDefDTO1).block();

        Flux<ProductDefDTO> productDefDTOFlux = productServices.findAllProducts();

        StepVerifier
                .create(productDefDTOFlux)
                .expectNextCount(3)
                .verifyComplete();

    }



    @Test
    void convertDtoToProduct() {

        ProductDefDTO productDefDTO = createProductDefDto();
        LimitPercentDTO[] limitPercentDTOArray = productDefDTO.getLimitPercents()
            .toArray(new LimitPercentDTO[0]);

        Arrays.sort(limitPercentDTOArray);
        ;


        ProductDef productDef = productServices.convertDtoToProduct(productDefDTO);
        LimitPercentDTO[] limitPercentDTOArray1 = productDef.getLimitPercents()
                .entrySet()
                .stream()
                .map(balanceTypesLongEntry -> LimitPercentDTO.builder()
                        .balanceTypes(Util.getBalanceTypes(balanceTypesLongEntry.getKey()))
                        .percent(balanceTypesLongEntry.getValue())
                        .build()
                )
                .toArray(LimitPercentDTO[]::new);

        Arrays.sort(limitPercentDTOArray1);

        assertAll(
                ()-> assertEquals(productDefDTO.getOrg(),productDef.getProductId().getOrg()),
                ()-> assertEquals(productDefDTO.getProduct(),productDef.getProductId().getProduct()),
                ()-> assertEquals(productDefDTO.getCardsValidityMonthNew(),productDef.getCardsValidityMonthNew()),
                ()-> assertEquals(productDefDTO.getCardsValidityMonthReplace(),productDef.getCardsValidityMonthReplace()),
                ()-> assertEquals(productDefDTO.getCardsValidityMonthReIssue(),productDef.getCardsValidityMonthReIssue()),
                ()-> assertEquals(productDefDTO.getDateRangeNewExpDate(),productDef.getDateRangeNewExpDate()),
                ()-> assertEquals(productDefDTO.getCardsWaiverActivationDays(),productDef.getCardsWaiverActivationDays()),
                ()-> assertEquals(productDefDTO.getDaysToCardsValid(),productDef.getDaysToCardsValid()),
                ()-> assertEquals(productDefDTO.getCardsActivationRequired(),productDef.getCardsActivationRequired()),
                ()-> assertEquals(productDefDTO.getServiceCode(),productDef.getServiceCode()),
                ()-> assertEquals(productDefDTO.getCardsReturn(),productDef.getCardsReturn()),
                ()-> assertEquals(productDefDTO.getBillingCurrencyCode(),productDef.getBillingCurrencyCode()),
                ()-> assertEquals(productDefDTO.getPrimaryAccountType(),Util.getAccountType(productDef.getPrimaryAccountType())),
                ()-> assertArrayEquals(limitPercentDTOArray,limitPercentDTOArray1)
        );

    }

    @Test
    void convertProductToDto() {

        ProductDef productDef = createProductDef();

        LimitPercentDTO[] limitPercentDTOArray1 = productDef.getLimitPercents()
                .entrySet()
                .stream()
                .map(balanceTypesLongEntry -> LimitPercentDTO.builder()
                        .balanceTypes(Util.getBalanceTypes(balanceTypesLongEntry.getKey()))
                        .percent(balanceTypesLongEntry.getValue())
                        .build()
                )
                .toArray(LimitPercentDTO[]::new);

        Arrays.sort(limitPercentDTOArray1);


        ProductDefDTO productDefDTO = productServices.convertProductToDto(productDef);
        LimitPercentDTO[] limitPercentDTOArray = productDefDTO.getLimitPercents()
                .toArray(new LimitPercentDTO[0]);

        Arrays.sort(limitPercentDTOArray);

        assertAll(
                ()-> assertEquals(productDefDTO.getOrg(),productDef.getProductId().getOrg()),
                ()-> assertEquals(productDefDTO.getProduct(),productDef.getProductId().getProduct()),
                ()-> assertEquals(productDefDTO.getCardsValidityMonthNew(),productDef.getCardsValidityMonthNew()),
                ()-> assertEquals(productDefDTO.getCardsValidityMonthReplace(),productDef.getCardsValidityMonthReplace()),
                ()-> assertEquals(productDefDTO.getCardsValidityMonthReIssue(),productDef.getCardsValidityMonthReIssue()),
                ()-> assertEquals(productDefDTO.getDateRangeNewExpDate(),productDef.getDateRangeNewExpDate()),
                ()-> assertEquals(productDefDTO.getCardsWaiverActivationDays(),productDef.getCardsWaiverActivationDays()),
                ()-> assertEquals(productDefDTO.getDaysToCardsValid(),productDef.getDaysToCardsValid()),
                ()-> assertEquals(productDefDTO.getCardsActivationRequired(),productDef.getCardsActivationRequired()),
                ()-> assertEquals(productDefDTO.getServiceCode(),productDef.getServiceCode()),
                ()-> assertEquals(productDefDTO.getCardsReturn(),productDef.getCardsReturn()),
                ()-> assertEquals(productDefDTO.getBillingCurrencyCode(),productDef.getBillingCurrencyCode()),
                ()-> assertEquals(productDefDTO.getPrimaryAccountType(),Util.getAccountType(productDef.getPrimaryAccountType())),
                ()-> assertArrayEquals(limitPercentDTOArray,limitPercentDTOArray1)
        );



    }

    @Test
    void updateProductDefFromDto() {

        ProductDef productDef1 = createProductDef();

        ProductDefUpdateDTO productDefDTO = createProductDefUpdateDTO(true,null);


        ProductDef productDef = productServices.updateProductDefFromDto(productDefDTO,productDef1);


        Long cashBalancePercent = productDef.getLimitPercents().get(BalanceTypes.CASH_BALANCE);
        Long internationalCashPercent = productDef.getLimitPercents().get(BalanceTypes.INTERNATIONAL_CASH);
        Long internationalCashInstPercent = productDef.getLimitPercents().get(BalanceTypes.INTERNATIONAL_CASH_INSTALLMENT);
        Long instBalPercent = productDef.getLimitPercents().get(BalanceTypes.INSTALLMENT_BALANCE);



        assertAll(
                ()-> assertEquals(productDefDTO.getCardsValidityMonthNew(),productDef.getCardsValidityMonthNew()),
                ()-> assertEquals(productDefDTO.getCardsValidityMonthReplace(),productDef.getCardsValidityMonthReplace()),
                ()-> assertEquals(productDefDTO.getCardsValidityMonthReIssue(),productDef.getCardsValidityMonthReIssue()),
                ()-> assertEquals(productDefDTO.getDateRangeNewExpDate(),productDef.getDateRangeNewExpDate()),
                ()-> assertEquals(productDefDTO.getCardsWaiverActivationDays(),productDef.getCardsWaiverActivationDays()),
                ()-> assertEquals(productDefDTO.getDaysToCardsValid(),productDef.getDaysToCardsValid()),
                ()-> assertEquals(productDefDTO.getCardsActivationRequired(),productDef.getCardsActivationRequired()),
                ()-> assertEquals(productDefDTO.getServiceCode(),productDef.getServiceCode()),
                ()-> assertEquals(productDefDTO.getCardsReturn(),productDef.getCardsReturn()),
                ()-> assertEquals(productDefDTO.getBillingCurrencyCode(),productDef.getBillingCurrencyCode()),
                ()-> assertEquals(productDefDTO.getPrimaryAccountType(),Util.getAccountType(productDef.getPrimaryAccountType())),
                ()-> assertEquals(2000000L,cashBalancePercent),
                ()-> assertEquals(500000L,internationalCashPercent),
                ()-> assertEquals(600000L,instBalPercent),
                ()-> assertNull(internationalCashInstPercent)

        );


    }

    @Test
    void updateProductDefFromDto1() {

        ProductDef productDef1 = createProductDef();

        Integer[] integers = {1,2,3,4,5,6,13,16};

        ProductDefUpdateDTO productDefDTO = createProductDefUpdateDTO(false,Arrays.asList(integers));


        ProductDef productDef = productServices.updateProductDefFromDto(productDefDTO,productDef1);


        Long cashBalancePercent = productDef.getLimitPercents().get(BalanceTypes.CASH_BALANCE);
        Long internationalCashPercent = productDef.getLimitPercents().get(BalanceTypes.INTERNATIONAL_CASH);
        Long internationalCashInstPercent = productDef.getLimitPercents().get(BalanceTypes.INTERNATIONAL_CASH_INSTALLMENT);
        Long instBalPercent = productDef.getLimitPercents().get(BalanceTypes.INSTALLMENT_BALANCE);



        assertAll(
                ()-> assertEquals(productDefDTO.getBillingCurrencyCode(),productDef.getBillingCurrencyCode()),
                ()-> assertEquals(productDefDTO.getPrimaryAccountType(),Util.getAccountType(productDef.getPrimaryAccountType())),
                ()-> assertEquals(productDefDTO.getServiceCode(),productDef.getServiceCode()),
                ()-> assertEquals(productDefDTO.getCardsReturn(),productDef.getCardsReturn()),
                ()-> assertEquals(2000000L,cashBalancePercent),
                ()-> assertEquals(500000L,internationalCashPercent),
//                ()-> assertEquals(600000L,instBalPercent),
                ()-> assertEquals(600000L,internationalCashInstPercent)

        );


    }

    @Test
    void updateProductDefFromDto2() {

        ProductDef productDef1 = createProductDef();

        Integer[] integers = {7,8,9,10,11,12,14,15};

        ProductDefUpdateDTO productDefDTO = createProductDefUpdateDTO(false,Arrays.asList(integers));


        ProductDef productDef = productServices.updateProductDefFromDto(productDefDTO,productDef1);


        Long cashBalancePercent = productDef.getLimitPercents().get(BalanceTypes.CASH_BALANCE);
        Long internationalCashPercent = productDef.getLimitPercents().get(BalanceTypes.INTERNATIONAL_CASH);
        Long internationalCashInstPercent = productDef.getLimitPercents().get(BalanceTypes.INTERNATIONAL_CASH_INSTALLMENT);
        Long instBalPercent = productDef.getLimitPercents().get(BalanceTypes.INSTALLMENT_BALANCE);



        assertAll(
                ()-> assertEquals(productDefDTO.getCardsValidityMonthNew(),productDef.getCardsValidityMonthNew()),
                ()-> assertEquals(productDefDTO.getCardsValidityMonthReplace(),productDef.getCardsValidityMonthReplace()),
                ()-> assertEquals(productDefDTO.getCardsValidityMonthReIssue(),productDef.getCardsValidityMonthReIssue()),
                ()-> assertEquals(productDefDTO.getDateRangeNewExpDate(),productDef.getDateRangeNewExpDate()),
                ()-> assertEquals(productDefDTO.getCardsWaiverActivationDays(),productDef.getCardsWaiverActivationDays()),
                ()-> assertEquals(productDefDTO.getDaysToCardsValid(),productDef.getDaysToCardsValid()),
                ()-> assertEquals(productDefDTO.getCardsActivationRequired(),productDef.getCardsActivationRequired()),


                ()-> assertNull(cashBalancePercent),
                ()-> assertEquals(1000000L,internationalCashPercent),
//                ()-> assertEquals(600000L,instBalPercent),
                ()-> assertEquals(3000000L,internationalCashInstPercent)

        );


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

        Map<BalanceTypes,Long>  percentMap = new HashMap<>();
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