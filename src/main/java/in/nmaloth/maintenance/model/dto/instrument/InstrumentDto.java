package in.nmaloth.maintenance.model.dto.instrument;

import in.nmaloth.entity.BlockType;
import in.nmaloth.entity.instrument.InstrumentType;
import in.nmaloth.maintenance.model.dto.account.AccountDefDTO;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.mapping.annotation.Indexed;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrumentDto {

    private String instrumentNumber;
    private String instrumentType;

    private boolean active;
    private String cardNumber;
    private Set<AccountDefDTO> accountDefDTOSet;
    private String customerNumber;
    private String corporateNumber;
    private String blockType;
    private String expiryDate;
    private int org;
    private int product;
}
