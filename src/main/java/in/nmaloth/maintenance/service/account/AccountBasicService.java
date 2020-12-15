package in.nmaloth.maintenance.service.account;

import in.nmaloth.entity.account.AccountBasic;
import in.nmaloth.entity.card.CardsBasic;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.maintenance.model.dto.account.AccountBasicAddDTO;
import in.nmaloth.maintenance.model.dto.account.AccountBasicDTO;
import in.nmaloth.maintenance.model.dto.account.AccountBasicUpdateDTO;
import in.nmaloth.maintenance.model.dto.card.CardBasicAddDTO;
import in.nmaloth.maintenance.model.dto.card.CardBasicDTO;
import in.nmaloth.maintenance.model.dto.card.CardBasicUpdateDTO;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface AccountBasicService {

    Mono<AccountBasic> createNewAccountBasic(AccountBasicAddDTO accountBasicAddDTO);
    Mono<AccountBasic> updateAccountBasic(AccountBasicUpdateDTO accountBasicUpdateDTO);
    Mono<AccountBasic> fetchAccountBasicInfo(String accountNumber);
    Mono<Optional<AccountBasic>> fetchAccountBasicInfoOptional(String accountNumber);
    Mono<AccountBasic> deleteAccountBasic(String accountNumber);

    AccountBasicDTO convertToDTO(AccountBasic accountBasic);
    AccountBasic convertDTOToAccountBasic(AccountBasicAddDTO accountBasicAddDTO,ProductDef productDef);
    AccountBasic updateAccountBasicFromDTO(AccountBasicUpdateDTO accountBasicUpdateDTO, AccountBasic accountBasic,ProductDef productDef);


}
