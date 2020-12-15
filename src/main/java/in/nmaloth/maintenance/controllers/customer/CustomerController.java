package in.nmaloth.maintenance.controllers.customer;

import in.nmaloth.maintenance.controllers.EndPoints;
import in.nmaloth.maintenance.model.dto.customer.CustomerAddDTO;
import in.nmaloth.maintenance.model.dto.customer.CustomerDTO;
import in.nmaloth.maintenance.model.dto.customer.CustomerUpdateDTO;
import in.nmaloth.maintenance.service.CombinedCreateService;
import in.nmaloth.maintenance.service.NumberService;
import in.nmaloth.maintenance.service.customer.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
public class CustomerController {

    private final CustomerService customerService;
    private final CombinedCreateService combinedCreateService;


    public CustomerController(CustomerService customerService, CombinedCreateService combinedCreateService) {
        this.customerService = customerService;
        this.combinedCreateService = combinedCreateService;
    }

    @PostMapping(EndPoints.CUSTOMER)
    public Mono<ResponseEntity<CustomerDTO>> createNewCustomer(@Valid @RequestBody CustomerAddDTO customerAddDTO){

        return combinedCreateService.createNewCustomer(customerAddDTO)
                .map(customerDTO -> ResponseEntity.status(HttpStatus.CREATED).body(customerDTO))

        ;

    }

    @GetMapping(EndPoints.CUSTOMER_ID)
    public Mono<CustomerDTO>  findCustomerInfo(@PathVariable String customerId){
        return customerService.fetchCustomerInfo(customerId)
                .map(customerDef -> customerService.convertToDTO(customerDef));
    }


    @DeleteMapping(EndPoints.CUSTOMER_ID)
    public Mono<CustomerDTO>  deleteCustomerInfo(@PathVariable String customerId){
        return customerService.deleteCustomerInfo(customerId)
                .map(customerDef -> customerService.convertToDTO(customerDef));
    }

    @PutMapping(EndPoints.CUSTOMER)
    public Mono<CustomerDTO>  updateCustomerInfo(@Valid @RequestBody CustomerUpdateDTO customerUpdateDTO){
        return customerService.updateCustomerRecord(customerUpdateDTO)
                .map(customerDef -> customerService.convertToDTO(customerDef));
    }



}
