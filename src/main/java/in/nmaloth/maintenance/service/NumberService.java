package in.nmaloth.maintenance.service;

import in.nmaloth.entity.instrument.InstrumentType;
import reactor.core.publisher.Mono;

public interface NumberService {

    Mono<String> generateNewCustomerNumber();
    Mono<String> generateNewAccountNumber();
    Mono<String> generateNewCardNumber(Integer org, Integer product );
    Mono<String> generateInstrumentNumber(InstrumentType instrumentType,Integer org, Integer product);

}
