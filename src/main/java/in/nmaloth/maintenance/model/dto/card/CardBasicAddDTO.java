package in.nmaloth.maintenance.model.dto.card;

import in.nmaloth.maintenance.model.dto.account.AccountDefDTO;
import in.nmaloth.maintenance.model.dto.card.CardLimitsDTO;
import lombok.*;
import org.hibernate.validator.constraints.CreditCardNumber;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardBasicAddDTO {


    private String cardId;

    @Pattern(regexp = "P|S|A")
    @NotNull
    private String cardholderType;

    @Pattern(regexp = "0|1|2|3|4|5|6")
    @Length(max = 1)
    private String blockType;

    @Positive
    @NotNull
    private Integer org;

    @Positive
    @NotNull
    private Integer product;

    @PositiveOrZero
    private Integer waiverDaysActivation;

    @Pattern(regexp = "A|I|F|P|T")
    private String cardStatus;

    @NotNull
    private Set<AccountDefDTO> accountDefDTOSet;

    @NotNull
    private String customerNumber;

    private String corporateNumber;


    @Valid
    private List<PeriodicCardLimitDTO> periodicCardLimitDTOList;




}
