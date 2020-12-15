package in.nmaloth.maintenance.model.dto.card;

import lombok.*;
import org.hibernate.validator.constraints.CreditCardNumber;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlasticUpdateDto {

    @CreditCardNumber
    @NotNull
    private String cardNumber;

    private Boolean cardActivate;


    private String plasticId;

    private Boolean dynamicCVV;

    @PositiveOrZero
    private Integer emergencyReplCardsExpiryDays;

    private LocalDate expiryDate;

    @Pattern(regexp = "0|1|2|3|4|5")
    private String cardAction;

}
