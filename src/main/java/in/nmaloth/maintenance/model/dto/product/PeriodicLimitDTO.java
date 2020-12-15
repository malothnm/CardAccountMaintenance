package in.nmaloth.maintenance.model.dto.product;

import in.nmaloth.entity.card.LimitType;
import in.nmaloth.entity.card.PeriodicType;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.PositiveOrZero;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PeriodicLimitDTO {

    @Pattern(regexp = "S|D|M|Y")
    @NotNull
    private String periodicType;

    @Pattern(regexp = "N|C|R|O|Q|A")
    @NotNull
    private String limitType;

    @PositiveOrZero
    @NotNull
    private Integer limitNumber;

    @PositiveOrZero
    @NotNull
    private Long limitAmount;

}
