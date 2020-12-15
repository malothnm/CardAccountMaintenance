package in.nmaloth.maintenance.dataService.card;

import in.nmaloth.entity.card.CardsBasic;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface CardBasicDataService {

    Mono<CardsBasic> saveCardBasicValues(CardsBasic cardsBasic);
    Mono<Optional<CardsBasic>> findCardBasicValuesById(String cardNumber);
    Mono<Optional<CardsBasic>> deleteCardBasicByCardNumber(String cardNumber);
}
