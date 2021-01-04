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
        return accountBasicDataService.findAccountBasic(accountBasicUpdateDTO.getAccountId())
                .map(accountBasicOptional -> {
                    if (accountBasicOptional.isPresent()) {
                        return accountBasicOptional.get();
                    }
                    throw new NotFoundException(" Invalid Account Number " + accountBasicUpdateDTO.getAccountId());
                })
                .map(accountBasic -> {
                    ProductDef productDef = productTable.findProductDef(accountBasic.getOrg(),accountBasic.getProduct());
                    return updateAccountBasicFromDTO(accountBasicUpdateDTO, accountBasic,productDef);
                        })
                .flatMap(accountBasicDataService::saveAccountBasic);


    }

    @Override
    public Mono<AccountBasic> fetchAccountBasicInfo(String accountId) {

        return accountBasicDataService.findAccountBasic(accountId)
                .map(accountBasicOptional -> {
                    if (accountBasicOptional.isPresent()) {
                        return accountBasicOptional.get();
                    }
                    throw new NotFoundException(" Invalid Account Number " + accountId);
                });
    }

    @Override
    public Mono<Optional<AccountBasic>> fetchAccountBasicInfoOptional(String accountId) {
        return accountBasicDataService.findAccountBasic(accountId);
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



        AccountBasicDTO.AccountBasicDTOBuilder builder = AccountBasicDTO.builder()
                .accountId(accountBasic.getAccountId())
                .org(accountBasic.getOrg())
                .product(accountBasic.getProduct())
                .blockType(Util.getBlockType(accountBasic.getBlockType()))
                .billingCurrencyCode(accountBasic.getBillingCurrencyCode())
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



        AccountBasic.AccountBasicBuilder builder = AccountBasic.builder()
                .accountId(accountBasicAddDTO.getAccountId())
                .billingCurrencyCode(accountBasicAddDTO.getBillingCurrencyCode())
                .blockType(Util.getBlockType(accountBasicAddDTO.getBlockType()))
                .org(accountBasicAddDTO.getOrg())
                .product(accountBasicAddDTO.getProduct())
                .customerNumber(accountBasicAddDTO.getCustomerNumber())
                .accountType(Util.getAccountType(accountBasicAddDTO.getAccountType()))
                ;

        if (!Util.getBlockType(BlockType.APPROVE).equals(accountBasicAddDTO.getBlockType())) {
            builder.dateBlockApplied(LocalDateTime.now());

        }
        return builder.build();

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



        return accountBasic;
    }


}
