package in.nmaloth.maintenance.controllers.customer;

import in.nmaloth.entity.customer.AddressType;
import in.nmaloth.entity.customer.CustomerDef;
import in.nmaloth.entity.customer.CustomerIDType;
import in.nmaloth.entity.customer.CustomerType;
import in.nmaloth.maintenance.controllers.EndPoints;
import in.nmaloth.maintenance.model.dto.customer.CustomerAddDTO;
import in.nmaloth.maintenance.model.dto.customer.CustomerDTO;
import in.nmaloth.maintenance.model.dto.customer.CustomerIDDTO;
import in.nmaloth.maintenance.model.dto.customer.CustomerUpdateDTO;
import in.nmaloth.maintenance.repository.customer.CustomerRepository;
import in.nmaloth.maintenance.service.customer.CustomerService;
import in.nmaloth.maintenance.util.Util;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext
@AutoConfigureWebTestClient
class CustomerControllerTest {
    @Autowired
    private CustomerRepository customerRepository;


    @Autowired
    private WebTestClient webTestClient;


    @Test
    void createNewCustomer() {

        CustomerAddDTO customerAddDTO = createCustomerAddDTO(true);

        webTestClient.post()
                .uri(EndPoints.CUSTOMER)
                .body(Mono.just(customerAddDTO),CustomerAddDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerDTO.class)
                ;

    }

    @Test
    void createNewCustomer1() {

        CustomerAddDTO customerAddDTO = createCustomerAddDTO(true);
        customerAddDTO.setCustomerNumber(null);

        webTestClient.post()
                .uri(EndPoints.CUSTOMER)
                .body(Mono.just(customerAddDTO),CustomerAddDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerDTO.class)
                .value(customerDTO -> {

                    Optional<CustomerDef> customerDefOptional = customerRepository.findById(customerDTO.getCustomerNumber());
                    assertTrue(customerDefOptional.isPresent());

                })
        ;

    }

    @Test
    void createNewCustomer2() {

        CustomerDef customerDef = createCustomerDef(true);
        customerRepository.save(customerDef);

        CustomerAddDTO customerAddDTO = createCustomerAddDTO(false);
        customerAddDTO.setCustomerNumber(customerDef.getCustomerNumber());

        webTestClient.post()
                .uri(EndPoints.CUSTOMER)
                .body(Mono.just(customerAddDTO),CustomerAddDTO.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
        ;

    }

    @Test
    void findCustomerInfo() {

        CustomerDef customerDef = createCustomerDef(true);
        customerRepository.save(customerDef);

        String url = EndPoints.CUSTOMER_ID.replace("{customerId}",customerDef.getCustomerNumber());

        webTestClient
                .get()
                .uri(url)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerDTO.class)
                .value(customerDTO -> {
                    assertEquals(customerDef.getCustomerNumber(),customerDTO.getCustomerNumber());
                })
                ;

    }

    @Test
    void findCustomerInfo1() {

        CustomerDef customerDef = createCustomerDef(true);

        String url = EndPoints.CUSTOMER_ID.replace("{customerId}",customerDef.getCustomerNumber());

        webTestClient
                .get()
                .uri(url)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)

        ;

    }

    @Test
    void deleteCustomerInfo() {

        CustomerDef customerDef = createCustomerDef(true);
        customerRepository.save(customerDef);

        String url = EndPoints.CUSTOMER_ID.replace("{customerId}",customerDef.getCustomerNumber());

        webTestClient
                .get()
                .uri(url)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerDTO.class)
        ;

    }

    @Test
    void updateCustomerInfo() {

        CustomerDef customerDef = createCustomerDef(true);
        customerRepository.save(customerDef);

        CustomerUpdateDTO customerUpdateDTO = createCustomerAddDTO(true,null);
        customerUpdateDTO.setCustomerNumber(customerDef.getCustomerNumber());

        webTestClient.put()
                .uri(EndPoints.CUSTOMER)
                .body(Mono.just(customerUpdateDTO),CustomerUpdateDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerDTO.class)
                ;
    }

    @Test
    void updateCustomerInfo1() {

        CustomerDef customerDef = createCustomerDef(true);
        customerRepository.save(customerDef);

        CustomerUpdateDTO customerUpdateDTO = createCustomerAddDTO(true,null);

        webTestClient.put()
                .uri(EndPoints.CUSTOMER)
                .body(Mono.just(customerUpdateDTO),CustomerUpdateDTO.class)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
        ;
    }


    private CustomerDef createCustomerDef(boolean allFields){

        Map<CustomerIDType,String> customerIDMap = new HashMap<>();
        customerIDMap.put(CustomerIDType.SSN_OR_NATIONAL_ID,"12345678");
        customerIDMap.put(CustomerIDType.DRIVERS_LICENCE,"ABCDEFGHIJ");


        CustomerDef.CustomerDefBuilder builder = CustomerDef.builder()
                .customerNumber(UUID.randomUUID().toString().replace("-",""))
                .customerType(CustomerType.OWNER)
                .addressType(AddressType.PRIMARY)
                .customerName("Test 1")
                .addressLine1("29, Janatha Road")
                .postalCode("123456")
                .countryCode("IND")
                ;

        if(allFields){
            builder
                    .addressLine2("vyttilla, kochi")
                    .state("kerala")
                    .customerIDMap(customerIDMap)
                    .primaryEmail("testemail.com")
                    .primaryPhoneNumber("34567890");
        }
        return builder.build();
    }

    private CustomerAddDTO createCustomerAddDTO(boolean allFields){

        List<CustomerIDDTO> customerIDDTOList = new ArrayList<>();
        customerIDDTOList.add(CustomerIDDTO.builder()
                .customerIdType(Util.getCustomerIDType(CustomerIDType.PASSPORT_ID))
                .customerId("7895BCD")
                .build())
        ;
        customerIDDTOList.add(CustomerIDDTO.builder()
                .customerIdType(Util.getCustomerIDType(CustomerIDType.TAX_ID))
                .customerId("APNPM5464")
                .build()
        )
        ;


        CustomerAddDTO.CustomerAddDTOBuilder builder = CustomerAddDTO.builder()
                .customerNumber(UUID.randomUUID().toString().replace("-",""))
                .customerType(Util.getCustomerType(CustomerType.CO_OWNER))
                .addressType(Util.getAddressType(AddressType.HOME))
                .customerName("Test 2")
                .addressLine1("35, Janatha Road")
                .postalCode("7890123")
                .countryCode("PAK")
                ;

        if(allFields){
            builder
                    .addressLine2("kadavanthra, karachi")
                    .state("karachi")
                    .customerIDDTOList(customerIDDTOList)
                    .primaryEmail("testemail1.com")
                    .primaryPhoneNumber("345345");
        }
        return builder.build();
    }

    private CustomerUpdateDTO createCustomerAddDTO(boolean allFields, List<Integer>fieldList){



        List<CustomerIDDTO> customerIDDTOListAdd = new ArrayList<>();
        customerIDDTOListAdd.add(CustomerIDDTO.builder()
                .customerIdType(Util.getCustomerIDType(CustomerIDType.SSN_OR_NATIONAL_ID))
                .customerId("SSNID")
                .build())
        ;

        customerIDDTOListAdd.add(CustomerIDDTO.builder()
                .customerIdType(Util.getCustomerIDType(CustomerIDType.CUSTOM_ID_1))
                .customerId("CUSTOM ID 1")
                .build())
        ;
        customerIDDTOListAdd.add(CustomerIDDTO.builder()
                .customerIdType(Util.getCustomerIDType(CustomerIDType.TAX_ID))
                .customerId("APNPM5464")
                .build()
        )
        ;

        List<CustomerIDDTO> customerIDDTOListDelete = new ArrayList<>();
        customerIDDTOListDelete.add(CustomerIDDTO.builder()
                .customerIdType(Util.getCustomerIDType(CustomerIDType.DRIVERS_LICENCE))
                .customerId("123456")
                .build()
        );



        CustomerUpdateDTO.CustomerUpdateDTOBuilder builder = CustomerUpdateDTO.builder()
                .customerNumber(UUID.randomUUID().toString().replace("-",""));


        if(allFields){
            builder
                    .customerType(Util.getCustomerType(CustomerType.CO_OWNER))
                    .addressType(Util.getAddressType(AddressType.HOME))
                    .customerName("Test 2")
                    .addressLine1("35, Janatha Road")
                    .postalCode("7890123")
                    .countryCode("PAK")
                    .addressLine2("kadavanthra, karachi")
                    .state("karachi")
                    .customerIDDTOListAdd(customerIDDTOListAdd)
                    .customerIDDTOListDelete(customerIDDTOListDelete)
                    .primaryEmail("testemail1.com")
                    .primaryPhoneNumber("345345");
            return builder.build();

        }

        fieldList.
                forEach(integer -> evaluateFields(integer,builder,customerIDDTOListAdd,customerIDDTOListDelete));

        return builder.build();
    }

    private void evaluateFields(Integer integer, CustomerUpdateDTO.CustomerUpdateDTOBuilder builder,
                                List<CustomerIDDTO> customerIDDTOListAdd, List<CustomerIDDTO> customerIDDTOListDelete) {

        switch (integer){
            case 1: {
                builder.customerType(Util.getCustomerType(CustomerType.CO_OWNER));
                break;
            }
            case 2: {
                builder.addressType(Util.getAddressType(AddressType.CORPORATE));
                break;
            }
            case 3: {
                builder.customerName("Test 3");
                break;
            }
            case 4: {
                builder.addressLine1("36, Janatha Road");
                break;
            }
            case 5: {
                builder.postalCode("7890124");
                break;
            }
            case 6: {
                builder.countryCode("SRL");
                break;
            }
            case 7: {
                builder.addressLine2("kadavanthra, Colombo");
                break;
            }
            case 8: {
                builder.state("jaffna");
                break;
            }
            case 9: {
                builder.customerIDDTOListAdd(customerIDDTOListAdd);
                break;
            }
            case 10: {
                builder.customerIDDTOListDelete(customerIDDTOListDelete);
                break;
            }
            case 11: {
                builder.primaryEmail("testemail11.com");
            }
            case 12: {
                builder.primaryPhoneNumber("123123123");
            }

        }
    }

}