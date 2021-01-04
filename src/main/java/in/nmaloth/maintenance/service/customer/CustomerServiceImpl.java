package in.nmaloth.maintenance.service.customer;

import in.nmaloth.entity.customer.CustomerDef;
import in.nmaloth.entity.customer.CustomerIDType;
import in.nmaloth.maintenance.dataService.customer.CustomerDefDataService;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.customer.CustomerAddDTO;
import in.nmaloth.maintenance.model.dto.customer.CustomerDTO;
import in.nmaloth.maintenance.model.dto.customer.CustomerIDDTO;
import in.nmaloth.maintenance.model.dto.customer.CustomerUpdateDTO;
import in.nmaloth.maintenance.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerDefDataService customerDefDataService;

    public CustomerServiceImpl(CustomerDefDataService customerDefDataService) {
        this.customerDefDataService = customerDefDataService;
    }


    @Override
    public Mono<CustomerDef> createNewCustomerRecord(CustomerAddDTO customerAddDTO) {
        return customerDefDataService.saveCustomerDef(convertDTOToCustomerDef(customerAddDTO));
    }

    @Override
    public Mono<CustomerDef> updateCustomerRecord(CustomerUpdateDTO customerUpdateDTO) {
        return customerDefDataService.findCustomerDefById(customerUpdateDTO.getCustomerId())
                .map(customerDefOptional -> {
                    if(customerDefOptional.isPresent()){
                        return customerDefOptional.get();
                    }
                    throw  new NotFoundException("Invalid Customer Number "+ customerUpdateDTO.getCustomerName());
                })
                .map(customerDef -> updateCustomerFromDTO(customerUpdateDTO,customerDef))
                .flatMap(customerDef -> customerDefDataService.saveCustomerDef(customerDef))

                ;
    }

    @Override
    public Mono<CustomerDef> fetchCustomerInfo(String customerId) {
        return customerDefDataService.findCustomerDefById(customerId)
                .map(customerDefOptional -> {
                    if(customerDefOptional.isPresent()){
                        return customerDefOptional.get();
                    }
                    throw new NotFoundException("Invalid Customer Number " + customerId);
                })
                ;
    }

    @Override
    public Mono<Optional<CustomerDef>> fetchCustomerInfoOptional(String customerId) {
        return customerDefDataService.findCustomerDefById(customerId);
    }

    @Override
    public Mono<CustomerDef> deleteCustomerInfo(String customerId) {
        return customerDefDataService.deleteCustomerDef(customerId)
                .map(customerDefOptional -> {
                    if(customerDefOptional.isPresent()){
                        return customerDefOptional.get();
                    }
                    throw new NotFoundException("Invalid Customer Number " + customerId);
                })
                ;
    }

    @Override
    public CustomerDTO convertToDTO(CustomerDef customerDef) {

        CustomerDTO.CustomerDTOBuilder builder = CustomerDTO.builder()
                .customerId(customerDef.getCustomerId())
                .addressType(Util.getAddressType(customerDef.getAddressType()))
                .customerType(Util.getCustomerType(customerDef.getCustomerType()))
                .customerName(customerDef.getCustomerName())
                .addressLine1(customerDef.getAddressLine1())
                .postalCode(customerDef.getPostalCode())
                .countryCode(customerDef.getCountryCode());

        if(customerDef.getAddressLine2() != null){
            builder.addressLine2(customerDef.getAddressLine2());
        }
        if(customerDef.getState() != null){
            builder.state(customerDef.getState());
        }
        if(customerDef.getCustomerIDMap() != null){
            List<CustomerIDDTO> customerIDDTOList = customerDef.getCustomerIDMap().entrySet()
                    .stream()
                    .map(customerIDTypeEntry -> CustomerIDDTO.builder()
                            .customerId(customerIDTypeEntry.getValue())
                            .customerIdType(Util.getCustomerIDType(customerIDTypeEntry.getKey()))
                            .build()
                    )
                    .collect(Collectors.toList());
            builder.customerIDDTOList(customerIDDTOList);
        }
        if(customerDef.getPrimaryPhoneNumber() != null){
            builder.primaryPhoneNumber(customerDef.getPrimaryPhoneNumber());
        }
        if(customerDef.getPrimaryEmail() != null){
            builder.primaryEmail(customerDef.getPrimaryEmail());
        }

        return builder.build();
    }

    @Override
    public CustomerDef convertDTOToCustomerDef(CustomerAddDTO customerAddDTO) {

        CustomerDef.CustomerDefBuilder builder = CustomerDef.builder()
                .customerId(customerAddDTO.getCustomerId())
                .addressType(Util.getAddressType(customerAddDTO.getAddressType()))
                .customerType(Util.getCustomerType(customerAddDTO.getCustomerType()))
                .customerName(customerAddDTO.getCustomerName())
                .addressLine1(customerAddDTO.getAddressLine1())
                .postalCode(customerAddDTO.getPostalCode())
                .countryCode(customerAddDTO.getCountryCode())
                ;

        if(customerAddDTO.getAddressLine2() != null){
            builder.addressLine2(customerAddDTO.getAddressLine2());
        }

        if(customerAddDTO.getState() != null){
            builder.state(customerAddDTO.getState());
        }
        if(customerAddDTO.getCustomerIDDTOList() != null){
            Map<CustomerIDType,String> customerIDMap = new HashMap<>();

            customerAddDTO.getCustomerIDDTOList()
                    .forEach(customerIDDTO -> customerIDMap
                            .put(Util.getCustomerIDType(customerIDDTO.getCustomerIdType()),customerIDDTO.getCustomerId()));
            builder.customerIDMap(customerIDMap);

        }

        if(customerAddDTO.getPrimaryPhoneNumber() != null){
            builder.primaryPhoneNumber(customerAddDTO.getPrimaryPhoneNumber());
        }

        if(customerAddDTO.getPrimaryEmail() != null){
            builder.primaryEmail(customerAddDTO.getPrimaryEmail());
        }

        return builder.build();
    }

    @Override
    public CustomerDef updateCustomerFromDTO(CustomerUpdateDTO customerUpdateDTO, CustomerDef customerDef) {

        if(customerUpdateDTO.getAddressType() != null){
            customerDef.setAddressType(Util.getAddressType(customerUpdateDTO.getAddressType()));
        }
        if(customerUpdateDTO.getCustomerType() != null){
            customerDef.setCustomerType(Util.getCustomerType(customerUpdateDTO.getCustomerType()));
        }
        if(customerUpdateDTO.getCustomerName() != null){
            customerDef.setCustomerName(customerUpdateDTO.getCustomerName());
        }
        if(customerUpdateDTO.getAddressLine1() != null){
            customerDef.setAddressLine1(customerUpdateDTO.getAddressLine1());
        }

        if(customerUpdateDTO.getAddressLine2() != null){
            customerDef.setAddressLine2(customerUpdateDTO.getAddressLine2());
        }

        if(customerUpdateDTO.getPostalCode() != null){
            customerDef.setPostalCode(customerUpdateDTO.getPostalCode());
        }
        if(customerUpdateDTO.getState() != null){
            customerDef.setState(customerUpdateDTO.getState());
        }
        if(customerUpdateDTO.getCountryCode() != null){
            customerDef.setCountryCode(customerUpdateDTO.getCountryCode());
        }
        if(customerUpdateDTO.getCustomerIDDTOListAdd() != null){
            if(customerDef.getCustomerIDMap() == null){
                customerDef.setCustomerIDMap(new HashMap<>());
            }

            customerUpdateDTO.getCustomerIDDTOListAdd()
                    .forEach(customerIDDTO -> customerDef.getCustomerIDMap()
                            .put(Util.getCustomerIDType(customerIDDTO.getCustomerIdType()),customerIDDTO.getCustomerId()));
        }

        if(customerUpdateDTO.getCustomerIDDTOListDelete() != null){
            if(customerDef.getCustomerIDMap() == null){
                customerDef.setCustomerIDMap(new HashMap<>());
            }

            customerUpdateDTO.getCustomerIDDTOListDelete()
                    .forEach(customerIDDTO -> customerDef.getCustomerIDMap()
                            .remove(Util.getCustomerIDType(customerIDDTO.getCustomerIdType())));
        }

        if(customerUpdateDTO.getPrimaryPhoneNumber() != null){
            customerDef.setPrimaryPhoneNumber(customerUpdateDTO.getPrimaryPhoneNumber());
        }

        if(customerUpdateDTO.getPrimaryEmail() != null){
            customerDef.setPrimaryEmail(customerUpdateDTO.getPrimaryEmail());
        }

        return customerDef;
    }
}
