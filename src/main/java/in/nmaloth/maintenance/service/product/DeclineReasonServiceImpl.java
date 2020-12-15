package in.nmaloth.maintenance.service.product;

import in.nmaloth.entity.product.DeclineReason;
import in.nmaloth.entity.product.DeclineReasonDef;
import in.nmaloth.maintenance.dataService.product.DeclineReasonDataService;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.instrument.InstrumentUpdateDTO;
import in.nmaloth.maintenance.model.dto.product.DeclineReasonDTO;
import in.nmaloth.maintenance.model.dto.product.DeclineReasonDefDTO;
import in.nmaloth.maintenance.model.dto.product.DeclineReasonUpdateDefDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DeclineReasonServiceImpl implements DeclineReasonService {


    private final DeclineReasonDataService declineReasonDataService;

    public DeclineReasonServiceImpl(DeclineReasonDataService declineReasonDataService) {
        this.declineReasonDataService = declineReasonDataService;
    }


    @Override
    public DeclineReasonDef createFromDTO(DeclineReasonDefDTO declineReasonDefDTO) {



        List<DeclineReason> declineReasonList = new ArrayList<>();


        declineReasonDefDTO.getDeclineReasonList()
                .forEach(declineReasonDTO ->declineReasonList.add(createFromDeclineReasonDto(declineReasonDTO)) );

        return DeclineReasonDef.builder()
                .serviceName(declineReasonDefDTO.getServiceName())
                .declineReasonList(declineReasonList)
                .build()
                ;



    }

    private DeclineReason createFromDeclineReasonDto(DeclineReasonDTO declineReasonDTO){
        return DeclineReason.builder()
                .action(declineReasonDTO.getAction())
                .approveDecline(declineReasonDTO.getApproveDecline())
                .declineReason(declineReasonDTO.getDeclineReason())
                .priority(declineReasonDTO.getPriority())
                .build();
    }
    @Override
    public DeclineReasonDefDTO createFrom(DeclineReasonDef declineReasonDef) {

        List<DeclineReasonDTO> declineReasonDTOList = new ArrayList<>();

        declineReasonDef.getDeclineReasonList()
                .forEach(declineReason -> declineReasonDTOList.add(createDtoFromDeclineReason(declineReason)));




        return DeclineReasonDefDTO.builder()
                .serviceName(declineReasonDef.getServiceName())
                .declineReasonList(declineReasonDTOList)
                .build()
                ;
    }

    private DeclineReasonDTO createDtoFromDeclineReason(DeclineReason declineReason){

        return DeclineReasonDTO.builder()
                .action(declineReason.getAction())
                .declineReason(declineReason.getDeclineReason())
                .priority(declineReason.getPriority())
                .approveDecline(declineReason.getApproveDecline())
                .build();
    }



    @Override
    public DeclineReasonDef updateFromDTO(DeclineReasonDef declineReasonDef, DeclineReasonUpdateDefDTO declineReasonUpdateDefDTO) {

        if(declineReasonUpdateDefDTO.getDeclineReasonAddList() != null){
            declineReasonUpdateDefDTO
                    .getDeclineReasonAddList()
                    .forEach(declineReasonDTO -> updateDtoToDeclineReason(declineReasonDef,declineReasonDTO));
        }


        if(declineReasonUpdateDefDTO.getDeclineReasonDeleteList() != null){
            declineReasonUpdateDefDTO
                    .getDeclineReasonDeleteList()
                    .forEach(declineReasonDTO -> deleteDeclineReasonDTO(declineReasonDef,declineReasonDTO));
        }



        return declineReasonDef;
    }

    @Override
    public Mono<DeclineReasonDefDTO> createNewDeclineReasonDTO(DeclineReasonDefDTO declineReasonDefDTO) {

        return declineReasonDataService.saveDeclineReason(createFromDTO(declineReasonDefDTO))
                .map(declineReasonDef -> createFrom(declineReasonDef))
                ;
    }

    @Override
    public Mono<DeclineReasonDefDTO> updateDeclineReason(DeclineReasonUpdateDefDTO declineReasonUpdateDefDTO) {
        return declineReasonDataService.fetchDeclineReason(declineReasonUpdateDefDTO.getServiceName())
                .map(declineReasonDefOptional -> {
                    if(declineReasonDefOptional.isPresent()){
                        return declineReasonDefOptional.get();
                    } else {
                        throw  new NotFoundException("Invalid ServiceName for update" + declineReasonUpdateDefDTO.getServiceName());
                    }
                })
                .map(declineReasonDef -> updateFromDTO(declineReasonDef,declineReasonUpdateDefDTO))
                .flatMap(declineReasonDef -> declineReasonDataService.saveDeclineReason(declineReasonDef))
                .map(declineReasonDef -> createFrom(declineReasonDef))
                ;
    }

    @Override
    public Mono<DeclineReasonDefDTO> fetchDeclineReason(String serviceName) {
        return declineReasonDataService.fetchDeclineReason(serviceName)
                .map(declineReasonDefOptional -> {
                    if(declineReasonDefOptional.isPresent()){
                        return declineReasonDefOptional.get();
                    } else {
                        throw  new NotFoundException("Invalid ServiceName for update" + serviceName);
                    }
                })
                .map(declineReasonDef -> createFrom(declineReasonDef))

                ;
    }

    @Override
    public Mono<Optional<DeclineReasonDef>> fetchDeclineReasonOptional(String serviceName) {
        return declineReasonDataService.fetchDeclineReason(serviceName);
    }

    @Override
    public Mono<DeclineReasonDefDTO> deleteDeclineReason(String serviceName) {
        return   declineReasonDataService.deleteDeclineReasonDef(serviceName)
                .map(declineReasonDefOptional -> {
                    if(declineReasonDefOptional.isPresent()){
                        return declineReasonDefOptional.get();
                    } else {
                        throw  new NotFoundException("Invalid ServiceName for update" + serviceName);
                    }
                })
                .map(declineReasonDef -> createFrom(declineReasonDef))
                ;
    }

    @Override
    public Flux<DeclineReasonDefDTO> findAllDeclineReasons() {
        return declineReasonDataService.findAllDeclineReasonDef()
                .map(declineReasonDef -> createFrom(declineReasonDef));
    }

    private void updateDtoToDeclineReason(DeclineReasonDef declineReasonDef, DeclineReasonDTO declineReasonDTO) {

        if(declineReasonDef.getDeclineReasonList() == null){
            declineReasonDef.setDeclineReasonList(new ArrayList<>());
        }
        Optional<DeclineReason> declineReasonOptional = declineReasonDef.getDeclineReasonList()
                .stream()
                .filter(declineReason -> declineReason.getDeclineReason().equalsIgnoreCase(declineReasonDTO.getDeclineReason()))
                .findFirst();

        if(declineReasonOptional.isPresent()){
            DeclineReason declineReason = declineReasonOptional.get();
            declineReason.setAction(declineReasonDTO.getAction());
            declineReason.setApproveDecline(declineReasonDTO.getApproveDecline());
            declineReason.setPriority(declineReasonDTO.getPriority());
        } else {
            DeclineReason declineReason = DeclineReason.builder()
                    .declineReason(declineReasonDTO.getDeclineReason())
                    .approveDecline(declineReasonDTO.getApproveDecline())
                    .priority(declineReasonDTO.getPriority())
                    .action(declineReasonDTO.getAction())
                    .build();
            declineReasonDef.getDeclineReasonList()
                    .add(declineReason);
        }


    }

    private void deleteDeclineReasonDTO(DeclineReasonDef declineReasonDef, DeclineReasonDTO declineReasonDTO) {

        Optional<DeclineReason> declineReasonOptional = declineReasonDef.getDeclineReasonList()
                .stream()
                .filter(declineReason -> declineReason.getDeclineReason().equalsIgnoreCase(declineReasonDTO.getDeclineReason()))
                .findFirst();

        if(declineReasonOptional.isPresent()){
            declineReasonDef.getDeclineReasonList()
                    .remove(declineReasonOptional.get());
        }
    }
}
