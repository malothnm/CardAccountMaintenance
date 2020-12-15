package in.nmaloth.maintenance.model.dto.card;

import lombok.*;

import javax.validation.constraints.Pattern;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class CardLimitsDTO {

    @Pattern(regexp = "N|C|R|O|Q|A")
    private String limitType;

    private Long limitAmount;
    private Integer limitNumber;

}
