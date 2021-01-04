package in.nmaloth.maintenance.service.instrument;

import in.nmaloth.entity.card.CardsBasic;
import in.nmaloth.entity.instrument.Instrument;
import in.nmaloth.maintenance.model.dto.instrument.InstrumentAddDTO;
import in.nmaloth.maintenance.model.dto.instrument.InstrumentDto;
import in.nmaloth.maintenance.model.dto.instrument.InstrumentUpdateDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface InstrumentService {

    Mono<Instrument> createNewInstrument(InstrumentAddDTO instrumentAddDTO);
    Mono<Instrument> updateInstrument(InstrumentUpdateDTO instrumentUpdateDTO);
    Mono<Instrument> fetchInstrument(String instrumentNumber);
    Flux<Instrument> fetchAllInstrumentsForCard(String cardId);
    Mono<Instrument> deleteInstrument(String instrumentNumber);
    Flux<Instrument> deleteAllInstrumentsForCard(String cardNumber);
    Mono<Instrument> saveInstrument(Instrument instrument);

    Instrument createInstrumentFromAddDTO(InstrumentAddDTO instrumentAddDTO, CardsBasic cardsBasic);
    Instrument createInstrumentFromUpdateDTO(InstrumentUpdateDTO instrumentUpdateDTO,Instrument instrument);
    Instrument updateAccounts(Instrument instrument,CardsBasic cardsBasic);
    InstrumentDto createDTOFromInstrument(Instrument instrument);


}
