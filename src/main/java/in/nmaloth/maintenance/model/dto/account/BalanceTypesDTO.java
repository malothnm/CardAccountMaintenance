package in.nmaloth.maintenance.model.dto.account;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class BalanceTypesDTO {

    @Pattern(regexp = "0|1|2|3|4|5|6|7")
    @NotNull
    private String balanceType;

    @PositiveOrZero
    @NotNull
    private Long limitAmount;
}
