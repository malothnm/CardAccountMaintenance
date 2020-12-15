package in.nmaloth.maintenance.controllers.product;

import in.nmaloth.entity.product.DeclineReason;
import in.nmaloth.entity.product.DeclineReasonDef;
import in.nmaloth.maintenance.controllers.EndPoints;
import in.nmaloth.maintenance.model.dto.product.DeclineReasonDTO;
import in.nmaloth.maintenance.model.dto.product.DeclineReasonDefDTO;
import in.nmaloth.maintenance.model.dto.product.DeclineReasonUpdateDefDTO;
import in.nmaloth.maintenance.repository.product.DeclineReasonDefRepository;
import in.nmaloth.maintenance.service.product.DeclineReasonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@DirtiesContext
@AutoConfigureWebTestClient
class DeclineReasonsControllerTest {

    @Autowired
    private DeclineReasonDefRepository declineReasonDefRepository;
    @Autowired
    private DeclineReasonService declineReasonService;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setup(){
        declineReasonDefRepository.findAll()
                .forEach(declineReasonDef -> declineReasonDefRepository.delete(declineReasonDef));
    }

    @Test
    void createNewDeclineReasonsDTO() {

        DeclineReasonDefDTO declineReasonDefDTO = createDeclineReasonDefDTO();

        webTestClient.post()
                .uri(EndPoints.DECLINE_REASONS)
                .body(Mono.just(declineReasonDefDTO),DeclineReasonDefDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DeclineReasonDefDTO.class)
        ;

    }


    @Test
    void createNewDeclineReasonsDTO1() {

        DeclineReasonDef declineReasonDef = createDeclineReasonDef();

        declineReasonDefRepository.save(declineReasonDef);

        DeclineReasonDefDTO declineReasonDefDTO = createDeclineReasonDefDTO();
        declineReasonDefDTO.setServiceName(declineReasonDef.getServiceName());

        webTestClient.post()
                .uri(EndPoints.DECLINE_REASONS)
                .body(Mono.just(declineReasonDefDTO),DeclineReasonDefDTO.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
        ;

    }
    @Test
    void getDeclineReasonForServiceName() {

        DeclineReasonDef declineReasonDef = createDeclineReasonDef();

        declineReasonDefRepository.save(declineReasonDef);

        String uri = EndPoints.DECLINE_REASONS_SERVICE_NAME.replace("{serviceName}",declineReasonDef.getServiceName());
        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DeclineReasonDefDTO.class);

    }

    @Test
    void getDeclineReasonForServiceName1() {

        DeclineReasonDef declineReasonDef = createDeclineReasonDef();

//        declineReasonDefRepository.save(declineReasonDef);

        String uri = EndPoints.DECLINE_REASONS_SERVICE_NAME.replace("{serviceName}",declineReasonDef.getServiceName());
        webTestClient.get()
                .uri(uri)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class);

    }


    @Test
    void getAllDeclineReasonDef() {

        DeclineReasonDef declineReasonDef = createDeclineReasonDef();
        declineReasonDefRepository.save(declineReasonDef);
        declineReasonDef.setServiceName("cryptoService");
        declineReasonDefRepository.save(declineReasonDef);
        declineReasonDef.setServiceName("loyaltyService");
        declineReasonDefRepository.save(declineReasonDef);

        webTestClient
                .get()
                .uri(EndPoints.DECLINE_REASONS)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DeclineReasonDefDTO.class)
                .hasSize(3)
        ;
    }

    @Test
    void deleteDeclineReasonForServiceName() {

        DeclineReasonDef declineReasonDef = createDeclineReasonDef();

        declineReasonDefRepository.save(declineReasonDef);

        String uri = EndPoints.DECLINE_REASONS_SERVICE_NAME.replace("{serviceName}",declineReasonDef.getServiceName());
        webTestClient.delete()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DeclineReasonDefDTO.class);


    }

    @Test
    void deleteDeclineReasonForServiceName1() {

        DeclineReasonDef declineReasonDef = createDeclineReasonDef();


        String uri = EndPoints.DECLINE_REASONS_SERVICE_NAME.replace("{serviceName}",declineReasonDef.getServiceName());
        webTestClient.delete()
                .uri(uri)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class);


    }

    @Test
    void putDeclineReasonForServiceName() {

        DeclineReasonDef declineReasonDef = createDeclineReasonDef();
        declineReasonDefRepository.save(declineReasonDef);

        DeclineReasonUpdateDefDTO declineReasonUpdateDefDTO = createDeclineReasonDefUpdateTO(true,true);
        declineReasonUpdateDefDTO.setServiceName(declineReasonDef.getServiceName());

        webTestClient.put()
                .uri(EndPoints.DECLINE_REASONS)
                .body(Mono.just(declineReasonUpdateDefDTO),DeclineReasonUpdateDefDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DeclineReasonDefDTO.class)
                ;
    }


    @Test
    void putDeclineReasonForServiceName1() {

        DeclineReasonDef declineReasonDef = createDeclineReasonDef();

        DeclineReasonUpdateDefDTO declineReasonUpdateDefDTO = createDeclineReasonDefUpdateTO(true,true);
        declineReasonUpdateDefDTO.setServiceName(declineReasonDef.getServiceName());

        webTestClient.put()
                .uri(EndPoints.DECLINE_REASONS)
                .body(Mono.just(declineReasonUpdateDefDTO),DeclineReasonUpdateDefDTO.class)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
        ;
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