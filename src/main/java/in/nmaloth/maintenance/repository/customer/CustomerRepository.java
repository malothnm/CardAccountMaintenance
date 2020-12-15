package in.nmaloth.maintenance.repository.customer;

import in.nmaloth.entity.customer.CustomerDef;
import org.springframework.data.repository.CrudRepository;

public interface CustomerRepository extends CrudRepository<CustomerDef,String> {
}
