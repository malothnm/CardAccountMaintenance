package in.nmaloth.maintenance.model.dto.instrument;

import in.nmaloth.maintenance.model.dto.account.AccountDefDTO;
import lombok.*;
import org.hibernate.validator.constraints.CreditCardNumber;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrumentAddDTO {


    private String instrumentNumber;
    @NotNull
    @Pattern(regexp = "0|1|2|3|4|5|6")
    private String instrumentType;

    @NotNull
    private Boolean active;

    @NotNull
    @CreditCardNumber
    private String cardNumber;


    @Pattern(regexp = "0|1|2|3|4|5|6")
    @Length(max = 1)
    private String blockType;

    private String expiryDate;

    @NotNull
    @PositiveOrZero
    private int org;

    @NotNull
    @PositiveOrZero
    private int product;


}
