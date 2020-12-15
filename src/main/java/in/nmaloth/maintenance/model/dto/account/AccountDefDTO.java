package in.nmaloth.maintenance.model.dto.account;

import in.nmaloth.entity.account.AccountType;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDefDTO {


    @NotNull
    private String accountNumber;

    @NotNull
    @Pattern(regexp = "S|CU|L|CR|U|P")
    private String accountType;

    @NotNull
    private String billingCurrencyCode;



}
