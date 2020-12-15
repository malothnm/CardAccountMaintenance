package in.nmaloth.maintenance.dataService.customer;

import in.nmaloth.entity.card.CardsBasic;
import in.nmaloth.entity.customer.CustomerDef;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface CustomerDefDataService {

    Mono<CustomerDef> saveCustomerDef(CustomerDef customerDef);
    Mono<Optional<CustomerDef>> findCustomerDefById(String customerNumber);
    Mono<Optional<CustomerDef>> deleteCustomerDef(String customerNumber);
}
