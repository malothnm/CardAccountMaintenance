package in.nmaloth.maintenance.repository.product;

import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.entity.product.ProductId;
import org.springframework.data.repository.CrudRepository;

public interface ProductDefRepository extends CrudRepository<ProductDef, ProductId> {
}
