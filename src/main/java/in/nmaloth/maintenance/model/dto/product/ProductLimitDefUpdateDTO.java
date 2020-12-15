package in.nmaloth.maintenance.model.dto.product;

import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ProductLimitDefUpdateDTO {

    @NotNull
    @PositiveOrZero
    private Integer org;

    @NotNull
    @PositiveOrZero
    private Integer product;


    @Valid
    private List<PeriodicLimitDTO> periodicLimitDTOListAdd;

    @Valid
    private List<PeriodicLimitDTO> periodicLimitDTOListDelete;


}
