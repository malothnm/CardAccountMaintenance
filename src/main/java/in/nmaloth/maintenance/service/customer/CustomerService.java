package in.nmaloth.maintenance.service.customer;

import in.nmaloth.entity.customer.CustomerDef;
import in.nmaloth.maintenance.model.dto.customer.CustomerAddDTO;
import in.nmaloth.maintenance.model.dto.customer.CustomerDTO;
import in.nmaloth.maintenance.model.dto.customer.CustomerUpdateDTO;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface CustomerService {

    Mono<CustomerDef> createNewCustomerRecord(CustomerAddDTO customerAddDTO);
    Mono<CustomerDef> updateCustomerRecord(CustomerUpdateDTO customerUpdateDTO);
    Mono<CustomerDef> fetchCustomerInfo(String customerId);
    Mono<Optional<CustomerDef>> fetchCustomerInfoOptional(String customerId);
    Mono<CustomerDef> deleteCustomerInfo(String customerId);

    CustomerDTO convertToDTO(CustomerDef customerDef);
    CustomerDef convertDTOToCustomerDef(CustomerAddDTO customerAddDTO);
    CustomerDef updateCustomerFromDTO(CustomerUpdateDTO customerUpdateDTO, CustomerDef customerDef);

}
