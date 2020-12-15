package in.nmaloth.maintenance.model.combined;

import in.nmaloth.entity.card.CardsBasic;
import in.nmaloth.entity.card.Plastic;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardsPlasticCombined {

    private CardsBasic cardsBasic;
    private List<Plastic> plasticList;

}
