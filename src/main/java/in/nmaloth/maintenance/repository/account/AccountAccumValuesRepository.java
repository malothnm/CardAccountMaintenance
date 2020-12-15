package in.nmaloth.maintenance.repository.account;

import in.nmaloth.entity.account.AccountAccumValues;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AccountAccumValuesRepository extends CrudRepository<AccountAccumValues,String>{

    Optional<AccountAccumValues> findByAccountNumber(String accountNumber);
    void deleteByAccountNumber(String accountNumber);


}
