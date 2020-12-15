package in.nmaloth.maintenance.model.dto.account;

import in.nmaloth.entity.BlockType;
import in.nmaloth.entity.account.BalanceTypes;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class AccountBasicDTO {

    private String accountNumber;

    private Integer org;
    private Integer product;
    private String accountType;
    private String blockType;
    private String billingCurrencyCode;
    private String previousBlockType;

    private LocalDateTime dateBlockCode;
    private LocalDateTime datePreviousBlockCode;

    private String customerNumber;
    private String corporateNumber;

    private String previousAccountNumber;
    private LocalDateTime dateTransfer;


    private List<BalanceTypesDTO> balanceTypesDTOList;

}
