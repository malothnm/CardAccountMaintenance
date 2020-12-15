package in.nmaloth.maintenance.service;

import in.nmaloth.entity.instrument.InstrumentType;
import in.nmaloth.entity.product.ProductCardGenDef;
import in.nmaloth.maintenance.exception.NumberCreationException;
import in.nmaloth.maintenance.model.dto.product.ProductCardGenUpdateDTO;
import in.nmaloth.maintenance.service.product.ProductCardGenService;
import in.nmaloth.maintenance.util.Util;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class NumberServiceImpl implements NumberService {

    private final ProductCardGenService productCardGenService;

    public NumberServiceImpl(ProductCardGenService productCardGenService) {
        this.productCardGenService = productCardGenService;
    }


    @Override
    public Mono<String> generateNewCustomerNumber() {
        return Mono.just(UUID.randomUUID().toString().replace("-",""));
    }

    @Override
    public Mono<String> generateNewAccountNumber() {

        return Mono.just(UUID.randomUUID().toString().replace("-",""));
    }

    @Override
    public Mono<String> generateNewCardNumber(Integer org, Integer product) {
        return productCardGenService.fetchProductCardGenInfo(org,product)
                .flatMap(productCardGenDef -> generateNextCardNumber(productCardGenDef))
                ;
    }

    @Override
    public Mono<String> generateInstrumentNumber(InstrumentType instrumentType,Integer org, Integer product) {


        switch (instrumentType){

            case ONE_TIME_USE_CARD:
            case CARD_LESS:{
                return Mono.just(UUID.randomUUID().toString().replace("-",""));
            }
            default: {
                return generateNewCardNumber(org,product);
            }
        }
    }

    private Mono<? extends String> generateNextCardNumber(ProductCardGenDef productCardGenDef) {

        String lastCardNumber = productCardGenDef.getLastGeneratedCardNumber();


        if(productCardGenDef.getStartingCardNumber().compareTo(lastCardNumber) > 0){
            lastCardNumber = productCardGenDef.getStartingCardNumber();
        }
        if(lastCardNumber.compareTo(productCardGenDef.getEndingGeneratedCardNumber()) > 0 ){
            throw  new NumberCreationException("Unable to create a new Number. Please update Ending Numbers");
        }

        String nextCardNumber = Util.generateNextCardNumber(lastCardNumber,productCardGenDef.getNumberIncrementBy());
        ProductCardGenUpdateDTO productCardGenUpdateDTO =  ProductCardGenUpdateDTO.builder()
                .org(productCardGenDef.getProductId().getOrg())
                .product(productCardGenDef.getProductId().getProduct())
                .lastGeneratedCardNumber(nextCardNumber)
                .build();

        return productCardGenService.updateProductCardGenDef(productCardGenUpdateDTO)
                .map(productCardGenDef1 -> nextCardNumber);

    }



}
