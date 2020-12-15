package in.nmaloth.maintenance.model.dto.product;

import lombok.*;
import org.springframework.data.gemfire.mapping.annotation.Indexed;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductCardGenDTO {

    @NotNull
    @PositiveOrZero
    private Integer org;

    @NotNull
    @PositiveOrZero
    private Integer product;

    @NotNull
    private String lastGeneratedCardNumber;

    @NotNull
    @PositiveOrZero
    private Integer numberIncrementBy;

    @NotNull
    @NotBlank
    private String endingGeneratedCardNumber;

    @NotNull
    @NotBlank
    private String startingCardNumber;

}
