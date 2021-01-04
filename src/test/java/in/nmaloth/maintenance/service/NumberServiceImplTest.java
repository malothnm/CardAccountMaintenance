package in.nmaloth.maintenance.service;

import in.nmaloth.entity.instrument.InstrumentType;
import in.nmaloth.entity.product.ProductCardGenDef;
import in.nmaloth.entity.product.ProductId;
import in.nmaloth.maintenance.exception.NumberCreationException;
import in.nmaloth.maintenance.repository.product.ProductCardGenRepository;
import in.nmaloth.maintenance.service.product.ProductCardGenService;
import in.nmaloth.maintenance.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NumberServiceImplTest {

    @Autowired
    private ProductCardGenRepository productCardGenRepository;

    @Autowired
    private ProductCardGenService productCardGenService;

    @Autowired
    private NumberService numberService;


    @BeforeEach
    void setup(){

        productCardGenRepository.findAll()
                .forEach(productCardGenDef -> productCardGenRepository.delete(productCardGenDef));

    }

    @Test
    void generateNewCustomerNumber() {
        StepVerifier.create(numberService.generateNewCustomerId())
            .expectNextCount(1)
            .verifyComplete()
        ;
    }

    @Test
    void generateNewAccountNumber() {

        StepVerifier.create(numberService.generateNewAccountId())
                .expectNextCount(1)
                .verifyComplete()
        ;
    }

    @Test
    void generateNewCardNumber() {


        StepVerifier.create(numberService.generateNewAccountId())
                .expectNextCount(1)
                .verifyComplete();

    }




    @Test
    void generateInstrumentNumber() {

        ProductCardGenDef productCardGenDef = createProductCardGenDef();
        productCardGenRepository.save(productCardGenDef);

        String lastCardNumber = productCardGenDef.getLastGeneratedCardNumber();
        String nextCardNumber = Util.generateNextCardNumber(productCardGenDef.getLastGeneratedCardNumber(),1);
        Mono<String> cardNumberMono = numberService
                .generateInstrumentNumber(InstrumentType.PLASTIC_DEBIT,productCardGenDef.getProductId().getOrg(),productCardGenDef.getProductId().getProduct());

        StepVerifier
                .create(cardNumberMono)
                .consumeNextWith(s -> {

                    ProductCardGenDef productCardGenDef1 = productCardGenRepository.findById(productCardGenDef.getProductId()).get();

                    assertAll(
                            ()-> assertEquals(nextCardNumber,s),
                            ()-> assertEquals(nextCardNumber,productCardGenDef1.getLastGeneratedCardNumber())

                    );
                })
                .verifyComplete();
    }

    @Test
    void generateInstrumentNumber1() {

        ProductCardGenDef productCardGenDef = createProductCardGenDef();
        productCardGenRepository.save(productCardGenDef);

        String lastCardNumber = productCardGenDef.getLastGeneratedCardNumber();
        String nextCardNumber = Util.generateNextCardNumber(productCardGenDef.getLastGeneratedCardNumber(),1);
        Mono<String> cardNumberMono = numberService
                .generateInstrumentNumber(InstrumentType.CARD_LESS,productCardGenDef.getProductId().getOrg(),productCardGenDef.getProductId().getProduct());

        StepVerifier
                .create(cardNumberMono)
                .consumeNextWith(s -> {

                    ProductCardGenDef productCardGenDef1 = productCardGenRepository.findById(productCardGenDef.getProductId()).get();

                    assertAll(
                            ()-> assertNotEquals(nextCardNumber,s),
                            ()-> assertEquals(lastCardNumber,productCardGenDef1.getLastGeneratedCardNumber())

                    );
                })
                .verifyComplete();
    }

    private ProductCardGenDef createProductCardGenDef(){

        return ProductCardGenDef.builder()
                .productId(new ProductId(1,201))
                .startingCardNumber("5500000000000004")
                .numberIncrementBy(1)
                .lastGeneratedCardNumber("5500000000000005")
                .endingGeneratedCardNumber("5500000099999999")
                .build();
    }
}