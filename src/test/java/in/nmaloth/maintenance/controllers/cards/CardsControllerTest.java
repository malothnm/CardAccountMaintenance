package in.nmaloth.maintenance.controllers.cards;

import in.nmaloth.entity.BlockType;
import in.nmaloth.entity.account.*;
import in.nmaloth.entity.card.*;
import in.nmaloth.entity.customer.AddressType;
import in.nmaloth.entity.customer.CustomerDef;
import in.nmaloth.entity.customer.CustomerIDType;
import in.nmaloth.entity.customer.CustomerType;
import in.nmaloth.entity.product.ProductCardGenDef;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.entity.product.ProductId;
import in.nmaloth.maintenance.config.data.ProductTable;
import in.nmaloth.maintenance.controllers.EndPoints;
import in.nmaloth.maintenance.model.combined.CardsCombinedDTO;
import in.nmaloth.maintenance.model.dto.account.AccountDefDTO;
import in.nmaloth.maintenance.model.dto.card.*;
import in.nmaloth.maintenance.repository.account.AccountBasicRepository;
import in.nmaloth.maintenance.repository.card.CardAccumulatedValuesRepository;
import in.nmaloth.maintenance.repository.card.CardsBasicRepository;
import in.nmaloth.maintenance.repository.customer.CustomerRepository;
import in.nmaloth.maintenance.repository.product.ProductCardGenRepository;
import in.nmaloth.maintenance.repository.product.ProductDefRepository;
import in.nmaloth.maintenance.service.cards.CardAccumValuesService;
import in.nmaloth.maintenance.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext
@AutoConfigureWebTestClient
class CardsControllerTest {


    @Autowired
    private ProductDefRepository productDefRepository;

    @Autowired
    private ProductTable productTable;


    @Autowired
    private CardsBasicRepository cardsBasicRepository;

    @Autowired
    private AccountBasicRepository accountBasicRepository;

    @Autowired
    private ProductCardGenRepository productCardGenRepository;

    @Autowired
    private CardAccumValuesService cardAccumValuesService;


    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CardAccumulatedValuesRepository cardAccumulatedValuesRepository;

    private Set<AccountDef> accountDefSet;

    private CustomerDef customerDef;

    private AccountBasic accountBasic;


    @BeforeEach
    void setup(){


        cardAccumulatedValuesRepository.findAll()
                .forEach(cardAccumulatedValues -> cardAccumulatedValuesRepository.delete(cardAccumulatedValues));


        cardsBasicRepository.findAll()
                .forEach(cardsBasic -> cardsBasicRepository.delete(cardsBasic));


        setupProductTable();

        setupCustomer();
        setupCustomer();
        setupAccounts();

    }


    @Test
    void createNewCardsRecord() {

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO(accountDefSet,customerDef.getCustomerId());

        webTestClient.post()
                .uri(EndPoints.CARDS)
                .body(Mono.just(cardBasicAddDTO),CardBasicAddDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CardsCombinedDTO.class)
                .value(cardsCombinedDTO -> {

                    String cardNumber = cardsCombinedDTO.getCardBasicDTO().getCardId();
                    Optional<CardsBasic> cardsBasicOptional = cardsBasicRepository.findById(cardNumber);
                    Optional<CardAccumulatedValues> cardAccumulatedValuesOptional = cardAccumulatedValuesRepository.findById(cardNumber);


                    assertAll(
                            ()-> assertTrue(cardsBasicOptional.isPresent()),
                            ()-> assertTrue(cardAccumulatedValuesOptional.isPresent())

                    );

                })
                ;

    }

    @Test
    void createNewCardsRecord1() {

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO(accountDefSet,customerDef.getCustomerId());
        cardBasicAddDTO.setCardId(null);

        webTestClient.post()
                .uri(EndPoints.CARDS)
                .body(Mono.just(cardBasicAddDTO),CardBasicAddDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CardsCombinedDTO.class)
                .value(cardsCombinedDTO -> {

                    String cardNumber = cardsCombinedDTO.getCardBasicDTO().getCardId();

                    Optional<CardsBasic> cardsBasicOptional = cardsBasicRepository.findById(cardNumber);
                    Optional<CardAccumulatedValues> cardAccumulatedValuesOptional = cardAccumulatedValuesRepository.findById(cardNumber);



                    assertAll(
                            ()-> assertNotNull(cardNumber),
                            ()-> assertTrue(cardsBasicOptional.isPresent()),
                            ()-> assertTrue(cardAccumulatedValuesOptional.isPresent())

                    );

                })
        ;

    }

    @Test
    void createNewCardsRecord2() {

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO(accountDefSet,customerDef.getCustomerId());
        cardBasicAddDTO.setCardId(null);

        AccountDef[] accountDefArray = accountDefSet.toArray(new AccountDef[0]);

        accountBasicRepository.deleteById(accountDefArray[0].getAccountNumber());

        webTestClient.post()
                .uri(EndPoints.CARDS)
                .body(Mono.just(cardBasicAddDTO),CardBasicAddDTO.class)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)

        ;

    }


    @Test
    void createNewCardsRecord3() {

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO(accountDefSet,customerDef.getCustomerId());
        cardBasicAddDTO.setCardId(null);

        AccountDef[] accountDefArray = accountDefSet.toArray(new AccountDef[0]);

        accountBasicRepository.deleteById(accountDefArray[1].getAccountNumber());

        webTestClient.post()
                .uri(EndPoints.CARDS)
                .body(Mono.just(cardBasicAddDTO),CardBasicAddDTO.class)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)

        ;

    }

    @Test
    void createNewCardsRecord4() {

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO(accountDefSet,customerDef.getCustomerId());
        cardBasicAddDTO.setCardId(null);

        AccountDef[] accountDefArray = accountDefSet.toArray(new AccountDef[0]);

        accountBasicRepository.deleteById(accountDefArray[2].getAccountNumber());

        webTestClient.post()
                .uri(EndPoints.CARDS)
                .body(Mono.just(cardBasicAddDTO),CardBasicAddDTO.class)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)

        ;

    }

    @Test
    void createNewCardsRecord5() {

        CardBasicAddDTO cardBasicAddDTO = createCardBasicAddDTO(accountDefSet,customerDef.getCustomerId());
        cardBasicAddDTO.setCardId(null);


        customerRepository.deleteById(cardBasicAddDTO.getCustomerNumber());

        webTestClient.post()
                .uri(EndPoints.CARDS)
                .body(Mono.just(cardBasicAddDTO),CardBasicAddDTO.class)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .value(s -> {

                    assertNotNull(s);
                        }


                )
        ;

    }

    @Test
    void findCardBasicDetails() {

        CardsBasic cardsBasic = createCardBasic(accountDefSet);
        cardsBasicRepository.save(cardsBasic);

        String url = EndPoints.CARDS_CARD_NBR.replace("{cardNumber}",cardsBasic.getCardId());

        webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CardBasicDTO.class)
                .value(cardBasicDTO -> {
                    assertEquals(cardsBasic.getCardId(),cardBasicDTO.getCardId());
                });

    }

    @Test
    void findCardBasicDetails1() {

        CardsBasic cardsBasic = createCardBasic(accountDefSet);

        String url = EndPoints.CARDS_CARD_NBR.replace("{cardNumber}",cardsBasic.getCardId());

        webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .value(errorMessage -> {
                    assertNotNull(errorMessage);
                });

    }

    @Test
    void findCardAccumulatedDetails() {

        CardsBasic cardsBasic = createCardBasic(accountDefSet);

        String url = EndPoints.CARDS_LIMITS_CARD_NBR.replace("{cardNumber}",cardsBasic.getCardId());

        CardAccumulatedValues cardAccumulatedValues = createCardAccumValues(cardsBasic.getCardId());

        cardAccumulatedValuesRepository.save(cardAccumulatedValues);

        webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CardAccumulatedValues.class)
                .value(cardAccumulatedValues1 -> {

                    assertNotNull(cardAccumulatedValues1.getCardId());
                });



    }

    @Test
    void findCardAccumulatedDetails1() {

        CardsBasic cardsBasic = createCardBasic(accountDefSet);

        String url = EndPoints.CARDS_LIMITS_CARD_NBR.replace("{cardNumber}",cardsBasic.getCardId());

        CardAccumulatedValues cardAccumulatedValues = createCardAccumValues(cardsBasic.getCardId());


        webTestClient.get()
                .uri(url)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .value(s -> {

                    assertNotNull(s);
                });



    }


    @Test
    void deleteCardBasicDetails() {

        CardsBasic cardsBasic = createCardBasic(accountDefSet);
        cardsBasicRepository.save(cardsBasic);

        String url = EndPoints.CARDS_CARD_NBR.replace("{cardNumber}",cardsBasic.getCardId());

        webTestClient.delete()
                .uri(url)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CardBasicDTO.class)
                .value(cardBasicDTO -> {

                    assertTrue(cardsBasicRepository.findById(cardBasicDTO.getCardId()).isEmpty());
                });

    }

    @Test
    void deleteCardBasicDetails1() {

        CardsBasic cardsBasic = createCardBasic(accountDefSet);

        String url = EndPoints.CARDS_CARD_NBR.replace("{cardNumber}",cardsBasic.getCardId());

        webTestClient.delete()
                .uri(url)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .value(s -> {

                    assertNotNull(s);
                });

    }

    @Test
    void deleteCardAccumulatedDetails() {

        CardsBasic cardsBasic = createCardBasic(accountDefSet);

        String url = EndPoints.CARDS_LIMITS_CARD_NBR.replace("{cardNumber}",cardsBasic.getCardId());

        CardAccumulatedValues cardAccumulatedValues = createCardAccumValues(cardsBasic.getCardId());

        cardAccumulatedValuesRepository.save(cardAccumulatedValues);

        webTestClient.delete()
                .uri(url)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CardAccumulatedValues.class)
                .value(cardAccumulatedValues1 -> {

                    assertTrue(cardAccumulatedValuesRepository.findById(cardAccumulatedValues.getCardId()).isEmpty());
                });

    }

    @Test
    void deleteCardAccumulatedDetails1() {

        CardsBasic cardsBasic = createCardBasic(accountDefSet);

        String url = EndPoints.CARDS_LIMITS_CARD_NBR.replace("{cardNumber}",cardsBasic.getCardId());

        CardAccumulatedValues cardAccumulatedValues = createCardAccumValues(cardsBasic.getCardId());

//        cardAccumulatedValuesRepository.save(cardAccumulatedValues);

        webTestClient.delete()
                .uri(url)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .value(s -> {

                    assertNotNull(s);
                });

    }

    @Test
    void updateCardsBasic() {

        CardsBasic cardsBasic = createCardBasic(accountDefSet);
        cardsBasic.setCustomerNumber(customerDef.getCustomerId());

        cardsBasicRepository.save(cardsBasic);


        CardAccumulatedValues cardAccumulatedValues = createCardAccumValues(cardsBasic.getCardId());
        cardAccumulatedValuesRepository.save(cardAccumulatedValues);

        AccountDef[] accountDefArray = accountDefSet.toArray(new AccountDef[0]);

        CardBasicUpdateDTO cardBasicUpdateDTO = createCardBasic(true,null, accountDefArray[1].getAccountNumber());
        cardBasicUpdateDTO.setCardId(cardsBasic.getCardId());
        webTestClient
                .put()
                .uri(EndPoints.CARDS)
                .body(Mono.just(cardBasicUpdateDTO),CardBasicUpdateDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CardsCombinedDTO.class)
                .value(cardsCombinedDTO -> {

                    String cardNumber = cardsCombinedDTO.getCardBasicDTO().getCardId();

                    Optional<CardsBasic> cardsBasicOptional = cardsBasicRepository.findById(cardNumber);
                    Optional<CardAccumulatedValues> cardAccumulatedValuesOptional = cardAccumulatedValuesRepository.findById(cardNumber);

                    assertAll(
                            ()-> assertTrue(cardsBasicOptional.isPresent()),
                            ()-> assertTrue(cardAccumulatedValuesOptional.isPresent()),
                            ()-> assertEquals(cardsCombinedDTO.getCardBasicDTO().getBlockType(),cardBasicUpdateDTO.getBlockType()),
                            ()-> assertEquals(cardsBasicOptional.get().getBlockType(),Util.getBlockType(cardBasicUpdateDTO.getBlockType()))

                            );
                });

    }

    @Test
    void updateCardsBasic1() {

        CardsBasic cardsBasic = createCardBasic(accountDefSet);
        cardsBasic.setCustomerNumber(customerDef.getCustomerId());

//        cardsBasicRepository.save(cardsBasic);



        CardAccumulatedValues cardAccumulatedValues = createCardAccumValues(cardsBasic.getCardId());
//        cardAccumulatedValuesRepository.save(cardAccumulatedValues);

        AccountDef[] accountDefArray = accountDefSet.toArray(new AccountDef[0]);

        CardBasicUpdateDTO cardBasicUpdateDTO = createCardBasic(true,null, accountDefArray[1].getAccountNumber());
        cardBasicUpdateDTO.setCardId(cardsBasic.getCardId());
        webTestClient
                .put()
                .uri(EndPoints.CARDS)
                .body(Mono.just(cardBasicUpdateDTO),CardBasicUpdateDTO.class)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .value(s -> {
                        assertNotNull(s);

                });

    }

    @Test
    void createNewPlastics(){

        CardsBasic cardsBasic = createCardBasic(accountDefSet);
        cardsBasic.setCustomerNumber(customerDef.getCustomerId());
        cardsBasicRepository.save(cardsBasic);

        long waiveActivation = cardsBasic.getWaiverDaysActivation();

        PlasticUpdateDto plasticUpdateDto = createPlastic(CardAction.NEW_CARD,false,null,null,null);

        webTestClient.post()
                .uri(EndPoints.CARDS_NEW_PLASTIC)
                .body(Mono.just(plasticUpdateDto),PlasticUpdateDto.class)
                .exchange().expectStatus().isOk()
                .expectBody(CardBasicDTO.class)
                .value(cardBasicDTO -> {

                    CardsBasic cardsBasic1 = cardsBasicRepository.findById(cardsBasic.getCardId()).get();

                    Plastic plastic = cardsBasic1.getPlasticList().get(0);

                    ProductDef productDef = productDefRepository.findById(new ProductId(cardsBasic.getOrg(),cardsBasic.getProduct())).get();

                    assertAll(
                            ()-> assertEquals(1,cardBasicDTO.getPlasticsDTOList().size()),
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
                });

    }

    @Test
    void createNewPlastics1(){

        CardsBasic cardsBasic = createCardBasic(accountDefSet);
        cardsBasic.setCustomerNumber(customerDef.getCustomerId());
//        cardsBasicRepository.save(cardsBasic);

        long waiveActivation = cardsBasic.getWaiverDaysActivation();

        PlasticUpdateDto plasticUpdateDto = createPlastic(CardAction.NEW_CARD,false,null,null,null);

        webTestClient.post()
                .uri(EndPoints.CARDS_NEW_PLASTIC)
                .body(Mono.just(plasticUpdateDto),PlasticUpdateDto.class)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .value(s -> {
                    assertNotNull(s);
                });
    }

    @Test
    void createNewPlasticsEmergencyRepl(){

        CardsBasic cardsBasic = createCardBasic(accountDefSet);
        cardsBasic.setCustomerNumber(customerDef.getCustomerId());
        cardsBasicRepository.save(cardsBasic);

        long waiveActivation = cardsBasic.getWaiverDaysActivation();

        PlasticUpdateDto plasticUpdateDto = createPlastic(CardAction.EMERGENCY_REPLACEMENT_CARD,false,30,null,null);

        webTestClient.post()
                .uri(EndPoints.CARDS_NEW_PLASTIC)
                .body(Mono.just(plasticUpdateDto),PlasticUpdateDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CardBasicDTO.class)
                .value(cardBasicDTO -> {

                    CardsBasic cardsBasic1 = cardsBasicRepository.findById(cardsBasic.getCardId()).get();

                    Plastic plastic = cardsBasic1.getPlasticList().get(0);
                    ProductDef productDef = productDefRepository.findById(new ProductId(cardsBasic.getOrg(),cardsBasic.getProduct())).get();

                    assertAll(
                            ()-> assertEquals(1,cardBasicDTO.getPlasticsDTOList().size()),
                            ()-> assertEquals(CardAction.NO_ACTION,plastic.getCardAction()),
                            ()-> assertEquals(CardAction.EMERGENCY_REPLACEMENT_CARD,plastic.getPendingCardAction()),
                            ()-> assertEquals(LocalDate.now().plusDays(plasticUpdateDto.getEmergencyReplCardsExpiryDays()).with(TemporalAdjusters.lastDayOfMonth()),
                                    plastic.getExpiryDate()),
                            ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                            ()-> assertNull(plastic.getDatePlasticIssued()),
                            ()-> assertNull(plastic.getCardActivatedDate()),
                            ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                            ()-> assertEquals(LocalDate.now().plusDays(productDef.getDaysToCardsValid()),plastic.getDateCardValidFrom()),
                            ()-> assertEquals(waiveActivation,plastic.getActivationWaiveDuration().toDays()),
                            ()-> assertEquals(plasticUpdateDto.getDynamicCVV(),plastic.getDynamicCVV())
                    );
                });

    }


    @Test
    void createNewPlasticsEmergencyRepl1(){

        CardsBasic cardsBasic = createCardBasic(accountDefSet);
        cardsBasic.setCustomerNumber(customerDef.getCustomerId());

        cardsBasicRepository.save(cardsBasic);

        long waiveActivation = cardsBasic.getWaiverDaysActivation();

        PlasticUpdateDto plasticUpdateDto = createPlastic(CardAction.EMERGENCY_REPLACEMENT_CARD,false,null,null,null);

        webTestClient.post()
                .uri(EndPoints.CARDS_NEW_PLASTIC)
                .body(Mono.just(plasticUpdateDto),PlasticUpdateDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .value(s-> {
                    assertNotNull(s);
                });

    }

    @Test
    void createNewPlasticRepl(){

        CardsBasic cardsBasic = createCardBasic(accountDefSet);

        ProductDef productDef = productDefRepository.findById(new ProductId(cardsBasic.getOrg(),cardsBasic.getProduct())).get();

        long waiveActivation = cardsBasic.getWaiverDaysActivation();

        Plastic plastic1 = createPlastic(CardAction.NEW_CARD,cardsBasic.getCardId());
        Plastic plastic2 = createPlastic(CardAction.NEW_CARD,cardsBasic.getCardId());
        plastic2.setExpiryDate(LocalDate.of(2021,4,30));
        Plastic plastic3 = createPlastic(CardAction.NEW_CARD,cardsBasic.getCardId());

        List<Plastic> plasticList = new ArrayList<>();
        plasticList.add(plastic1);
        plasticList.add(plastic2);
        plasticList.add(plastic3);

        cardsBasic.setPlasticList(plasticList);
        cardsBasicRepository.save(cardsBasic);

        PlasticUpdateDto plasticUpdateDto = createPlastic(CardAction.REPLACEMENT_CARD,false,null,plastic2.getPlasticId(),null);

        webTestClient.post()
                .uri(EndPoints.CARDS_NEW_PLASTIC)
                .body(Mono.just(plasticUpdateDto),PlasticUpdateDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CardBasicDTO.class)
                .value(cardBasicDTO -> {

                    CardsBasic cardsBasic1 = cardsBasicRepository.findById(cardsBasic.getCardId()).get();

                    String plasticId = cardBasicDTO.getPlasticsDTOList().stream()
                            .filter(plasticsDTO -> !plasticsDTO.getPlasticId().equals(plastic1.getPlasticId()))
                            .filter(plasticsDTO -> !plasticsDTO.getPlasticId().equals(plastic2.getPlasticId()))
                            .filter(plasticsDTO -> !plasticsDTO.getPlasticId().equals(plastic3.getPlasticId()))
                            .map(plasticsDTO -> plasticsDTO.getPlasticId())
                            .findFirst()
                            .get();



                    Plastic plastic = findPlasticById(plasticId,cardsBasic1.getPlasticList());

                    assertAll(
                            ()-> assertEquals(4,cardsBasic1.getPlasticList().size()),
                            ()-> assertEquals(CardAction.NO_ACTION,plastic.getCardAction()),
                            ()-> assertEquals(CardAction.REPLACEMENT_CARD,plastic.getPendingCardAction()),
                            ()-> assertEquals(LocalDate.now().plusMonths(productDef.getCardsValidityMonthReplace()).with(TemporalAdjusters.lastDayOfMonth()),
                                    plastic.getExpiryDate()),
                            ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                            ()-> assertNull(plastic.getDatePlasticIssued()),
                            ()-> assertNull(plastic.getCardActivatedDate()),
                            ()-> assertEquals(!productDef.getCardsActivationRequired(),plastic.getCardActivated()),
                            ()-> assertEquals(LocalDate.now().plusDays(productDef.getDaysToCardsValid()),plastic.getDateCardValidFrom()),
                            ()-> assertEquals(waiveActivation,plastic.getActivationWaiveDuration().toDays()),
                            ()-> assertEquals(plasticUpdateDto.getDynamicCVV(),plastic.getDynamicCVV())

                    );

                });


    }


    private Plastic findPlasticById(String plasticId,List<Plastic> plasticList){

        return plasticList.stream()
                .filter(plastic -> plastic.getPlasticId().equals(plasticId))
                .findFirst()
                .get();
    }

    @Test
    void fetchPlasticInfo(){

        CardsBasic cardsBasic = createCardBasic(accountDefSet);

        ProductDef productDef = productDefRepository.findById(new ProductId(cardsBasic.getOrg(),cardsBasic.getProduct())).get();

        long waiveActivation = cardsBasic.getWaiverDaysActivation();

        Plastic plastic1 = createPlastic(CardAction.NEW_CARD,cardsBasic.getCardId());
        Plastic plastic2 = createPlastic(CardAction.NEW_CARD,cardsBasic.getCardId());
        plastic2.setExpiryDate(LocalDate.of(2021,4,30));
        Plastic plastic3 = createPlastic(CardAction.NEW_CARD,cardsBasic.getCardId());

        List<Plastic> plasticList = new ArrayList<>();
        plasticList.add(plastic1);
        plasticList.add(plastic2);
        plasticList.add(plastic3);

        cardsBasic.setPlasticList(plasticList);
        cardsBasicRepository.save(cardsBasic);

        String uri = EndPoints.CARDS_CARD_NBR_PLASTIC_ID.replace("{cardNumber}",cardsBasic.getCardId())
                .replace("{plasticId}",plastic2.getPlasticId());


        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PlasticsDTO.class)
                .value(plasticsDTO -> {

                    assertAll(
                            ()-> assertEquals(plastic2.getActivationWaiveDuration().toDays(),plasticsDTO.getActivationWaiveDuration()),
                            ()-> assertEquals(plastic2.getDynamicCVV(),plasticsDTO.getDynamicCVV()),
                            ()-> assertEquals(plastic2.getCardAction(),Util.getCardAction(plasticsDTO.getCardAction())),
                            ()-> assertEquals(plastic2.getCardActivated(),plasticsDTO.getCardActivated()),
                            ()-> assertEquals(plastic2.getCardActivatedDate(),plasticsDTO.getCardActivatedDate()),
                            ()-> assertEquals(plastic2.getDateCardValidFrom(),plasticsDTO.getDateCardValidFrom()),
                            ()-> assertEquals(plastic2.getDatePlasticIssued(),plasticsDTO.getDatePlasticIssued()),
                            ()-> assertEquals(plastic2.getExpiryDate(),plasticsDTO.getExpiryDate()),
                            ()-> assertEquals(plastic2.getPendingCardAction(),Util.getCardAction(plasticsDTO.getPendingCardAction())),
                            ()-> assertEquals(plastic2.getPlasticId(),plasticsDTO.getPlasticId())
                    );

                });

    }

    @Test
    void fetchPlasticInfo1(){

        CardsBasic cardsBasic = createCardBasic(accountDefSet);


        ProductDef productDef = productDefRepository.findById(new ProductId(cardsBasic.getOrg(),cardsBasic.getProduct())).get();

        long waiveActivation = cardsBasic.getWaiverDaysActivation();

        Plastic plastic1 = createPlastic(CardAction.NEW_CARD,cardsBasic.getCardId());
        Plastic plastic2 = createPlastic(CardAction.NEW_CARD,cardsBasic.getCardId());
        plastic2.setExpiryDate(LocalDate.of(2021,4,30));
        Plastic plastic3 = createPlastic(CardAction.NEW_CARD,cardsBasic.getCardId());

        List<Plastic> plasticList = new ArrayList<>();
        plasticList.add(plastic1);
//        plasticList.add(plastic2);
        plasticList.add(plastic3);

        cardsBasic.setPlasticList(plasticList);
        cardsBasicRepository.save(cardsBasic);

        String uri = EndPoints.CARDS_CARD_NBR_PLASTIC_ID.replace("{cardNumber}",cardsBasic.getCardId())
                .replace("{plasticId}",plastic2.getPlasticId());


        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .value(s -> {
                    assertNotNull(s);
                });

    }


    @Test
    void deletePlasticInfo(){

        CardsBasic cardsBasic = createCardBasic(accountDefSet);


        ProductDef productDef = productDefRepository.findById(new ProductId(cardsBasic.getOrg(),cardsBasic.getProduct())).get();

        long waiveActivation = cardsBasic.getWaiverDaysActivation();

        Plastic plastic1 = createPlastic(CardAction.NEW_CARD,cardsBasic.getCardId());
        Plastic plastic2 = createPlastic(CardAction.NEW_CARD,cardsBasic.getCardId());
        plastic2.setExpiryDate(LocalDate.of(2021,4,30));
        Plastic plastic3 = createPlastic(CardAction.NEW_CARD,cardsBasic.getCardId());

        List<Plastic> plasticList = new ArrayList<>();
        plasticList.add(plastic1);
        plasticList.add(plastic2);
        plasticList.add(plastic3);

        cardsBasic.setPlasticList(plasticList);
        cardsBasicRepository.save(cardsBasic);

        String uri = EndPoints.CARDS_CARD_NBR_PLASTIC_ID.replace("{cardNumber}",cardsBasic.getCardId())
                .replace("{plasticId}",plastic2.getPlasticId());


        webTestClient.delete()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CardBasicDTO.class)
                .value(cardBasicDTO -> {

                    CardsBasic cardsBasic1 = cardsBasicRepository.findById(cardsBasic.getCardId()).get();

                    Optional<Plastic> plasticOptional = cardsBasic1.getPlasticList()
                            .stream()
                            .filter(plastic -> plastic.getPlasticId().equals(plastic2.getPlasticId()))
                            .findFirst();

                    assertTrue(plasticOptional.isEmpty());
                });

    }

    @Test
    void deletePlasticInfo1(){

        CardsBasic cardsBasic = createCardBasic(accountDefSet);

        ProductDef productDef = productDefRepository.findById(new ProductId(cardsBasic.getOrg(),cardsBasic.getProduct())).get();

        long waiveActivation = cardsBasic.getWaiverDaysActivation();

        Plastic plastic1 = createPlastic(CardAction.NEW_CARD,cardsBasic.getCardId());
        Plastic plastic2 = createPlastic(CardAction.NEW_CARD,cardsBasic.getCardId());
        plastic2.setExpiryDate(LocalDate.of(2021,4,30));
        Plastic plastic3 = createPlastic(CardAction.NEW_CARD,cardsBasic.getCardId());

        List<Plastic> plasticList = new ArrayList<>();
        plasticList.add(plastic1);
//        plasticList.add(plastic2);
        plasticList.add(plastic3);

        cardsBasic.setPlasticList(plasticList);
        cardsBasicRepository.save(cardsBasic);

        String uri = EndPoints.CARDS_CARD_NBR_PLASTIC_ID.replace("{cardNumber}",cardsBasic.getCardId())
                .replace("{plasticId}",plastic2.getPlasticId());


        webTestClient.delete()
                .uri(uri)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .value(s -> {
                    assertNotNull(s);
                });

    }

    @Test
    void fetchPlasticInfoForCard(){

        CardsBasic cardsBasic = createCardBasic(accountDefSet);

        ProductDef productDef = productDefRepository.findById(new ProductId(cardsBasic.getOrg(),cardsBasic.getProduct())).get();

        long waiveActivation = cardsBasic.getWaiverDaysActivation();

        Plastic plastic1 = createPlastic(CardAction.NEW_CARD,cardsBasic.getCardId());
        Plastic plastic2 = createPlastic(CardAction.NEW_CARD,cardsBasic.getCardId());
        plastic2.setExpiryDate(LocalDate.of(2021,4,30));
        Plastic plastic3 = createPlastic(CardAction.NEW_CARD,cardsBasic.getCardId());

        List<Plastic> plasticList = new ArrayList<>();
        plasticList.add(plastic1);
        plasticList.add(plastic2);
        plasticList.add(plastic3);

        cardsBasic.setPlasticList(plasticList);
        cardsBasicRepository.save(cardsBasic);

        String uri = EndPoints.CARDS_PLASTIC_CARD_NUMBER.replace("{cardNumber}",cardsBasic.getCardId());


        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PlasticsDTO.class)
                .hasSize(3);
    }


    @Test
    void deletePlasticInfoForCard(){

        CardsBasic cardsBasic = createCardBasic(accountDefSet);


        ProductDef productDef = productDefRepository.findById(new ProductId(cardsBasic.getOrg(),cardsBasic.getProduct())).get();

        long waiveActivation = cardsBasic.getWaiverDaysActivation();

        Plastic plastic1 = createPlastic(CardAction.NEW_CARD,cardsBasic.getCardId());
        Plastic plastic2 = createPlastic(CardAction.NEW_CARD,cardsBasic.getCardId());
        plastic2.setExpiryDate(LocalDate.of(2021,4,30));
        Plastic plastic3 = createPlastic(CardAction.NEW_CARD,cardsBasic.getCardId());

        List<Plastic> plasticList = new ArrayList<>();
        plasticList.add(plastic1);
        plasticList.add(plastic2);
        plasticList.add(plastic3);

        cardsBasic.setPlasticList(plasticList);
        cardsBasicRepository.save(cardsBasic);

        String uri = EndPoints.CARDS_PLASTIC_CARD_NUMBER.replace("{cardNumber}",cardsBasic.getCardId());


        webTestClient.delete()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CardBasicDTO.class)
                .consumeWith(cardBasicDTOEntityExchangeResult -> {

                    CardBasicDTO cardBasicDTO = cardBasicDTOEntityExchangeResult.getResponseBody();

                   CardsBasic cardsBasic1 = cardsBasicRepository.findById(cardsBasic.getCardId()).get();

                    assertAll(
                            ()-> assertEquals(0, cardBasicDTO.getPlasticsDTOList().size()),
                            ()-> assertEquals(0,cardsBasic1.getPlasticList().size())

                            );


                })

        ;
    }

    private Plastic createPlastic(CardAction cardAction,String cardNumber){

        return Plastic.builder()
                .dynamicCVV(true)
                .cardAction(cardAction)
                .activationWaiveDuration(Duration.ofDays(10))
                .cardActivated(true)
                .cardActivatedDate(LocalDateTime.now())
                .dateCardValidFrom(LocalDate.of(2020,12,02))
                .datePlasticIssued(LocalDateTime.now())
                .expiryDate(LocalDate.of(2022,04,30))
                .pendingCardAction(CardAction.NO_ACTION)
                .plasticId(UUID.randomUUID().toString().replace("-",""))
                .build()
                ;
    }

    private PlasticUpdateDto createPlastic(CardAction cardAction,Boolean activation,
                                           Integer replacementDays,String plasticID,LocalDate expiryDate){

        String  cardNumber = Util.generateCardNumberFromStarter("491652996363189");

        PlasticUpdateDto.PlasticUpdateDtoBuilder builder = PlasticUpdateDto.builder()
                .dynamicCVV(true)
                .cardAction(Util.getCardAction(cardAction))
                .cardId(cardNumber);

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

    private CardAccumulatedValues createCardAccumValues(String cardId){


        Map<PeriodicType,Map<LimitType,PeriodicCardAmount>> periodicLimitMap = new HashMap<>();

        Map<LimitType,PeriodicCardAmount> cardLimitMap = new HashMap<>();

        PeriodicCardAmount periodicCardAmount1 = PeriodicCardAmount.builder()
                .limitType(LimitType.NO_SPECIFIC)
                .transactionNumber(100)
                .transactionAmount(10000L)
                .build();

        PeriodicCardAmount periodicCardAmount2  = PeriodicCardAmount.builder()
                .limitType(LimitType.CASH)
                .transactionNumber(200)
                .transactionAmount(20000L)
                .build();

        PeriodicCardAmount periodicCardAmount3 = PeriodicCardAmount.builder()
                .limitType(LimitType.RETAIL)
                .transactionNumber(300)
                .transactionAmount(30000L)
                .build();

        cardLimitMap.put(periodicCardAmount1.getLimitType(),periodicCardAmount1);
        cardLimitMap.put(periodicCardAmount2.getLimitType(),periodicCardAmount2);
        cardLimitMap.put(periodicCardAmount3.getLimitType(),periodicCardAmount3);


        periodicLimitMap.put(PeriodicType.SINGLE,cardLimitMap);
        periodicLimitMap.put(PeriodicType.DAILY,cardLimitMap);
        periodicLimitMap.put(PeriodicType.MONTHLY,cardLimitMap);

        Map<PeriodicType,Map<LimitType,PeriodicCardAmount>> periodicAmountMap = new HashMap<>();

        Map<LimitType,PeriodicCardAmount> cardAmountMap = new HashMap<>();

        PeriodicCardAmount periodicCardAccum1 = PeriodicCardAmount.builder()
                .limitType(LimitType.NO_SPECIFIC)
                .transactionNumber(0)
                .transactionAmount(0L)
                .build();

        PeriodicCardAmount periodicCardAccum2  = PeriodicCardAmount.builder()
                .limitType(LimitType.CASH)
                .transactionNumber(0)
                .transactionAmount(0L)
                .build();

        PeriodicCardAmount periodicCardAccum3 = PeriodicCardAmount.builder()
                .limitType(LimitType.RETAIL)
                .transactionNumber(0)
                .transactionAmount(0L)
                .build();

        cardAmountMap.put(periodicCardAccum1.getLimitType(),periodicCardAccum1);
        cardAmountMap.put(periodicCardAccum2.getLimitType(),periodicCardAccum2);
        cardAmountMap.put(periodicCardAccum3.getLimitType(),periodicCardAccum3);


        periodicAmountMap.put(PeriodicType.SINGLE,cardAmountMap);
        periodicAmountMap.put(PeriodicType.DAILY,cardAmountMap);
        periodicAmountMap.put(PeriodicType.MONTHLY,cardAmountMap);


        return CardAccumulatedValues.builder()
                .cardId(cardId)
                .org(1)
                .product(201)
                .periodicTypePeriodicCardLimitMap(periodicLimitMap)
                .periodicCardAccumulatedValueMap(periodicAmountMap)
                .build();
    }


    private CardsBasic createCardBasic(Set<AccountDef> accountDefSet){





        return CardsBasic.builder()
                .cardId(Util.generateCardNumberFromStarter("491652996363189"))
                .cardholderType(CardHolderType.PRIMARY)
                .blockType(BlockType.APPROVE)
                .cardStatus(CardStatus.ACTIVE)
                .org(001)
                .product(201)
                .waiverDaysActivation(10)
                .accountDefSet(accountDefSet)
                .customerNumber(UUID.randomUUID().toString().replace("-",""))
                .build();
    }

    private CardBasicAddDTO createCardBasicAddDTO(Set<AccountDef> accountDefSet,String customerNumber){


        Set<AccountDefDTO> accountDefDTOSet = accountDefSet.stream()
                .map(accountDef -> AccountDefDTO.builder()
                        .billingCurrencyCode(accountDef.getBillingCurrencyCode())
                        .accountId(accountDef.getAccountNumber())
                        .accountType(Util.getAccountType(accountDef.getAccountType()))
                        .build()
                )
                .collect(Collectors.toSet());

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
                .cardId(Util.generateCardNumberFromStarter("491652996363189"))
                .cardholderType(Util.getCardHolderType(CardHolderType.PRIMARY))
                .blockType(Util.getBlockType(BlockType.APPROVE))
                .cardStatus(Util.getCardStatus(CardStatus.ACTIVE))
                .org(001)
                .product(201)
                .waiverDaysActivation(10)
                .periodicCardLimitDTOList(periodicCardLimitDTOList)
                .accountDefDTOSet(accountDefDTOSet)
                .customerNumber(customerNumber)
                .build();
    }

    private CardBasicUpdateDTO createCardBasic(boolean allFields, List<Integer> integerList, String deleteAccountNumber){

        CardBasicUpdateDTO.CardBasicUpdateDTOBuilder builder = CardBasicUpdateDTO.builder()
                .cardId(Util.generateCardNumberFromStarter("491652996363189"));

        List<PeriodicCardLimitDTO> periodicCardLimitDTOList = new ArrayList<>();
        List<PeriodicCardLimitDTO> periodicCardLimitDTOListDelete = new ArrayList<>();


        List<CardLimitsDTO> cardLimitsDTOList = new ArrayList<>();
        List<CardLimitsDTO> cardLimitsDTOListDelete = new ArrayList<>();


        CardLimitsDTO cardLimitsDTO1 = CardLimitsDTO.builder()
                .limitType(Util.getLimitType(LimitType.NO_SPECIFIC))
                .limitNumber(400)
                .limitAmount(40000L)
                .build();

        CardLimitsDTO cardLimitsDTO2 = CardLimitsDTO.builder()
                .limitType(Util.getLimitType(LimitType.OTC))
                .limitNumber(600)
                .limitAmount(60000L)
                .build();

        CardLimitsDTO cardLimitsDTO3 = CardLimitsDTO.builder()
                .limitType(Util.getLimitType(LimitType.QUASI_CASH))
                .limitNumber(700)
                .limitAmount(70000L)
                .build();

        CardLimitsDTO cardLimitsDTO4 = CardLimitsDTO.builder()
                .limitType(Util.getLimitType(LimitType.CASH))
                .limitNumber(800)
                .limitAmount(80000L)
                .build();


        cardLimitsDTOList.add(cardLimitsDTO1);
        cardLimitsDTOList.add(cardLimitsDTO2);
        cardLimitsDTOList.add(cardLimitsDTO3);

        cardLimitsDTOListDelete.add(cardLimitsDTO4);
        cardLimitsDTOListDelete.add(cardLimitsDTO1);

        PeriodicCardLimitDTO periodicCardLimitDTO1 = PeriodicCardLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.SINGLE))
                .cardLimitsDTOList(cardLimitsDTOList)
                .build()
                ;

        PeriodicCardLimitDTO periodicCardLimitDTO2 = PeriodicCardLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.MONTHLY))
                .cardLimitsDTOList(cardLimitsDTOList)
                .build()
                ;


        PeriodicCardLimitDTO periodicCardLimitDTO4 = PeriodicCardLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.SINGLE))
                .cardLimitsDTOList(cardLimitsDTOListDelete)
                .build()
                ;

        PeriodicCardLimitDTO periodicCardLimitDTO5 = PeriodicCardLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.DAILY))
                .cardLimitsDTOList(cardLimitsDTOListDelete)
                .build()
                ;

        PeriodicCardLimitDTO periodicCardLimitDTO3 = PeriodicCardLimitDTO.builder()
                .periodicType(Util.getPeriodicType(PeriodicType.DAILY))
                .cardLimitsDTOList(cardLimitsDTOList)
                .build()
                ;

        periodicCardLimitDTOList.add(periodicCardLimitDTO1);
        periodicCardLimitDTOList.add(periodicCardLimitDTO2);
        periodicCardLimitDTOList.add(periodicCardLimitDTO3);

        periodicCardLimitDTOListDelete.add(periodicCardLimitDTO4);
        periodicCardLimitDTOListDelete.add(periodicCardLimitDTO5);


        AccountDefDTO accountDefDTO1 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.SAVINGS))
                .billingCurrencyCode("124")
                .accountId(UUID.randomUUID().toString().replace("-",""))
                .build();

        AccountDefDTO accountDefDTO2 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.CREDIT))
                .billingCurrencyCode("840")
                .accountId(UUID.randomUUID().toString().replace("-",""))
                .build();
        AccountDefDTO accountDefDTO3 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.CURRENT))
                .billingCurrencyCode("484")
                .accountId(UUID.randomUUID().toString().replace("-",""))
                .build();

        AccountDefDTO accountDefDTO4 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.UNIVERSAL))
                .billingCurrencyCode("840")
                .accountId(deleteAccountNumber)
                .build();

        Set<AccountDefDTO> accountDefDTOSetAdd = new HashSet<>();

        Set<AccountDefDTO> accountDefDTOSetDelete = new HashSet<>();
        accountDefDTOSetDelete .add(accountDefDTO4);

        accountDefDTOSetAdd.add(accountDefDTO1);
        accountDefDTOSetAdd.add(accountDefDTO2);
        accountDefDTOSetAdd.add(accountDefDTO3);



        if(allFields){
            return builder
                    .blockType(Util.getBlockType(BlockType.BLOCK_DECLINE))
                    .cardHolderType(Util.getCardHolderType(CardHolderType.SECONDARY))
                    .cardsReturned(1)
                    .cardStatus(Util.getCardStatus(CardStatus.FRAUD))
                    .waiverDaysActivation(20)
                    .periodicCardLimitDTOAddList(periodicCardLimitDTOList)
                    .periodicCardLimitDTODeleteList(periodicCardLimitDTOListDelete)
                    .customerNumber(UUID.randomUUID().toString().replace("-",""))
                    .accountDefDTOSetDelete(accountDefDTOSetDelete)
                    .accountDefDTOSetAdd(accountDefDTOSetAdd)
                    .build();
        }

        integerList.forEach(integer -> evaluateBuilders(integer,builder,periodicCardLimitDTOList,
                periodicCardLimitDTOListDelete,deleteAccountNumber));

        return builder.build();

    }

    private void evaluateBuilders(Integer integer, CardBasicUpdateDTO.CardBasicUpdateDTOBuilder builder,
                                  List<PeriodicCardLimitDTO> periodicCardLimitDTOList,
                                  List<PeriodicCardLimitDTO> periodicCardLimitDTOListDelete,String deleteAccountNumber) {


        AccountDefDTO accountDefDTO1 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.SAVINGS))
                .billingCurrencyCode("124")
                .accountId(UUID.randomUUID().toString().replace("-",""))
                .build();

        AccountDefDTO accountDefDTO2 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.CREDIT))
                .billingCurrencyCode("840")
                .accountId(UUID.randomUUID().toString().replace("-",""))
                .build();
        AccountDefDTO accountDefDTO3 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.CURRENT))
                .billingCurrencyCode("484")
                .accountId(UUID.randomUUID().toString().replace("-",""))
                .build();

        AccountDefDTO accountDefDTO4 = AccountDefDTO.builder()
                .accountType(Util.getAccountType(AccountType.UNIVERSAL))
                .billingCurrencyCode("840")
                .accountId(deleteAccountNumber)
                .build();

        Set<AccountDefDTO> accountDefDTOSetAdd = new HashSet<>();

        Set<AccountDefDTO> accountDefDTOSetDelete = new HashSet<>();
        accountDefDTOSetDelete .add(accountDefDTO4);

        accountDefDTOSetAdd.add(accountDefDTO1);
        accountDefDTOSetAdd.add(accountDefDTO2);
        accountDefDTOSetAdd.add(accountDefDTO3);

        switch (integer){
            case 1: {
                builder.blockType(Util.getBlockType(BlockType.BLOCK_SUSPECTED_FRAUD));
                break;
            }
            case 2: {
                builder.cardHolderType(Util.getCardHolderType(CardHolderType.ADDITIONAL));
                break;
            }
            case 3: {
                builder.cardsReturned(2);
                break;
            }
            case 4: {
                builder.cardStatus(Util.getCardStatus(CardStatus.TRANSFER));
                break;
            }
            case 5: {
                builder.waiverDaysActivation(30);
                break;
            }
            case 6 : {
                builder.periodicCardLimitDTOAddList(periodicCardLimitDTOList);
                break;
            }
            case 7: {
                builder.periodicCardLimitDTODeleteList(periodicCardLimitDTOListDelete);
                break;
            }
            case 8: {
                builder.accountDefDTOSetAdd(accountDefDTOSetAdd);
                break;
            }
            case 9: {
                builder.accountDefDTOSetDelete(accountDefDTOSetDelete);
                break;
            }
            case 10:{
                builder.customerNumber(UUID.randomUUID().toString().replace("-",""));
            }
        }
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
                .cardsValidityMonthReplace(35)
                .cardsValidityMonthReIssue(40)
                .cardsValidityMonthNew(44)
                .cardsActivationRequired(true)
                .limitPercents(percentMap)
                .cardsReturn(10)
                .primaryAccountType(AccountType.CREDIT)
                .billingCurrencyCode("840")
                .build();

    }

    private ProductCardGenDef createProductCardGenDef(){

        return ProductCardGenDef.builder()
                .productId(new ProductId(1,201))
                .startingCardNumber("5500000000000004")
                .numberIncrementBy(1)
                .lastGeneratedCardNumber("5500000000000045")
                .endingGeneratedCardNumber("5500000099999999")
                .build();
    }


    private AccountAccumValues createAccountAccum(String accountId){

        Map<BalanceTypes,Long> balanceTypesMap = new HashMap<>();

        balanceTypesMap.put(BalanceTypes.CURRENT_BALANCE,100000L);
        balanceTypesMap.put(BalanceTypes.CASH_BALANCE,50000L);
        balanceTypesMap.put(BalanceTypes.INTERNATIONAL_CASH,30000L);
        balanceTypesMap.put(BalanceTypes.INTERNATIONAL,70000L);
        balanceTypesMap.put(BalanceTypes.INSTALLMENT_BALANCE,80000L);
        balanceTypesMap.put(BalanceTypes.INSTALLMENT_CASH,20000L);
        balanceTypesMap.put(BalanceTypes.INTERNATIONAL_INSTALLMENT,10000L);


        AccountBalances accountBalance = AccountBalances.builder()
                .memoCr(0L)
                .memoDb(0L)
                .postedBalance(0L)
                .build();

        Map<BalanceTypes, AccountBalances> accountBalancesMap = new HashMap<>();

        accountBalancesMap.put(BalanceTypes.CURRENT_BALANCE,accountBalance);
        accountBalancesMap.put(BalanceTypes.CASH_BALANCE,accountBalance);
        accountBalancesMap.put(BalanceTypes.INTERNATIONAL_CASH,accountBalance);
        accountBalancesMap.put(BalanceTypes.INTERNATIONAL,accountBalance);
        accountBalancesMap.put(BalanceTypes.INSTALLMENT_BALANCE,accountBalance);
        accountBalancesMap.put(BalanceTypes.INSTALLMENT_CASH,accountBalance);
        accountBalancesMap.put(BalanceTypes.INTERNATIONAL_INSTALLMENT,accountBalance);



        return AccountAccumValues.builder()
                .org(1)
                .product(201)
                .accountId(accountId)
                .limitsMap(balanceTypesMap)
                .balancesMap(accountBalancesMap)
                .build();

    }

    private AccountBasic createAccountBasic(String customerNumber){

        return AccountBasic.builder()
                .org(001)
                .product(201)
                .accountId(UUID.randomUUID().toString().replace("-",""))
                .blockType(BlockType.BLOCK_DECLINE)
                .dateBlockApplied(LocalDateTime.now())
                .billingCurrencyCode("840")
                .datePreviousBLockType(LocalDateTime.of(2020,12,23,11,24,30))
                .previousBlockType(BlockType.BLOCK_SUSPECTED_FRAUD)
                .accountType(AccountType.CREDIT)
                .customerNumber(customerNumber)
                .corporateNumber(UUID.randomUUID().toString().replace("-",""))
                .previousAccountNumber(UUID.randomUUID().toString().replace("-",""))
                .dateTransfer(LocalDateTime.now())
                .build();

    }

    private CustomerDef createCustomerDef(boolean allFields){

        Map<CustomerIDType,String> customerIDMap = new HashMap<>();
        customerIDMap.put(CustomerIDType.SSN_OR_NATIONAL_ID,"12345678");
        customerIDMap.put(CustomerIDType.DRIVERS_LICENCE,"ABCDEFGHIJ");


        CustomerDef.CustomerDefBuilder builder = CustomerDef.builder()
                .customerId(UUID.randomUUID().toString().replace("-",""))
                .customerType(CustomerType.OWNER)
                .addressType(AddressType.PRIMARY)
                .customerName("Test 1")
                .addressLine1("29, Janatha Road")
                .postalCode("123456")
                .countryCode("IND")
                ;

        if(allFields){
            builder
                    .addressLine2("vyttilla, kochi")
                    .state("kerala")
                    .customerIDMap(customerIDMap)
                    .primaryEmail("testemail.com")
                    .primaryPhoneNumber("34567890");
        }
        return builder.build();
    }

    private void setupCustomer() {

        customerRepository.findAll()
                .forEach(customerDef1 -> customerRepository.delete(customerDef1));

        customerDef = createCustomerDef(true);
        customerRepository.save(customerDef);

    }

    private void setupProductTable() {

        productDefRepository.findAll()
                .forEach(productDef -> productDefRepository.delete(productDef));

        productCardGenRepository.findAll()
                .forEach(productCardGenDef -> productCardGenRepository.delete(productCardGenDef));


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

        ProductCardGenDef productCardGenDef = createProductCardGenDef();
        productCardGenDef.getProductId().setOrg(1);
        productCardGenDef.getProductId().setProduct(201);
        productCardGenRepository.save(productCardGenDef);

        ProductCardGenDef productCardGenDef1 = createProductCardGenDef();
        productCardGenDef1.getProductId().setOrg(001);
        productCardGenDef1.getProductId().setProduct(202);
        productCardGenRepository.save(productCardGenDef1);

        ProductCardGenDef productCardGenDef2 = createProductCardGenDef();
        productCardGenDef2.getProductId().setOrg(001);
        productCardGenDef2.getProductId().setProduct(203);
        productCardGenRepository.save(productCardGenDef2);

    }

    private void setupAccounts() {


        accountBasicRepository.findAll()
                .forEach(accountBasic -> accountBasicRepository.delete(accountBasic));

        AccountBasic accountBasic = createAccountBasic(customerDef.getCustomerId());
        accountBasic.setAccountType(AccountType.UNIVERSAL);
        accountBasicRepository.save(accountBasic);

        accountBasic = createAccountBasic(customerDef.getCustomerId());
        accountBasic.setAccountType(AccountType.CREDIT);
        accountBasicRepository.save(accountBasic);

        accountBasic = createAccountBasic(customerDef.getCustomerId());
        accountBasic.setAccountType(AccountType.SAVINGS);
        this.accountBasic = accountBasicRepository.save(accountBasic);



        accountDefSet = StreamSupport.stream(accountBasicRepository.findAll().spliterator(),false)
                .map(accountBasic1 -> AccountDef.builder()
                        .accountNumber(accountBasic1.getAccountId())
                        .billingCurrencyCode(accountBasic1.getBillingCurrencyCode())
                        .accountType(accountBasic1.getAccountType())
                        .build()
                ).collect(Collectors.toSet());


    }


}