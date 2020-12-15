package in.nmaloth.maintenance.repository.card;

import in.nmaloth.entity.card.Plastic;
import in.nmaloth.entity.card.PlasticKey;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PlasticRepository extends CrudRepository<Plastic, PlasticKey> {

    Iterable<Plastic> findAllByCardNumber(String cardNumber);


}
