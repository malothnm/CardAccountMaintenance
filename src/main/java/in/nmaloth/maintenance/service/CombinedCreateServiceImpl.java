package in.nmaloth.maintenance.service;

import in.nmaloth.entity.account.AccountAccumValues;
import in.nmaloth.entity.account.AccountBasic;
import in.nmaloth.entity.card.CardsBasic;
import in.nmaloth.entity.card.LimitType;
import in.nmaloth.entity.card.PeriodicCardAmount;
import in.nmaloth.entity.card.PeriodicType;
import in.nmaloth.entity.customer.CustomerDef;
import in.nmaloth.entity.instrument.InstrumentType;
import in.nmaloth.maintenance.exception.AlreadyPresentException;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.combined.AccountsCombined;
import in.nmaloth.maintenance.model.combined.AccountsCombinedDTO;
import in.nmaloth.maintenance.model.combined.CardsCombined;
import in.nmaloth.maintenance.model.combined.CardsCombinedDTO;
import in.nmaloth.maintenance.model.dto.account.AccountBasicAddDTO;
import in.nmaloth.maintenance.model.dto.account.AccountDefDTO;
import in.nmaloth.maintenance.model.dto.card.CardBasicAddDTO;
import in.nmaloth.maintenance.model.dto.card.PeriodicLimitSet;
import in.nmaloth.maintenance.model.dto.customer.CustomerAddDTO;
import in.nmaloth.maintenance.model.dto.customer.CustomerDTO;
import in.nmaloth.maintenance.model.dto.instrument.InstrumentAddDTO;
import in.nmaloth.maintenance.model.dto.instrument.InstrumentDto;
import in.nmaloth.maintenance.service.account.AccountAccumValuesService;
import in.nmaloth.maintenance.service.account.AccountBasicService;
import in.nmaloth.maintenance.service.cards.CardAccumValuesService;
import in.nmaloth.maintenance.service.cards.CardsBasicService;
import in.nmaloth.maintenance.service.customer.CustomerService;
import in.nmaloth.maintenance.service.instrument.InstrumentService;
import in.nmaloth.maintenance.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CombinedCreateServiceImpl implements CombinedCreateService {

    private final NumberService numberService;
    private final CustomerService customerService;
    private final AccountBasicService accountBasicService;
    private final AccountAccumValuesService accountAccumValuesService;
    private final CardsBasicService cardsBasicService;
    private final CardAccumValuesService cardAccumValuesService;
    private final InstrumentService instrumentService;



    public CombinedCreateServiceImpl(NumberService numberService,
                                     CustomerService customerService,
                                     AccountBasicService accountBasicService,
                                     AccountAccumValuesService accountAccumValuesService,
                                     CardsBasicService cardsBasicService,
                                     CardAccumValuesService cardAccumValuesService,
                                     InstrumentService instrumentService) {

        this.numberService = numberService;
        this.customerService = customerService;
        this.accountBasicService = accountBasicService;
        this.accountAccumValuesService = accountAccumValuesService;
        this.cardsBasicService = cardsBasicService;
        this.cardAccumValuesService = cardAccumValuesService;
        this.instrumentService = instrumentService;
    }


    @Override
    public Mono<CustomerDTO> createNewCustomer(CustomerAddDTO customerAddDTO) {
        return getCustomerNumberMono(customerAddDTO)
                .flatMap(customerAddDTO1 -> customerService.createNewCustomerRecord(customerAddDTO1))
                .map(customerDef -> customerService.convertToDTO(customerDef))
                ;
    }

    @Override
    public Mono<AccountsCombinedDTO> createNewAccount(AccountBasicAddDTO accountBasicAddDTO) {

        return getAccountNumberMono(accountBasicAddDTO).zipWith(getCustomerOptionalMono(accountBasicAddDTO.getCustomerNumber()))
                .map(tuple2 -> tuple2.getT1())
                .flatMap(accountBasicAddDTO1 -> accountBasicService.createNewAccountBasic(accountBasicAddDTO1))
                .map(accountBasic -> createCombinedAccounts(accountBasic))
                .flatMap(accountsCombined -> accountAccumValuesService.saveAccountAccumValues(accountsCombined.getAccountAccumValues())
                        .map(accountAccumValues -> createCombinedAccountsDTQ(accountsCombined.getAccountBasic(), accountAccumValues))
                );
    }

    @Override
    public Mono<CardsCombinedDTO> createNewCard(CardBasicAddDTO cardBasicAddDTO) {

        Mono<String> reducedMono = combineAccountAndCustomerMono(cardBasicAddDTO.getCustomerNumber(), cardBasicAddDTO.getAccountDefDTOSet());

        return reducedMono.zipWith(getCardNumberNumberMono(cardBasicAddDTO))
                .map(tuple2 -> tuple2.getT2())
                .flatMap(cardBasicAddDTO1 -> {

                     log.debug(" Test {}", cardBasicAddDTO1.getCustomerNumber() );
                    return cardsBasicService.createNewCardsRecord(cardBasicAddDTO1);

                } )
                .map(cardsBasic -> createCardsCombine(cardsBasic))
                .flatMap(cardsCombined -> {

                            log.debug("Test");
                            return cardAccumValuesService.saveAccountAccumValues(cardsCombined.getCardAccumulatedValues())
                                .map(cardAccumulatedValues -> cardsCombined)
                            ;

                        }
                       )
                .map(cardsCombined -> convertCardsCombinedToDTO(cardsCombined));

    }

    @Override
    public Mono<InstrumentDto> createNewInstrument(InstrumentAddDTO instrumentAddDTO) {

        return generateInstrumentNumber(instrumentAddDTO)
                .flatMap(instrumentAddDTO1 -> instrumentService.createNewInstrument(instrumentAddDTO1))
                .map(instrument -> instrumentService.createDTOFromInstrument(instrument));


    }

    private Mono<InstrumentAddDTO> generateInstrumentNumber(InstrumentAddDTO instrumentAddDTO){

        if(instrumentAddDTO.getInstrumentNumber() == null){
            return generateInstrumentNumber(Util.getInstrumentType(instrumentAddDTO.getInstrumentType()),
                    instrumentAddDTO.getOrg(),instrumentAddDTO.getProduct())
                    .map(instrumentNumber ->  {
                        instrumentAddDTO.setInstrumentNumber(instrumentNumber);
                        return instrumentAddDTO;
                    });
        }

        return Mono.just(instrumentAddDTO);
    }

    private Mono<String> generateInstrumentNumber(InstrumentType instrumentType,int org, int product){

        switch (instrumentType){
            case CARD_LESS:
                return Mono.just(UUID.randomUUID().toString().replace("-",""));
            default:
                return numberService.generateNewCardNumber(org,product);
        }

    }

    private CardsCombinedDTO convertCardsCombinedToDTO(CardsCombined cardsCombined) {

        return CardsCombinedDTO.builder()
                .cardAccumValuesDTO(cardAccumValuesService.convertToDTO(cardsCombined.getCardAccumulatedValues()))
                .cardBasicDTO(cardsBasicService.convertToDTO(cardsCombined.getCardsBasic()))
                .build();
    }

    private CardsCombined createCardsCombine(CardsBasic cardsBasic) {


        List<PeriodicLimitSet> periodicLimitDTOList = cardsBasic.getPeriodicTypePeriodicCardLimitMap().entrySet()
                .stream()
                .map(periodicTypeMapEntry -> buildPeriodicLimitSet(periodicTypeMapEntry))
                .collect(Collectors.toList());

        Set<PeriodicLimitSet> periodicLimitDTOSet = new HashSet<>(periodicLimitDTOList);

        return CardsCombined.builder()
                .cardsBasic(cardsBasic)
                .cardAccumulatedValues(cardAccumValuesService.initializeAccumValues(cardsBasic.getCardNumber(),
                        periodicLimitDTOSet,
                        cardsBasic.getOrg(),
                        cardsBasic.getProduct())).build();

    }

    private PeriodicLimitSet buildPeriodicLimitSet(Map.Entry<PeriodicType, Map<LimitType, PeriodicCardAmount>> periodicTypeMapEntry) {

        return PeriodicLimitSet.builder()
                .periodicType(periodicTypeMapEntry.getKey())
                .limitTypeSet(periodicTypeMapEntry.getValue().keySet())
                .build();
    }

    private Mono<String> combineAccountAndCustomerMono(String customerNumber, Set<AccountDefDTO> accountDefDTOSet) {

        if (accountDefDTOSet.size() < 1) {
            throw new NotFoundException(" No Valid Account Present ");
        }

        Mono<String> customerNumberMono = getCustomerOptionalMono(customerNumber)
                .map(customerDefOptional -> customerDefOptional.get().getCustomerNumber());

        return customerNumberMono.concatWith(createAccountNumberFlux(accountDefDTOSet))
                .reduce((s, s2) -> s2);


    }


    private Flux<String> createAccountNumberFlux(Set<AccountDefDTO> accountDefDTOSet) {

        List<String> accountList = accountDefDTOSet.stream()
                .map(accountDefDTO -> accountDefDTO.getAccountNumber())
                .collect(Collectors.toList());

        return Flux.fromIterable(accountList)
                .flatMap(accountNumber -> getAccountBasicMono(accountNumber))
                .map(accountBasicOptional -> accountBasicOptional.get().getAccountNumber());
    }


    private Mono<CustomerAddDTO> getCustomerNumberMono(CustomerAddDTO customerAddDTO) {

        if (customerAddDTO.getCustomerNumber() == null) {
            return numberService.generateNewCustomerNumber()
                    .map(customerNumber -> {
                        customerAddDTO.setCustomerNumber(customerNumber);
                        return customerAddDTO;
                    })
                    ;
        }
        return customerService.fetchCustomerInfoOptional(customerAddDTO.getCustomerNumber())
                .doOnNext(customerDefOptional -> {
                    if (customerDefOptional.isPresent()) {
                        throw new AlreadyPresentException("Customer id already Present " + customerAddDTO.getCustomerNumber());
                    }
                })
                .map(customerDefOptional -> customerAddDTO);
    }


    private Mono<AccountBasicAddDTO> getAccountNumberMono(AccountBasicAddDTO accountBasicAddDTO) {

        if (accountBasicAddDTO.getAccountNumber() == null) {
            return numberService.generateNewAccountNumber()
                    .map(accountNumber -> {
                        accountBasicAddDTO.setAccountNumber(accountNumber);
                        return accountBasicAddDTO;
                    })
                    ;
        }
        return accountBasicService.fetchAccountBasicInfoOptional(accountBasicAddDTO.getAccountNumber())
                .doOnNext(accountBasicOptional -> {
                    if (accountBasicOptional.isPresent()) {
                        throw new AlreadyPresentException("Account Number already Present. Cannot Add " + accountBasicAddDTO.getAccountNumber());
                    }
                })
                .map(accountBasicOptional -> accountBasicAddDTO);
    }

    private Mono<Optional<CustomerDef>> getCustomerOptionalMono(String customerNumber) {

        return customerService.fetchCustomerInfoOptional(customerNumber)
                .doOnNext(customerDefOptional -> {
                    if (customerDefOptional.isEmpty()) {
                        throw new NotFoundException(" Customer ID not Found");
                    }
                });

    }

    private Mono<Optional<AccountBasic>> getAccountBasicMono(String accountNumber) {

        return accountBasicService.fetchAccountBasicInfoOptional(accountNumber)
                .doOnNext(accountBasicOptional -> {
                    if (accountBasicOptional.isEmpty()) {
                        throw new NotFoundException(" Account Number Not Found" + accountNumber);
                    }
                });
    }

    private AccountsCombined createCombinedAccounts(AccountBasic accountBasic) {

        return AccountsCombined.builder()
                .accountBasic(accountBasic)
                .accountAccumValues(accountAccumValuesService.initializeAccumValues(accountBasic.getAccountNumber(),
                        accountBasic.getLimitsMap().keySet()))
                .build();
    }

    private AccountsCombinedDTO createCombinedAccountsDTQ(AccountBasic accountBasic, AccountAccumValues accountAccumValues) {

        return AccountsCombinedDTO.builder()
                .accountBasicDTO(accountBasicService.convertToDTO(accountBasic))
                .accountAccumValuesDTO(accountAccumValuesService.convertToDTO(accountAccumValues))
                .build();

    }

    private Mono<CardBasicAddDTO> getCardNumberNumberMono(CardBasicAddDTO cardBasicAddDTO) {

        if (cardBasicAddDTO.getCardNumber() == null) {
            return numberService.generateNewCardNumber(cardBasicAddDTO.getOrg(), cardBasicAddDTO.getProduct())
                    .map(cardNumber -> {
                        cardBasicAddDTO.setCardNumber(cardNumber);
                        return cardBasicAddDTO;
                    })
                    ;
        }
        return cardsBasicService.fetchCardInfOptional(cardBasicAddDTO.getCardNumber())
                .doOnNext(cardsBasicOptional -> {
                    if (cardsBasicOptional.isPresent()) {
                        throw new AlreadyPresentException("Card Number already Present ");
                    }
                })
                .map(cardsBasicOptional -> cardBasicAddDTO);
    }
}
