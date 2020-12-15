package in.nmaloth.maintenance.model.dto.product;


import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeclineReasonUpdateDefDTO {

    @NotNull
    @NotBlank
    private String serviceName;

    @Valid
    private List<DeclineReasonDTO> declineReasonAddList;

    @Valid
    private List<DeclineReasonDTO> declineReasonDeleteList;


}
