package in.nmaloth.maintenance.model.dto.account;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AccountBasicAddDTO {

    private String accountId;

    @NotNull
    @PositiveOrZero
    private Integer org;


    @NotNull
    @PositiveOrZero
    private Integer product;

    @NotNull
    @Pattern(regexp = "S|CR|L|CU|U|P")
    private String accountType;


    @Pattern(regexp = "0|1|2|3|4|5|6")
    private String blockType;

    @Length(max = 3,min = 3)
    @NotNull
    private String billingCurrencyCode;


    @NotNull
    private String customerNumber;

    private String corporateNumber;

    @NotNull
    @Valid
    private List<BalanceTypesDTO> balanceTypesDTOList;
}
