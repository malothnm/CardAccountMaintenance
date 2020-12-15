package in.nmaloth.maintenance.model.dto.product;


import in.nmaloth.entity.product.DeclineReason;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeclineReasonDefDTO {

    @NotNull
    @NotBlank
    private String serviceName;

    @NotNull
    @Valid
    private List<DeclineReasonDTO> declineReasonList;

}
