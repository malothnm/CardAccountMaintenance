package in.nmaloth.maintenance;

import in.nmaloth.entity.account.AccountAccumValues;
import in.nmaloth.entity.account.AccountBasic;
import in.nmaloth.entity.card.CardAccumulatedValues;
import in.nmaloth.entity.card.CardsBasic;
import in.nmaloth.entity.card.Plastic;
import in.nmaloth.entity.customer.CustomerDef;
import in.nmaloth.entity.instrument.Instrument;
import in.nmaloth.entity.product.DeclineReasonDef;
import in.nmaloth.entity.product.ProductCardGenDef;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.entity.product.ProductLimitsDef;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnableClusterConfiguration;
import org.springframework.data.gemfire.config.annotation.EnableEntityDefinedRegions;
import org.springframework.data.gemfire.config.annotation.EnablePdx;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;

@EnableClusterConfiguration
@ComponentScan(basePackageClasses = {CardAccountMaintenance.class,Instrument.class,AccountAccumValues.class,AccountBasic.class,
        CardAccumulatedValues.class,CardsBasic.class,DeclineReasonDef.class,Plastic.class,ProductDef.class,ProductLimitsDef.class,
        ProductCardGenDef.class, CustomerDef.class
}
)
@EnableEntityDefinedRegions(basePackageClasses = {Instrument.class,AccountAccumValues.class,AccountBasic.class,
        CardAccumulatedValues.class,CardsBasic.class,DeclineReasonDef.class,Plastic.class,ProductDef.class,ProductLimitsDef.class,
        ProductCardGenDef.class,CustomerDef.class
})
@SpringBootApplication
@ClientCacheApplication
@EnablePdx
public class CardAccountMaintenance {

    public static void main(String[] args) {
        SpringApplication.run(CardAccountMaintenance.class,args);
    }
}
