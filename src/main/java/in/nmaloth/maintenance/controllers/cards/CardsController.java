package in.nmaloth.maintenance.controllers.cards;

import in.nmaloth.entity.card.*;
import in.nmaloth.maintenance.controllers.EndPoints;
import in.nmaloth.maintenance.model.combined.CardsCombined;
import in.nmaloth.maintenance.model.combined.CardsCombinedDTO;
import in.nmaloth.maintenance.model.dto.card.*;
import in.nmaloth.maintenance.service.CombinedCreateService;
import in.nmaloth.maintenance.service.cards.CardAccumValuesService;
import in.nmaloth.maintenance.service.cards.CardsBasicService;
import in.nmaloth.maintenance.service.cards.PlasticServices;
import in.nmaloth.maintenance.service.instrument.InstrumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class CardsController {

    private final CardsBasicService cardsBasicService;
    private final CardAccumValuesService cardAccumValuesService;
    private final CombinedCreateService combinedCreateService;
    private final PlasticServices plasticServices;
    private final InstrumentService instrumentService;



    public CardsController(CardsBasicService cardsBasicService,
                           CardAccumValuesService cardAccumValuesService,
                           CombinedCreateService combinedCreateService,
                           PlasticServices plasticServices,
                           InstrumentService instrumentService) {

        this.cardsBasicService = cardsBasicService;
        this.cardAccumValuesService = cardAccumValuesService;
        this.combinedCreateService = combinedCreateService;
        this.plasticServices = plasticServices;
        this.instrumentService = instrumentService;
    }

    @PostMapping(EndPoints.CARDS)
    public Mono<ResponseEntity<CardsCombinedDTO>> createNewCardsRecord(@Valid @RequestBody CardBasicAddDTO cardBasicAddDTO) {

        return combinedCreateService.createNewCard(cardBasicAddDTO)
                .map(cardsCombinedDTO -> ResponseEntity.status(HttpStatus.CREATED).body(cardsCombinedDTO));

    }

    @GetMapping(EndPoints.CARDS_CARD_NBR)
    public Mono<CardBasicDTO> findCardBasicDetails(@PathVariable String cardNumber){
        return cardsBasicService.fetchCardInfo(cardNumber)
                .map(cardsBasic -> cardsBasicService.convertToDTO(cardsBasic));
    }

    @GetMapping(EndPoints.CARDS_LIMITS_CARD_NBR)
    public Mono<CardAccumValuesDTO> findCardAccumulatedDetails(@PathVariable String cardNumber){
        return cardAccumValuesService.fetchCardAccumValuesByCardNumber(cardNumber)
                .map(cardAccumulatedValues -> cardAccumValuesService.convertToDTO(cardAccumulatedValues));
    }

    @DeleteMapping(EndPoints.CARDS_CARD_NBR)
    public Mono<CardBasicDTO> deleteCardBasicDetails(@PathVariable String cardNumber){
        return cardsBasicService.deleteCardInfo(cardNumber)
                .map(cardsBasic -> cardsBasicService.convertToDTO(cardsBasic));
    }

    @DeleteMapping(EndPoints.CARDS_LIMITS_CARD_NBR)
    public Mono<CardAccumValuesDTO> deleteCardAccumulatedDetails(@PathVariable String cardNumber){
        return cardAccumValuesService.deleteCardAccumValuesByCardNumber(cardNumber)
                .map(cardAccumulatedValues -> cardAccumValuesService.convertToDTO(cardAccumulatedValues));
    }


    @PutMapping(EndPoints.CARDS)
    public Mono<CardsCombinedDTO> updateCardsBasic(@Valid @RequestBody CardBasicUpdateDTO cardBasicUpdateDTO){

        Mono<CardsBasic> cardBasicMono = cardsBasicService.updateCards(cardBasicUpdateDTO);
        Mono<CardAccumulatedValues> cardAccumulatedValuesMono = cardAccumValuesService.fetchCardAccumValuesByCardNumber(cardBasicUpdateDTO.getCardNumber());

        return cardBasicMono.zipWith(cardAccumulatedValuesMono)
                .map(tuple2 ->  createCardsCombine(tuple2.getT1(),tuple2.getT2()))
                .flatMap(cardsCombined -> cardAccumValuesService.saveAccountAccumValues(cardsCombined.getCardAccumulatedValues())
                        .map(cardAccumulatedValues -> cardsCombined)
                )
                .doOnNext(cardsCombined -> {

                    if(cardBasicUpdateDTO.getAccountDefDTOSetDelete() != null || cardBasicUpdateDTO.getAccountDefDTOSetAdd() != null){
                        instrumentService.fetchAllInstrumentsForCard(cardsCombined.getCardsBasic().getCardNumber())
                                .map(instrument -> {
                                    instrument.setAccountDefSet(cardsCombined.getCardsBasic().getAccountDefSet());
                                    return instrument;
                                })
                                .flatMap(instrument -> instrumentService.saveInstrument(instrument))
                                .subscribe();
                    }
                } )
                .map(cardsCombined -> CardsCombinedDTO.builder()
                        .cardBasicDTO(cardsBasicService.convertToDTO(cardsCombined.getCardsBasic()))
                        .cardAccumValuesDTO(cardAccumValuesService.convertToDTO(cardsCombined.getCardAccumulatedValues()))
                        .build()
                )
                ;
    }

    @PostMapping(EndPoints.CARDS_NEW_PLASTIC)
    public Mono<PlasticsDTO> createNewPlastics(@Valid @RequestBody PlasticUpdateDto plasticUpdateDto){

        plasticServices.validatePlasticForNewPlastic(plasticUpdateDto);

        return plasticServices.createNewPlastic(plasticUpdateDto)
                .map(plastic -> plasticServices.convertPlasticDTO(plastic));

    }

    @GetMapping(EndPoints.CARDS_CARD_NBR_PLASTIC_ID)
    public Mono<PlasticsDTO> fetchPlasticInfo(@PathVariable String cardNumber, @PathVariable String plasticId){

        return plasticServices.fetchPlasticInfo(plasticId,cardNumber)
                .map(plastic -> plasticServices.convertPlasticDTO(plastic));

    }

    @DeleteMapping(EndPoints.CARDS_CARD_NBR_PLASTIC_ID)
    public Mono<PlasticsDTO> deletePlasticInfo(@PathVariable String cardNumber, @PathVariable String plasticId){

        return plasticServices.deletePlasticInfo(plasticId,cardNumber)
                .map(plastic -> plasticServices.convertPlasticDTO(plastic));

    }

    @PutMapping(EndPoints.CARDS_PLASTIC_CARD_NUMBER)
    public Mono<PlasticsDTO> updatePlasticInfo(@Valid @RequestBody PlasticUpdateDto plasticUpdateDto){
        return plasticServices.updatePlasticData(plasticUpdateDto)
                .map(plastic -> plasticServices.convertPlasticDTO(plastic));
    }


    @GetMapping(EndPoints.CARDS_PLASTIC_CARD_NUMBER)
    public Flux<PlasticsDTO> fetchPlasticInfoForCard(@PathVariable String cardNumber){

        return plasticServices.fetchAllPlasticInfo(cardNumber)
                .map(plastic -> plasticServices.convertPlasticDTO(plastic));

    }

    @DeleteMapping(EndPoints.CARDS_PLASTIC_CARD_NUMBER)
    public Flux<PlasticsDTO> deletePlasticInfoForCard(@PathVariable String cardNumber){

        return plasticServices.deleteAllPlastics(cardNumber)
                .map(plastic -> plasticServices.convertPlasticDTO(plastic));

    }

    private CardsCombined createCardsCombine(CardsBasic cardsBasic,CardAccumulatedValues cardAccumulatedValues) {


        List<PeriodicLimitSet> periodicLimitDTOList = cardsBasic.getPeriodicTypePeriodicCardLimitMap().entrySet()
                .stream()
                .map(periodicTypeMapEntry -> buildPeriodicLimitSet(periodicTypeMapEntry))
                .collect(Collectors.toList());

        Set<PeriodicLimitSet> periodicLimitDTOSet = new HashSet<>(periodicLimitDTOList);

        return CardsCombined.builder()
                .cardsBasic(cardsBasic)
                .cardAccumulatedValues(cardAccumValuesService.updateNewAccumValues(
                        periodicLimitDTOSet, cardAccumulatedValues)).build();

    }

    private PeriodicLimitSet buildPeriodicLimitSet(Map.Entry<PeriodicType, Map<LimitType, PeriodicCardAmount>> periodicTypeMapEntry) {

        return PeriodicLimitSet.builder()
                .periodicType(periodicTypeMapEntry.getKey())
                .limitTypeSet(periodicTypeMapEntry.getValue().keySet())
                .build();
    }




}
