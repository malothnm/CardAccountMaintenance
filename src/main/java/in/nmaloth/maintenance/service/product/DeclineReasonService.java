package in.nmaloth.maintenance.service.product;

import in.nmaloth.entity.product.DeclineReason;
import in.nmaloth.entity.product.DeclineReasonDef;
import in.nmaloth.maintenance.model.dto.product.DeclineReasonDefDTO;
import in.nmaloth.maintenance.model.dto.product.DeclineReasonUpdateDefDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface DeclineReasonService {

    DeclineReasonDef createFromDTO(DeclineReasonDefDTO declineReasonDefDTO);
    DeclineReasonDefDTO createFrom(DeclineReasonDef declineReasonDef);
    DeclineReasonDef updateFromDTO(DeclineReasonDef declineReasonDef, DeclineReasonUpdateDefDTO declineReasonUpdateDefDTO);


    Mono<DeclineReasonDefDTO> createNewDeclineReasonDTO(DeclineReasonDefDTO declineReasonDefDTO);
    Mono<DeclineReasonDefDTO> updateDeclineReason(DeclineReasonUpdateDefDTO declineReasonUpdateDefDTO);
    Mono<DeclineReasonDefDTO> fetchDeclineReason(String serviceName);
    Mono<Optional<DeclineReasonDef>> fetchDeclineReasonOptional(String serviceName);
    Mono<DeclineReasonDefDTO> deleteDeclineReason(String serviceName);
    Flux<DeclineReasonDefDTO> findAllDeclineReasons();



}
