package in.nmaloth.maintenance.dataService.card;

import in.nmaloth.entity.card.CardAccumulatedValues;
import in.nmaloth.maintenance.repository.card.CardAccumulatedValuesRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class CardAccumulatedValuesDataServiceImpl implements CardAccumulatedValuesDataService {


    private final CardAccumulatedValuesRepository cardAccumulatedValuesRepository;

    public CardAccumulatedValuesDataServiceImpl(CardAccumulatedValuesRepository cardAccumulatedValuesRepository) {
        this.cardAccumulatedValuesRepository = cardAccumulatedValuesRepository;
    }


    @Override
    public Mono<CardAccumulatedValues> saveCardBasicValues(CardAccumulatedValues cardAccumulatedValues) {

        CompletableFuture<CardAccumulatedValues> completableFuture = CompletableFuture
                .supplyAsync(()-> cardAccumulatedValuesRepository.save(cardAccumulatedValues));

        return Mono.fromFuture(completableFuture);
    }

    @Override
    public Mono<Optional<CardAccumulatedValues>> findCardBasicValuesByCardNumber(String cardNumber) {

        CompletableFuture<Optional<CardAccumulatedValues>> completableFuture = CompletableFuture
                .supplyAsync(()-> cardAccumulatedValuesRepository.findById(cardNumber));
        return Mono.fromFuture(completableFuture);
    }

    @Override
    public Mono<Optional<CardAccumulatedValues>> deleteCardBasicByCardNumber(String cardNumber) {

        return findCardBasicValuesByCardNumber(cardNumber)
                .doOnNext(cardAccumulatedValuesOptional ->{
                    if(cardAccumulatedValuesOptional.isPresent()){
                        cardAccumulatedValuesRepository.delete(cardAccumulatedValuesOptional.get());
                    }
                } );
    }
}
