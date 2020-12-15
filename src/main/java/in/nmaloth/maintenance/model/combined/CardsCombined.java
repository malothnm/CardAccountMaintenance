package in.nmaloth.maintenance.model.combined;

import in.nmaloth.entity.card.CardAccumulatedValues;
import in.nmaloth.entity.card.CardsBasic;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardsCombined {

    private CardsBasic cardsBasic;
    private CardAccumulatedValues cardAccumulatedValues;
}
