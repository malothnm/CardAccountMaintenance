package in.nmaloth.maintenance.service.account;

import in.nmaloth.entity.BlockType;
import in.nmaloth.entity.account.AccountBasic;
import in.nmaloth.entity.account.BalanceTypes;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.maintenance.config.data.ProductTable;
import in.nmaloth.maintenance.dataService.account.AccountBasicDataService;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.account.AccountBasicAddDTO;
import in.nmaloth.maintenance.model.dto.account.AccountBasicDTO;
import in.nmaloth.maintenance.model.dto.account.AccountBasicUpdateDTO;
import in.nmaloth.maintenance.model.dto.account.BalanceTypesDTO;
import in.nmaloth.maintenance.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AccountBasicServiceImpl implements AccountBasicService {

    private final AccountBasicDataService accountBasicDataService;

    private final ProductTable productTable;

    @Value("${balance.percentNode}")
    private int percentNode;

    private long percentValue;


    public AccountBasicServiceImpl(AccountBasicDataService accountBasicDataService, ProductTable productTable) {
        this.accountBasicDataService = accountBasicDataService;
        this.productTable = productTable;

    }


    @Override
    public Mono<AccountBasic> createNewAccountBasic(AccountBasicAddDTO accountBasicAddDTO) {
        ProductDef productDef = productTable.findProductDef(accountBasicAddDTO.getOrg(), accountBasicAddDTO.getProduct());

        if(productDef == null){
            throw new NotFoundException("Invalid Org or Product");
        }

        return accountBasicDataService.saveAccountBasic(convertDTOToAccountBasic(accountBasicAddDTO, productDef))
                ;
    }

    @Override
    public Mono<AccountBasic> updateAccountBasic(AccountBasicUpdateDTO accountBasicUpdateDTO) {
        return accountBasicDataService.findAccountBasic(accountBasicUpdateDTO.getAccountNumber())
                .map(accountBasicOptional -> {
                    if (accountBasicOptional.isPresent()) {
                        return accountBasicOptional.get();
                    }
                    throw new NotFoundException(" Invalid Account Number " + accountBasicUpdateDTO.getAccountNumber());
                })
                .map(accountBasic -> {
                    ProductDef productDef = productTable.findProductDef(accountBasic.getOrg(),accountBasic.getProduct());
                    return updateAccountBasicFromDTO(accountBasicUpdateDTO, accountBasic,productDef);
                        })
                .flatMap(accountBasicDataService::saveAccountBasic);


    }

    @Override
    public Mono<AccountBasic> fetchAccountBasicInfo(String accountNumber) {

        return accountBasicDataService.findAccountBasic(accountNumber)
                .map(accountBasicOptional -> {
                    if (accountBasicOptional.isPresent()) {
                        return accountBasicOptional.get();
                    }
                    throw new NotFoundException(" Invalid Account Number " + accountNumber);
                });
    }

    @Override
    public Mono<Optional<AccountBasic>> fetchAccountBasicInfoOptional(String accountNumber) {
        return accountBasicDataService.findAccountBasic(accountNumber);
    }

    @Override
    public Mono<AccountBasic> deleteAccountBasic(String accountNumber) {
        return accountBasicDataService.deleteAccountBasicByAcctNumber(accountNumber)
                .map(accountBasicOptional -> {
                    if (accountBasicOptional.isPresent()) {
                        return accountBasicOptional.get();
                    }
                    throw new NotFoundException(" Invalid Account Number " + accountNumber);
                });
    }

    @Override
    public AccountBasicDTO convertToDTO(AccountBasic accountBasic) {

        List<BalanceTypesDTO> balanceTypesDTOList = accountBasic.getLimitsMap()
                .entrySet()
                .stream()
                .map(balanceTypesLongEntry -> BalanceTypesDTO.builder()
                        .balanceType(Util.getBalanceTypes(balanceTypesLongEntry.getKey()))
                        .limitAmount(balanceTypesLongEntry.getValue())
                        .build())
                .collect(Collectors.toList());


        AccountBasicDTO.AccountBasicDTOBuilder builder = AccountBasicDTO.builder()
                .accountNumber(accountBasic.getAccountNumber())
                .org(accountBasic.getOrg())
                .product(accountBasic.getProduct())
                .blockType(Util.getBlockType(accountBasic.getBlockType()))
                .billingCurrencyCode(accountBasic.getBillingCurrencyCode())
                .balanceTypesDTOList(balanceTypesDTOList)
                .customerNumber(accountBasic.getCustomerNumber())
                .accountType(Util.getAccountType(accountBasic.getAccountType()))
                ;

        if (accountBasic.getDateBlockApplied() != null) {
            builder.dateBlockCode(accountBasic.getDateBlockApplied());
        }
        if (accountBasic.getPreviousBlockType() != null) {
            builder.previousBlockType(Util.getBlockType(accountBasic.getPreviousBlockType()));
        }
        if (accountBasic.getDatePreviousBLockType() != null) {
            builder.datePreviousBlockCode(accountBasic.getDatePreviousBLockType());
        }

        if(accountBasic.getCorporateNumber() != null){
            builder.corporateNumber(accountBasic.getCorporateNumber());
        }

        if(accountBasic.getPreviousAccountNumber() != null){
            builder.previousAccountNumber( accountBasic.getPreviousAccountNumber());
        }
        if(accountBasic.getDateTransfer() != null){
            builder.dateTransfer(accountBasic.getDateTransfer());
        }
        return builder.build();
    }

    @Override
    public AccountBasic convertDTOToAccountBasic(AccountBasicAddDTO accountBasicAddDTO, ProductDef productDef) {

        Map<BalanceTypes, Long> balanceTypeMap = new HashMap<>();
        accountBasicAddDTO.getBalanceTypesDTOList()
                .stream()
                .forEach(balanceTypesDTO -> balanceTypeMap.put(Util.getBalanceTypes(balanceTypesDTO.getBalanceType()),
                        balanceTypesDTO.getLimitAmount()));


        Long creditLimit = balanceTypeMap.get(BalanceTypes.CURRENT_BALANCE);

        if (creditLimit == null) {
            throw new RuntimeException("Credit Limit cannot be null" + accountBasicAddDTO.getAccountNumber());
        }


        productDef.getLimitPercents().entrySet()
                .stream()
                .filter(balanceTypesEntry -> balanceTypeMap.get(balanceTypesEntry.getKey()) == null)
                .forEach(balanceTypesLongEntry -> calculatePercentLimits(creditLimit, balanceTypesLongEntry.getKey(),
                        balanceTypesLongEntry.getValue(), balanceTypeMap));


        AccountBasic.AccountBasicBuilder builder = AccountBasic.builder()
                .accountNumber(accountBasicAddDTO.getAccountNumber())
                .billingCurrencyCode(accountBasicAddDTO.getBillingCurrencyCode())
                .blockType(Util.getBlockType(accountBasicAddDTO.getBlockType()))
                .org(accountBasicAddDTO.getOrg())
                .product(accountBasicAddDTO.getProduct())
                .limitsMap(balanceTypeMap)
                .customerNumber(accountBasicAddDTO.getCustomerNumber())
                .accountType(Util.getAccountType(accountBasicAddDTO.getAccountType()))
                ;

        if (!Util.getBlockType(BlockType.APPROVE).equals(accountBasicAddDTO.getBlockType())) {
            builder.dateBlockApplied(LocalDateTime.now());

        }
        return builder.build();

    }

    private void calculatePercentLimits(Long creditLimit, BalanceTypes balanceType, Long percent, Map<BalanceTypes, Long> balanceTypeMap) {

        if(percentValue == 0){

            percentValue = 1;
            for (int i = 0; i < percentNode; i++) {
                percentValue = percentValue * 10;
            }
        }


        long balanceValueWithoutNode = creditLimit * percent;
        long balanceValue = Math.floorDiv(balanceValueWithoutNode, percentValue);
        balanceTypeMap.put(balanceType, balanceValue);
    }

    @Override
    public AccountBasic updateAccountBasicFromDTO(AccountBasicUpdateDTO accountBasicUpdateDTO, AccountBasic accountBasic,
                                                  ProductDef productDef) {

        if (accountBasicUpdateDTO.getBlockType() != null) {

            if (!accountBasic.getBlockType().equals(Util.getBlockType(accountBasicUpdateDTO.getBlockType()))) {

                accountBasic.setPreviousBlockType(accountBasic.getBlockType());
                if (accountBasic.getDateBlockApplied() != null) {
                    accountBasic.setDatePreviousBLockType(accountBasic.getDateBlockApplied());
                }

                accountBasic.setBlockType(Util.getBlockType(accountBasicUpdateDTO.getBlockType()));
                accountBasic.setDateBlockApplied(LocalDateTime.now());

            }
        }
        if (accountBasicUpdateDTO.getBillingCurrencyCode() != null) {
            accountBasic.setBillingCurrencyCode(accountBasicUpdateDTO.getBillingCurrencyCode());
        }

        if(accountBasicUpdateDTO.getCorporateNumber() != null){
            accountBasic.setCorporateNumber(accountBasicUpdateDTO.getCorporateNumber());
        }

        if(accountBasicUpdateDTO.getCustomerNumber() != null){
            accountBasic.setCustomerNumber(accountBasicUpdateDTO.getCustomerNumber());
        }

        if(accountBasicUpdateDTO.getAccountType() != null){
            accountBasic.setAccountType(Util.getAccountType(accountBasicUpdateDTO.getAccountType()));
        }


        if (accountBasicUpdateDTO.getBalanceTypesDTOListAdd() != null) {


            if (accountBasic.getLimitsMap() == null) {
                accountBasic.setLimitsMap(new HashMap<>());
            }

            Map<BalanceTypes,Long> balanceTypesMap = convertToMap(accountBasicUpdateDTO.getBalanceTypesDTOListAdd());

            accountBasicUpdateDTO.getBalanceTypesDTOListAdd()
                    .forEach(balanceTypesDTO -> accountBasic.getLimitsMap().put(Util.getBalanceTypes(balanceTypesDTO.getBalanceType())
                            , balanceTypesDTO.getLimitAmount()));

            Long creditLimit = balanceTypesMap.get(BalanceTypes.CURRENT_BALANCE);
            if(creditLimit != null){
                recalculateLimits(productDef,accountBasic.getLimitsMap(),creditLimit);
            }
        }

        if (accountBasicUpdateDTO.getBalanceTypesDTOListDelete() != null) {
            if (accountBasic.getLimitsMap() != null) {

                Map<BalanceTypes,Long> balanceTypesMap = convertToMap(accountBasicUpdateDTO.getBalanceTypesDTOListDelete());

                if(balanceTypesMap.get(BalanceTypes.CURRENT_BALANCE) == null){
                    accountBasicUpdateDTO.getBalanceTypesDTOListDelete()
                            .forEach(balanceTypesDTO -> accountBasic.getLimitsMap().remove(Util.getBalanceTypes(balanceTypesDTO.getBalanceType())));
                }  else {
                    initializeLimits(accountBasic.getLimitsMap());
                }

            }
        }

        return accountBasic;
    }

    private void recalculateLimits(ProductDef productDef, Map<BalanceTypes, Long> balanceTypeMap,Long creditLimit) {


        productDef
                .getLimitPercents()
                .entrySet()
                .stream()
                .filter(balanceTypesEntry -> !balanceTypesEntry.getKey().equals(BalanceTypes.CURRENT_BALANCE))
                .forEach(balanceTypesEntry->
                        calculatePercentLimits(creditLimit,balanceTypesEntry.getKey(),balanceTypesEntry.getValue(),balanceTypeMap));



    }

    private void initializeLimits(Map<BalanceTypes,Long> balanceTypesMap){

        balanceTypesMap.entrySet()
                .stream()
                .forEach(balanceTypesLongEntry -> balanceTypesMap.put(balanceTypesLongEntry.getKey(),0L));
    }

    private Map<BalanceTypes,Long> convertToMap(List<BalanceTypesDTO> balanceTypesDTOList){

        Map<BalanceTypes,Long> balanceTypesMap = new HashMap<>();

        balanceTypesDTOList.forEach(balanceTypesDTO ->
                balanceTypesMap.put(Util.getBalanceTypes(balanceTypesDTO.getBalanceType()),balanceTypesDTO.getLimitAmount()));

        return balanceTypesMap;
    }
}
