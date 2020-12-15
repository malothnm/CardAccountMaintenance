package in.nmaloth.maintenance.model.dto.account;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class AccountBalancesDTO {

    private String balanceType;
    private long postedBalance;
    private long memoDb;
    private long memoCr;

}
