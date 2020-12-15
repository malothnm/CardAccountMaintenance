package in.nmaloth.maintenance.controllers.product;

import in.nmaloth.maintenance.controllers.EndPoints;
import in.nmaloth.maintenance.exception.AlreadyPresentException;
import in.nmaloth.maintenance.model.dto.product.DeclineReasonDefDTO;
import in.nmaloth.maintenance.model.dto.product.DeclineReasonUpdateDefDTO;
import in.nmaloth.maintenance.service.product.DeclineReasonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
public class DeclineReasonsController {

    private final DeclineReasonService declineReasonService;

    public DeclineReasonsController(DeclineReasonService declineReasonService) {
        this.declineReasonService = declineReasonService;
    }


    @PostMapping(EndPoints.DECLINE_REASONS)
    public Mono<ResponseEntity<DeclineReasonDefDTO>> createNewDeclineReasonsDTO(@Valid @RequestBody DeclineReasonDefDTO declineReasonDefDTO){

        return declineReasonService.fetchDeclineReasonOptional(declineReasonDefDTO.getServiceName())
                .doOnNext(declineReasonDefOptional -> {
                    if(declineReasonDefOptional.isPresent()){
                        throw new AlreadyPresentException(" Decline Reason Already Present");
                    }
                })
                .flatMap(declineReasonDefOptional -> declineReasonService.createNewDeclineReasonDTO(declineReasonDefDTO))
                .map(declineReasonDefDTO1 -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(declineReasonDefDTO1)
                );
    }

    @GetMapping(EndPoints.DECLINE_REASONS_SERVICE_NAME)
    public Mono<DeclineReasonDefDTO> getDeclineReasonForServiceName(@PathVariable String serviceName){
        return declineReasonService.fetchDeclineReason(serviceName);
    }

    @GetMapping(EndPoints.DECLINE_REASONS)
    public Flux<DeclineReasonDefDTO> getAllDeclineReasonDef(){
        return declineReasonService.findAllDeclineReasons();
    }

    @DeleteMapping(EndPoints.DECLINE_REASONS_SERVICE_NAME)
    public Mono<DeclineReasonDefDTO> deleteDeclineReasonForServiceName(@PathVariable String serviceName){
        return declineReasonService.deleteDeclineReason(serviceName);
    }

    @PutMapping(EndPoints.DECLINE_REASONS)
    public Mono<DeclineReasonDefDTO> putDeclineReasonForServiceName(@Valid @RequestBody DeclineReasonUpdateDefDTO declineReasonUpdateDefDTO){
        return declineReasonService.updateDeclineReason(declineReasonUpdateDefDTO);
    }

}
