package in.nmaloth.maintenance.service.cards;

import in.nmaloth.entity.card.CardAction;
import in.nmaloth.entity.card.CardsBasic;
import in.nmaloth.entity.card.Plastic;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.maintenance.config.data.ProductTable;
import in.nmaloth.maintenance.exception.InvalidInputDataException;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.combined.CardsPlasticCombined;
import in.nmaloth.maintenance.model.dto.card.PlasticUpdateDto;
import in.nmaloth.maintenance.model.dto.card.PlasticsDTO;
import in.nmaloth.maintenance.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j
public class PlasticServicesImpl implements PlasticServices {

    private final CardsBasicService cardsBasicService;
    private final ProductTable productTable;

    public PlasticServicesImpl( CardsBasicService cardsBasicService,
                               ProductTable productTable) {
        this.cardsBasicService = cardsBasicService;
        this.productTable = productTable;
    }


    @Override
    public Mono<Plastic> fetchPlasticInfo(String plasticId,String cardNumber) {
        return cardsBasicService.fetchCardInfo(cardNumber)
                .map(cardsBasic -> findPlastic(plasticId,cardsBasic))
                ;
    }

    @Override
    public Mono<CardsBasic> deletePlasticInfo(String plasticId,String cardNumber) {
        return cardsBasicService.fetchCardInfo(cardNumber)
                .map(cardsBasic -> {
                    Plastic plastic = findPlastic(plasticId,cardsBasic);
                    cardsBasic.getPlasticList().remove(plastic);
                    return cardsBasic;
                })
               .flatMap(cardsBasic -> cardsBasicService.saveCardsRecord(cardsBasic))
                ;
    }

    @Override
    public Flux<Plastic> fetchAllPlasticInfo(String cardNumber) {
        return cardsBasicService.fetchCardInfo(cardNumber)
                .flatMapMany(cardsBasic -> Flux.fromIterable(cardsBasic.getPlasticList()))
                ;
    }

    @Override
    public Mono<CardsBasic> deleteAllPlastics(String cardNumber) {
        return cardsBasicService.fetchCardInfo(cardNumber)
                .map(cardsBasic -> {
                    cardsBasic.setPlasticList(new ArrayList<>());
                    return cardsBasic;
                })
                .flatMap(cardsBasic -> cardsBasicService.saveCardsRecord(cardsBasic))
                ;
    }


    @Override
    public Mono<CardsBasic> createNewPlastic(PlasticUpdateDto plasticUpdateDto) {

        return cardsBasicService.fetchCardInfo(plasticUpdateDto.getCardId())
                .map(cardsBasic -> {
                    ProductDef productDef = productTable.findProductDef(cardsBasic.getOrg(),cardsBasic.getProduct());
                    Plastic plastic = updatePlastic(plasticUpdateDto, cardsBasic, productDef);
                    if(cardsBasic.getPlasticList() == null){
                        cardsBasic.setPlasticList(new ArrayList<>());
                    }
                    cardsBasic.getPlasticList().add(plastic);
                    return cardsBasic;
                })
                .flatMap(cardsBasic -> cardsBasicService.saveCardsRecord(cardsBasic))
                ;
    }

    @Override
    public Mono<Plastic> updatePlasticData(PlasticUpdateDto plasticUpdateDto) {

        return cardsBasicService.fetchCardInfo(plasticUpdateDto.getCardId())
                .map(cardsBasic -> {

                    Plastic plastic = findPlastic(plasticUpdateDto.getPlasticId(),cardsBasic);
                    updatePlasticFields(plastic,plasticUpdateDto);
                    return cardsBasic;
                })
                .flatMap(cardsBasic -> cardsBasicService.saveCardsRecord(cardsBasic))
                .map(cardsBasic -> findPlastic(plasticUpdateDto.getPlasticId(),cardsBasic))

                ;
    }

    private Plastic findPlastic(String plasticId, CardsBasic cardsBasic){

        return cardsBasic.getPlasticList()
                .stream()
                .filter(plastic -> plastic.getPlasticId().equals(plasticId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("plastic id not Found "));

    }

    @Override
    public void validatePlasticForNewPlastic(PlasticUpdateDto plasticUpdateDto) {

        switch (Util.getCardAction(plasticUpdateDto.getCardAction())){
            case REISSUE_CARD:
            case REPLACEMENT_CARD:
                validatePlasticId(plasticUpdateDto.getPlasticId());
                return;
            case NO_ACTION:
                return;
            case EMERGENCY_REPLACEMENT_CARD:
                validateReplacementDays(plasticUpdateDto.getEmergencyReplCardsExpiryDays());
        }

    }

    private void validateReplacementDays(Integer emergencyReplCardsExpiryDays) {

        if(emergencyReplCardsExpiryDays == null|| emergencyReplCardsExpiryDays == 0){
            throw  new InvalidInputDataException(" Emergency Replacement Days is Mandatory");
        }
    }

    private void validatePlasticId(String plasticId) {

        if(plasticId == null){
            throw  new InvalidInputDataException("plastic id required");
        }


    }

    private Plastic updatePlasticFields(Plastic plastic, PlasticUpdateDto plasticUpdateDto) {

        if((plastic.getCardActivated() == null) || (plastic.getCardActivated() != null && !plastic.getCardActivated())){
            if( plasticUpdateDto.getCardActivate() != null && plasticUpdateDto.getCardActivate()){
                plastic.setCardActivated(true);
                plastic.setCardActivatedDate(LocalDateTime.now());
            }
        }

        if(plasticUpdateDto.getDynamicCVV() != null && plasticUpdateDto.getDynamicCVV()){
            plastic.setDynamicCVV(true);
        }

        return plastic;

    }


    @Override
    public PlasticsDTO convertPlasticDTO(Plastic plastic) {

        return cardsBasicService.convertPlasticDTO(plastic);

    }

    @Override
    public Plastic updatePlastic(PlasticUpdateDto plasticUpdateDto, CardsBasic cardsBasic, ProductDef productDef) {

        CardAction cardAction = Util.getCardAction(plasticUpdateDto.getCardAction());
        switch (cardAction){
            case NEW_CARD:{
                return createNewPlastic(productDef,cardsBasic,plasticUpdateDto);
            }
            case REISSUE_CARD:{
                return createReissuePlastic(productDef,cardsBasic,cardsBasic.getPlasticList(),plasticUpdateDto);
            }
            case ADDITIONAL_CARD:{
                throw  new RuntimeException(" Additional Card Not Supported ");
            }
            case REPLACEMENT_CARD:{
                return createReplacePlastic(productDef,cardsBasic,cardsBasic.getPlasticList(),plasticUpdateDto);

            }
            case EMERGENCY_REPLACEMENT_CARD:{

                return createEmergencyReplacementPlastic(productDef,cardsBasic,plasticUpdateDto);
            }
            case NO_ACTION:{

                return createNoPlasticCard(productDef,cardsBasic,plasticUpdateDto);
            }
        }

        throw  new RuntimeException(" Invalid Action Received");

    }

    private Plastic createNoPlasticCard(ProductDef productDef, CardsBasic cardsBasic, PlasticUpdateDto plasticUpdateDto) {

        LocalDate newExpiryDate;
        if(plasticUpdateDto.getExpiryDate() != null && plasticUpdateDto.getExpiryDate().isAfter(LocalDate.now())){
            newExpiryDate = plasticUpdateDto.getExpiryDate().with(TemporalAdjusters.lastDayOfMonth());
        } else {
            newExpiryDate = LocalDate.now().plusMonths(productDef.getCardsValidityMonthNew())
                    .with(TemporalAdjusters.lastDayOfMonth());
        }

        LocalDate validDate = LocalDate.now().plusDays(productDef.getDaysToCardsValid());

        return buildPlastic(CardAction.NO_ACTION,productDef, cardsBasic,
                newExpiryDate, validDate,plasticUpdateDto.getDynamicCVV());

    }

    private Plastic createNewPlastic(ProductDef productDef, CardsBasic cardsBasic, PlasticUpdateDto plasticUpdateDto) {

        LocalDate newExpiryDate;

        if(plasticUpdateDto.getExpiryDate() != null && plasticUpdateDto.getExpiryDate().isAfter(LocalDate.now())){
            newExpiryDate = plasticUpdateDto.getExpiryDate().with(TemporalAdjusters.lastDayOfMonth());
        } else {
            newExpiryDate = LocalDate.now().plusMonths(productDef.getCardsValidityMonthNew())
                    .with(TemporalAdjusters.lastDayOfMonth());
        }


        LocalDate validDate = LocalDate.now().plusDays(productDef.getDaysToCardsValid());

        return buildPlastic(CardAction.NEW_CARD,productDef, cardsBasic,
                newExpiryDate, validDate,plasticUpdateDto.getDynamicCVV());
    }

    private Plastic createEmergencyReplacementPlastic(ProductDef productDef,CardsBasic cardsBasic,PlasticUpdateDto plasticUpdateDto){


        LocalDate newExpiryDate = LocalDate.now().plusDays(plasticUpdateDto.getEmergencyReplCardsExpiryDays())
                .with(TemporalAdjusters.lastDayOfMonth());
        LocalDate validDate = LocalDate.now().plusDays(productDef.getDaysToCardsValid());
        return buildPlastic(CardAction.EMERGENCY_REPLACEMENT_CARD, productDef, cardsBasic,
                newExpiryDate, validDate,plasticUpdateDto.getDynamicCVV());
    }



    private Plastic createReissuePlastic(ProductDef productDef, CardsBasic cardsBasic,  List<Plastic> plasticList,
                                         PlasticUpdateDto plasticUpdateDto){

        Optional<Plastic> optionalPlastic = plasticList.stream()
                .filter(plastic -> plastic.getPlasticId().equals(plasticUpdateDto.getPlasticId()))
                .findFirst();

        if(optionalPlastic.isPresent()){
            Plastic plastic = optionalPlastic.get();

            LocalDate expiryDate;

            if(plasticUpdateDto.getExpiryDate() != null && plasticUpdateDto.getExpiryDate().isAfter(LocalDate.now())){
                expiryDate = plasticUpdateDto.getExpiryDate().with(TemporalAdjusters.lastDayOfMonth());
            } else {
                expiryDate = plastic.getExpiryDate()
                        .plusMonths(productDef.getCardsValidityMonthReIssue())
                        .with(TemporalAdjusters.lastDayOfMonth());

            }

            LocalDate validDate = LocalDate.now().plusDays(productDef.getDaysToCardsValid());
            Plastic plasticReissue = buildPlastic(CardAction.REISSUE_CARD,productDef,cardsBasic,expiryDate,validDate,
                    plasticUpdateDto.getDynamicCVV());
            return plasticReissue;
        } else {
            throw new NotFoundException("Invalid Plastic Id : " + plasticUpdateDto.getPlasticId());
        }

    }

    private Plastic createReplacePlastic(ProductDef productDef,CardsBasic cardsBasic,
                                         List<Plastic> plasticList,PlasticUpdateDto plasticUpdateDto){

        Optional<Plastic> optionalPlastic = plasticList.stream()
                .filter(plastic -> plastic.getPlasticId().equals(plasticUpdateDto.getPlasticId()))
                .findFirst();

        if(optionalPlastic.isPresent()){
            Plastic plastic = optionalPlastic.get();
            int yearsRange = Period.between(LocalDate.now(),plastic.getExpiryDate()).getYears();
            int monthsRange = Period.between(LocalDate.now(),plastic.getExpiryDate()).getMonths();

            int totalMonths = yearsRange * 12 + monthsRange;

            LocalDate expiryDate;
            if(totalMonths < productDef.getDateRangeNewExpDate() && productDef.getDateRangeNewExpDate() > 0){
                expiryDate = LocalDate.now()
                        .plusMonths(productDef.getCardsValidityMonthReplace())
                        .with(TemporalAdjusters.lastDayOfMonth())
                ;
            } else {
                expiryDate = plastic.getExpiryDate();
            }

            LocalDate validDate = LocalDate.now().plusDays(productDef.getDaysToCardsValid());
            Plastic plasticReplace = buildPlastic(CardAction.REPLACEMENT_CARD,productDef,cardsBasic,expiryDate,validDate,
                    plasticUpdateDto.getDynamicCVV());
            return plasticReplace;
        } else {
            throw new NotFoundException("Invalid Plastic Id : " + plasticUpdateDto.getPlasticId());
        }

    }

    private Plastic buildPlastic(CardAction cardAction, ProductDef productDef,
                                 CardsBasic cardsBasic, LocalDate newExpiryDate,
                                 LocalDate validDate,Boolean dynamicCvv) {
        Plastic.PlasticBuilder builder = Plastic.builder()
                .cardAction(CardAction.NO_ACTION)
                .expiryDate(newExpiryDate)
                .cardActivated(!productDef.getCardsActivationRequired())
                .dateCardValidFrom(validDate)
                .activationWaiveDuration(Duration.ofDays(cardsBasic.getWaiverDaysActivation()))
//                .datePlasticIssued(LocalDateTime.now())
                .dynamicCVV(dynamicCvv)
                .pendingCardAction(cardAction)
                .plasticId(UUID.randomUUID().toString().replace("-", ""))
                ;

        if(!productDef.getCardsActivationRequired()){
            builder.cardActivatedDate(LocalDateTime.now());
        }


        return  builder.build();

    }
}
