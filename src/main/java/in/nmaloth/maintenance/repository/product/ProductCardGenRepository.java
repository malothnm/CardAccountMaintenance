package in.nmaloth.maintenance.repository.product;

import in.nmaloth.entity.product.ProductCardGenDef;
import in.nmaloth.entity.product.ProductId;
import org.springframework.data.repository.CrudRepository;

public interface ProductCardGenRepository extends CrudRepository<ProductCardGenDef, ProductId> {
}
