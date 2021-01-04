package in.nmaloth.maintenance.service.instrument;

import in.nmaloth.entity.account.AccountDef;
import in.nmaloth.entity.card.CardsBasic;
import in.nmaloth.entity.instrument.Instrument;
import in.nmaloth.maintenance.dataService.instrument.InstrumentDataService;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.account.AccountDefDTO;
import in.nmaloth.maintenance.model.dto.instrument.InstrumentAddDTO;
import in.nmaloth.maintenance.model.dto.instrument.InstrumentDto;
import in.nmaloth.maintenance.model.dto.instrument.InstrumentUpdateDTO;
import in.nmaloth.maintenance.service.cards.CardsBasicService;
import in.nmaloth.maintenance.util.Util;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class InstrumentServiceImpl implements InstrumentService {

    private final InstrumentDataService instrumentDataService;
    private final CardsBasicService cardsBasicService;

    public InstrumentServiceImpl(InstrumentDataService instrumentDataService,
                                 CardsBasicService cardsBasicService) {
        this.instrumentDataService = instrumentDataService;
        this.cardsBasicService = cardsBasicService;
    }


    @Override
    public Mono<Instrument> createNewInstrument(InstrumentAddDTO instrumentAddDTO) {


        return cardsBasicService.fetchCardInfo(instrumentAddDTO.getCardId())
                .map(cardsBasic -> createInstrumentFromAddDTO(instrumentAddDTO,cardsBasic))
                .flatMap(instrument -> instrumentDataService.saveInstrument(instrument))

                ;
    }

    @Override
    public Mono<Instrument> updateInstrument(InstrumentUpdateDTO instrumentUpdateDTO) {

        return instrumentDataService.findInstrumentById(instrumentUpdateDTO.getInstrumentNumber())
                .map(instrumentOptional -> {

                    if(instrumentOptional.isPresent()){
                        return instrumentOptional.get();
                    } else {
                        throw  new NotFoundException("Invalid InstrumentNumber for update" + instrumentUpdateDTO.getInstrumentNumber());
                    }
                })
                .map(instrument -> createInstrumentFromUpdateDTO(instrumentUpdateDTO,instrument))
                .flatMap(instrument -> instrumentDataService.saveInstrument(instrument))

                ;
    }

    @Override
    public Mono<Instrument> fetchInstrument(String instrumentNumber) {
        return instrumentDataService.findInstrumentById(instrumentNumber)
                .map(instrumentOptional ->{
                    if(instrumentOptional.isPresent()){
                        return instrumentOptional.get();
                    } else {
                        throw  new NotFoundException("Invalid InstrumentNumber for update" + instrumentNumber);
                    }
                } )
                ;
    }

    @Override
    public Flux<Instrument> fetchAllInstrumentsForCard(String cardId) {
        return instrumentDataService.findAllInstrumentsByCardNumber(cardId)
                ;

    }



    @Override
    public Mono<Instrument> deleteInstrument(String instrumentNumber) {

        return instrumentDataService.deletePlasticById(instrumentNumber)
                .map(instrumentOptional -> {
                    if(instrumentOptional.isPresent()){
                        return instrumentOptional.get();
                    } else {
                        throw  new NotFoundException("Invalid InstrumentNumber for update" + instrumentNumber);
                    }
                })
                ;
    }

    @Override
    public Flux<Instrument> deleteAllInstrumentsForCard(String cardNumber) {

        return instrumentDataService.deleteAllPlastics(cardNumber)
                ;
    }

    @Override
    public Mono<Instrument> saveInstrument(Instrument instrument) {


        return instrumentDataService.saveInstrument(instrument);
    }

    @Override
    public Instrument createInstrumentFromAddDTO(InstrumentAddDTO instrumentAddDTO, CardsBasic cardsBasic) {

        Instrument instrument = new Instrument();

        instrument.setInstrumentNumber(instrumentAddDTO.getInstrumentNumber());
        instrument.setInstrumentType(Util.getInstrumentType(instrumentAddDTO.getInstrumentType()));
        instrument.setActive(instrumentAddDTO.getActive());
        instrument.setCardNumber(instrumentAddDTO.getCardId());
//        instrument.setAccountNumber(instrumentAddDTO.getAccountNumber());

        instrument.setAccountDefSet(cardsBasic.getAccountDefSet());

        instrument.setCustomerNumber(cardsBasic.getCustomerNumber());
        if(cardsBasic.getCorporateNumber() != null){
            instrument.setCorporateNumber(cardsBasic.getCorporateNumber());
        }
        if (instrumentAddDTO.getBlockType() != null){
            instrument.setBlockType(Util.getBlockType(instrumentAddDTO.getBlockType()));
        }

        if(instrumentAddDTO.getExpiryDate() != null){
            instrument.setExpiryDate(LocalDate.parse(instrumentAddDTO.getExpiryDate(), DateTimeFormatter.BASIC_ISO_DATE));
        }
        instrument.setOrg(instrumentAddDTO.getOrg());
        instrument.setProduct(instrumentAddDTO.getProduct());
        return instrument;
    }

    @Override
    public Instrument createInstrumentFromUpdateDTO(InstrumentUpdateDTO instrumentUpdateDTO,Instrument instrument) {


        if(instrumentUpdateDTO.getInstrumentType() != null){
            instrument.setInstrumentType(Util.getInstrumentType(instrumentUpdateDTO.getInstrumentType()));

        }
        if(instrumentUpdateDTO.getActive() != null){
            instrument.setActive(instrumentUpdateDTO.getActive());
        }
        if(instrumentUpdateDTO.getCardId() != null){
            instrument.setCardNumber(instrumentUpdateDTO.getCardId());
        }
//        if(instrumentUpdateDTO.getAccountNumber() != null){
//            instrument.setAccountNumber(instrumentUpdateDTO.getAccountNumber());
//        }



        if (instrumentUpdateDTO.getBlockType() != null){
            instrument.setBlockType(Util.getBlockType(instrumentUpdateDTO.getBlockType()));
        }

        if(instrumentUpdateDTO.getExpiryDate() != null){
            instrument.setExpiryDate(LocalDate.parse(instrumentUpdateDTO.getExpiryDate(), DateTimeFormatter.BASIC_ISO_DATE));
        }
        if(instrumentUpdateDTO.getOrg() != null){
            instrument.setOrg(instrumentUpdateDTO.getOrg());

        }
        if(instrumentUpdateDTO.getProduct() != null){
            instrument.setProduct(instrumentUpdateDTO.getProduct());
        }
        return instrument;
    }

    @Override
    public Instrument updateAccounts(Instrument instrument, CardsBasic cardsBasic) {

        instrument.setAccountDefSet(cardsBasic.getAccountDefSet());
        instrument.setCustomerNumber(cardsBasic.getCustomerNumber());

        if(cardsBasic.getCorporateNumber() != null){
            instrument.setCorporateNumber(cardsBasic.getCorporateNumber());
        }
        return instrument;
    }

    private void deleteAccountDef(Set<AccountDefDTO> accountDefDTOSetDelete, Instrument instrument) {

        accountDefDTOSetDelete.stream()
                .map(accountDefDTO -> AccountDef.builder()
                        .accountType(Util.getAccountType(accountDefDTO.getAccountType()))
                        .billingCurrencyCode(accountDefDTO.getBillingCurrencyCode())
                        .accountNumber(accountDefDTO.getAccountId())
                        .build()
                )
                .forEach(accountDef -> instrument.getAccountDefSet().remove(accountDef));
    }


    @Override
    public InstrumentDto createDTOFromInstrument(Instrument instrument) {

        InstrumentDto.InstrumentDtoBuilder instrumentDtoBuilder = InstrumentDto.builder()
                .instrumentNumber(instrument.getInstrumentNumber())
//                .accountNumber(instrument.getAccountNumber())
                .cardId(instrument.getCardNumber())
                .customerId(instrument.getCustomerNumber())
                .instrumentType(Util.getInstrumentType(instrument.getInstrumentType()))
                .org(instrument.getOrg())
                .product(instrument.getProduct())
                .active(instrument.isActive())
                .accountDefDTOSet(instrument.getAccountDefSet().stream()
                        .map(accountDef -> AccountDefDTO.builder()
                                .accountId(accountDef.getAccountNumber())
                                .billingCurrencyCode(accountDef.getBillingCurrencyCode())
                                .accountType(Util.getAccountType(accountDef.getAccountType()))
                                .build()
                        )
                        .collect(Collectors.toSet())
                )
                ;

        if(instrument.getCorporateNumber() != null){
            instrumentDtoBuilder.corporateNumber(instrument.getCorporateNumber());
        }
        if(instrument.getExpiryDate() != null){
            instrumentDtoBuilder.expiryDate(instrument.getExpiryDate().format(DateTimeFormatter.BASIC_ISO_DATE));
        }
        if(instrument.getBlockType() != null){
            instrumentDtoBuilder.blockType(Util.getBlockType(instrument.getBlockType()));
        }
        return instrumentDtoBuilder.build();
    }
}
