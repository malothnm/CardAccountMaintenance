package in.nmaloth.maintenance.model.dto.product;

import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ProductDefUpdateDTO {

    @NotNull
    @PositiveOrZero
    private Integer org;

    @NotNull
    @PositiveOrZero
    private Integer product;

    @PositiveOrZero
    private Integer cardsValidityMonthNew;

    @PositiveOrZero
    private Integer cardsValidityMonthReplace;

    @PositiveOrZero
    private Integer cardsValidityMonthReIssue;

    @PositiveOrZero
    private Integer dateRangeNewExpDate;

    @PositiveOrZero
    private Integer cardsWaiverActivationDays;

    @PositiveOrZero
    private Integer daysToCardsValid;

    @PositiveOrZero
    private Integer cardsReturn;

    private Boolean cardsActivationRequired;

    @Pattern(regexp = "S|CR|CU|L|U|P")
    private String primaryAccountType;

    @Length(max = 3,min = 3)
    private String billingCurrencyCode;

    @PositiveOrZero
    @Max(1000)
    private Integer serviceCode;

    @Valid
    private List<LimitPercentDTO> limitPercentListAdd;

    @Valid
    private List<LimitPercentDTO> limitPercentListDelete;



}
