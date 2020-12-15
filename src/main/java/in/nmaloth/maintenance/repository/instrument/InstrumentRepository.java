package in.nmaloth.maintenance.repository.instrument;

import in.nmaloth.entity.instrument.Instrument;
import org.springframework.data.repository.CrudRepository;

import java.util.Iterator;
import java.util.Optional;

public interface InstrumentRepository extends CrudRepository<Instrument,String> {

    Iterable<Instrument> findAllByCardNumber(String cardNumber);

}
