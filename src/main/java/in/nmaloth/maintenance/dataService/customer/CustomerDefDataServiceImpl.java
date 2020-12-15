package in.nmaloth.maintenance.dataService.customer;

import in.nmaloth.entity.customer.CustomerDef;
import in.nmaloth.maintenance.repository.customer.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class CustomerDefDataServiceImpl implements CustomerDefDataService {

    private final CustomerRepository customerRepository;

    public CustomerDefDataServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }


    @Override
    public Mono<CustomerDef> saveCustomerDef(CustomerDef customerDef) {

        CompletableFuture<CustomerDef> completableFuture = CompletableFuture
                .supplyAsync(() -> customerRepository.save(customerDef));
        return Mono.fromFuture(completableFuture);
    }

    @Override
    public Mono<Optional<CustomerDef>> findCustomerDefById(String customerNumber) {

        CompletableFuture<Optional<CustomerDef>> completableFuture = CompletableFuture
                .supplyAsync(() -> customerRepository.findById(customerNumber));

        return Mono.fromFuture(completableFuture);
    }

    @Override
    public Mono<Optional<CustomerDef>> deleteCustomerDef(String customerNumber) {
        return findCustomerDefById(customerNumber)
                .doOnNext(customerDefOptional -> {
                    if(customerDefOptional.isPresent()){
                        customerRepository.delete(customerDefOptional.get());
                    }
                } )
                ;
    }
}
