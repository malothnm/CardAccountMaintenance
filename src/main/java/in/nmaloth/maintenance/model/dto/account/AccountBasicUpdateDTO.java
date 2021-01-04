package in.nmaloth.maintenance.model.dto.account;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AccountBasicUpdateDTO {

    @NotNull
    private String accountId;

    @Pattern(regexp = "0|1|2|3|4|5|6")
    private String blockType;

    @Length(max = 3,min = 3)
    private String billingCurrencyCode;

    @Pattern(regexp = "S|CR|L|CU|U|P")
    private String accountType;

    private String customerNumber;
    private String corporateNumber;

    @Valid
    private List<BalanceTypesDTO> balanceTypesDTOListAdd;
    @Valid
    private List<BalanceTypesDTO> balanceTypesDTOListDelete;

}
