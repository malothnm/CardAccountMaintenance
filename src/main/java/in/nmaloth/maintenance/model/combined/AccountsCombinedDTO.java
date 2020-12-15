package in.nmaloth.maintenance.model.combined;

import in.nmaloth.entity.account.AccountBasic;
import in.nmaloth.maintenance.model.dto.account.AccountAccumValuesDTO;
import in.nmaloth.maintenance.model.dto.account.AccountBasicDTO;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountsCombinedDTO {

    private AccountBasicDTO accountBasicDTO;
    private AccountAccumValuesDTO accountAccumValuesDTO;

}
