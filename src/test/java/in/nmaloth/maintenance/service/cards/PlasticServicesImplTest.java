package in.nmaloth.maintenance.service.cards;

import in.nmaloth.entity.BlockType;
import in.nmaloth.entity.account.AccountType;
import in.nmaloth.entity.account.BalanceTypes;
import in.nmaloth.entity.card.*;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.entity.product.ProductId;
import in.nmaloth.maintenance.config.data.ProductTable;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.account.AccountDefDTO;
import in.nmaloth.maintenance.model.dto.card.*;
import in.nmaloth.maintenance.repository.card.CardsBasicRepository;
import in.nmaloth.maintenance.repository.card.PlasticRepository;
import in.nmaloth.maintenance.repository.product.ProductDefRepository;
import in.nmaloth.maintenance.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PlasticServicesImplTest {


    @Autowired
    private PlasticServices plasticServices;

    @Autowired
    ProductTable productTable;

    @Autowired
    private ProductDefRepository productDefRepository;

    @Autowired
    private CardsBasicService cardsBasicService;

    @Autowired
    private PlasticRepository plasticRepository;

    @Autowired
    private CardsBasicRepository cardsBasicRepository;





    @BeforeEach
    void setup(){

        productDefRepository.findAll()
                .forEach(productDef -> productDefRepository.delete(productDef));
        updateProductTable();
        plasticRepository.findAll()
                .forEach(plastic -> plasticRepository.delete(plastic));

        cardsBasicRepository.findAll()
                .forEach(cardsBasic -> cardsBasicRepository.delete(cardsBasic));


    }


    @Test
    void savePlastic(){
        Plastic plastic = createPlastic();
        plasticServices.savePlastic(plastic).block();

        Optional<Plastic> plasticOptional = plasticRepository.findById(plastic.getPlasticKey());

        assertTrue(plasticOptional.isPresent());

    }


    @Test
    void fetchPlasticInfo(){
        Plastic plastic = createPlastic();
        plasticServices.savePlastic(plastic).block();

        Mono<Plastic> plasticMono = plasticServices.fetchPlasticInfo(plastic.getPlasticKey().getId(),plastic.getCardNumber());

        StepVerifier.create(plasticMono)
                .expectNextCount(1)
                .verifyComplete();


    }

    @Test
    void fetchPlasticInfo1(){
        Mono<Plastic> plasticMono = plasticServices.fetchPlasticInfo("123","3456");

        StepVerifier.create(plasticMono)
                .expectError(NotFoundException.class)
                .verify();


    }

    @Test
    void deletePlasticInfo(){

        Plastic plastic = createPlastic();
        plasticServices.savePlastic(plastic).block();

        plasticServices.deletePlasticInfo(plastic.getPlasticKey().getId(),plastic.getCardNumber()).block();
        Optional<Plastic> plasticOptional = plasticRepository.findById(plastic.getPlasticKey());

        assertTrue(plasticOptional.isEmpty());

    }

    @Test
    void deletePlasticInfo1(){

        Plastic plastic = createPlastic();
        plasticServices.savePlastic(plastic).block();

        Mono<Plastic> plasticMono = plasticServices.deletePlasticInfo("1234", "3244");

        StepVerifier
                .create(plasticMono)
                .expectError(NotFoundException.class)
                .verify();


    }

    @Test
    void fetchAllPlasticInfo(){

        Plastic plastic = createPlastic();
        plasticServices.savePlastic(plastic).block();

        plastic.getPlasticKey().setId(UUID.randomUUID().toString().replace("-",""));
        plasticServices.savePlastic(plastic).block();

        plastic.getPlasticKey().setId(UUID.randomUUID().toString().replace("-",""));
        plasticServices.savePlastic(plastic).block();

        Flux<Plastic> plasticFlux = plasticServices.fetchAllPlasticInfo(plastic.getCardNumber());

        StepVerifier
                .create(plasticFlux)
                .expectNextCount(3)
                .verifyComplete();

    }


    @Test
    void deleteAllPlastics(){

        Plastic plastic = createPlastic();
        plasticServices.savePlastic(plastic).block();

        plastic.getPlasticKey().setId(UUID.randomUUID().toString().replace("-",""));
        plasticServices.savePlastic(plastic).block();

        plastic.getPlasticKey().setId(UUID.randomUUID().toString().replace("-",""));
        plasticServices.savePlastic(plastic).block();

        plasticServices.deleteAllPlastics(plastic.getCardNumber()).blockLast();

        Iterable<Plastic> plastics = plasticRepository.findAllByCardNumber(plastic.getCardNumber());

        List<Plastic> plasticList = new ArrayList<>();

        plastics.forEach(plasticList::add);

        assertEquals(0,plasticList.size());

    }

    @Test
    void createNewPlastic(){

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO();
        ProductDef productDef = productTable.findProductDef(cardBasicAddDTO.getOrg(),cardBasicAddDTO.getProduct());
        CardsBasic cardsBasic = cardsBasicService.convertDTOToCardBasic(cardBasicAddDTO,productDef);
        cardsBasicRepository.save(cardsBasic);


        PlasticUpdateDto plasticUpdateDto = createPlastic(CardAction.NEW_CARD,null,null,null,null);
        plasticUpdateDto.setCardNumber(cardsBasic.getCardNumber());


        Plastic plastic1 = plasticServices.createNewPlastic(plasticUpdateDto).block();

        Plastic plastic = plasticRepository.findById(plastic1.getPlasticKey()).get();



        long waiveActivation = cardsBasic.getWaiverDaysActivation();
        assertAll(
                ()->assertEquals(plastic.getPlasticKey().getCardNumber(),cardsBasic.getCardNumber()),
                ()-> assertNotNull(plastic.getPlasticKey().getId()),
                ()-> assertEquals(CardAction.NO_ACTION,plastic.getCardAction()),
                ()-> assertEquals(CardAction.NEW_CARD,plastic.getPendingCardAction()),
                ()-> assertEquals(LocalDate.now().plusMonths(productDef.getCardsValidityMonthNew()).with(TemporalAdjusters.lastDayOfMonth()),
                        plastic.getExpiryDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertNull(plastic.getDatePlasticIssued()),
                ()-> assertNull(plastic.getCardActivatedDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertEquals(LocalDate.now().plusDays(productDef.getDaysToCardsValid()),plastic.getDateCardValidFrom()),
                ()-> assertEquals(waiveActivation,plastic.getActivationWaiveDuration().toDays()),
                ()-> assertEquals(plasticUpdateDto.getDynamicCVV(),plastic.getDynamicCVV())

        );


    }


    @Test
    void createReplacePlastic(){

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO();
        ProductDef productDef = productTable.findProductDef(cardBasicAddDTO.getOrg(),cardBasicAddDTO.getProduct());
        CardsBasic cardsBasic = cardsBasicService.convertDTOToCardBasic(cardBasicAddDTO,productDef);
        cardsBasicRepository.save(cardsBasic);

        Plastic plastic1 = createPlastic();
        plastic1.setCardNumber(cardsBasic.getCardNumber());
        plasticRepository.save(plastic1);
        Plastic plastic2 = createPlastic();
        plastic2.setCardNumber(cardsBasic.getCardNumber());
        plasticRepository.save(plastic2);

        Plastic plastic3 = createPlastic();
        plastic3.setCardNumber(cardsBasic.getCardNumber());
        plasticRepository.save(plastic3);




        PlasticUpdateDto plasticUpdateDto = createPlastic(CardAction.REPLACEMENT_CARD,null,null,plastic2.getPlasticKey().getId(),null);
        plasticUpdateDto.setCardNumber(cardsBasic.getCardNumber());


        Plastic plastic4 = plasticServices.createNewPlastic(plasticUpdateDto).block();

        Plastic plastic = plasticRepository.findById(plastic4.getPlasticKey()).get();



        long waiveActivation = cardsBasic.getWaiverDaysActivation();
        assertAll(
                ()->assertEquals(plastic.getPlasticKey().getCardNumber(),cardsBasic.getCardNumber()),
                ()-> assertNotNull(plastic.getPlasticKey().getId()),
                ()-> assertEquals(CardAction.NO_ACTION,plastic.getCardAction()),
                ()-> assertEquals(CardAction.REPLACEMENT_CARD,plastic.getPendingCardAction()),
                ()-> assertEquals(plastic2.getExpiryDate(),
                        plastic.getExpiryDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertNull(plastic.getDatePlasticIssued()),
                ()-> assertNull(plastic.getCardActivatedDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertEquals(LocalDate.now().plusDays(productDef.getDaysToCardsValid()),plastic.getDateCardValidFrom()),
                ()-> assertEquals(waiveActivation,plastic.getActivationWaiveDuration().toDays()),
                ()-> assertEquals(plasticUpdateDto.getDynamicCVV(),plastic.getDynamicCVV())

        );


    }



    @Test
    void convertPlasticDTO() {
        Plastic plastic = createPlastic();

        PlasticsDTO plasticsDTO = plasticServices.convertPlasticDTO(plastic);


        assertAll(
                ()-> assertEquals(plastic.getActivationWaiveDuration().toDays(),plasticsDTO.getActivationWaiveDuration()),
                ()-> assertEquals(plastic.getDynamicCVV(),plasticsDTO.getDynamicCVV()),
                ()-> assertEquals(plastic.getCardAction(),Util.getCardAction(plasticsDTO.getCardAction())),
                ()-> assertEquals(plastic.getCardActivated(),plasticsDTO.getCardActivated()),
                ()-> assertEquals(plastic.getCardActivatedDate(),plasticsDTO.getCardActivatedDate()),
                ()-> assertEquals(plastic.getCardNumber(),plasticsDTO.getCardNumber()),
                ()-> assertEquals(plastic.getDateCardValidFrom(),plasticsDTO.getDateCardValidFrom()),
                ()-> assertEquals(plastic.getDatePlasticIssued(),plasticsDTO.getDatePlasticIssued()),
                ()-> assertEquals(plastic.getExpiryDate(),plasticsDTO.getExpiryDate()),
                ()-> assertEquals(plastic.getPendingCardAction(),Util.getCardAction(plasticsDTO.getPendingCardAction())),
                ()-> assertEquals(plastic.getPlasticKey().getId(),plasticsDTO.getId())
        );

    }

    @Test
    void updatePlasticNew() {

        ProductDef productDef = createProductDef();

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO();
        cardBasicAddDTO.setOrg(productDef.getProductId().getOrg());
        cardBasicAddDTO.setProduct(productDef.getProductId().getProduct());

        CardsBasic cardsBasic = cardsBasicService.convertDTOToCardBasic(cardBasicAddDTO,productDef);

        List<Plastic> plasticList = new ArrayList<>();

        PlasticUpdateDto plasticUpdateDto = createPlastic(CardAction.NEW_CARD,false,null,null,null);

        Plastic plastic = plasticServices.updatePlastic(plasticUpdateDto,cardsBasic,productDef,plasticList);

        long waiveActivation = cardsBasic.getWaiverDaysActivation();
        assertAll(
                ()->assertEquals(plastic.getPlasticKey().getCardNumber(),cardsBasic.getCardNumber()),
                ()-> assertNotNull(plastic.getPlasticKey().getId()),
                ()-> assertEquals(CardAction.NO_ACTION,plastic.getCardAction()),
                ()-> assertEquals(CardAction.NEW_CARD,plastic.getPendingCardAction()),
                ()-> assertEquals(LocalDate.now().plusMonths(productDef.getCardsValidityMonthNew()).with(TemporalAdjusters.lastDayOfMonth()),
                        plastic.getExpiryDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertNull(plastic.getDatePlasticIssued()),
                ()-> assertNull(plastic.getCardActivatedDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertEquals(LocalDate.now().plusDays(productDef.getDaysToCardsValid()),plastic.getDateCardValidFrom()),
                ()-> assertEquals(waiveActivation,plastic.getActivationWaiveDuration().toDays()),
                ()-> assertEquals(plasticUpdateDto.getDynamicCVV(),plastic.getDynamicCVV())

        );


    }

    @Test
    void updatePlasticNew1() {

        ProductDef productDef = createProductDef();

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO();
        cardBasicAddDTO.setOrg(productDef.getProductId().getOrg());
        cardBasicAddDTO.setProduct(productDef.getProductId().getProduct());

        CardsBasic cardsBasic = cardsBasicService.convertDTOToCardBasic(cardBasicAddDTO,productDef);

        List<Plastic> plasticList = new ArrayList<>();

        PlasticUpdateDto plasticUpdateDto = createPlastic(CardAction.NEW_CARD,false,null,null,LocalDate.of(2022,12,30));

        Plastic plastic = plasticServices.updatePlastic(plasticUpdateDto,cardsBasic,productDef,plasticList);

        long waiveActivation = cardsBasic.getWaiverDaysActivation();
        assertAll(
                ()->assertEquals(plastic.getPlasticKey().getCardNumber(),cardsBasic.getCardNumber()),
                ()-> assertNotNull(plastic.getPlasticKey().getId()),
                ()-> assertEquals(CardAction.NO_ACTION,plastic.getCardAction()),
                ()-> assertEquals(CardAction.NEW_CARD,plastic.getPendingCardAction()),
                ()-> assertEquals(LocalDate.of(2022,12,31),
                        plastic.getExpiryDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertNull(plastic.getDatePlasticIssued()),
                ()-> assertNull(plastic.getCardActivatedDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertEquals(LocalDate.now().plusDays(productDef.getDaysToCardsValid()),plastic.getDateCardValidFrom()),
                ()-> assertEquals(waiveActivation,plastic.getActivationWaiveDuration().toDays()),
                ()-> assertEquals(plasticUpdateDto.getDynamicCVV(),plastic.getDynamicCVV())

        );


    }


    @Test
    void updatePlasticNew2() {

        ProductDef productDef = createProductDef();
        productDef.setCardsActivationRequired(false);

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO();
        cardBasicAddDTO.setOrg(productDef.getProductId().getOrg());
        cardBasicAddDTO.setProduct(productDef.getProductId().getProduct());

        CardsBasic cardsBasic = cardsBasicService.convertDTOToCardBasic(cardBasicAddDTO,productDef);

        List<Plastic> plasticList = new ArrayList<>();

        PlasticUpdateDto plasticUpdateDto = createPlastic(CardAction.NEW_CARD,false,null,null,LocalDate.of(2022,12,30));

        Plastic plastic = plasticServices.updatePlastic(plasticUpdateDto,cardsBasic,productDef,plasticList);

        long waiveActivation = cardsBasic.getWaiverDaysActivation();
        assertAll(
                ()->assertEquals(plastic.getPlasticKey().getCardNumber(),cardsBasic.getCardNumber()),
                ()-> assertNotNull(plastic.getPlasticKey().getId()),
                ()-> assertEquals(CardAction.NO_ACTION,plastic.getCardAction()),
                ()-> assertEquals(CardAction.NEW_CARD,plastic.getPendingCardAction()),
                ()-> assertEquals(LocalDate.of(2022,12,31),
                        plastic.getExpiryDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertNull(plastic.getDatePlasticIssued()),
                ()-> assertNotNull(plastic.getCardActivatedDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertEquals(LocalDate.now().plusDays(productDef.getDaysToCardsValid()),plastic.getDateCardValidFrom()),
                ()-> assertEquals(waiveActivation,plastic.getActivationWaiveDuration().toDays()),
                ()-> assertEquals(plasticUpdateDto.getDynamicCVV(),plastic.getDynamicCVV())

        );


    }



    @Test
    void updatePlasticEmergencyRepl() {

        ProductDef productDef = createProductDef();
        productDef.setCardsActivationRequired(false);

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO();
        cardBasicAddDTO.setOrg(productDef.getProductId().getOrg());
        cardBasicAddDTO.setProduct(productDef.getProductId().getProduct());

        CardsBasic cardsBasic = cardsBasicService.convertDTOToCardBasic(cardBasicAddDTO,productDef);

        List<Plastic> plasticList = new ArrayList<>();

        PlasticUpdateDto plasticUpdateDto = createPlastic(CardAction.EMERGENCY_REPLACEMENT_CARD,false,30,null,null);

        Plastic plastic = plasticServices.updatePlastic(plasticUpdateDto,cardsBasic,productDef,plasticList);

        long waiveActivation = cardsBasic.getWaiverDaysActivation();
        assertAll(
                ()->assertEquals(plastic.getPlasticKey().getCardNumber(),cardsBasic.getCardNumber()),
                ()-> assertNotNull(plastic.getPlasticKey().getId()),
                ()-> assertEquals(CardAction.NO_ACTION,plastic.getCardAction()),
                ()-> assertEquals(CardAction.EMERGENCY_REPLACEMENT_CARD,plastic.getPendingCardAction()),
                ()-> assertEquals(LocalDate.now().plusDays(plasticUpdateDto.getEmergencyReplCardsExpiryDays()).with(TemporalAdjusters.lastDayOfMonth()),
                        plastic.getExpiryDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertNull(plastic.getDatePlasticIssued()),
                ()-> assertNotNull(plastic.getCardActivatedDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertEquals(LocalDate.now().plusDays(productDef.getDaysToCardsValid()),plastic.getDateCardValidFrom()),
                ()-> assertEquals(waiveActivation,plastic.getActivationWaiveDuration().toDays()),
                ()-> assertEquals(plasticUpdateDto.getDynamicCVV(),plastic.getDynamicCVV())

        );


    }

    @Test
    void updatePlasticNoAction() {

        ProductDef productDef = createProductDef();
        productDef.setCardsActivationRequired(false);

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO();
        cardBasicAddDTO.setOrg(productDef.getProductId().getOrg());
        cardBasicAddDTO.setProduct(productDef.getProductId().getProduct());

        CardsBasic cardsBasic = cardsBasicService.convertDTOToCardBasic(cardBasicAddDTO,productDef);

        List<Plastic> plasticList = new ArrayList<>();

        PlasticUpdateDto plasticUpdateDto = createPlastic(CardAction.NO_ACTION,false,null,null,null);

        Plastic plastic = plasticServices.updatePlastic(plasticUpdateDto,cardsBasic,productDef,plasticList);

        long waiveActivation = cardsBasic.getWaiverDaysActivation();
        assertAll(
                ()->assertEquals(plastic.getPlasticKey().getCardNumber(),cardsBasic.getCardNumber()),
                ()-> assertNotNull(plastic.getPlasticKey().getId()),
                ()-> assertEquals(CardAction.NO_ACTION,plastic.getCardAction()),
                ()-> assertEquals(CardAction.NO_ACTION,plastic.getPendingCardAction()),
                ()-> assertEquals(LocalDate.now().plusMonths(productDef.getCardsValidityMonthNew()).with(TemporalAdjusters.lastDayOfMonth()),
                        plastic.getExpiryDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertNull(plastic.getDatePlasticIssued()),
                ()-> assertNotNull(plastic.getCardActivatedDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertEquals(LocalDate.now().plusDays(productDef.getDaysToCardsValid()),plastic.getDateCardValidFrom()),
                ()-> assertEquals(waiveActivation,plastic.getActivationWaiveDuration().toDays()),
                ()-> assertEquals(plasticUpdateDto.getDynamicCVV(),plastic.getDynamicCVV())

        );


    }


    @Test
    void updatePlasticRepls() {

        ProductDef productDef = createProductDef();
        productDef.setCardsActivationRequired(false);

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO();
        cardBasicAddDTO.setOrg(productDef.getProductId().getOrg());
        cardBasicAddDTO.setProduct(productDef.getProductId().getProduct());

        CardsBasic cardsBasic = cardsBasicService.convertDTOToCardBasic(cardBasicAddDTO,productDef);

        Plastic plastic1 = createPlastic();
        Plastic plastic2 = createPlastic();
        plastic2.setExpiryDate(LocalDate.of(2021,4,30));
        Plastic plastic3 = createPlastic();



        List<Plastic> plasticList = new ArrayList<>();
        plasticList.add(plastic1);
        plasticList.add(plastic2);
        plasticList.add(plastic3);

        PlasticUpdateDto plasticUpdateDto = createPlastic(CardAction.REPLACEMENT_CARD,false,null,plastic2.getPlasticKey().getId(),null);

        Plastic plastic = plasticServices.updatePlastic(plasticUpdateDto,cardsBasic,productDef,plasticList);

        long waiveActivation = cardsBasic.getWaiverDaysActivation();
        assertAll(
                ()->assertEquals(plastic.getPlasticKey().getCardNumber(),cardsBasic.getCardNumber()),
                ()-> assertNotNull(plastic.getPlasticKey().getId()),
                ()-> assertEquals(CardAction.NO_ACTION,plastic.getCardAction()),
                ()-> assertEquals(CardAction.REPLACEMENT_CARD,plastic.getPendingCardAction()),
                ()-> assertEquals(LocalDate.now().plusMonths(productDef.getCardsValidityMonthReplace()).with(TemporalAdjusters.lastDayOfMonth()),
                        plastic.getExpiryDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertNull(plastic.getDatePlasticIssued()),
                ()-> assertNotNull(plastic.getCardActivatedDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertEquals(LocalDate.now().plusDays(productDef.getDaysToCardsValid()),plastic.getDateCardValidFrom()),
                ()-> assertEquals(waiveActivation,plastic.getActivationWaiveDuration().toDays()),
                ()-> assertEquals(plasticUpdateDto.getDynamicCVV(),plastic.getDynamicCVV())

        );


    }

    @Test
    void updatePlasticRepls1() {

        ProductDef productDef = createProductDef();
        productDef.setCardsActivationRequired(false);

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO();
        cardBasicAddDTO.setOrg(productDef.getProductId().getOrg());
        cardBasicAddDTO.setProduct(productDef.getProductId().getProduct());

        CardsBasic cardsBasic = cardsBasicService.convertDTOToCardBasic(cardBasicAddDTO,productDef);

        Plastic plastic1 = createPlastic();
        Plastic plastic2 = createPlastic();
        Plastic plastic3 = createPlastic();



        List<Plastic> plasticList = new ArrayList<>();
        plasticList.add(plastic1);
        plasticList.add(plastic2);
        plasticList.add(plastic3);

        PlasticUpdateDto plasticUpdateDto = createPlastic(CardAction.REPLACEMENT_CARD,false,null,plastic2.getPlasticKey().getId(),null);

        Plastic plastic = plasticServices.updatePlastic(plasticUpdateDto,cardsBasic,productDef,plasticList);

        long waiveActivation = cardsBasic.getWaiverDaysActivation();
        assertAll(
                ()->assertEquals(plastic.getPlasticKey().getCardNumber(),cardsBasic.getCardNumber()),
                ()-> assertNotNull(plastic.getPlasticKey().getId()),
                ()-> assertEquals(CardAction.NO_ACTION,plastic.getCardAction()),
                ()-> assertEquals(CardAction.REPLACEMENT_CARD,plastic.getPendingCardAction()),
                ()-> assertEquals(plastic2.getExpiryDate(),
                        plastic.getExpiryDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertNull(plastic.getDatePlasticIssued()),
                ()-> assertNotNull(plastic.getCardActivatedDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertEquals(LocalDate.now().plusDays(productDef.getDaysToCardsValid()),plastic.getDateCardValidFrom()),
                ()-> assertEquals(waiveActivation,plastic.getActivationWaiveDuration().toDays()),
                ()-> assertEquals(plasticUpdateDto.getDynamicCVV(),plastic.getDynamicCVV())

        );


    }

    @Test
    void updatePlasticRepls2() {

        ProductDef productDef = createProductDef();
        productDef.setCardsActivationRequired(false);

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO();
        cardBasicAddDTO.setOrg(productDef.getProductId().getOrg());
        cardBasicAddDTO.setProduct(productDef.getProductId().getProduct());

        CardsBasic cardsBasic = cardsBasicService.convertDTOToCardBasic(cardBasicAddDTO,productDef);

        Plastic plastic1 = createPlastic();
        Plastic plastic2 = createPlastic();
        Plastic plastic3 = createPlastic();



        List<Plastic> plasticList = new ArrayList<>();
        plasticList.add(plastic1);
        plasticList.add(plastic2);
        plasticList.add(plastic3);

        PlasticUpdateDto plasticUpdateDto = createPlastic(CardAction.REPLACEMENT_CARD,false,null,plastic2.getPlasticKey().getId(),LocalDate.of(2025,03,31));

        Plastic plastic = plasticServices.updatePlastic(plasticUpdateDto,cardsBasic,productDef,plasticList);

        long waiveActivation = cardsBasic.getWaiverDaysActivation();
        assertAll(
                ()->assertEquals(plastic.getPlasticKey().getCardNumber(),cardsBasic.getCardNumber()),
                ()-> assertNotNull(plastic.getPlasticKey().getId()),
                ()-> assertEquals(CardAction.NO_ACTION,plastic.getCardAction()),
                ()-> assertEquals(CardAction.REPLACEMENT_CARD,plastic.getPendingCardAction()),
                ()-> assertEquals(plastic2.getExpiryDate(),
                        plastic.getExpiryDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertNull(plastic.getDatePlasticIssued()),
                ()-> assertNotNull(plastic.getCardActivatedDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertEquals(LocalDate.now().plusDays(productDef.getDaysToCardsValid()),plastic.getDateCardValidFrom()),
                ()-> assertEquals(waiveActivation,plastic.getActivationWaiveDuration().toDays()),
                ()-> assertEquals(plasticUpdateDto.getDynamicCVV(),plastic.getDynamicCVV())

        );


    }

    @Test
    void updatePlasticReIssue() {

        ProductDef productDef = createProductDef();
        productDef.setCardsActivationRequired(false);

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO();
        cardBasicAddDTO.setOrg(productDef.getProductId().getOrg());
        cardBasicAddDTO.setProduct(productDef.getProductId().getProduct());

        CardsBasic cardsBasic = cardsBasicService.convertDTOToCardBasic(cardBasicAddDTO,productDef);

        Plastic plastic1 = createPlastic();
        Plastic plastic2 = createPlastic();
        plastic2.setExpiryDate(LocalDate.of(2021,4,30));
        Plastic plastic3 = createPlastic();



        List<Plastic> plasticList = new ArrayList<>();
        plasticList.add(plastic1);
        plasticList.add(plastic2);
        plasticList.add(plastic3);

        PlasticUpdateDto plasticUpdateDto = createPlastic(CardAction.REISSUE_CARD,false,null,plastic2.getPlasticKey().getId(),null);

        Plastic plastic = plasticServices.updatePlastic(plasticUpdateDto,cardsBasic,productDef,plasticList);

        long waiveActivation = cardsBasic.getWaiverDaysActivation();
        assertAll(
                ()->assertEquals(plastic.getPlasticKey().getCardNumber(),cardsBasic.getCardNumber()),
                ()-> assertNotNull(plastic.getPlasticKey().getId()),
                ()-> assertEquals(CardAction.NO_ACTION,plastic.getCardAction()),
                ()-> assertEquals(CardAction.REISSUE_CARD,plastic.getPendingCardAction()),
                ()-> assertEquals(plastic2.getExpiryDate().plusMonths(productDef.getCardsValidityMonthReIssue()).with(TemporalAdjusters.lastDayOfMonth()),
                        plastic.getExpiryDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertNull(plastic.getDatePlasticIssued()),
                ()-> assertNotNull(plastic.getCardActivatedDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertEquals(LocalDate.now().plusDays(productDef.getDaysToCardsValid()),plastic.getDateCardValidFrom()),
                ()-> assertEquals(waiveActivation,plastic.getActivationWaiveDuration().toDays()),
                ()-> assertEquals(plasticUpdateDto.getDynamicCVV(),plastic.getDynamicCVV())

        );


    }

    @Test
    void updatePlasticReIssue1() {

        ProductDef productDef = createProductDef();
        productDef.setCardsActivationRequired(false);

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO();
        cardBasicAddDTO.setOrg(productDef.getProductId().getOrg());
        cardBasicAddDTO.setProduct(productDef.getProductId().getProduct());

        CardsBasic cardsBasic = cardsBasicService.convertDTOToCardBasic(cardBasicAddDTO,productDef);

        Plastic plastic1 = createPlastic();
        Plastic plastic2 = createPlastic();
        plastic2.setExpiryDate(LocalDate.of(2021,4,30));
        Plastic plastic3 = createPlastic();



        List<Plastic> plasticList = new ArrayList<>();
        plasticList.add(plastic1);
        plasticList.add(plastic2);
        plasticList.add(plastic3);

        PlasticUpdateDto plasticUpdateDto = createPlastic(CardAction.REISSUE_CARD,false,null,plastic2.getPlasticKey().getId(),LocalDate.of(2023,12,2));

        Plastic plastic = plasticServices.updatePlastic(plasticUpdateDto,cardsBasic,productDef,plasticList);

        long waiveActivation = cardsBasic.getWaiverDaysActivation();
        assertAll(
                ()->assertEquals(plastic.getPlasticKey().getCardNumber(),cardsBasic.getCardNumber()),
                ()-> assertNotNull(plastic.getPlasticKey().getId()),
                ()-> assertEquals(CardAction.NO_ACTION,plastic.getCardAction()),
                ()-> assertEquals(CardAction.REISSUE_CARD,plastic.getPendingCardAction()),
                ()-> assertEquals(LocalDate.of(2023,12,2).with(TemporalAdjusters.lastDayOfMonth()),
                        plastic.getExpiryDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertNull(plastic.getDatePlasticIssued()),
                ()-> assertNotNull(plastic.getCardActivatedDate()),
                ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                ()-> assertEquals(LocalDate.now().plusDays(productDef.getDaysToCardsValid()),plastic.getDateCardValidFrom()),
                ()-> assertEquals(waiveActivation,plastic.getActivationWaiveDuration().toDays()),
                ()-> assertEquals(plasticUpdateDto.getDynamicCVV(),plastic.getDynamicCVV())

        );


    }

    private Plastic createPlastic(){

        String  cardNumber = Util.generateCardNumberFromStarter("491652996363189");

        return Plastic.builder()
                .dynamicCVV(true)
                .cardAction(CardAction.NO_ACTION)
                .activationWaiveDuration(Duration.ofDays(10))
                .cardActivated(true)
                .cardActivatedDate(LocalDateTime.now())
                .cardNumber(cardNumber)
                .dateCardValidFrom(LocalDate.of(2020,12,02))
                .datePlasticIssued(LocalDateTime.now())
                .expiryDate(LocalDate.of(2022,04,30))
                .pendingCardAction(CardAction.NEW_CARD)
                .plasticKey(new PlasticKey(UUID.randomUUID().toString().replace("-",""),cardNumber))
                .build()
                ;
    }

    private PlasticUpdateDto createPlastic(CardAction cardAction,Boolean activation,Integer replacementDays,String plasticID,LocalDate expiryDate){

        String  cardNumber = Util.generateCardNumberFromStarter("491652996363189");

        PlasticUpdateDto.PlasticUpdateDtoBuilder builder = PlasticUpdateDto.builder()
                .dynamicCVV(true)
                .cardAction(Util.getCardAction(cardAction))
                .cardNumber(cardNumber);

        if(activation != null){
            builder
                    .cardActivate(activation);
        }
        if(replacementDays != null){
            builder
                    .emergencyReplCardsExpiryDays(replacementDays);
        }

        if(plasticID != null){
            builder
                    .plasticId(plasticID);
        }

        if(expiryDate != null){
            builder.expiryDate(expiryDate);
        }

        return builder.build()
                ;
    }


    private ProductDef createProductDef(){

        Map<BalanceTypes,Long> percentMap = new HashMap<>();
        percentMap.put(BalanceTypes.CASH_BALANCE,2000000L);
        percentMap.put(BalanceTypes.INTERNATIONAL_CASH,1000000L);
        percentMap.put(BalanceTypes.INTERNATIONAL_CASH_INSTALLMENT,3000000L);


        return ProductDef.builder()
                .serviceCode(301)
                .productId(new ProductId(001,201))
                .daysToCardsValid(11)
                .dateRangeNewExpDate(10)
                .cardsWaiverActivationDays(5)
                .cardsValidityMonthReplace(24)
                .cardsValidityMonthReIssue(36)
                .cardsValidityMonthNew(48)
                .cardsActivationRequired(true)
                .limitPercents(percentMap)
                .cardsReturn(10)
                .billingCurrencyCode("840")
                .primaryAccountType(AccountType.CREDIT)
                .build();

    }

    private void updateProductTable() {

        ProductDef productDef = createProductDef();
        productDefRepository.save(productDef);
        productTable.loadMap(productDef);

        ProductDef productDef1 = createProductDef();
        productDef1.getProductId().setOrg(001);
        productDef1.getProductId().setProduct(202);
        productDefRepository.save(productDef1);
        productTable.loadMap(productDef1);

        ProductDef productDef2 = createProductDef();
        productDef2.getProductId().setOrg(001);
        productDef2.getProductId().setProduct(203);
        productDefRepository.save(productDef2);
        productTable.loadMap(productDef2);
    }

    private CardBasicAddDTO createCardBasicAddDTO(){


        AccountDefDTO accountDefDTO1 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.SAVINGS))
                .billingCurrencyCode("124")
                .accountNumber(UUID.randomUUID().toString().replace("-",""))
                .build();


        AccountDefDTO accountDefDTO4 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.UNIVERSAL))
                .billingCurrencyCode("840")
                .accountNumber(UUID.randomUUID().toString().replace("-",""))
                .build();

        Set<AccountDefDTO> accountDefDTOSet = new HashSet<>();


        accountDefDTOSet.add(accountDefDTO1);
        accountDefDTOSet.add(accountDefDTO4);

        List<PeriodicCardLimitDTO> periodicCardLimitDTOList = new ArrayList<>();

        List<CardLimitsDTO> cardLimitsDTOList = new ArrayList<>();

        CardLimitsDTO cardLimitsDTO1 = CardLimitsDTO.builder()
                .limitType(Util.getLimitType(LimitType.NO_SPECIFIC))
                .limitNumber(100)
                .limitAmount(10000L)
                .build();

        CardLimitsDTO cardLimitsDTO2 = CardLimitsDTO.builder()
                .limitType(Util.getLimitType(LimitType.CASH))
                .limitNumber(200)
                .limitAmount(20000L)
                .build();

        CardLimitsDTO cardLimitsDTO3 = CardLimitsDTO.builder()
                .limitType(Util.getLimitType(LimitType.RETAIL))
                .limitNumber(300)
                .limitAmount(30000L)
                .build();

        cardLimitsDTOList.add(cardLimitsDTO1);
        cardLimitsDTOList.add(cardLimitsDTO2);
        cardLimitsDTOList.add(cardLimitsDTO3);


        PeriodicCardLimitDTO periodicCardLimitDTO1 = PeriodicCardLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.SINGLE))
                .cardLimitsDTOList(cardLimitsDTOList)
                .build()
                ;


        PeriodicCardLimitDTO periodicCardLimitDTO2 = PeriodicCardLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.DAILY))
                .cardLimitsDTOList(cardLimitsDTOList)
                .build()
                ;

        periodicCardLimitDTOList.add(periodicCardLimitDTO1);
        periodicCardLimitDTOList.add(periodicCardLimitDTO2);


        return CardBasicAddDTO.builder()
                .cardNumber(Util.generateCardNumberFromStarter("491652996363189"))
                .cardholderType(Util.getCardHolderType(CardHolderType.PRIMARY))
                .blockType(Util.getBlockType(BlockType.APPROVE))
                .cardStatus(Util.getCardStatus(CardStatus.ACTIVE))
                .org(001)
                .product(201)
                .waiverDaysActivation(10)
                .periodicCardLimitDTOList(periodicCardLimitDTOList)
                .accountDefDTOSet(accountDefDTOSet)
                .customerNumber(UUID.randomUUID().toString().replace("-",""))
                .build();
    }

    @Test
    void updatePlasticData() {

        Plastic plastic = createPlastic();
        plastic.setCardActivatedDate(null);
        plastic.setDynamicCVV(false);
        plastic.setCardActivated(false);

        plasticRepository.save(plastic);

        PlasticUpdateDto plasticUpdateDto = PlasticUpdateDto.builder()
                .plasticId(plastic.getPlasticKey().getId())
                .cardNumber(plastic.getCardNumber())
                .cardActivate(true)
                .dynamicCVV(true)
                .build();

        plasticServices.updatePlasticData(plasticUpdateDto).block();

        Plastic plastic1 = plasticRepository.findById(new PlasticKey(plasticUpdateDto.getPlasticId(),plasticUpdateDto.getCardNumber())).get();

        assertAll(
                ()-> assertTrue(plastic1.getCardActivated()),
                ()-> assertNotNull(plastic1.getCardActivatedDate()),
                ()-> assertTrue(plastic1.getDynamicCVV())
        );



    }

    @Test
    void updatePlasticData1() {

        Plastic plastic = createPlastic();
        plastic.setCardActivatedDate(null);
        plastic.setDynamicCVV(false);
        plastic.setCardActivated(false);

        plasticRepository.save(plastic);

        PlasticUpdateDto plasticUpdateDto = PlasticUpdateDto.builder()
                .plasticId(plastic.getPlasticKey().getId())
                .cardNumber(plastic.getCardNumber())
                .cardActivate(true)
                .build();

        plasticServices.updatePlasticData(plasticUpdateDto).block();

        Plastic plastic1 = plasticRepository.findById(new PlasticKey(plasticUpdateDto.getPlasticId(),plasticUpdateDto.getCardNumber())).get();

        assertAll(
                ()-> assertTrue(plastic1.getCardActivated()),
                ()-> assertNotNull(plastic1.getCardActivatedDate()),
                ()-> assertFalse(plastic1.getDynamicCVV())
        );



    }

    @Test
    void updatePlasticData2() {

        Plastic plastic = createPlastic();
        plastic.setCardActivatedDate(null);
        plastic.setDynamicCVV(false);
        plastic.setCardActivated(false);

        plasticRepository.save(plastic);

        PlasticUpdateDto plasticUpdateDto = PlasticUpdateDto.builder()
                .plasticId(plastic.getPlasticKey().getId())
                .cardNumber(plastic.getCardNumber())
                .dynamicCVV(true)
                .build();

        plasticServices.updatePlasticData(plasticUpdateDto).block();

        Plastic plastic1 = plasticRepository.findById(new PlasticKey(plasticUpdateDto.getPlasticId(),plasticUpdateDto.getCardNumber())).get();

        assertAll(
                ()-> assertFalse(plastic1.getCardActivated()),
                ()-> assertNull(plastic1.getCardActivatedDate()),
                ()-> assertTrue(plastic1.getDynamicCVV())
        );

    }

    @Test
    void updatePlasticData3() {

        Plastic plastic = createPlastic();
        plastic.setCardActivatedDate(LocalDateTime.of(2020,12,11,22,01,01));
        plastic.setDynamicCVV(false);
        plastic.setCardActivated(true);

        plasticRepository.save(plastic);

        PlasticUpdateDto plasticUpdateDto = PlasticUpdateDto.builder()
                .plasticId(plastic.getPlasticKey().getId())
                .cardNumber(plastic.getCardNumber())
                .dynamicCVV(true)
                .cardActivate(true)
                .build();

        plasticServices.updatePlasticData(plasticUpdateDto).block();

        Plastic plastic1 = plasticRepository.findById(new PlasticKey(plasticUpdateDto.getPlasticId(),plasticUpdateDto.getCardNumber())).get();

        assertAll(
                ()-> assertTrue(plastic1.getCardActivated()),
                ()-> assertEquals(LocalDateTime.of(2020,12,11,22,01,01),plastic1.getCardActivatedDate()),
                ()-> assertTrue(plastic1.getDynamicCVV())
        );

    }
    @Test
    void updatePlasticData4() {

        Plastic plastic = createPlastic();
        plastic.setCardActivatedDate(null);
        plastic.setDynamicCVV(false);
        plastic.setCardActivated(null);

        plasticRepository.save(plastic);

        PlasticUpdateDto plasticUpdateDto = PlasticUpdateDto.builder()
                .plasticId(plastic.getPlasticKey().getId())
                .cardNumber(plastic.getCardNumber())
                .dynamicCVV(true)
                .cardActivate(true)
                .build();

        plasticServices.updatePlasticData(plasticUpdateDto).block();

        Plastic plastic1 = plasticRepository.findById(new PlasticKey(plasticUpdateDto.getPlasticId(),plasticUpdateDto.getCardNumber())).get();

        assertAll(
                ()-> assertTrue(plastic1.getCardActivated()),
                ()-> assertNotNull(plastic1.getCardActivatedDate()),
                ()-> assertTrue(plastic1.getDynamicCVV())
        );
    }

}