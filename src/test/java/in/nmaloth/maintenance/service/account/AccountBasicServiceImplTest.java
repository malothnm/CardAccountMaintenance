package in.nmaloth.maintenance.service.account;

import in.nmaloth.entity.BlockType;
import in.nmaloth.entity.account.AccountBasic;
import in.nmaloth.entity.account.AccountType;
import in.nmaloth.entity.account.BalanceTypes;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.entity.product.ProductId;
import in.nmaloth.maintenance.config.data.ProductTable;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.account.AccountBasicAddDTO;
import in.nmaloth.maintenance.model.dto.account.AccountBasicDTO;
import in.nmaloth.maintenance.model.dto.account.AccountBasicUpdateDTO;
import in.nmaloth.maintenance.model.dto.account.BalanceTypesDTO;
import in.nmaloth.maintenance.repository.account.AccountBasicRepository;
import in.nmaloth.maintenance.repository.product.ProductDefRepository;
import in.nmaloth.maintenance.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AccountBasicServiceImplTest {

    @Autowired
    private AccountBasicService accountBasicService;

    @Autowired
    private ProductDefRepository productDefRepository;

    @Autowired
    private AccountBasicRepository accountBasicRepository;

    @Autowired
    private ProductTable productTable;


    @BeforeEach
    void setup(){
        productDefRepository.findAll()
                .forEach(productDef -> productDefRepository.delete(productDef));


        accountBasicRepository.findAll()
                .forEach(cardsBasic -> accountBasicRepository.delete(cardsBasic));

        ProductDef productDef = createProductDef();
        productDefRepository.save(productDef);
        productTable.loadMap(productDef);

        ProductDef productDef1 = createProductDef();
        productDef1.getProductId().setOrg(001);
        productDef1.getProductId().setProduct(202);

        productDefRepository.save(productDef1);
        productTable.loadMap(productDef1);

        ProductDef productDef2 = createProductDef();
        productDef2.getProductId().setOrg(001);
        productDef2.getProductId().setProduct(203);
        productDefRepository.save(productDef2);
        productTable.loadMap(productDef2);



}

    @Test
    void createNewAccountBasic() {

        AccountBasicAddDTO accountBasicAddDTO = createAccountBasicAddDTO();

        AccountBasic accountBasic1 = accountBasicService.createNewAccountBasic(accountBasicAddDTO).block();


        AccountBasic accountBasic = accountBasicRepository.findById(accountBasicAddDTO.getAccountId()).get();


        assertAll(
                ()-> assertEquals(accountBasicAddDTO.getAccountId(),accountBasic.getAccountId()),
                ()-> assertEquals(accountBasicAddDTO.getBlockType(),Util.getBlockType(accountBasic.getBlockType())),
                ()-> assertEquals(accountBasicAddDTO.getOrg(),accountBasic.getOrg()),
                ()-> assertEquals(accountBasicAddDTO.getProduct(),accountBasic.getProduct()),
                ()-> assertEquals(accountBasicAddDTO.getBillingCurrencyCode(),accountBasic.getBillingCurrencyCode())

        );

    }

    @Test
    void updateAccountBasic() {

        AccountBasic accountBasic1 = createAccountBasic();
        BlockType blockType = accountBasic1.getBlockType();
        LocalDateTime localDateTime = accountBasic1.getDateBlockApplied();
        accountBasicRepository.save(accountBasic1);

        AccountBasicUpdateDTO accountBasicUpdateDTO = createAccountUpdateDTO(true,accountBasic1.getAccountId(),null);

        accountBasicService.updateAccountBasic(accountBasicUpdateDTO).block();

        AccountBasic accountBasic = accountBasicRepository.findById(accountBasic1.getAccountId()).get();



        assertAll(
                ()-> assertEquals(accountBasicUpdateDTO.getAccountId(),accountBasic.getAccountId()),
                ()-> assertEquals(Util.getBlockType(accountBasicUpdateDTO.getBlockType()),accountBasic.getBlockType()),
                ()-> assertEquals(accountBasicUpdateDTO.getBillingCurrencyCode(),accountBasic.getBillingCurrencyCode()),
                ()-> assertEquals(blockType,accountBasic.getPreviousBlockType()),
                ()-> assertEquals(localDateTime,accountBasic.getDatePreviousBLockType()),
                ()-> assertNotNull(accountBasic.getDateBlockApplied())
        );


    }

    @Test
    void fetchAccountBasicInfo() {

        AccountBasic accountBasic = createAccountBasic();
        accountBasicRepository.save(accountBasic);

        Mono<AccountBasic> accountBasicMono = accountBasicService.fetchAccountBasicInfo(accountBasic.getAccountId());

        StepVerifier
                .create(accountBasicMono)
                .expectNextCount(1)
                .verifyComplete();

    }

    @Test
    void fetchAccountBasicInfo1() {


        Mono<AccountBasic> accountBasicMono = accountBasicService.fetchAccountBasicInfo("1234");

        StepVerifier
                .create(accountBasicMono)
                .expectError(NotFoundException.class)
                .verify();


    }

    @Test
    void deleteAccountBasic() {

        AccountBasic accountBasic = createAccountBasic();
        accountBasicRepository.save(accountBasic);

        accountBasicService.deleteAccountBasic(accountBasic.getAccountId()).block();

        Optional<AccountBasic> accountBasicOptional = accountBasicRepository.findById(accountBasic.getAccountId());

        assertTrue(accountBasicOptional.isEmpty());

    }

    @Test
    void deleteAccountBasic1() {

        Mono<AccountBasic> accountBasicMono = accountBasicService.deleteAccountBasic("12345");

        StepVerifier
                .create(accountBasicMono)
                .expectError(NotFoundException.class)
                .verify();

    }

    @Test
    void convertToDTO() {

        AccountBasic accountBasic = createAccountBasic();
        AccountBasicDTO accountBasicDTO = accountBasicService.convertToDTO(accountBasic);

        assertAll(
                ()-> assertEquals(accountBasic.getAccountId(),accountBasicDTO.getAccountId()),
                ()-> assertEquals(accountBasic.getBillingCurrencyCode(),accountBasicDTO.getBillingCurrencyCode()),
                ()-> assertEquals(accountBasic.getBlockType(),Util.getBlockType(accountBasicDTO.getBlockType())),
                ()-> assertEquals(accountBasic.getOrg(),accountBasicDTO.getOrg()),
                ()-> assertEquals(accountBasic.getProduct(),accountBasicDTO.getProduct()),
                ()->assertEquals(accountBasic.getDateBlockApplied(),accountBasicDTO.getDateBlockCode()),
                ()->assertEquals(accountBasic.getPreviousBlockType(),Util.getBlockType(accountBasicDTO.getPreviousBlockType())),
                ()-> assertEquals(accountBasic.getDatePreviousBLockType(),accountBasicDTO.getDatePreviousBlockCode()),
                ()-> assertEquals(accountBasic.getAccountType(),Util.getAccountType(accountBasicDTO.getAccountType())),
                ()-> assertEquals(accountBasic.getCustomerNumber(),accountBasicDTO.getCustomerNumber()),
                ()-> assertEquals(accountBasic.getPreviousAccountNumber(),accountBasicDTO.getPreviousAccountNumber()),
                ()-> assertEquals(accountBasic.getDateTransfer(),accountBasicDTO.getDateTransfer())
        );
    }

    @Test
    void convertDTOToAccountBasic() {

        AccountBasicAddDTO accountBasicAddDTO = createAccountBasicAddDTO();

        AccountBasic accountBasic = accountBasicService.convertDTOToAccountBasic(accountBasicAddDTO,createProductDef());


        assertAll(
                ()-> assertEquals(accountBasicAddDTO.getAccountId(),accountBasic.getAccountId()),
                ()-> assertEquals(accountBasicAddDTO.getBlockType(),Util.getBlockType(accountBasic.getBlockType())),
                ()-> assertEquals(accountBasicAddDTO.getOrg(),accountBasic.getOrg()),
                ()-> assertEquals(accountBasicAddDTO.getProduct(),accountBasic.getProduct()),
                ()-> assertEquals(accountBasicAddDTO.getBillingCurrencyCode(),accountBasic.getBillingCurrencyCode()),
                ()-> assertEquals(Util.getAccountType(accountBasicAddDTO.getAccountType()),accountBasic.getAccountType()),
                ()-> assertEquals(accountBasicAddDTO.getCustomerNumber(),accountBasic.getCustomerNumber()),
                ()-> assertNull(accountBasic.getCorporateNumber()),
                ()-> assertNull(accountBasic.getPreviousAccountNumber()),
                ()-> assertNull(accountBasic.getDateTransfer())

        );


    }

    @Test
    void updateAccountBasicFromDTO1() {

        AccountBasic accountBasic = createAccountBasic();
        BlockType blockType = accountBasic.getBlockType();
        LocalDateTime localDateTime = accountBasic.getDateBlockApplied();
        AccountBasicUpdateDTO accountBasicUpdateDTO = createAccountUpdateDTO(true,accountBasic.getAccountId(),null);

        ProductDef productDef = createProductDef();

        accountBasicService.updateAccountBasicFromDTO(accountBasicUpdateDTO,accountBasic,productDef);

        assertAll(
                ()-> assertEquals(accountBasicUpdateDTO.getAccountId(),accountBasic.getAccountId()),
                ()-> assertEquals(Util.getBlockType(accountBasicUpdateDTO.getBlockType()),accountBasic.getBlockType()),
                ()-> assertEquals(accountBasicUpdateDTO.getBillingCurrencyCode(),accountBasic.getBillingCurrencyCode()),
                ()-> assertEquals(blockType,accountBasic.getPreviousBlockType()),
                ()-> assertEquals(localDateTime,accountBasic.getDatePreviousBLockType()),
                ()-> assertNotNull(accountBasic.getDateBlockApplied())
        );

    }

    @Test
    void updateAccountBasicFromDTO2() {

        AccountBasic accountBasic = createAccountBasic();

        List<Integer> integerList = Arrays.asList(1,3);
        BlockType blockType = accountBasic.getBlockType();
        LocalDateTime localDateTime = accountBasic.getDateBlockApplied();
        AccountBasicUpdateDTO accountBasicUpdateDTO = createAccountUpdateDTO(false,accountBasic.getAccountId(),integerList);

        ProductDef productDef = createProductDef();

        accountBasicService.updateAccountBasicFromDTO(accountBasicUpdateDTO,accountBasic,productDef);


        assertAll(
                ()-> assertEquals(accountBasicUpdateDTO.getAccountId(),accountBasic.getAccountId()),
                ()-> assertEquals(blockType,accountBasic.getBlockType()),
                ()-> assertEquals(localDateTime,accountBasic.getDateBlockApplied()),
                ()-> assertEquals(accountBasicUpdateDTO.getBillingCurrencyCode(),accountBasic.getBillingCurrencyCode())

        );

    }


    @Test
    void updateAccountBasicFromDTO3() {

        AccountBasic accountBasic = createAccountBasic();

        String currCode = accountBasic.getBillingCurrencyCode();
        BlockType blockType = accountBasic.getBlockType();
        LocalDateTime localDateTime = accountBasic.getDateBlockApplied();

        List<Integer> integerList = Arrays.asList(2,4,10);
        ProductDef productDef = createProductDef();
        AccountBasicUpdateDTO accountBasicUpdateDTO = createAccountUpdateDTO(false,accountBasic.getAccountId(),integerList);

        accountBasicService.updateAccountBasicFromDTO(accountBasicUpdateDTO,accountBasic,productDef);

        assertAll(
                ()-> assertEquals(accountBasicUpdateDTO.getAccountId(),accountBasic.getAccountId()),
                ()-> assertEquals(blockType,accountBasic.getPreviousBlockType()),
                ()-> assertEquals(Util.getBlockType(accountBasicUpdateDTO.getBlockType()),accountBasic.getBlockType()),
                ()-> assertEquals(localDateTime,accountBasic.getDatePreviousBLockType()),
                ()-> assertEquals(currCode,accountBasic.getBillingCurrencyCode()),
                ()-> assertEquals(accountBasicUpdateDTO.getCorporateNumber(),accountBasic.getCorporateNumber())
        );

    }


    @Test
    void updateAccountBasicFromDTO5() {

        AccountBasic accountBasic = createAccountBasic();

        String currCode = accountBasic.getBillingCurrencyCode();
        List<Integer> integerList = Arrays.asList(7,8,9);
        BlockType blockType = accountBasic.getBlockType();
        LocalDateTime localDateTime = accountBasic.getDateBlockApplied();
        AccountBasicUpdateDTO accountBasicUpdateDTO = createAccountUpdateDTO(false,accountBasic.getAccountId(),integerList);

        ProductDef productDef = createProductDef();

        accountBasicService.updateAccountBasicFromDTO(accountBasicUpdateDTO,accountBasic,productDef);


        assertAll(
                ()-> assertEquals(accountBasicUpdateDTO.getAccountId(),accountBasic.getAccountId()),
                ()-> assertEquals(blockType,accountBasic.getBlockType()),
                ()-> assertEquals(localDateTime,accountBasic.getDateBlockApplied()),
                ()-> assertEquals(currCode,accountBasic.getBillingCurrencyCode()),
                ()-> assertEquals(accountBasic.getAccountType(),Util.getAccountType(accountBasicUpdateDTO.getAccountType())),
                ()-> assertEquals(accountBasicUpdateDTO.getCustomerNumber(),accountBasic.getCustomerNumber())
        );

    }
    private AccountBasic createAccountBasic(){


        return AccountBasic.builder()
                .org(001)
                .product(201)
                .accountId(UUID.randomUUID().toString().replace("-",""))
                .blockType(BlockType.BLOCK_DECLINE)
                .dateBlockApplied(LocalDateTime.now())
                .billingCurrencyCode("840")
                .datePreviousBLockType(LocalDateTime.of(2020,12,23,11,24,30))
                .previousBlockType(BlockType.BLOCK_SUSPECTED_FRAUD)
                .accountType(AccountType.CREDIT)
                .customerNumber(UUID.randomUUID().toString().replace("-",""))
                .corporateNumber(UUID.randomUUID().toString().replace("-",""))
                .previousAccountNumber(UUID.randomUUID().toString().replace("-",""))
                .dateTransfer(LocalDateTime.now())
                .build();

    }

    private AccountBasicAddDTO createAccountBasicAddDTO(){

        List<BalanceTypesDTO> balanceTypesDTOList = new ArrayList<>();
        balanceTypesDTOList.add(BalanceTypesDTO.builder()
                .balanceType(Util.getBalanceTypes(BalanceTypes.CURRENT_BALANCE))
                .limitAmount(500000L)
                .build()
        );

        balanceTypesDTOList.add(BalanceTypesDTO.builder()
                .balanceType(Util.getBalanceTypes(BalanceTypes.CASH_BALANCE))
                .limitAmount(400000L)
                .build()
        );

        return AccountBasicAddDTO.builder()
                .accountId(UUID.randomUUID().toString().replace("-",""))
                .org(001)
                .product(201)
                .billingCurrencyCode("840")
                .blockType(Util.getBlockType(BlockType.APPROVE))
                .balanceTypesDTOList(balanceTypesDTOList)
                .accountType(Util.getAccountType(AccountType.SAVINGS))
                .customerNumber(UUID.randomUUID().toString().replace("-",""))
                .build();

    }

    private ProductDef createProductDef(){

        Map<BalanceTypes,Long> percentMap = new HashMap<>();
        percentMap.put(BalanceTypes.CASH_BALANCE,200000L);
        percentMap.put(BalanceTypes.INTERNATIONAL_CASH,100000L);
        percentMap.put(BalanceTypes.INTERNATIONAL_CASH_INSTALLMENT,300000L);
        percentMap.put(BalanceTypes.INSTALLMENT_BALANCE,500000L);



        return ProductDef.builder()
                .serviceCode(301)
                .productId(new ProductId(001,201))
                .daysToCardsValid(11)
                .dateRangeNewExpDate(10)
                .cardsWaiverActivationDays(5)
                .cardsValidityMonthReplace(35)
                .cardsValidityMonthReIssue(40)
                .cardsValidityMonthNew(44)
                .cardsActivationRequired(false)
                .limitPercents(percentMap)
                .cardsReturn(10)
                .build();

    }

    private AccountBasicUpdateDTO createAccountUpdateDTO(boolean allfields , String accountNumber, List<Integer> fieldList){

        AccountBasicUpdateDTO.AccountBasicUpdateDTOBuilder builder = AccountBasicUpdateDTO.builder()
                .accountId(accountNumber);

        List<BalanceTypesDTO> balanceTypesDTOListAdd = new ArrayList<>();
        List<BalanceTypesDTO> balanceTypesDTOListDelete = new ArrayList<>();

        balanceTypesDTOListAdd.add(BalanceTypesDTO.builder()
                .balanceType(Util.getBalanceTypes(BalanceTypes.INTERNATIONAL_INSTALLMENT))
                .limitAmount(500000L)
                .build()
        );
        balanceTypesDTOListAdd.add(BalanceTypesDTO.builder()
                .balanceType(Util.getBalanceTypes(BalanceTypes.INTERNATIONAL_CASH_INSTALLMENT))
                .limitAmount(450000L)
                .build()
        );

        balanceTypesDTOListAdd.add(BalanceTypesDTO.builder()
                .balanceType(Util.getBalanceTypes(BalanceTypes.CASH_BALANCE))
                .limitAmount(60000L)
                .build()
        );

        balanceTypesDTOListDelete.add(BalanceTypesDTO.builder()
                .balanceType(Util.getBalanceTypes(BalanceTypes.INSTALLMENT_CASH))
                .build()
        );




        if(allfields){
            return builder
                    .billingCurrencyCode("484")
                    .blockType(Util.getBlockType(BlockType.BLOCK_FRAUD))
                    .balanceTypesDTOListAdd(balanceTypesDTOListAdd)
                    .balanceTypesDTOListDelete(balanceTypesDTOListDelete)
                    .corporateNumber(UUID.randomUUID().toString().replace("-",""))
                    .customerNumber(UUID.randomUUID().toString().replace("-",""))
                    .accountType(Util.getAccountType(AccountType.CURRENT))
                    .build();
        }

        fieldList.forEach(integer -> populateBuilder(builder,integer,balanceTypesDTOListAdd,balanceTypesDTOListDelete));

        return builder.build();
    }

    private void populateBuilder(AccountBasicUpdateDTO.AccountBasicUpdateDTOBuilder builder, Integer integer,
                                 List<BalanceTypesDTO> balanceTypesDTOListAdd, List<BalanceTypesDTO> balanceTypesDTOListDelete) {

        switch (integer){
            case 1: {
                builder.billingCurrencyCode("124");
                break;
            }
            case 2: {
                builder.blockType(Util.getBlockType(BlockType.BLOCK_TEMP));
                break;
            }
            case 3: {
                builder.balanceTypesDTOListAdd(balanceTypesDTOListAdd);
                break;
            }
            case 4: {
                builder.balanceTypesDTOListDelete(balanceTypesDTOListDelete);
                break;
            }
            case 6: {
                balanceTypesDTOListAdd.add(BalanceTypesDTO.builder()
                        .balanceType(Util.getBalanceTypes(BalanceTypes.CURRENT_BALANCE))
                        .limitAmount(200000L)
                        .build()
                );
                builder.balanceTypesDTOListAdd(balanceTypesDTOListAdd);
            }
            case 7: {
                balanceTypesDTOListDelete.add(BalanceTypesDTO.builder()
                        .balanceType(Util.getBalanceTypes(BalanceTypes.CURRENT_BALANCE))
                        .limitAmount(0L)
                        .build()
                );
                builder.balanceTypesDTOListDelete(balanceTypesDTOListDelete);
            }
            case 8: {
                builder.accountType(Util.getAccountType(AccountType.UNIVERSAL));
            }
            case 9 : {
                builder.customerNumber(UUID.randomUUID().toString().replace("-",""));
            }
            case 10: {
                builder.corporateNumber(UUID.randomUUID().toString().replace("-",""));
            }
        }
    }

}