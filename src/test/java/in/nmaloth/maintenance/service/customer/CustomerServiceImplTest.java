package in.nmaloth.maintenance.service.customer;

import in.nmaloth.entity.customer.AddressType;
import in.nmaloth.entity.customer.CustomerDef;
import in.nmaloth.entity.customer.CustomerIDType;
import in.nmaloth.entity.customer.CustomerType;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.customer.CustomerAddDTO;
import in.nmaloth.maintenance.model.dto.customer.CustomerDTO;
import in.nmaloth.maintenance.model.dto.customer.CustomerIDDTO;
import in.nmaloth.maintenance.model.dto.customer.CustomerUpdateDTO;
import in.nmaloth.maintenance.repository.customer.CustomerRepository;
import in.nmaloth.maintenance.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CustomerServiceImplTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setup(){
        customerRepository.findAll()
                .forEach(customerDef -> customerRepository.delete(customerDef));
    }


    @Test
    void createNewCustomerRecord() {

        CustomerAddDTO customerAddDTO = createCustomerAddDTO(true);
        customerService.createNewCustomerRecord(customerAddDTO).block();

        CustomerDef customerDef = customerRepository.findById(customerAddDTO.getCustomerNumber()).get();

        assertAll(
                ()-> assertEquals(customerAddDTO.getCustomerNumber(),customerDef.getCustomerNumber()),
                ()-> assertEquals(customerAddDTO.getAddressType(),Util.getAddressType(customerDef.getAddressType())),
                ()-> assertEquals(customerAddDTO.getCustomerType(),Util.getCustomerType(customerDef.getCustomerType())),
                ()-> assertEquals(customerAddDTO.getCustomerName(),customerDef.getCustomerName()),
                ()-> assertEquals(customerAddDTO.getAddressLine1(),customerDef.getAddressLine1()),
                ()-> assertEquals(customerAddDTO.getAddressLine2(),customerDef.getAddressLine2()),
                ()-> assertEquals(customerAddDTO.getPostalCode(),customerDef.getPostalCode()),
                ()-> assertEquals(customerAddDTO.getState(),customerDef.getState()),
                ()-> assertEquals(customerAddDTO.getCountryCode(),customerDef.getCountryCode()),
                ()-> assertEquals(customerAddDTO.getCustomerIDDTOList().size(),customerDef.getCustomerIDMap().size()),
                ()-> assertEquals(customerAddDTO.getPrimaryEmail(),customerDef.getPrimaryEmail()),
                ()-> assertEquals(customerAddDTO.getPrimaryPhoneNumber(),customerDef.getPrimaryPhoneNumber())
        );




    }

    @Test
    void updateCustomerRecord() {

        CustomerDef customerDef = createCustomerDef(true);
        customerRepository.save(customerDef);

        CustomerUpdateDTO customerUpdateDTO = createCustomerAddDTO(true,null);
        customerUpdateDTO.setCustomerNumber(customerDef.getCustomerNumber());
        customerService.updateCustomerRecord(customerUpdateDTO).block();

        CustomerDef customerDef1 = customerRepository.findById(customerUpdateDTO.getCustomerNumber()).get();


        assertAll(
                ()-> assertEquals(customerUpdateDTO.getCustomerNumber(), customerDef1.getCustomerNumber()),
                ()-> assertEquals(customerUpdateDTO.getAddressType(),Util.getAddressType(customerDef1.getAddressType())),
                ()-> assertEquals(customerUpdateDTO.getCustomerType(),Util.getCustomerType(customerDef1.getCustomerType())),
                ()-> assertEquals(customerUpdateDTO.getCustomerName(),customerDef1.getCustomerName()),
                ()-> assertEquals(customerUpdateDTO.getAddressLine1(),customerDef1.getAddressLine1()),
                ()-> assertEquals(customerUpdateDTO.getAddressLine2(),customerDef1.getAddressLine2()),
                ()-> assertEquals(customerUpdateDTO.getPostalCode(),customerDef1.getPostalCode()),
                ()-> assertEquals(customerUpdateDTO.getState(),customerDef1.getState()),
                ()-> assertEquals(customerUpdateDTO.getCountryCode(),customerDef1.getCountryCode()),
                ()-> assertEquals(3,customerDef1.getCustomerIDMap().size()),
                ()-> assertEquals(customerUpdateDTO.getPrimaryEmail(),customerDef1.getPrimaryEmail()),
                ()-> assertEquals(customerUpdateDTO.getPrimaryPhoneNumber(),customerDef1.getPrimaryPhoneNumber())
        );

    }

    @Test
    void updateCustomerRecord1() {


        CustomerUpdateDTO customerUpdateDTO = createCustomerAddDTO(true,null);
        Mono<CustomerDef> customerDefMono = customerService.updateCustomerRecord(customerUpdateDTO);

        StepVerifier.create(customerDefMono)
                .expectError(NotFoundException.class)
                .verify();


    }

    @Test
    void fetchCustomerInfo() {

        CustomerDef customerDef = createCustomerDef(true);
        customerRepository.save(customerDef);

        Mono<CustomerDef> customerDefMono = customerService.fetchCustomerInfo(customerDef.getCustomerNumber());

        StepVerifier
                .create(customerDefMono)
                .expectNextCount(1)
                .verifyComplete();

    }

    @Test
    void fetchCustomerInfo1() {


        Mono<CustomerDef> customerDefMono = customerService.fetchCustomerInfo("12345");

        StepVerifier
                .create(customerDefMono)
                .expectError(NotFoundException.class)
                .verify();

    }

    @Test
    void deleteCustomerInfo() {

        CustomerDef customerDef = createCustomerDef(true);
        customerRepository.save(customerDef);

        customerService.deleteCustomerInfo(customerDef.getCustomerNumber()).block();

        Optional<CustomerDef> customerDefOptional = customerRepository.findById(customerDef.getCustomerNumber());

        assertTrue(customerDefOptional.isEmpty());

    }

    @Test
    void deleteCustomerInfo1() {



        Mono<CustomerDef> customerDefMono = customerService.deleteCustomerInfo("1234");

        StepVerifier.create(customerDefMono)
                .expectError(NotFoundException.class)
                .verify();



    }

    @Test
    void convertToDTO() {

        CustomerDef customerDef = createCustomerDef(true);
        CustomerDTO customerDTO = customerService.convertToDTO(customerDef);

        assertAll(
                ()-> assertEquals(customerDef.getCustomerNumber(),customerDTO.getCustomerNumber()),
                ()-> assertEquals(customerDef.getAddressType(),Util.getAddressType(customerDTO.getAddressType())),
                ()-> assertEquals(customerDef.getCustomerType(),Util.getCustomerType(customerDTO.getCustomerType())),
                ()-> assertEquals(customerDef.getCustomerName(),customerDTO.getCustomerName()),
                ()-> assertEquals(customerDef.getAddressLine1(),customerDTO.getAddressLine1()),
                ()-> assertEquals(customerDef.getAddressLine2(),customerDTO.getAddressLine2()),
                ()-> assertEquals(customerDef.getPostalCode(),customerDTO.getPostalCode()),
                ()-> assertEquals(customerDef.getState(),customerDTO.getState()),
                ()-> assertEquals(customerDef.getCountryCode(),customerDTO.getCountryCode()),
                ()-> assertEquals(customerDef.getCustomerIDMap().size(),customerDTO.getCustomerIDDTOList().size()),
                ()-> assertEquals(customerDef.getPrimaryEmail(),customerDTO.getPrimaryEmail()),
                ()-> assertEquals(customerDef.getPrimaryPhoneNumber(),customerDTO.getPrimaryPhoneNumber())
        );
    }


    @Test
    void convertToDTO1() {

        CustomerDef customerDef = createCustomerDef(false);
        CustomerDTO customerDTO = customerService.convertToDTO(customerDef);

        assertAll(
                ()-> assertEquals(customerDef.getCustomerNumber(),customerDTO.getCustomerNumber()),
                ()-> assertEquals(customerDef.getAddressType(),Util.getAddressType(customerDTO.getAddressType())),
                ()-> assertEquals(customerDef.getCustomerType(),Util.getCustomerType(customerDTO.getCustomerType())),
                ()-> assertEquals(customerDef.getCustomerName(),customerDTO.getCustomerName()),
                ()-> assertEquals(customerDef.getAddressLine1(),customerDTO.getAddressLine1()),
                ()-> assertNull(customerDTO.getAddressLine2()),
                ()-> assertEquals(customerDef.getPostalCode(),customerDTO.getPostalCode()),
                ()-> assertNull(customerDTO.getState()),
                ()-> assertEquals(customerDef.getCountryCode(),customerDTO.getCountryCode()),
                ()-> assertNull(customerDTO.getCustomerIDDTOList()),
                ()-> assertNull(customerDTO.getPrimaryEmail()),
                ()-> assertNull(customerDTO.getPrimaryPhoneNumber())
        );
    }
    @Test
    void convertDTOToCustomerDef() {

        CustomerAddDTO customerAddDTO = createCustomerAddDTO(true);
        CustomerDef customerDef = customerService.convertDTOToCustomerDef(customerAddDTO);

        assertAll(
                ()-> assertEquals(customerAddDTO.getCustomerNumber(),customerDef.getCustomerNumber()),
                ()-> assertEquals(customerAddDTO.getAddressType(),Util.getAddressType(customerDef.getAddressType())),
                ()-> assertEquals(customerAddDTO.getCustomerType(),Util.getCustomerType(customerDef.getCustomerType())),
                ()-> assertEquals(customerAddDTO.getCustomerName(),customerDef.getCustomerName()),
                ()-> assertEquals(customerAddDTO.getAddressLine1(),customerDef.getAddressLine1()),
                ()-> assertEquals(customerAddDTO.getAddressLine2(),customerDef.getAddressLine2()),
                ()-> assertEquals(customerAddDTO.getPostalCode(),customerDef.getPostalCode()),
                ()-> assertEquals(customerAddDTO.getState(),customerDef.getState()),
                ()-> assertEquals(customerAddDTO.getCountryCode(),customerDef.getCountryCode()),
                ()-> assertEquals(customerAddDTO.getCustomerIDDTOList().size(),customerDef.getCustomerIDMap().size()),
                ()-> assertEquals(customerAddDTO.getPrimaryEmail(),customerDef.getPrimaryEmail()),
                ()-> assertEquals(customerAddDTO.getPrimaryPhoneNumber(),customerDef.getPrimaryPhoneNumber())
        );
    }

    @Test
    void convertDTOToCustomerDef1() {

        CustomerAddDTO customerAddDTO = createCustomerAddDTO(false);
        CustomerDef customerDef = customerService.convertDTOToCustomerDef(customerAddDTO);

        assertAll(
                ()-> assertEquals(customerAddDTO.getCustomerNumber(),customerDef.getCustomerNumber()),
                ()-> assertEquals(customerAddDTO.getAddressType(),Util.getAddressType(customerDef.getAddressType())),
                ()-> assertEquals(customerAddDTO.getCustomerType(),Util.getCustomerType(customerDef.getCustomerType())),
                ()-> assertEquals(customerAddDTO.getCustomerName(),customerDef.getCustomerName()),
                ()-> assertEquals(customerAddDTO.getAddressLine1(),customerDef.getAddressLine1()),
                ()-> assertNull(customerDef.getAddressLine2()),
                ()-> assertEquals(customerAddDTO.getPostalCode(),customerDef.getPostalCode()),
                ()-> assertNull(customerDef.getState()),
                ()-> assertEquals(customerAddDTO.getCountryCode(),customerDef.getCountryCode()),
                ()-> assertNull(customerDef.getCustomerIDMap()),
                ()-> assertNull(customerDef.getPrimaryEmail()),
                ()-> assertNull(customerDef.getPrimaryPhoneNumber())
        );
    }

    @Test
    void updateCustomerFromDTO() {

        CustomerDef customerDef = createCustomerDef(true);
        CustomerUpdateDTO customerUpdateDTO = createCustomerAddDTO(true,null);
        customerUpdateDTO.setCustomerNumber(customerDef.getCustomerNumber());

        customerService.updateCustomerFromDTO(customerUpdateDTO,customerDef);

        assertAll(
                ()-> assertEquals(customerUpdateDTO.getCustomerNumber(),customerDef.getCustomerNumber()),
                ()-> assertEquals(customerUpdateDTO.getAddressType(),Util.getAddressType(customerDef.getAddressType())),
                ()-> assertEquals(customerUpdateDTO.getCustomerType(),Util.getCustomerType(customerDef.getCustomerType())),
                ()-> assertEquals(customerUpdateDTO.getCustomerName(),customerDef.getCustomerName()),
                ()-> assertEquals(customerUpdateDTO.getAddressLine1(),customerDef.getAddressLine1()),
                ()-> assertEquals(customerUpdateDTO.getAddressLine2(),customerDef.getAddressLine2()),
                ()-> assertEquals(customerUpdateDTO.getPostalCode(),customerDef.getPostalCode()),
                ()-> assertEquals(customerUpdateDTO.getState(),customerDef.getState()),
                ()-> assertEquals(customerUpdateDTO.getCountryCode(),customerDef.getCountryCode()),
                ()-> assertEquals(3,customerDef.getCustomerIDMap().size()),
                ()-> assertEquals(customerUpdateDTO.getPrimaryEmail(),customerDef.getPrimaryEmail()),
                ()-> assertEquals(customerUpdateDTO.getPrimaryPhoneNumber(),customerDef.getPrimaryPhoneNumber())
        );


    }

    @Test
    void updateCustomerFromDTO1() {

        CustomerDef customerDef = createCustomerDef(false);
        CustomerUpdateDTO customerUpdateDTO = createCustomerAddDTO(true,null);
        customerUpdateDTO.setCustomerNumber(customerDef.getCustomerNumber());

        customerService.updateCustomerFromDTO(customerUpdateDTO,customerDef);

        assertAll(
                ()-> assertEquals(customerUpdateDTO.getCustomerNumber(),customerDef.getCustomerNumber()),
                ()-> assertEquals(customerUpdateDTO.getAddressType(),Util.getAddressType(customerDef.getAddressType())),
                ()-> assertEquals(customerUpdateDTO.getCustomerType(),Util.getCustomerType(customerDef.getCustomerType())),
                ()-> assertEquals(customerUpdateDTO.getCustomerName(),customerDef.getCustomerName()),
                ()-> assertEquals(customerUpdateDTO.getAddressLine1(),customerDef.getAddressLine1()),
                ()-> assertEquals(customerUpdateDTO.getAddressLine2(),customerDef.getAddressLine2()),
                ()-> assertEquals(customerUpdateDTO.getPostalCode(),customerDef.getPostalCode()),
                ()-> assertEquals(customerUpdateDTO.getState(),customerDef.getState()),
                ()-> assertEquals(customerUpdateDTO.getCountryCode(),customerDef.getCountryCode()),
                ()-> assertEquals(3,customerDef.getCustomerIDMap().size()),
                ()-> assertEquals(customerUpdateDTO.getPrimaryEmail(),customerDef.getPrimaryEmail()),
                ()-> assertEquals(customerUpdateDTO.getPrimaryPhoneNumber(),customerDef.getPrimaryPhoneNumber())
        );

    }

    @Test
    void updateCustomerFromDTO2() {

        CustomerDef customerDef = createCustomerDef(true);

        String prevCountryCode = customerDef.getCountryCode();
        String prevState = customerDef.getState();
        String prevAddressLine2 = customerDef.getAddressLine2();
        String prevPrimaryEmail = customerDef.getPrimaryEmail();
        String prevPhoneNumber = customerDef.getPrimaryPhoneNumber();


        List<Integer> integerList = Arrays.asList(1,2,3,4,5,9);
        CustomerUpdateDTO customerUpdateDTO = createCustomerAddDTO(false,integerList);
        customerUpdateDTO.setCustomerNumber(customerDef.getCustomerNumber());

        customerService.updateCustomerFromDTO(customerUpdateDTO,customerDef);

        assertAll(
                ()-> assertEquals(customerUpdateDTO.getCustomerNumber(),customerDef.getCustomerNumber()),
                ()-> assertEquals(customerUpdateDTO.getAddressType(),Util.getAddressType(customerDef.getAddressType())),
                ()-> assertEquals(customerUpdateDTO.getCustomerType(),Util.getCustomerType(customerDef.getCustomerType())),
                ()-> assertEquals(customerUpdateDTO.getCustomerName(),customerDef.getCustomerName()),
                ()-> assertEquals(customerUpdateDTO.getAddressLine1(),customerDef.getAddressLine1()),
                ()-> assertEquals(prevAddressLine2,customerDef.getAddressLine2()),
                ()-> assertEquals(customerUpdateDTO.getPostalCode(),customerDef.getPostalCode()),
                ()-> assertEquals(prevState,customerDef.getState()),
                ()-> assertEquals(prevCountryCode,customerDef.getCountryCode()),
                ()-> assertEquals(4,customerDef.getCustomerIDMap().size()),
                ()-> assertEquals(prevPrimaryEmail,customerDef.getPrimaryEmail()),
                ()-> assertEquals(prevPhoneNumber,customerDef.getPrimaryPhoneNumber())
        );

    }

    @Test
    void updateCustomerFromDTO3() {

        CustomerDef customerDef = createCustomerDef(true);

        CustomerType prevCustomerType = customerDef.getCustomerType();
        AddressType prevAddressType = customerDef.getAddressType();
        String prevName = customerDef.getCustomerName();
        String prevAddressLine1 = customerDef.getAddressLine1();
        String prevPostalCode = customerDef.getPostalCode();


        List<Integer> integerList = Arrays.asList(6,7,8,10,11,12);
        CustomerUpdateDTO customerUpdateDTO = createCustomerAddDTO(false,integerList);
        customerUpdateDTO.setCustomerNumber(customerDef.getCustomerNumber());

        customerService.updateCustomerFromDTO(customerUpdateDTO,customerDef);

        assertAll(
                ()-> assertEquals(customerUpdateDTO.getCustomerNumber(),customerDef.getCustomerNumber()),
                ()-> assertEquals(prevAddressType,customerDef.getAddressType()),
                ()-> assertEquals(prevCustomerType,customerDef.getCustomerType()),
                ()-> assertEquals(prevName,customerDef.getCustomerName()),
                ()-> assertEquals(prevAddressLine1,customerDef.getAddressLine1()),
                ()-> assertEquals(customerUpdateDTO.getAddressLine2(),customerDef.getAddressLine2()),
                ()-> assertEquals(prevPostalCode,customerDef.getPostalCode()),
                ()-> assertEquals(customerUpdateDTO.getState(),customerDef.getState()),
                ()-> assertEquals(customerUpdateDTO.getCountryCode(),customerDef.getCountryCode()),
                ()-> assertEquals(1,customerDef.getCustomerIDMap().size()),
                ()-> assertEquals(customerUpdateDTO.getPrimaryEmail(),customerDef.getPrimaryEmail()),
                ()-> assertEquals(customerUpdateDTO.getPrimaryPhoneNumber(),customerDef.getPrimaryPhoneNumber())
        );

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

    private CustomerUpdateDTO createCustomerAddDTO(boolean allFields,List<Integer>fieldList){



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