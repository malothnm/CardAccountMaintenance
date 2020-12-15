package in.nmaloth.maintenance.model.dto.product;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductCardGenUpdateDTO {

    @NotNull
    @PositiveOrZero
    private Integer org;

    @NotNull
    @PositiveOrZero
    private Integer product;

    private String lastGeneratedCardNumber;

    private Integer numberIncrementBy;

    private String endingGeneratedCardNumber;

    private String startingCardNumber;

}
