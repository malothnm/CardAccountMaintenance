package in.nmaloth.maintenance.model.dto.card;

import in.nmaloth.entity.card.PeriodicType;
import lombok.*;

import javax.validation.constraints.Pattern;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class PeriodicCardLimitDTO {

    @Pattern(regexp = "S|D|M|Y")
    private String periodicType;

    private List<CardLimitsDTO> cardLimitsDTOList;

}
