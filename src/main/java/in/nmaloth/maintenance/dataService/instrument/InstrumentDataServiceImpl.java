package in.nmaloth.maintenance.dataService.instrument;

import in.nmaloth.entity.instrument.Instrument;
import in.nmaloth.maintenance.repository.instrument.InstrumentRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class InstrumentDataServiceImpl implements InstrumentDataService {

    private final InstrumentRepository instrumentRepository;

    public InstrumentDataServiceImpl(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    @Override
    public Mono<Instrument> saveInstrument(Instrument instrument) {

        CompletableFuture<Instrument> completableFuture = CompletableFuture
                .supplyAsync(()-> instrumentRepository.save(instrument));

        return Mono.fromFuture(completableFuture);
    }

    @Override
    public Flux<Instrument> findAllInstrumentsByCardNumber(String cardNumber) {

        CompletableFuture<Iterable<Instrument>> completableFuture = CompletableFuture
                .supplyAsync(() -> instrumentRepository.findAllByCardNumber(cardNumber));


        return Mono.fromFuture(completableFuture)
                .flatMapMany(instruments -> {
                    return Flux.fromIterable(instruments);
                })
                ;
    }

    



    @Override
    public Mono<Optional<Instrument>> findInstrumentById(String instrumentNumber) {

        CompletableFuture<Optional<Instrument>> completableFuture = CompletableFuture
                .supplyAsync(()-> instrumentRepository.findById(instrumentNumber));

        return Mono.fromFuture(completableFuture);
    }

    @Override
    public Flux<Instrument> deleteAllPlastics(String cardNumber) {

        return findAllInstrumentsByCardNumber(cardNumber)
                .doOnNext(instrument -> instrumentRepository.delete(instrument));

    }

    @Override
    public Mono<Optional<Instrument>> deletePlasticById(String instrumentNumber) {

        return findInstrumentById(instrumentNumber)
            .doOnNext(instrumentOptional -> {
                if(instrumentOptional.isPresent()){
                    instrumentRepository.delete(instrumentOptional.get());
                }
            })  ;

    }
}
