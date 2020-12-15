package in.nmaloth.maintenance.model.dto.customer;

import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerIDDTO {

    @NotNull
    @Pattern(regexp = "0|1|2|3|4|5|6|7")
    private String customerIdType;

    @NotNull
    private String  customerId;

}
