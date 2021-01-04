package in.nmaloth.maintenance.model.dto.account;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class AccountAccumValuesDTO {


    private String accountId;
    private List<BalanceTypesDTO> balanceTypesDTOList;
    private List<AccountBalancesDTO> accountBalancesDTOList;

}
