package in.nmaloth.maintenance.controllers.account;


import in.nmaloth.entity.account.AccountAccumValues;
import in.nmaloth.entity.account.AccountBasic;
import in.nmaloth.maintenance.controllers.EndPoints;
import in.nmaloth.maintenance.model.combined.AccountsCombined;
import in.nmaloth.maintenance.model.combined.AccountsCombinedDTO;
import in.nmaloth.maintenance.model.dto.account.AccountAccumValuesDTO;
import in.nmaloth.maintenance.model.dto.account.AccountBasicAddDTO;
import in.nmaloth.maintenance.model.dto.account.AccountBasicDTO;
import in.nmaloth.maintenance.model.dto.account.AccountBasicUpdateDTO;
import in.nmaloth.maintenance.service.CombinedCreateService;
import in.nmaloth.maintenance.service.account.AccountAccumValuesService;
import in.nmaloth.maintenance.service.account.AccountBasicService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
public class AccountsController {

    private final AccountBasicService accountBasicService;
    private final AccountAccumValuesService accountAccumValuesService;
    private final CombinedCreateService combinedCreateService;


    public AccountsController(AccountBasicService accountBasicService,
                              AccountAccumValuesService accountAccumValuesService,
                              CombinedCreateService combinedCreateService) {

        this.accountBasicService = accountBasicService;
        this.accountAccumValuesService = accountAccumValuesService;
        this.combinedCreateService = combinedCreateService;
    }


    @PostMapping(EndPoints.ACCOUNTS)
    public Mono<ResponseEntity<AccountsCombinedDTO>> addNewAccountsRecord(@Valid @RequestBody AccountBasicAddDTO accountBasicAddDTO) {

        return combinedCreateService.createNewAccount(accountBasicAddDTO)
                .map(accountsCombinedDTO -> ResponseEntity.status(HttpStatus.CREATED).body(accountsCombinedDTO))

                ;

    }

    @GetMapping(EndPoints.ACCOUNTS_ACCOUNT_NBR)
    public Mono<AccountBasicDTO> getAccounts(@PathVariable String accountNumber) {

        return accountBasicService.fetchAccountBasicInfo(accountNumber)
                .map(accountBasic -> accountBasicService.convertToDTO(accountBasic));
    }

    @DeleteMapping(EndPoints.ACCOUNTS_ACCOUNT_NBR)
    public Mono<AccountBasicDTO> deleteAccounts(@PathVariable String accountNumber) {

        return accountBasicService.deleteAccountBasic(accountNumber)
                .map(accountBasic -> accountBasicService.convertToDTO(accountBasic));
    }

    @PutMapping(EndPoints.ACCOUNTS)
    public Mono<AccountsCombinedDTO> updateAccounts(@Valid @RequestBody AccountBasicUpdateDTO accountBasicUpdateDTO) {

        Mono<AccountBasic> accountBasicMono = accountBasicService.updateAccountBasic(accountBasicUpdateDTO);

        Mono<AccountAccumValues> accountAccumValuesMono = accountAccumValuesService
                .updateAccumValues(accountBasicUpdateDTO.getBalanceTypesDTOListAdd(),accountBasicUpdateDTO.getBalanceTypesDTOListDelete(),accountBasicUpdateDTO.getAccountId());

        return accountBasicMono.zipWith(accountAccumValuesMono)
                .map(tuple2 -> AccountsCombinedDTO.builder()
                        .accountBasicDTO(accountBasicService.convertToDTO(tuple2.getT1()))
                        .accountAccumValuesDTO(accountAccumValuesService.convertToDTO(tuple2.getT2()))
                        .build())
                ;

    }

    @GetMapping(EndPoints.ACCOUNTS_LIMITS_ACCOUNT_NBR)
    public Mono<AccountAccumValuesDTO> fetchAccountLimits(@PathVariable String accountNumber) {
        return accountAccumValuesService.fetchAccountAccumValuesByAccountId(accountNumber)
                .map(accountAccumValues -> accountAccumValuesService.convertToDTO(accountAccumValues))
                ;
    }


}
