package in.nmaloth.maintenance.dataService.product;

import in.nmaloth.entity.product.DeclineReasonDef;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface DeclineReasonDataService {

    Mono<Optional<DeclineReasonDef>> fetchDeclineReason(String serviceName);
    Mono<DeclineReasonDef> saveDeclineReason(DeclineReasonDef declineReasonDef);
    Mono<Optional<DeclineReasonDef>> deleteDeclineReasonDef(String serviceName);
    Flux<DeclineReasonDef> findAllDeclineReasonDef();

}
