package in.nmaloth.maintenance.service.cards;

import in.nmaloth.entity.card.CardAction;
import in.nmaloth.entity.card.CardsBasic;
import in.nmaloth.entity.card.Plastic;
import in.nmaloth.entity.card.PlasticKey;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.maintenance.config.data.ProductTable;
import in.nmaloth.maintenance.dataService.card.PlasticDataService;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j
public class PlasticServicesImpl implements PlasticServices {

    private final PlasticDataService plasticDataService;
    private final CardsBasicService cardsBasicService;
    private final ProductTable productTable;

    public PlasticServicesImpl(PlasticDataService plasticDataService,
                               CardsBasicService cardsBasicService,
                               ProductTable productTable) {
        this.plasticDataService = plasticDataService;
        this.cardsBasicService = cardsBasicService;
        this.productTable = productTable;
    }


    @Override
    public Mono<Plastic> fetchPlasticInfo(String plasticId,String cardNumber) {
        return plasticDataService.findPlasticById(plasticId,cardNumber)
                .map(plasticOptional -> {
                    if(plasticOptional.isPresent()){
                        return plasticOptional.get();
                    }
                    throw  new NotFoundException(" Invalid Plastic ID " + plasticId + " card number " + cardNumber);
                })
                ;
    }

    @Override
    public Mono<Plastic> deletePlasticInfo(String plasticId,String cardNumber) {
        return plasticDataService.deletePlasticById(plasticId,cardNumber)
                .map(plasticOptional -> {
                    if(plasticOptional.isPresent()){
                        return plasticOptional.get();
                    }
                    throw  new NotFoundException(" Invalid Plastic ID " + plasticId + " card number " + cardNumber);
                })
                ;
    }

    @Override
    public Flux<Plastic> fetchAllPlasticInfo(String cardNumber) {
        return plasticDataService.findAllPlastic(cardNumber)
                ;
    }

    @Override
    public Flux<Plastic> deleteAllPlastics(String cardNumber) {
        return plasticDataService.deleteAllPlastics(cardNumber)
                ;
    }

    @Override
    public Mono<Plastic> savePlastic(Plastic plastic) {
        return plasticDataService.savePlastic(plastic)
                ;
    }

    @Override
    public Mono<Plastic> createNewPlastic(PlasticUpdateDto plasticUpdateDto) {

        return cardsBasicService.fetchCardInfo(plasticUpdateDto.getCardNumber())
                .flatMap(cardsBasic -> createCombinedLayout(cardsBasic))
                .map(cardsPlasticCombined -> {
                    CardsBasic cardsBasic = cardsPlasticCombined.getCardsBasic();
                    ProductDef productDef = productTable.findProductDef(cardsBasic.getOrg(),cardsBasic.getProduct());
                    return updatePlastic(plasticUpdateDto, cardsBasic, productDef, cardsPlasticCombined.getPlasticList());
                })
                .flatMap(plastic -> plasticDataService.savePlastic(plastic))
                ;
    }

    @Override
    public Mono<Plastic> updatePlasticData(PlasticUpdateDto plasticUpdateDto) {

        return plasticDataService.findPlasticById(plasticUpdateDto.getPlasticId(),plasticUpdateDto.getCardNumber())
                .map(plasticOptional -> {
                    if(plasticOptional.isEmpty()){
                        throw  new NotFoundException("plastic id not Found ");
                    }
                    return plasticOptional.get();
                })
                .map(plastic -> updatePlasticFields(plastic,plasticUpdateDto))
                .flatMap(plastic -> plasticDataService.savePlastic(plastic))

                ;
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

    private Mono<CardsPlasticCombined> createCombinedLayout(CardsBasic cardsBasic) {
        return plasticDataService.findListPlastic(cardsBasic.getCardNumber())
                .map(plasticList -> new CardsPlasticCombined(cardsBasic,plasticList));
    }

    @Override
    public PlasticsDTO convertPlasticDTO(Plastic plastic) {

        PlasticsDTO.PlasticsDTOBuilder builder = PlasticsDTO.builder()
                .id(plastic.getPlasticKey().getId())
                .cardNumber(plastic.getCardNumber())
                .activationWaiveDuration(plastic.getActivationWaiveDuration().toDays())
                .cardAction(Util.getCardAction(plastic.getCardAction()))
                .cardActivated(plastic.getCardActivated());

        if(plastic.getExpiryDate() != null){
            builder.expiryDate(plastic.getExpiryDate());
        }

        if(plastic.getCardActivatedDate() != null){
            builder.cardActivatedDate(plastic.getCardActivatedDate());
        }
        if(plastic.getDatePlasticIssued() != null){
            builder.datePlasticIssued(plastic.getDatePlasticIssued());
        }
        if(plastic.getDateCardValidFrom() != null){
            builder.dateCardValidFrom(plastic.getDateCardValidFrom());
        }
        if(plastic.getDynamicCVV() != null){
            builder.dynamicCVV(plastic.getDynamicCVV());
        }
        if(plastic.getPendingCardAction() != null){
            builder.pendingCardAction(Util.getCardAction(plastic.getPendingCardAction()));
        }

        return builder.build();
    }

    @Override
    public Plastic updatePlastic(PlasticUpdateDto plasticUpdateDto, CardsBasic cardsBasic, ProductDef productDef,List<Plastic> plasticList) {

        CardAction cardAction = Util.getCardAction(plasticUpdateDto.getCardAction());
        switch (cardAction){
            case NEW_CARD:{
                return createNewPlastic(productDef,cardsBasic,plasticUpdateDto);
            }
            case REISSUE_CARD:{
                return createReissuePlastic(productDef,cardsBasic,plasticList,plasticUpdateDto);
            }
            case ADDITIONAL_CARD:{
                throw  new RuntimeException(" Additional Card Not Supported ");
            }
            case REPLACEMENT_CARD:{
                return createReplacePlastic(productDef,cardsBasic,plasticList,plasticUpdateDto);

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
                .filter(plastic -> plastic.getPlasticKey().getId().equals(plasticUpdateDto.getPlasticId()))
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
                .filter(plastic -> plastic.getPlasticKey().getId().equals(plasticUpdateDto.getPlasticId()))
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
                .plasticKey(new PlasticKey(UUID.randomUUID().toString().replace("-", ""), cardsBasic.getCardNumber()))
                .cardNumber(cardsBasic.getCardNumber())
                ;

        if(!productDef.getCardsActivationRequired()){
            builder.cardActivatedDate(LocalDateTime.now());
        }


        return  builder.build();

    }
}
