package in.nmaloth.maintenance.service.account;

import in.nmaloth.entity.account.AccountAccumValues;
import in.nmaloth.entity.account.AccountBalances;
import in.nmaloth.entity.account.BalanceTypes;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.maintenance.config.data.ProductTable;
import in.nmaloth.maintenance.dataService.account.AccountAccumValuesDataService;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.account.AccountAccumValuesDTO;
import in.nmaloth.maintenance.model.dto.account.AccountBalancesDTO;
import in.nmaloth.maintenance.model.dto.account.BalanceTypesDTO;
import in.nmaloth.maintenance.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AccountAccumValuesServiceImpl implements AccountAccumValuesService {

    private final AccountAccumValuesDataService accountAccumValuesDataService;

    private final ProductTable productTable;

    @Value("${balance.percentNode}")
    private int percentNode;

    private long percentValue;


    public AccountAccumValuesServiceImpl(AccountAccumValuesDataService accountAccumValuesDataService,
                                         ProductTable productTable) {
        this.accountAccumValuesDataService = accountAccumValuesDataService;
        this.productTable = productTable;
    }


    @Override
    public Mono<AccountAccumValues> fetchAccountAccumValuesByAccountId(String accountId) {
        return accountAccumValuesDataService.findAccountAccumValuesById(accountId)
                .map(accountAccumValuesOptional -> {
                    if(accountAccumValuesOptional.isPresent()){
                        return accountAccumValuesOptional.get();
                    }
                    throw  new NotFoundException("Invalid Account Number " + accountId);
                })
                ;
    }

    @Override
    public Mono<AccountAccumValues> saveAccountAccumValues(AccountAccumValues accountAccumValues) {

        return accountAccumValuesDataService.saveAccountAccumValues(accountAccumValues);
    }

    @Override
    public Mono<AccountAccumValues> createNewAccumValues(String accountId, List<BalanceTypesDTO> balanceTypesDTOList, int org, int product) {
        AccountAccumValues accountAccumValues = initializeAccumValues(accountId,balanceTypesDTOList,org,product);
        return saveAccountAccumValues(accountAccumValues);

    }

    @Override
    public Mono<AccountAccumValues> updateAccumValues(List<BalanceTypesDTO> balanceTypesDTOListAdd, List<BalanceTypesDTO> balanceTypesDTOListDelete, String accountId) {
        return fetchAccountAccumValuesByAccountId(accountId)
                .map(accountAccumValues -> updateAccumValues(balanceTypesDTOListAdd,balanceTypesDTOListDelete,accountAccumValues))
                .flatMap(accountAccumValues -> saveAccountAccumValues(accountAccumValues))
                ;
    }

    @Override
    public AccountAccumValuesDTO convertToDTO(AccountAccumValues accountAccumValues) {



        List<BalanceTypesDTO> balanceTypesDTOList = accountAccumValues.getLimitsMap()
                .entrySet()
                .stream()
                .map(balanceTypesLongEntry -> BalanceTypesDTO.builder()
                        .balanceType(Util.getBalanceTypes(balanceTypesLongEntry.getKey()))
                        .limitAmount(balanceTypesLongEntry.getValue())
                        .build())
                .collect(Collectors.toList());


        List<AccountBalancesDTO> accountBalancesDTOList = accountAccumValues.getBalancesMap().entrySet()
                .stream()
                .map(accountBalancesEntry ->  AccountBalancesDTO.builder()
                        .balanceType(Util.getBalanceTypes(accountBalancesEntry.getKey()))
                        .postedBalance(accountBalancesEntry.getValue().getPostedBalance())
                        .memoCr(accountBalancesEntry.getValue().getMemoCr())
                        .memoDb(accountBalancesEntry.getValue().getMemoDb())
                        .build()
                )
                .collect(Collectors.toList());



        return AccountAccumValuesDTO.builder()
                .accountId(accountAccumValues.getAccountId())
                .accountBalancesDTOList(accountBalancesDTOList)
                .balanceTypesDTOList(balanceTypesDTOList)
                .build()
                ;
    }


    @Override
    public AccountAccumValues initializeAccumValues(String accountId, List<BalanceTypesDTO> balanceTypesDTOList,
                                                    int org, int product) {

        Map<BalanceTypes, Long> balanceTypeMap = new HashMap<>();
        Map<BalanceTypes, AccountBalances> accountBalancesMap = new HashMap<>();

        ProductDef productDef = productTable.findProductDef(org,product);

        if(productDef == null){
            throw new NotFoundException("Invalid Org or Product");
        }

        balanceTypesDTOList
                .stream()
                .peek(balanceTypesDTO -> accountBalancesMap.put(Util.getBalanceTypes(balanceTypesDTO.getBalanceType()),
                        initAccountBalance()))
                .forEach(balanceTypesDTO -> balanceTypeMap.put(Util.getBalanceTypes(balanceTypesDTO.getBalanceType()),
                        balanceTypesDTO.getLimitAmount()));


        Long creditLimit = balanceTypeMap.get(BalanceTypes.CURRENT_BALANCE);

        if (creditLimit == null) {
            throw new RuntimeException("Credit Limit cannot be null" + accountId);
        }


        productDef.getLimitPercents().entrySet()
                .stream()
                .filter(balanceTypesEntry -> balanceTypeMap.get(balanceTypesEntry.getKey()) == null)
                .peek(balanceTypesLongEntry -> accountBalancesMap.put(balanceTypesLongEntry.getKey(),initAccountBalance()))
                .forEach(balanceTypesLongEntry -> calculatePercentLimits(creditLimit, balanceTypesLongEntry.getKey(),
                        balanceTypesLongEntry.getValue(), balanceTypeMap));



        return AccountAccumValues.builder()
                .accountId(accountId)
                .org(org)
                .product(product)
                .balancesMap(accountBalancesMap)
                .limitsMap(balanceTypeMap)
                .build()
                ;

    }

    private  AccountBalances initAccountBalance(){
        return AccountBalances.builder()
                .memoCr(0)
                .memoDb(0)
                .postedBalance(0)
                .build();

    }
    @Override
    public AccountAccumValues updateAccumValues(List<BalanceTypesDTO> balanceTypesDTOListAdd,
                                                List<BalanceTypesDTO> balanceTypesDTOListDelete,
                                                   AccountAccumValues accountAccumValues) {


        ProductDef productDef = productTable.findProductDef(accountAccumValues.getOrg(),accountAccumValues.getProduct());

        if(productDef == null){
            throw new NotFoundException("Invalid Org or Product");
        }


        if (balanceTypesDTOListAdd != null) {

            if (accountAccumValues.getLimitsMap() == null) {
                accountAccumValues.setLimitsMap(new HashMap<>());
            }

            if(accountAccumValues.getBalancesMap() == null){
                accountAccumValues.setBalancesMap(new HashMap<>());
            }

            Map<BalanceTypes,Long> balanceTypesMap = convertToMap(balanceTypesDTOListAdd);

            balanceTypesDTOListAdd
                    .forEach(balanceTypesDTO -> {

                        BalanceTypes balanceTypes = Util.getBalanceTypes(balanceTypesDTO.getBalanceType());
                        accountAccumValues.getLimitsMap().put(balanceTypes, balanceTypesDTO.getLimitAmount());

                        if( !accountAccumValues.getBalancesMap().containsKey(balanceTypes)) {

                            accountAccumValues.getBalancesMap().put(balanceTypes, initAccountBalance());
                        }

                    });

            Long creditLimit = balanceTypesMap.get(BalanceTypes.CURRENT_BALANCE);
            if(creditLimit != null){
                recalculateLimits(productDef,accountAccumValues.getLimitsMap(),creditLimit);
            }
        }

        if ( balanceTypesDTOListDelete != null) {
            if (accountAccumValues.getLimitsMap() != null) {

                Map<BalanceTypes,Long> balanceTypesMap = convertToMap(balanceTypesDTOListDelete);

                if(balanceTypesMap.get(BalanceTypes.CURRENT_BALANCE) == null){
                    balanceTypesDTOListDelete
                            .forEach(balanceTypesDTO ->{

                                accountAccumValues.getLimitsMap().remove(Util.getBalanceTypes(balanceTypesDTO.getBalanceType()));

                            });
                }  else {

                    initializeLimits(accountAccumValues.getLimitsMap());
                }
            }
        }

        return accountAccumValues;
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

    private void recalculateLimits(ProductDef productDef, Map<BalanceTypes, Long> balanceTypeMap, Long creditLimit) {


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
