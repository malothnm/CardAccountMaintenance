package in.nmaloth.maintenance.dataService.card;

import in.nmaloth.entity.card.CardAccumulatedValues;
import in.nmaloth.entity.card.CardsBasic;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface CardAccumulatedValuesDataService {

    Mono<CardAccumulatedValues> saveCardBasicValues(CardAccumulatedValues cardAccumulatedValues);
    Mono<Optional<CardAccumulatedValues>> findCardBasicValuesByCardNumber(String cardNumber);
    Mono<Optional<CardAccumulatedValues>> deleteCardBasicByCardNumber(String cardNumber);
}
