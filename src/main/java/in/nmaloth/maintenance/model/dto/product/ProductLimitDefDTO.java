package in.nmaloth.maintenance.model.dto.product;

import lombok.*;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ProductLimitDefDTO {

    @NotNull
    @PositiveOrZero
    private Integer org;

    @NotNull
    @PositiveOrZero
    private Integer product;


    @NotNull
    private List<PeriodicLimitDTO> periodicLimitDTOList;

}
