package in.nmaloth.maintenance.config.data;

import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.entity.product.ProductId;

import java.util.HashMap;
import java.util.Map;

public class ProductTable {

    private final Map<ProductId,ProductDef> productMap = new HashMap<>();


    public ProductDef findProductDef(Integer org, Integer product){
        return productMap.get(new ProductId(org,product));
    }

    public void loadMap(ProductDef productDef){

        productMap.put(productDef.getProductId(),productDef);
    }

}
