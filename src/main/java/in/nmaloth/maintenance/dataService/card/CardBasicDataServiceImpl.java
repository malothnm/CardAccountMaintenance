package in.nmaloth.maintenance.dataService.card;

import in.nmaloth.entity.card.CardsBasic;
import in.nmaloth.maintenance.repository.card.CardsBasicRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class CardBasicDataServiceImpl implements CardBasicDataService {

    private final CardsBasicRepository cardsBasicRepository;

    public CardBasicDataServiceImpl(CardsBasicRepository cardsBasicRepository) {
        this.cardsBasicRepository = cardsBasicRepository;
    }

    @Override
    public Mono<CardsBasic> saveCardBasicValues(CardsBasic cardsBasic) {

        CompletableFuture<CardsBasic> completableFuture = CompletableFuture
                .supplyAsync(()-> cardsBasicRepository.save(cardsBasic));
        return Mono.fromFuture(completableFuture);
    }

    @Override
    public Mono<Optional<CardsBasic>> findCardBasicValuesById(String cardNumber) {

        CompletableFuture<Optional<CardsBasic>> completableFuture = CompletableFuture
                .supplyAsync(()-> cardsBasicRepository.findById(cardNumber));
        return Mono.fromFuture(completableFuture);
    }

    @Override
    public Mono<Optional<CardsBasic>> deleteCardBasicByCardNumber(String cardNumber) {

        return findCardBasicValuesById(cardNumber)
                .doOnNext(cardsBasicOptional ->{
                    if(cardsBasicOptional.isPresent()){
                        cardsBasicRepository.delete(cardsBasicOptional.get());
                    }
                } );
    }
}
