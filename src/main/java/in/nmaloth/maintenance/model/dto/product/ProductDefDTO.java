package in.nmaloth.maintenance.model.dto.product;

import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductDefDTO {

    @NotNull
    @PositiveOrZero
    private Integer org;

    @NotNull
    @PositiveOrZero
    private Integer product;

    @NotNull
    @PositiveOrZero
    private Integer cardsValidityMonthNew;

    @NotNull
    @PositiveOrZero
    private Integer cardsValidityMonthReplace;

    @NotNull
    @PositiveOrZero
    private Integer cardsValidityMonthReIssue;

    @NotNull
    @PositiveOrZero
    private Integer dateRangeNewExpDate;

    @NotNull
    @PositiveOrZero
    private Integer cardsWaiverActivationDays;

    @NotNull
    @PositiveOrZero
    private Integer daysToCardsValid;

    @NotNull
    private Boolean cardsActivationRequired;

    @NotNull
    @PositiveOrZero
    private Integer cardsReturn;

    @NotNull
    @Pattern(regexp = "S|CR|CU|L|U|P")
    private String primaryAccountType;

    @NotNull
    private String billingCurrencyCode;

    @NotNull
    @PositiveOrZero
    private Integer serviceCode;

    @Valid
    private List<LimitPercentDTO> limitPercents;

}
