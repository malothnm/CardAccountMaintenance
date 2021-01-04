package in.nmaloth.maintenance.model.dto.card;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlasticsDTO {

    private String plasticId;
    private LocalDate expiryDate;
    private Boolean cardActivated;
    private LocalDateTime cardActivatedDate;
    private LocalDateTime datePlasticIssued;
    private LocalDate dateCardValidFrom;
    private Boolean dynamicCVV;
    private Long activationWaiveDuration;
    private String pendingCardAction;
    private String cardAction;
}
