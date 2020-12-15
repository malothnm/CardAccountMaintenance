package in.nmaloth.maintenance.service.cards;

import in.nmaloth.entity.card.CardAccumulatedValues;
import in.nmaloth.maintenance.model.dto.card.CardAccumValuesDTO;
import in.nmaloth.maintenance.model.dto.card.PeriodicLimitSet;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.Set;

public interface CardAccumValuesService {

    Mono<CardAccumulatedValues> fetchCardAccumValuesByCardNumber(String cardNumber);
    Mono<Optional<CardAccumulatedValues>> fetchCardAccumValuesOptional(String cardNumber);

    Mono<CardAccumulatedValues> deleteCardAccumValuesByCardNumber(String cardNumber);
    Mono<CardAccumulatedValues> saveAccountAccumValues(CardAccumulatedValues cardAccumulatedValues);

    CardAccumValuesDTO convertToDTO(CardAccumulatedValues cardAccumulatedValues);
    CardAccumulatedValues initializeAccumValues(String cardNumber, Set<PeriodicLimitSet> periodicLimitDTOSet, int org, int product);
    CardAccumulatedValues updateNewAccumValues( Set<PeriodicLimitSet> periodicLimitDTOSet, CardAccumulatedValues cardAccumulatedValues);


}
