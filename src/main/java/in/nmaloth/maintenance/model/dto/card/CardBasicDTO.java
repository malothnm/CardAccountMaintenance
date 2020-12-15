package in.nmaloth.maintenance.model.dto.card;

import in.nmaloth.entity.BlockType;
import in.nmaloth.entity.card.CardHolderType;
import in.nmaloth.entity.card.CardStatus;
import in.nmaloth.maintenance.model.dto.account.AccountDefDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CardBasicDTO {

    private String cardNumber;
    private Integer product;
    private Integer org;
    private String cardStatus;
    private String cardholderType;
    private String blockType;
    private String prevBlockType;
    private LocalDateTime dateBlockCode;
    private LocalDateTime datePrevBlockCode;
    private Integer waiverDaysActivation;
    private Integer cardReturnNumber;

    private String prevCardNumber;
    private LocalDateTime dateTransfer;

    private String customerNumber;
    private String corporateNumber;

    private Set<AccountDefDTO> accountDefDTOSet;

    private List<PeriodicCardLimitDTO> periodicCardLimitDTOList;



}
