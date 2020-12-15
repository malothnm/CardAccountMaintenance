package in.nmaloth.maintenance.service.product;

import in.nmaloth.entity.account.BalanceTypes;
import in.nmaloth.entity.product.ProductDef;
import in.nmaloth.entity.product.ProductId;
import in.nmaloth.maintenance.dataService.product.ProductDefDataService;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.product.LimitPercentDTO;
import in.nmaloth.maintenance.model.dto.product.ProductDefDTO;
import in.nmaloth.maintenance.model.dto.product.ProductDefUpdateDTO;
import in.nmaloth.maintenance.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductServicesImpl implements ProductServices {

    private final ProductDefDataService productDefDataService;

    public ProductServicesImpl(ProductDefDataService productDefDataService) {
        this.productDefDataService = productDefDataService;
    }


    @Override
    public ProductDef convertDtoToProduct(ProductDefDTO productDefDTO) {

        ProductId productId = new ProductId(productDefDTO.getOrg(), productDefDTO.getProduct());

        Map<BalanceTypes,Long> balancePercent = new HashMap<>();

        if(productDefDTO.getLimitPercents() != null){
            productDefDTO.getLimitPercents()
                    .forEach(limitPercentDTO ->
                            balancePercent.put(Util.getBalanceTypes(limitPercentDTO.getBalanceTypes()),limitPercentDTO.getPercent()));
        }



        return ProductDef.builder()
                .cardsActivationRequired(productDefDTO.getCardsActivationRequired())
                .cardsValidityMonthNew(productDefDTO.getCardsValidityMonthNew())
                .cardsValidityMonthReIssue(productDefDTO.getCardsValidityMonthReIssue())
                .cardsValidityMonthReplace(productDefDTO.getCardsValidityMonthReplace())
                .cardsWaiverActivationDays(productDefDTO.getCardsWaiverActivationDays())
                .dateRangeNewExpDate(productDefDTO.getDateRangeNewExpDate())
                .daysToCardsValid(productDefDTO.getDaysToCardsValid())
                .cardsReturn(productDefDTO.getCardsReturn())
                .productId(productId)
                .serviceCode(productDefDTO.getServiceCode())
                .primaryAccountType(Util.getAccountType(productDefDTO.getPrimaryAccountType()))
                .billingCurrencyCode(productDefDTO.getBillingCurrencyCode())
                .limitPercents(balancePercent)
                .build()

        ;



    }

    @Override
    public ProductDefDTO convertProductToDto(ProductDef productDef) {


        List<LimitPercentDTO> limitPercentDTOList = productDef.getLimitPercents().entrySet()
                .stream()
                .map(percentEntry -> LimitPercentDTO.builder()
                        .balanceTypes(Util.getBalanceTypes(percentEntry.getKey()))
                        .percent(percentEntry.getValue())
                        .build())
                .collect(Collectors.toList())
                ;

        return ProductDefDTO.builder()
                .cardsActivationRequired(productDef.getCardsActivationRequired())
                .cardsValidityMonthNew(productDef.getCardsValidityMonthNew())
                .cardsValidityMonthReIssue(productDef.getCardsValidityMonthReIssue())
                .cardsValidityMonthReplace(productDef.getCardsValidityMonthReplace())
                .cardsWaiverActivationDays(productDef.getCardsWaiverActivationDays())
                .dateRangeNewExpDate(productDef.getDateRangeNewExpDate())
                .daysToCardsValid(productDef.getDaysToCardsValid())
                .limitPercents(limitPercentDTOList)
                .org(productDef.getProductId().getOrg())
                .product(productDef.getProductId().getProduct())
                .cardsReturn(productDef.getCardsReturn())
                .serviceCode(productDef.getServiceCode())
                .billingCurrencyCode(productDef.getBillingCurrencyCode())
                .primaryAccountType(Util.getAccountType(productDef.getPrimaryAccountType()))
                .build();
    }

    @Override
    public ProductDef updateProductDefFromDto(ProductDefUpdateDTO productDefUpdateDTO, ProductDef productDef) {


        if(productDefUpdateDTO.getCardsValidityMonthNew() != null ){
            productDef.setCardsValidityMonthNew(productDefUpdateDTO.getCardsValidityMonthNew());
        }

        if(productDefUpdateDTO.getCardsValidityMonthReplace() != null){
            productDef.setCardsValidityMonthReplace(productDefUpdateDTO.getCardsValidityMonthReplace());
        }

        if(productDefUpdateDTO.getCardsValidityMonthReIssue() != null){
            productDef.setCardsValidityMonthReIssue(productDefUpdateDTO.getCardsValidityMonthReIssue());
        }

        if(productDefUpdateDTO.getDateRangeNewExpDate() != null){
            productDef.setDateRangeNewExpDate(productDefUpdateDTO.getDateRangeNewExpDate());
        }

        if(productDefUpdateDTO.getCardsWaiverActivationDays() != null){
            productDef.setCardsWaiverActivationDays(productDefUpdateDTO.getCardsWaiverActivationDays());
        }

        if(productDefUpdateDTO.getDaysToCardsValid() != null){
            productDef.setDaysToCardsValid(productDefUpdateDTO.getDaysToCardsValid());
        }
        if(productDefUpdateDTO.getCardsActivationRequired() != null){
            productDef.setCardsActivationRequired(productDefUpdateDTO.getCardsActivationRequired());
        }
        if(productDefUpdateDTO.getServiceCode() != null){
            productDef.setServiceCode(productDefUpdateDTO.getServiceCode());
        }

        if(productDefUpdateDTO.getCardsReturn() != null){
            productDef.setCardsReturn(productDefUpdateDTO.getCardsReturn());
        }

        if(productDefUpdateDTO.getPrimaryAccountType() != null){
            productDef.setPrimaryAccountType(Util.getAccountType(productDefUpdateDTO.getPrimaryAccountType()));
        }

        if(productDefUpdateDTO.getBillingCurrencyCode() != null){
            productDef.setBillingCurrencyCode(productDefUpdateDTO.getBillingCurrencyCode());
        }

        if(productDefUpdateDTO.getLimitPercentListAdd() != null){
            productDefUpdateDTO.getLimitPercentListAdd()
                    .forEach(limitPercentDTO -> updateLimitPercent(productDef,limitPercentDTO));
        }

        if(productDefUpdateDTO.getLimitPercentListDelete() != null){
            productDefUpdateDTO.getLimitPercentListDelete()
                    .forEach(limitPercentDTO -> deleteLimitPercent(productDef,limitPercentDTO));
        }



        return productDef;
    }

    @Override
    public Mono<ProductDefDTO> createNewProductDef(ProductDefDTO productDefDTO) {

        return productDefDataService.saveProductDef(convertDtoToProduct(productDefDTO))
                .map(productDef -> convertProductToDto(productDef));

    }

    @Override
    public Mono<ProductDefDTO> updateProductDef(ProductDefUpdateDTO productDefUpdateDTO) {

        return productDefDataService.fetchProductDef(new ProductId(productDefUpdateDTO.getOrg(),productDefUpdateDTO.getProduct()))
                .map(productDefOptional -> {

                    if(productDefOptional.isPresent()){
                        return productDefOptional.get();
                    }
                    throw new NotFoundException("No product def found for Org " + productDefUpdateDTO.getOrg() + " Product "
                            + productDefUpdateDTO.getProduct());
                } )
                .map(productDef -> updateProductDefFromDto(productDefUpdateDTO,productDef))
                .flatMap(productDef -> productDefDataService.saveProductDef(productDef))
                .map(productDef -> convertProductToDto(productDef));

    }

    @Override
    public Mono<ProductDefDTO> fetchProductInfo(Integer org, Integer product) {

        return productDefDataService.fetchProductDef(new ProductId(org,product))
                .map(productDefOptional -> {

                    if(productDefOptional.isPresent()){
                        return productDefOptional.get();
                    }
                    throw new NotFoundException("No product def found for Org " + org + " Product " + product);
                } )
                .map(productDef -> convertProductToDto(productDef));

    }

    @Override
    public Mono<Optional<ProductDef>> fetchProductOptional(Integer org, Integer product) {
        return productDefDataService.fetchProductDef(new ProductId(org,product));
    }

    @Override
    public Mono<ProductDefDTO> deleteProduct(Integer org, Integer product) {

        return productDefDataService.deleteProductDef(new ProductId(org,product))
                .map(productDefOptional -> {
                    if(productDefOptional.isPresent()){
                        return productDefOptional.get();
                    }
                    throw new NotFoundException(" Invalid Product for Org" + org + " and Product " + product);
                })
                .map(productDef -> convertProductToDto(productDef));

    }

    @Override
    public Flux<ProductDefDTO> findAllProducts() {

        return productDefDataService.fetchAllProducts()
                .map(productDef -> convertProductToDto(productDef));

    }

    private void deleteLimitPercent(ProductDef productDef, LimitPercentDTO limitPercentDTO) {

        productDef.getLimitPercents().remove(Util.getBalanceTypes(limitPercentDTO.getBalanceTypes()));

    }

    private void updateLimitPercent(ProductDef productDef, LimitPercentDTO limitPercentDTO) {
        Long percent = productDef.getLimitPercents()
                .put(Util.getBalanceTypes(limitPercentDTO.getBalanceTypes()),limitPercentDTO.getPercent());
    }
}
