package in.nmaloth.maintenance.dataService.instrument;

import in.nmaloth.entity.instrument.Instrument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface InstrumentDataService {

    Mono<Instrument> saveInstrument(Instrument instrument);
    Flux<Instrument> findAllInstrumentsByCardNumber(String cardNumber);
    Mono<Optional<Instrument>> findInstrumentById(String instrumentNumber);
    Flux<Instrument> deleteAllPlastics(String cardNumber);
    Mono<Optional<Instrument>> deletePlasticById(String instrumentNumber);
}
