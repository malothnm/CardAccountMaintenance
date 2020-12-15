package in.nmaloth.maintenance.service.product;

import in.nmaloth.entity.card.PeriodicCardAmount;
import in.nmaloth.entity.card.PeriodicType;
import in.nmaloth.entity.product.ProductId;
import in.nmaloth.entity.product.ProductLimitsDef;
import in.nmaloth.maintenance.dataService.product.ProductLimitsDataService;
import in.nmaloth.maintenance.exception.NotFoundException;
import in.nmaloth.maintenance.model.dto.product.PeriodicLimitDTO;
import in.nmaloth.maintenance.model.dto.product.ProductLimitDefDTO;
import in.nmaloth.maintenance.model.dto.product.ProductLimitDefUpdateDTO;
import in.nmaloth.maintenance.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Not;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductLimitServiceImpl implements ProductLimitService {

    private final ProductLimitsDataService productLimitsDataService;

    public ProductLimitServiceImpl(ProductLimitsDataService productLimitsDataService) {
        this.productLimitsDataService = productLimitsDataService;
    }


    @Override
    public Mono<ProductLimitDefDTO> createNewProductLimitsDef(ProductLimitDefDTO productLimitDefDTO) {


        return productLimitsDataService.saveProductLimitsDef(convertDTOToProductLimitsDef(productLimitDefDTO))
                .map(productLimitsDef -> convertToDTO(productLimitsDef))

                ;
    }

    @Override
    public Mono<ProductLimitDefDTO> fetchProductInfo(Integer org, Integer product) {
        return productLimitsDataService.fetchProductLimitsDef(new ProductId(org,product))
                .map(productLimitsDefOptional -> {

                    if(productLimitsDefOptional.isPresent()){
                        return productLimitsDefOptional.get();
                    }
                    throw new NotFoundException("Invalid Org " + org +  " And Product" + product);
                })
                .map(productLimitsDef -> convertToDTO(productLimitsDef))

                ;
    }

    @Override
    public Mono<Optional<ProductLimitsDef>> fetchProductOptionalInfo(Integer org, Integer product) {
        return productLimitsDataService.fetchProductLimitsDef(new ProductId(org,product));
    }


    @Override
    public Mono<ProductLimitDefDTO> updateProductDef(ProductLimitDefUpdateDTO productLimitDefUpdateDTO) {
        return productLimitsDataService.fetchProductLimitsDef(new ProductId(productLimitDefUpdateDTO.getOrg(),productLimitDefUpdateDTO.getProduct()))
                .map(productLimitsDefOptional -> {

                    if(productLimitsDefOptional.isPresent()){
                        return productLimitsDefOptional.get();
                    }
                    throw new NotFoundException("Invalid Org " + productLimitDefUpdateDTO.getOrg() + " And Product" + productLimitDefUpdateDTO.getProduct());
                })
                .map(productLimitsDef -> updateProductLimitDef(productLimitDefUpdateDTO,productLimitsDef))
                .flatMap(productLimitsDef -> productLimitsDataService.saveProductLimitsDef(productLimitsDef))
                .map(productLimitsDef -> convertToDTO(productLimitsDef))

                ;
    }



    @Override
    public Mono<ProductLimitDefDTO> deleteProduct(Integer org, Integer product) {
        return productLimitsDataService.deleteProductLimitsDef(new ProductId(org,product))
                .map(productLimitsDefOptional -> {

                    if(productLimitsDefOptional.isPresent()){
                        return productLimitsDefOptional.get();
                    }
                    throw new NotFoundException("Invalid Org " + org + " product "+ product);
                })
                .map(productLimitsDef -> convertToDTO(productLimitsDef))
                ;
    }

    @Override
    public Flux<ProductLimitDefDTO> findAllProducts() {
        return productLimitsDataService.fetchAllProductLimits()
                .map(productLimitsDef -> convertToDTO(productLimitsDef))
                ;
    }

    @Override
    public ProductLimitsDef convertDTOToProductLimitsDef(ProductLimitDefDTO productLimitDefDTO) {

        Set<PeriodicType> periodicTypeSet = new HashSet<>();


        Map<PeriodicType, List<PeriodicCardAmount>> periodicCardAmountMap = new HashMap<>();

        productLimitDefDTO.getPeriodicLimitDTOList()
                .forEach(periodicLimitDTO -> periodicTypeSet.add(Util.getPeriodicType(periodicLimitDTO.getPeriodicType())));

        periodicTypeSet
                .forEach(periodicType -> updatePeriodicLimitList(productLimitDefDTO,periodicType,periodicCardAmountMap));




        return ProductLimitsDef.builder()
                .productId(new ProductId(productLimitDefDTO.getOrg(), productLimitDefDTO.getProduct()))
                .cardLimitMap(periodicCardAmountMap)
                .build();

    }

    private void updatePeriodicLimitList(ProductLimitDefDTO productLimitDefDTO, PeriodicType periodicType,
                                         Map<PeriodicType, List<PeriodicCardAmount>> periodicCardAmountMap) {

        List<PeriodicCardAmount> periodicCardAmountList = productLimitDefDTO.getPeriodicLimitDTOList()
                .stream()
                .filter(periodicLimitDTO -> Util.getPeriodicType(periodicLimitDTO.getPeriodicType()).equals(periodicType))
                .map(periodicLimitDTO -> convertDTOtoPeriodicLimit(periodicLimitDTO))
                .collect(Collectors.toList());

        periodicCardAmountMap.put(periodicType,periodicCardAmountList);

    }

    private PeriodicCardAmount convertDTOtoPeriodicLimit(PeriodicLimitDTO periodicLimitDTO) {

       return PeriodicCardAmount.builder()
                .limitType(Util.getLimitType(periodicLimitDTO.getLimitType()))
                .transactionAmount(periodicLimitDTO.getLimitAmount())
                .transactionNumber(periodicLimitDTO.getLimitNumber())
                .build();
    }

    @Override
    public ProductLimitDefDTO convertToDTO(ProductLimitsDef productLimitsDef) {

        List<PeriodicLimitDTO> periodicLimitDTOList = productLimitsDef.getCardLimitMap().entrySet()
                .stream()
                .flatMap(periodicTypeListEntry -> convertPeriodicLimitToDTO(periodicTypeListEntry).stream())
                .collect(Collectors.toList());


        return ProductLimitDefDTO.builder()
                .org(productLimitsDef.getProductId().getOrg())
                .product(productLimitsDef.getProductId().getProduct())
                .periodicLimitDTOList(periodicLimitDTOList)
                .build();

    }


    private List<PeriodicLimitDTO> convertPeriodicLimitToDTO(Map.Entry<PeriodicType, List<PeriodicCardAmount>>
                                                                     periodicTypeListEntry){
        String periodicType = Util.getPeriodicType(periodicTypeListEntry.getKey());

        return periodicTypeListEntry.getValue()
                .stream()
                .map(periodicCardAmount -> PeriodicLimitDTO.builder()
                        .periodicType(periodicType)
                        .limitAmount(periodicCardAmount.getTransactionAmount())
                        .limitNumber(periodicCardAmount.getTransactionNumber())
                        .limitType(Util.getLimitType(periodicCardAmount.getLimitType()))
                        .build()
                ).collect(Collectors.toList());
    }

    @Override
    public ProductLimitsDef updateProductLimitDef(ProductLimitDefUpdateDTO productLimitDefUpdateDTO, ProductLimitsDef productLimitsDef) {

        if(productLimitDefUpdateDTO.getPeriodicLimitDTOListAdd() != null){
            productLimitDefUpdateDTO.getPeriodicLimitDTOListAdd()
                    .forEach(periodicLimitDTO -> updatePeriodicLimitDef(productLimitsDef,periodicLimitDTO));
        }

        if(productLimitDefUpdateDTO.getPeriodicLimitDTOListDelete() != null){
            productLimitDefUpdateDTO.getPeriodicLimitDTOListDelete()
                    .forEach(periodicLimitDTO -> deletePeriodicLimitDef(productLimitsDef,periodicLimitDTO));
        }




        return productLimitsDef;
    }

    private void deletePeriodicLimitDef(ProductLimitsDef productLimitsDef, PeriodicLimitDTO periodicLimitDTO) {

        List<PeriodicCardAmount> periodicCardAmountList = productLimitsDef.getCardLimitMap().get(Util.getPeriodicType(periodicLimitDTO.getPeriodicType()));

        if(periodicCardAmountList != null){

            Optional<PeriodicCardAmount> periodicCardAmountOptional = periodicCardAmountList.stream()
                    .filter(periodicCardAmount -> periodicCardAmount.getLimitType().equals(Util.getLimitType(periodicLimitDTO.getLimitType())))
                    .findFirst();

            if(periodicCardAmountOptional.isPresent()){
                periodicCardAmountList.remove(periodicCardAmountOptional.get());
            }

        }

    }

    private void updatePeriodicLimitDef(ProductLimitsDef productLimitsDef, PeriodicLimitDTO periodicLimitDTO) {

        List<PeriodicCardAmount> periodicCardAmountList = productLimitsDef.getCardLimitMap().get(Util.getPeriodicType(periodicLimitDTO.getPeriodicType()));

        if(periodicCardAmountList == null){
            PeriodicCardAmount periodicCardAmount = PeriodicCardAmount.builder()
                    .transactionNumber(periodicLimitDTO.getLimitNumber())
                    .transactionAmount(periodicLimitDTO.getLimitAmount())
                    .limitType(Util.getLimitType(periodicLimitDTO.getLimitType()))
                    .build();

            periodicCardAmountList = new ArrayList<>();
            periodicCardAmountList.add(periodicCardAmount);

            productLimitsDef.getCardLimitMap().put(Util.getPeriodicType(periodicLimitDTO.getPeriodicType()),periodicCardAmountList);
        } else {

            Optional<PeriodicCardAmount> periodicCardAmountOptional = periodicCardAmountList.stream()
                    .filter(periodicCardAmount -> periodicCardAmount.getLimitType().equals(Util.getLimitType(periodicLimitDTO.getLimitType())))
                    .findFirst();

            if(periodicCardAmountOptional.isPresent()){

                PeriodicCardAmount periodicCardAmount = periodicCardAmountOptional.get();
                periodicCardAmount.setTransactionAmount(periodicLimitDTO.getLimitAmount());
                periodicCardAmount.setTransactionNumber(periodicLimitDTO.getLimitNumber());
            } else {
                periodicCardAmountList.add(PeriodicCardAmount.builder()
                        .limitType(Util.getLimitType(periodicLimitDTO.getLimitType()))
                        .transactionAmount(periodicLimitDTO.getLimitAmount())
                        .transactionNumber(periodicLimitDTO.getLimitNumber())
                        .build()
                );
            }
        }
    }

}
