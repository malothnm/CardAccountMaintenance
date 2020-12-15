package in.nmaloth.maintenance.service;

import in.nmaloth.maintenance.model.combined.AccountsCombinedDTO;
import in.nmaloth.maintenance.model.combined.CardsCombinedDTO;
import in.nmaloth.maintenance.model.dto.account.AccountBasicAddDTO;
import in.nmaloth.maintenance.model.dto.card.CardBasicAddDTO;
import in.nmaloth.maintenance.model.dto.customer.CustomerAddDTO;
import in.nmaloth.maintenance.model.dto.customer.CustomerDTO;
import in.nmaloth.maintenance.model.dto.instrument.InstrumentAddDTO;
import in.nmaloth.maintenance.model.dto.instrument.InstrumentDto;
import reactor.core.publisher.Mono;

import javax.sound.midi.Instrument;

public interface CombinedCreateService {

    Mono<CustomerDTO> createNewCustomer(CustomerAddDTO customerAddDTO);
    Mono<AccountsCombinedDTO> createNewAccount(AccountBasicAddDTO accountBasicAddDTO);
    Mono<CardsCombinedDTO> createNewCard(CardBasicAddDTO cardBasicAddDTO);
    Mono<InstrumentDto> createNewInstrument(InstrumentAddDTO instrumentAddDTO);


}
