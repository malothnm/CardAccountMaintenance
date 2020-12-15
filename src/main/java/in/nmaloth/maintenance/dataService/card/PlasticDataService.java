package in.nmaloth.maintenance.dataService.card;

import in.nmaloth.entity.card.Plastic;
import in.nmaloth.entity.card.PlasticKey;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public interface PlasticDataService {

    Mono<Plastic> savePlastic(Plastic plastic);
    Flux<Plastic>  findAllPlastic(String cardNumber);
    Mono<Optional<Plastic>> findPlasticById(String id,String cardNumber);
    Flux<Plastic> deleteAllPlastics(String cardNumber);
    Mono<List<Plastic>> findListPlastic(String cardNumber);
    Mono<Optional<Plastic>> deletePlasticById(String id, String cardNumber);
}
