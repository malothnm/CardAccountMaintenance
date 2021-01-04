package in.nmaloth.maintenance.service.cards;

import in.nmaloth.entity.card.CardAccumulatedValues;
import in.nmaloth.maintenance.model.dto.card.CardAccumValuesDTO;
import in.nmaloth.maintenance.model.dto.card.PeriodicCardLimitDTO;
import in.nmaloth.maintenance.model.dto.card.PeriodicLimitSet;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CardAccumValuesService {

    Mono<CardAccumulatedValues> fetchCardAccumValuesByCardNumber(String cardId);
    Mono<Optional<CardAccumulatedValues>> fetchCardAccumValuesOptional(String cardId);

    Mono<CardAccumulatedValues> deleteCardAccumValuesByCardNumber(String cardId);
    Mono<CardAccumulatedValues> saveAccountAccumValues(CardAccumulatedValues cardAccumulatedValues);

    Mono<CardAccumulatedValues> initializeCardAccumValues(String cardId, List<PeriodicCardLimitDTO> periodicCardLimitDTOList, int org, int product);

    Mono<CardAccumulatedValues> updateNewAccumValues(  List<PeriodicCardLimitDTO> periodicCardLimitDTOAddList,
                                                 List<PeriodicCardLimitDTO> periodicCardLimitDTODeleteList,
                                                 String cardId);


    CardAccumValuesDTO convertToDTO(CardAccumulatedValues cardAccumulatedValues);
    CardAccumulatedValues initializeAccumValues(String cardId, List<PeriodicCardLimitDTO> periodicCardLimitDTOList, int org, int product);
    CardAccumulatedValues updateNewAccumValues(  List<PeriodicCardLimitDTO> periodicCardLimitDTOAddList,
                                                 List<PeriodicCardLimitDTO> periodicCardLimitDTODeleteList,
                                                 CardAccumulatedValues cardAccumulatedValues);


}
