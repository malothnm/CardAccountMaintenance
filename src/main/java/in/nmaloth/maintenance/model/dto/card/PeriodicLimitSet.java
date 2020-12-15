package in.nmaloth.maintenance.model.dto.card;

import in.nmaloth.entity.card.LimitType;
import in.nmaloth.entity.card.PeriodicType;
import lombok.*;

import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class PeriodicLimitSet {
    private PeriodicType periodicType;
    private Set<LimitType> limitTypeSet;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PeriodicLimitSet)) return false;
        PeriodicLimitSet that = (PeriodicLimitSet) o;
        return periodicType == that.periodicType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(periodicType);
    }
}
