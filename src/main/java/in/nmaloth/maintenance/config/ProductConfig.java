package in.nmaloth.maintenance.config;

import in.nmaloth.maintenance.config.data.ProductTable;
import in.nmaloth.maintenance.service.product.ProductServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductConfig {

    private final ProductServices productServices;

    public ProductConfig(ProductServices productServices) {
        this.productServices = productServices;
    }

    @Bean
    public ProductTable populateProductTable(){

        ProductTable productTable = new ProductTable();

        productServices.findAllProducts()
                .map(productDefDTO -> productServices.convertDtoToProduct(productDefDTO))
                .doOnNext(productDef -> productTable.loadMap(productDef))
                .subscribe();

        return productTable;
    }
}
