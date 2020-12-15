package in.nmaloth.maintenance.model.dto.product;

import lombok.*;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class DeclineReasonDTO {

    @NotNull
    @NotBlank
    private String action;
    @NotNull
    private Boolean approveDecline;
    @NotNull
    @NotBlank
    private String declineReason;

    @PositiveOrZero
    @NotNull
    private Integer priority;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeclineReasonDTO)) return false;
        DeclineReasonDTO that = (DeclineReasonDTO) o;
        return getDeclineReason().equals(that.getDeclineReason());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDeclineReason());
    }
}
