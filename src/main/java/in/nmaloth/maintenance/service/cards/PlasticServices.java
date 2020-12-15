package in.nmaloth.maintenance.service.cards;

import in.nmaloth.entity.card.CardsBasic;
import in.nmaloth.entity.card.Plastic;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.maintenance.model.dto.card.CardBasicDTO;
import in.nmaloth.maintenance.model.dto.card.PlasticUpdateDto;
import in.nmaloth.maintenance.model.dto.card.PlasticsDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PlasticServices {


    Mono<Plastic> fetchPlasticInfo(String plasticId,String cardNumber);
    Mono<Plastic> deletePlasticInfo(String plasticId,String cardNumber);
    Flux<Plastic> fetchAllPlasticInfo(String cardNumber);
    Flux<Plastic> deleteAllPlastics(String cardNumber);
    Mono<Plastic> savePlastic(Plastic plastic);
    Mono<Plastic> createNewPlastic(PlasticUpdateDto plasticUpdateDto);
    Mono<Plastic> updatePlasticData(PlasticUpdateDto plasticUpdateDto);
    void validatePlasticForNewPlastic(PlasticUpdateDto plasticUpdateDto);

    PlasticsDTO convertPlasticDTO(Plastic plastic);
    Plastic updatePlastic(PlasticUpdateDto plasticUpdateDto, CardsBasic cardsBasic, ProductDef productDef, List<Plastic> plasticList);

}
