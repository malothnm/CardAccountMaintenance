package in.nmaloth.maintenance.model.combined;

import in.nmaloth.maintenance.model.dto.card.CardAccumValuesDTO;
import in.nmaloth.maintenance.model.dto.card.CardBasicDTO;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardsCombinedDTO {

    private CardBasicDTO cardBasicDTO;
    private CardAccumValuesDTO cardAccumValuesDTO;
}
