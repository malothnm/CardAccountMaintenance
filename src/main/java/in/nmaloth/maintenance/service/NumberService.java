package in.nmaloth.maintenance.service;

import in.nmaloth.entity.instrument.InstrumentType;
import reactor.core.publisher.Mono;

public interface NumberService {

    Mono<String> generateNewCustomerId();
    Mono<String> generateNewAccountId();
    Mono<String> generateNewCardId();
    Mono<String> generateInstrumentNumber(InstrumentType instrumentType,Integer org, Integer product);

}
