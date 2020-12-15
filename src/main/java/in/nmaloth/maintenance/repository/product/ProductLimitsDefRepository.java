package in.nmaloth.maintenance.repository.product;

import in.nmaloth.entity.product.ProductId;
import in.nmaloth.entity.product.ProductLimitsDef;
import org.springframework.data.repository.CrudRepository;

public interface ProductLimitsDefRepository extends CrudRepository<ProductLimitsDef, ProductId> {
}
