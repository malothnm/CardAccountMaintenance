package in.nmaloth.maintenance.service.cards;

import in.nmaloth.entity.card.CardsBasic;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.maintenance.model.dto.card.CardBasicAddDTO;
import in.nmaloth.maintenance.model.dto.card.CardBasicDTO;
import in.nmaloth.maintenance.model.dto.card.CardBasicUpdateDTO;
import reactor.core.publisher.Mono;

import javax.swing.text.html.Option;
import java.util.Optional;


public interface CardsBasicService {


     Mono<CardsBasic> createNewCardsRecord(CardBasicAddDTO cardBasicAddDTO);
     Mono<CardsBasic> updateCards(CardBasicUpdateDTO cardBasicUpdateDTO);
     Mono<CardsBasic> fetchCardInfo(String cardNumber);
     Mono<Optional<CardsBasic>> fetchCardInfOptional(String cardNumber);
     Mono<CardsBasic> deleteCardInfo(String cardNumber);

     CardBasicDTO convertToDTO(CardsBasic cardsBasic);
     CardsBasic convertDTOToCardBasic(CardBasicAddDTO cardBasicAddDTO, ProductDef productDef);
     CardsBasic updateCardBasicFromDTO(CardBasicUpdateDTO cardBasicUpdateDTO, ProductDef productDef,CardsBasic cardsBasic);



}
