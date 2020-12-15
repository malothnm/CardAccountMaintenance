package in.nmaloth.maintenance.controllers.instrument;

import in.nmaloth.maintenance.controllers.EndPoints;
import in.nmaloth.maintenance.model.dto.instrument.InstrumentAddDTO;
import in.nmaloth.maintenance.model.dto.instrument.InstrumentDto;
import in.nmaloth.maintenance.model.dto.instrument.InstrumentUpdateDTO;
import in.nmaloth.maintenance.service.CombinedCreateService;
import in.nmaloth.maintenance.service.instrument.InstrumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
public class InstrumentController {

    private final InstrumentService instrumentService;
    private final CombinedCreateService combinedCreateService;


    public InstrumentController(InstrumentService instrumentService, CombinedCreateService combinedCreateService) {
        this.instrumentService = instrumentService;
        this.combinedCreateService = combinedCreateService;
    }


    @PostMapping(EndPoints.INSTRUMENT)
    public Mono<ResponseEntity<InstrumentDto>> createNewInstrument(@Valid @RequestBody InstrumentAddDTO instrumentAddDTO){

        return combinedCreateService.createNewInstrument(instrumentAddDTO)
                .map(instrumentDto -> ResponseEntity.status(HttpStatus.CREATED).body(instrumentDto));
    }

    @GetMapping(EndPoints.INSTRUMENT_NBR)
    public Mono<InstrumentDto> fetchInstrumentNumber(@PathVariable String instrumentNumber){

        return instrumentService.fetchInstrument(instrumentNumber)
                .map(instrument -> instrumentService.createDTOFromInstrument(instrument));
    }

    @DeleteMapping(EndPoints.INSTRUMENT_NBR)
    public Mono<InstrumentDto> deleteInstrumentNumber(@PathVariable String instrumentNumber){

        return instrumentService.deleteInstrument(instrumentNumber)
                .map(instrument -> instrumentService.createDTOFromInstrument(instrument));
    }

    @PutMapping(EndPoints.INSTRUMENT)
    public Mono<InstrumentDto> updateinstrument(@Valid @RequestBody InstrumentUpdateDTO instrumentUpdateDTO){

        return instrumentService.updateInstrument(instrumentUpdateDTO)
                .map(instrument -> instrumentService.createDTOFromInstrument(instrument));

    }

    @GetMapping(EndPoints.CARD_INSTRUMENT)
    public Flux<InstrumentDto> fetchAllInstrumentsForCard(@PathVariable String cardNumber){

        return instrumentService.fetchAllInstrumentsForCard(cardNumber)
                .map(instrument -> instrumentService.createDTOFromInstrument(instrument));

    }

    @DeleteMapping(EndPoints.CARD_INSTRUMENT)
    public Flux<InstrumentDto> deleteAllInstrumentsForCard(@PathVariable String cardNumber){

        return instrumentService.deleteAllInstrumentsForCard(cardNumber)
                .map(instrument -> instrumentService.createDTOFromInstrument(instrument));

    }
}
