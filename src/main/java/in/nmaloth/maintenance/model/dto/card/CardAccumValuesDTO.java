package in.nmaloth.maintenance.model.dto.card;

import in.nmaloth.entity.card.LimitType;
import in.nmaloth.entity.card.PeriodicCardAmount;
import in.nmaloth.entity.card.PeriodicType;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.Map;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardAccumValuesDTO {

    private String cardId;

    private Integer org;
    private Integer product;

    private List<PeriodicCardLimitDTO> periodicCardLimitDTOList;

    private List<PeriodicCardLimitDTO> periodicCardAccumulatedValueList;
}
