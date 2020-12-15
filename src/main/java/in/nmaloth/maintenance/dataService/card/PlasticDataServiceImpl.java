package in.nmaloth.maintenance.dataService.card;

import in.nmaloth.entity.card.Plastic;
import in.nmaloth.entity.card.PlasticKey;
import in.nmaloth.maintenance.repository.card.PlasticRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class PlasticDataServiceImpl implements PlasticDataService {

    private final PlasticRepository plasticRepository;

    public PlasticDataServiceImpl(PlasticRepository plasticRepository) {
        this.plasticRepository = plasticRepository;
    }


    @Override
    public Mono<Plastic> savePlastic(Plastic plastic) {

        CompletableFuture<Plastic> completableFuture = CompletableFuture
                .supplyAsync(()-> plasticRepository.save(plastic));
        return Mono.fromFuture(completableFuture);
    }

    @Override
    public Flux<Plastic> findAllPlastic(String cardNumber) {
        CompletableFuture<Iterable<Plastic>> completableFuture = CompletableFuture
                .supplyAsync(()-> plasticRepository.findAllByCardNumber(cardNumber));

        return Mono.fromFuture(completableFuture)
                .flatMapMany(plastics -> {
                    return Flux.fromIterable(plastics);
                });
    }

    @Override
    public Mono<Optional<Plastic>> findPlasticById(String id,String cardNumber) {

        CompletableFuture<Optional<Plastic>> completableFuture = CompletableFuture
                .supplyAsync(()-> plasticRepository.findById(new PlasticKey(id,cardNumber)));

        return Mono.fromFuture(completableFuture);
    }

    @Override
    public Flux<Plastic> deleteAllPlastics(String cardNumber) {

        return findAllPlastic(cardNumber)
                .doOnNext(plastic -> plasticRepository.delete(plastic));


    }

    @Override
    public Mono<List<Plastic>> findListPlastic(String cardNumber) {
        CompletableFuture<Iterable<Plastic>> completableFuture = CompletableFuture
                .supplyAsync(()-> plasticRepository.findAllByCardNumber(cardNumber));

        return Mono.fromFuture(completableFuture)
                .map(plasticIterable -> {
                    List<Plastic> plasticList = new ArrayList<>();
                    plasticIterable.forEach(plasticList::add);
                    return plasticList;
                } )
                ;
    }

    @Override
    public Mono<Optional<Plastic>> deletePlasticById(String id,String cardNumber) {

        return findPlasticById(id, cardNumber)
                .doOnNext(plasticOptional ->{
                    if(plasticOptional.isPresent()){
                        plasticRepository.delete(plasticOptional.get());
                    }
                } );
    }
}
