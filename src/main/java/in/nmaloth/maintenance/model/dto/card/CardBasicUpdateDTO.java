package in.nmaloth.maintenance.model.dto.card;

import in.nmaloth.maintenance.model.dto.account.AccountDefDTO;
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
public class CardBasicUpdateDTO {


    @NotNull
    private String cardId;

    @Pattern(regexp = "P|S|A")
    private String cardHolderType;

    @Pattern(regexp = "0|1|2|3|4|5|6")
    @Length(max = 1)
    private String blockType;

    @PositiveOrZero
    private Integer waiverDaysActivation;

    @PositiveOrZero
    private Integer cardsReturned;

    @Pattern(regexp = "A|I|F|P|T")
    private String cardStatus;

    private String customerNumber;

    private String corporateNumber;

    @Valid
    private Set<AccountDefDTO> accountDefDTOSetAdd;
    @Valid
    private Set<AccountDefDTO> accountDefDTOSetDelete;

    private List<PeriodicCardLimitDTO> periodicCardLimitDTOAddList;

    private List<PeriodicCardLimitDTO> periodicCardLimitDTODeleteList;





}
