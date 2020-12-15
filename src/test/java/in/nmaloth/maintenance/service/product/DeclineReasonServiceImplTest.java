package in.nmaloth.maintenance.service.product;

import in.nmaloth.entity.product.DeclineReason;
import in.nmaloth.entity.product.DeclineReasonDef;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.product.DeclineReasonDTO;
import in.nmaloth.maintenance.model.dto.product.DeclineReasonDefDTO;
import in.nmaloth.maintenance.model.dto.product.DeclineReasonUpdateDefDTO;
import in.nmaloth.maintenance.repository.product.DeclineReasonDefRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DeclineReasonServiceImplTest {


    @Autowired
    private DeclineReasonService declineReasonService;

    @Autowired
    private DeclineReasonDefRepository declineReasonDefRepository;

    @BeforeEach
    void cleanDeclineReason(){

        declineReasonDefRepository.findAll()
                .forEach(declineReasonDef -> declineReasonDefRepository.delete(declineReasonDef));
    }



    @Test
    void createNewDeclineReasonDTO(){

        DeclineReasonDefDTO declineReasonDefDTO = createDeclineReasonDefDTO();
        DeclineReasonDefDTO declineReasonDefDTO1 = declineReasonService.createNewDeclineReasonDTO(declineReasonDefDTO).block();

        DeclineReasonDef declineReasonDef = declineReasonDefRepository.findById(declineReasonDefDTO.getServiceName()).get();



        assertAll(
                ()-> assertEquals(declineReasonDef.getServiceName(),declineReasonDefDTO1.getServiceName()),
                ()-> assertEquals(declineReasonDef.getDeclineReasonList().size(),declineReasonDefDTO.getDeclineReasonList().size()),
                ()-> assertEquals(declineReasonDefDTO1.getDeclineReasonList().size(),declineReasonDefDTO.getDeclineReasonList().size())

                );



    };

    @Test
    void updateDeclineReason1(){

        DeclineReasonDefDTO declineReasonDefDTOTest = declineReasonService.createNewDeclineReasonDTO(createDeclineReasonDefDTO()).block();

        DeclineReasonDef declineReasonDef = declineReasonService.createFromDTO(declineReasonDefDTOTest);
        DeclineReason declineReason = declineReasonDef.getDeclineReasonList().get(2);


        DeclineReasonUpdateDefDTO declineReasonUpdateDefDTO = createDeclineReasonDefUpdateTO(true,true);

        DeclineReasonDefDTO declineReasonDefDTO1Test = declineReasonService.updateDeclineReason(declineReasonUpdateDefDTO).block();


        DeclineReasonDef declineReasonDef1 = declineReasonService.createFromDTO(declineReasonDefDTO1Test);

        DeclineReason declineReason1 = declineReasonDef1.getDeclineReasonList().get(0);
        DeclineReason declineReason2 = declineReasonDef1.getDeclineReasonList().get(1);
        DeclineReason declineReason3 = declineReasonDef1.getDeclineReasonList().get(2);


        DeclineReasonDTO declineReasonDTO1 = declineReasonUpdateDefDTO.getDeclineReasonAddList().get(0);
        DeclineReasonDTO declineReasonDTO2 = declineReasonUpdateDefDTO.getDeclineReasonAddList().get(1);


        assertAll(
                ()-> assertEquals(declineReasonDef.getServiceName(),declineReasonDef1.getServiceName()),
                ()-> assertEquals(3, declineReasonDef1.getDeclineReasonList().size()),
                ()->assertEquals(declineReason1.getAction(),declineReasonDTO1.getAction()),
                ()-> assertEquals(declineReason1.getApproveDecline(),declineReasonDTO1.getApproveDecline()),
                ()-> assertEquals(declineReason1.getDeclineReason(),declineReasonDTO1.getDeclineReason()),
                ()-> assertEquals(declineReason1.getPriority(),declineReasonDTO1.getPriority()),
                ()->assertEquals(declineReason3.getAction(),declineReasonDTO2.getAction()),
                ()-> assertEquals(declineReason3.getApproveDecline(),declineReasonDTO2.getApproveDecline()),
                ()-> assertEquals(declineReason3.getDeclineReason(),declineReasonDTO2.getDeclineReason()),
                ()-> assertEquals(declineReason3.getPriority(),declineReasonDTO2.getPriority()),
                ()->assertEquals(declineReason.getAction(),declineReason2.getAction()),
                ()-> assertEquals(declineReason.getApproveDecline(),declineReason2.getApproveDecline()),
                ()-> assertEquals(declineReason.getDeclineReason(),declineReason2.getDeclineReason()),
                ()-> assertEquals(declineReason.getPriority(),declineReason2.getPriority())

        );



    };

    @Test
    void updateDeclineReason2(){

        DeclineReasonUpdateDefDTO declineReasonUpdateDefDTO = createDeclineReasonDefUpdateTO(true,true);

        Mono<DeclineReasonDefDTO> declineReasonDefDTOMOno = declineReasonService.updateDeclineReason(declineReasonUpdateDefDTO);


        StepVerifier.create(declineReasonDefDTOMOno)
                .expectError(NotFoundException.class)
                .verify();

    }

    @Test
    void fetchDeclineReason1(){

        DeclineReasonDefDTO declineReasonDefDTO = createDeclineReasonDefDTO();
        DeclineReasonDTO[] declineReasonDefDTOArray = declineReasonDefDTO.getDeclineReasonList()
                .stream()
                .toArray(DeclineReasonDTO[]::new);

       declineReasonService.createNewDeclineReasonDTO(declineReasonDefDTO).block();

        DeclineReasonDefDTO declineReasonDefDTO1 = declineReasonService.fetchDeclineReason(declineReasonDefDTO.getServiceName()).block();

        DeclineReasonDTO[] declineReasonDefDTOArray1 = declineReasonDefDTO1.getDeclineReasonList()
                .stream()
                .toArray(DeclineReasonDTO[]::new);



        DeclineReasonDef declineReasonDef = declineReasonDefRepository.findById(declineReasonDefDTO.getServiceName()).get();

        DeclineReasonDefDTO declineReasonDefDTO3 = declineReasonService.createFrom(declineReasonDef);

        DeclineReasonDTO[] declineReasonDefDTOArray2 = declineReasonDefDTO3.getDeclineReasonList()
                .stream()
                .toArray(DeclineReasonDTO[]::new);

        assertAll(
                ()-> assertEquals(declineReasonDef.getServiceName(),declineReasonDefDTO1.getServiceName()),
                ()-> assertEquals(declineReasonDef.getDeclineReasonList().size(),declineReasonDefDTO.getDeclineReasonList().size()),
                ()-> assertEquals(declineReasonDefDTO1.getDeclineReasonList().size(),declineReasonDefDTO.getDeclineReasonList().size()),
                ()-> assertEquals(declineReasonDefDTO.getServiceName(),declineReasonDefDTO1.getServiceName()),
                ()-> assertArrayEquals(declineReasonDefDTOArray,declineReasonDefDTOArray1),
                ()-> assertArrayEquals(declineReasonDefDTOArray2,declineReasonDefDTOArray1)

        );




    };


    @Test
    void fetchDeclineReason2(){


        Mono<DeclineReasonDefDTO> declineReasonDefDTOMono = declineReasonService.fetchDeclineReason("test");

        StepVerifier
                .create(declineReasonDefDTOMono)
                .expectError(NotFoundException.class)
                .verify();

    };

    @Test
    void deleteDeclineReason(){

        DeclineReasonDefDTO declineReasonDefDTO = createDeclineReasonDefDTO();
        declineReasonService.createNewDeclineReasonDTO(declineReasonDefDTO).block();

        declineReasonService.deleteDeclineReason(declineReasonDefDTO.getServiceName()).block();

        Mono<DeclineReasonDefDTO> declineReasonDefDTOMono = declineReasonService.fetchDeclineReason(declineReasonDefDTO.getServiceName());
        StepVerifier
                .create(declineReasonDefDTOMono)
                .expectError(NotFoundException.class)
                .verify();


    };

    @Test
    void deleteDeclineReason1(){


        Mono<DeclineReasonDefDTO> declineReasonDefDTOMono = declineReasonService.deleteDeclineReason("test");

        StepVerifier
                .create(declineReasonDefDTOMono)
                .expectError(NotFoundException.class)
                .verify();


    };

    @Test
    void findAllDeclineReasons(){

        DeclineReasonDefDTO declineReasonDefDTO = createDeclineReasonDefDTO();
        declineReasonService.createNewDeclineReasonDTO(declineReasonDefDTO).block();
        declineReasonDefDTO.setServiceName("cryptoService");
        declineReasonService.createNewDeclineReasonDTO(declineReasonDefDTO).block();
        declineReasonDefDTO.setServiceName("distributor");
        declineReasonService.createNewDeclineReasonDTO(declineReasonDefDTO).block();

        Flux<DeclineReasonDefDTO> declineReasonDefDTOFlux = declineReasonService.findAllDeclineReasons();

        StepVerifier
                .create(declineReasonDefDTOFlux)
                .expectNextCount(3)
                .verifyComplete();

    };


    @Test
    void createFromDTO() {

        DeclineReasonDefDTO declineReasonDefDTO = createDeclineReasonDefDTO();
        DeclineReasonDef declineReasonDef = declineReasonService.createFromDTO(declineReasonDefDTO);

        DeclineReason declineReason1 = declineReasonDef.getDeclineReasonList().get(0);
        DeclineReason declineReason2 = declineReasonDef.getDeclineReasonList().get(1);
        DeclineReason declineReason3 = declineReasonDef.getDeclineReasonList().get(2);

        DeclineReasonDTO declineReasonDTO1 = declineReasonDefDTO.getDeclineReasonList().get(0);
        DeclineReasonDTO declineReasonDTO2 = declineReasonDefDTO.getDeclineReasonList().get(1);
        DeclineReasonDTO declineReasonDTO3 = declineReasonDefDTO.getDeclineReasonList().get(2);



        assertAll(
                ()-> assertEquals(declineReasonDef.getServiceName(),declineReasonDefDTO.getServiceName()),
                ()-> assertEquals(declineReasonDef.getDeclineReasonList().size(),declineReasonDefDTO.getDeclineReasonList().size()),
                ()->assertEquals(declineReason1.getAction(),declineReasonDTO1.getAction()),
                ()-> assertEquals(declineReason1.getApproveDecline(),declineReasonDTO1.getApproveDecline()),
                ()-> assertEquals(declineReason1.getDeclineReason(),declineReasonDTO1.getDeclineReason()),
                ()-> assertEquals(declineReason1.getPriority(),declineReasonDTO1.getPriority()),
                ()->assertEquals(declineReason2.getAction(),declineReasonDTO2.getAction()),
                ()-> assertEquals(declineReason2.getApproveDecline(),declineReasonDTO2.getApproveDecline()),
                ()-> assertEquals(declineReason2.getDeclineReason(),declineReasonDTO2.getDeclineReason()),
                ()-> assertEquals(declineReason2.getPriority(),declineReasonDTO2.getPriority()),
                ()->assertEquals(declineReason3.getAction(),declineReasonDTO3.getAction()),
                ()-> assertEquals(declineReason3.getApproveDecline(),declineReasonDTO3.getApproveDecline()),
                ()-> assertEquals(declineReason3.getDeclineReason(),declineReasonDTO3.getDeclineReason()),
                ()-> assertEquals(declineReason3.getPriority(),declineReasonDTO3.getPriority())
        );

    }

    @Test
    void createFrom() {

        DeclineReasonDef declineReasonDef = createDeclineReasonDef();
        DeclineReasonDefDTO declineReasonDefDTO = declineReasonService.createFrom(declineReasonDef);

        DeclineReason declineReason1 = declineReasonDef.getDeclineReasonList().get(0);
        DeclineReason declineReason2 = declineReasonDef.getDeclineReasonList().get(1);
        DeclineReason declineReason3 = declineReasonDef.getDeclineReasonList().get(2);

        DeclineReasonDTO declineReasonDTO1 = declineReasonDefDTO.getDeclineReasonList().get(0);
        DeclineReasonDTO declineReasonDTO2 = declineReasonDefDTO.getDeclineReasonList().get(1);
        DeclineReasonDTO declineReasonDTO3 = declineReasonDefDTO.getDeclineReasonList().get(2);



        assertAll(
                ()-> assertEquals(declineReasonDef.getServiceName(),declineReasonDefDTO.getServiceName()),
                ()-> assertEquals(declineReasonDef.getDeclineReasonList().size(),declineReasonDefDTO.getDeclineReasonList().size()),
                ()->assertEquals(declineReason1.getAction(),declineReasonDTO1.getAction()),
                ()-> assertEquals(declineReason1.getApproveDecline(),declineReasonDTO1.getApproveDecline()),
                ()-> assertEquals(declineReason1.getDeclineReason(),declineReasonDTO1.getDeclineReason()),
                ()-> assertEquals(declineReason1.getPriority(),declineReasonDTO1.getPriority()),
                ()->assertEquals(declineReason2.getAction(),declineReasonDTO2.getAction()),
                ()-> assertEquals(declineReason2.getApproveDecline(),declineReasonDTO2.getApproveDecline()),
                ()-> assertEquals(declineReason2.getDeclineReason(),declineReasonDTO2.getDeclineReason()),
                ()-> assertEquals(declineReason2.getPriority(),declineReasonDTO2.getPriority()),
                ()->assertEquals(declineReason3.getAction(),declineReasonDTO3.getAction()),
                ()-> assertEquals(declineReason3.getApproveDecline(),declineReasonDTO3.getApproveDecline()),
                ()-> assertEquals(declineReason3.getDeclineReason(),declineReasonDTO3.getDeclineReason()),
                ()-> assertEquals(declineReason3.getPriority(),declineReasonDTO3.getPriority())
        );

    }

    @Test
    void updateFromDTO1() {

        DeclineReasonDef declineReasonDef =  createDeclineReasonDef();
        DeclineReason declineReason = declineReasonDef.getDeclineReasonList().get(2);

        DeclineReasonUpdateDefDTO declineReasonUpdateDefDTO = createDeclineReasonDefUpdateTO(true,true);

        DeclineReasonDef declineReasonDef1 = declineReasonService.updateFromDTO(declineReasonDef,declineReasonUpdateDefDTO);

        DeclineReason declineReason1 = declineReasonDef1.getDeclineReasonList().get(0);
        DeclineReason declineReason2 = declineReasonDef1.getDeclineReasonList().get(1);
        DeclineReason declineReason3 = declineReasonDef1.getDeclineReasonList().get(2);


        DeclineReasonDTO declineReasonDTO1 = declineReasonUpdateDefDTO.getDeclineReasonAddList().get(0);
        DeclineReasonDTO declineReasonDTO2 = declineReasonUpdateDefDTO.getDeclineReasonAddList().get(1);


        assertAll(
                ()-> assertEquals(declineReasonDef.getServiceName(),declineReasonDef1.getServiceName()),
                ()-> assertEquals(3, declineReasonDef1.getDeclineReasonList().size()),
                ()->assertEquals(declineReason1.getAction(),declineReasonDTO1.getAction()),
                ()-> assertEquals(declineReason1.getApproveDecline(),declineReasonDTO1.getApproveDecline()),
                ()-> assertEquals(declineReason1.getDeclineReason(),declineReasonDTO1.getDeclineReason()),
                ()-> assertEquals(declineReason1.getPriority(),declineReasonDTO1.getPriority()),
                ()->assertEquals(declineReason3.getAction(),declineReasonDTO2.getAction()),
                ()-> assertEquals(declineReason3.getApproveDecline(),declineReasonDTO2.getApproveDecline()),
                ()-> assertEquals(declineReason3.getDeclineReason(),declineReasonDTO2.getDeclineReason()),
                ()-> assertEquals(declineReason3.getPriority(),declineReasonDTO2.getPriority()),
                ()->assertEquals(declineReason.getAction(),declineReason2.getAction()),
                ()-> assertEquals(declineReason.getApproveDecline(),declineReason2.getApproveDecline()),
                ()-> assertEquals(declineReason.getDeclineReason(),declineReason2.getDeclineReason()),
                ()-> assertEquals(declineReason.getPriority(),declineReason2.getPriority())

        );
    }

    @Test
    void updateFromDTO2() {

        DeclineReasonDef declineReasonDef =  createDeclineReasonDef();
        DeclineReason declineReason03 = declineReasonDef.getDeclineReasonList().get(2);
        DeclineReason declineReason02 = declineReasonDef.getDeclineReasonList().get(1);
        DeclineReason declineReason01 = declineReasonDef.getDeclineReasonList().get(0);



        DeclineReasonUpdateDefDTO declineReasonUpdateDefDTO = createDeclineReasonDefUpdateTO(true,false);

        DeclineReasonDef declineReasonDef1 = declineReasonService.updateFromDTO(declineReasonDef,declineReasonUpdateDefDTO);

        DeclineReason declineReason1 = declineReasonDef1.getDeclineReasonList().get(0);
        DeclineReason declineReason2 = declineReasonDef1.getDeclineReasonList().get(1);
        DeclineReason declineReason3 = declineReasonDef1.getDeclineReasonList().get(2);
        DeclineReason declineReason4 = declineReasonDef1.getDeclineReasonList().get(3);


        DeclineReasonDTO declineReasonDTO1 = declineReasonUpdateDefDTO.getDeclineReasonAddList().get(0);
        DeclineReasonDTO declineReasonDTO2 = declineReasonUpdateDefDTO.getDeclineReasonAddList().get(1);


        assertAll(
                ()-> assertEquals(declineReasonDef.getServiceName(),declineReasonDef1.getServiceName()),
                ()-> assertEquals(4, declineReasonDef1.getDeclineReasonList().size()),
                ()->assertEquals(declineReason1.getAction(),declineReasonDTO1.getAction()),
                ()-> assertEquals(declineReason1.getApproveDecline(),declineReasonDTO1.getApproveDecline()),
                ()-> assertEquals(declineReason1.getDeclineReason(),declineReasonDTO1.getDeclineReason()),
                ()-> assertEquals(declineReason1.getPriority(),declineReasonDTO1.getPriority()),
                ()->assertEquals(declineReason4.getAction(),declineReasonDTO2.getAction()),
                ()-> assertEquals(declineReason4.getApproveDecline(),declineReasonDTO2.getApproveDecline()),
                ()-> assertEquals(declineReason4.getDeclineReason(),declineReasonDTO2.getDeclineReason()),
                ()-> assertEquals(declineReason4.getPriority(),declineReasonDTO2.getPriority()),
                ()->assertEquals(declineReason03.getAction(),declineReason3.getAction()),
                ()-> assertEquals(declineReason03.getApproveDecline(),declineReason3.getApproveDecline()),
                ()-> assertEquals(declineReason03.getDeclineReason(),declineReason3.getDeclineReason()),
                ()-> assertEquals(declineReason03.getPriority(),declineReason3.getPriority()),
                ()->assertEquals(declineReason02.getAction(),declineReason2.getAction()),
                ()-> assertEquals(declineReason02.getApproveDecline(),declineReason2.getApproveDecline()),
                ()-> assertEquals(declineReason02.getDeclineReason(),declineReason2.getDeclineReason()),
                ()-> assertEquals(declineReason02.getPriority(),declineReason2.getPriority())


        );
    }


    @Test
    void updateFromDTO3() {

        DeclineReasonDef declineReasonDef =  createDeclineReasonDef();
        DeclineReason declineReason03 = declineReasonDef.getDeclineReasonList().get(2);
        DeclineReason declineReason02 = declineReasonDef.getDeclineReasonList().get(1);
        DeclineReason declineReason01 = declineReasonDef.getDeclineReasonList().get(0);



        DeclineReasonUpdateDefDTO declineReasonUpdateDefDTO = createDeclineReasonDefUpdateTO(false,true);

        DeclineReasonDef declineReasonDef1 = declineReasonService.updateFromDTO(declineReasonDef,declineReasonUpdateDefDTO);

        DeclineReason declineReason1 = declineReasonDef1.getDeclineReasonList().get(0);
        DeclineReason declineReason2 = declineReasonDef1.getDeclineReasonList().get(1);
//        DeclineReason declineReason3 = declineReasonDef1.getDeclineReasonList().get(2);


//        DeclineReasonDTO declineReasonDTO1 = declineReasonUpdateDefDTO.getDeclineReasonAddList().get(0);
//        DeclineReasonDTO declineReasonDTO2 = declineReasonUpdateDefDTO.getDeclineReasonAddList().get(1);


        assertAll(
                ()-> assertEquals(declineReasonDef.getServiceName(),declineReasonDef1.getServiceName()),
                ()-> assertEquals(2, declineReasonDef1.getDeclineReasonList().size()),
                ()->assertEquals(declineReason1.getAction(),declineReason01.getAction()),
                ()-> assertEquals(declineReason1.getApproveDecline(),declineReason01.getApproveDecline()),
                ()-> assertEquals(declineReason1.getDeclineReason(),declineReason01.getDeclineReason()),
                ()-> assertEquals(declineReason1.getPriority(),declineReason01.getPriority()),
                ()->assertEquals(declineReason03.getAction(),declineReason2.getAction()),
                ()-> assertEquals(declineReason03.getApproveDecline(),declineReason2.getApproveDecline()),
                ()-> assertEquals(declineReason03.getDeclineReason(),declineReason2.getDeclineReason()),
                ()-> assertEquals(declineReason03.getPriority(),declineReason2.getPriority())

        );
    }




    private DeclineReasonDefDTO createDeclineReasonDefDTO(){

        DeclineReasonDTO declineReasonDTO1 = DeclineReasonDTO.builder()
                .declineReason("BLK_CARD")
                .action("05")
                .approveDecline(true)
                .priority(1)
                .build();

        DeclineReasonDTO declineReasonDTO2 = DeclineReasonDTO.builder()
                .declineReason("LIMIT_CARD")
                .action("06")
                .approveDecline(false)
                .priority(2)
                .build();

        DeclineReasonDTO declineReasonDTO3 = DeclineReasonDTO.builder()
                .declineReason("LIMIT_ACCT")
                .action("07")
                .approveDecline(true)
                .priority(3)
                .build();


        List<DeclineReasonDTO> declineReasonDTOList = new ArrayList<>();
        declineReasonDTOList.add(declineReasonDTO1);
        declineReasonDTOList.add(declineReasonDTO2);
        declineReasonDTOList.add(declineReasonDTO3);
        return DeclineReasonDefDTO.builder()
                .serviceName("limitsService")
                .declineReasonList(declineReasonDTOList)
                .build();
    }


    private DeclineReasonUpdateDefDTO createDeclineReasonDefUpdateTO(boolean addTrue, boolean deleteTrue){

        DeclineReasonDTO declineReasonDTO1 = DeclineReasonDTO.builder()
                .declineReason("BLK_CARD")
                .action("08")
                .approveDecline(false)
                .priority(2)
                .build();

        DeclineReasonDTO declineReasonDTO2 = DeclineReasonDTO.builder()
                .declineReason("LIMIT_CARD")
                .action("09")
                .approveDecline(true)
                .priority(1)
                .build();

        DeclineReasonDTO declineReasonDTO3 = DeclineReasonDTO.builder()
                .declineReason("TEST_REASON")
                .action("10")
                .approveDecline(true)
                .priority(6)
                .build();


        List<DeclineReasonDTO> declineReasonAddDTOList = new ArrayList<>();
        declineReasonAddDTOList.add(declineReasonDTO1);
        declineReasonAddDTOList.add(declineReasonDTO3);

        List<DeclineReasonDTO> declineReasonDeleteDTOList = new ArrayList<>();

        declineReasonDeleteDTOList.add(declineReasonDTO2);

        DeclineReasonUpdateDefDTO.DeclineReasonUpdateDefDTOBuilder builder = DeclineReasonUpdateDefDTO.builder().serviceName("limitsService")
                ;

        if(addTrue){
            builder.declineReasonAddList(declineReasonAddDTOList);
        }
        if(deleteTrue){
            builder.declineReasonDeleteList(declineReasonDeleteDTOList);
        }

        return builder
                .build();
    }


    private DeclineReasonDef createDeclineReasonDef(){

        DeclineReason declineReason1 = DeclineReason.builder()
                .declineReason("BLK_CARD")
                .action("11")
                .approveDecline(true)
                .priority(9)
                .build();

        DeclineReason declineReason2 = DeclineReason.builder()
                .declineReason("LIMIT_CARD")
                .action("12")
                .approveDecline(true)
                .priority(1)
                .build();

        DeclineReason declineReason3 = DeclineReason.builder()
                .declineReason("LIMIT_ACCT")
                .action("13")
                .approveDecline(true)
                .priority(5)
                .build();


        List<DeclineReason> declineReasonList = new ArrayList<>();
        declineReasonList.add(declineReason1);
        declineReasonList.add(declineReason2);
        declineReasonList.add(declineReason3);
        return DeclineReasonDef.builder()
                .serviceName("limitsService")
                .declineReasonList(declineReasonList)
                .build();
    }
}