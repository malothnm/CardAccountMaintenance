package in.nmaloth.maintenance.model.combined;

import in.nmaloth.entity.account.AccountAccumValues;
import in.nmaloth.entity.account.AccountBasic;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountsCombined {

    private AccountBasic accountBasic;
    private AccountAccumValues accountAccumValues;

}
