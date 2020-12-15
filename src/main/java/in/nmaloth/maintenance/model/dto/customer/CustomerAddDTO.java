package in.nmaloth.maintenance.model.dto.customer;

import in.nmaloth.entity.customer.AddressType;
import in.nmaloth.entity.customer.CustomerIDType;
import in.nmaloth.entity.customer.CustomerType;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAddDTO {

    private String customerNumber;

    @NotNull
    @Pattern(regexp = "H|O|P|C|S|1|2|3|4" )
    private String addressType;

    @NotNull
    @Pattern(regexp = "O|C" )
    private String customerType;

    @NotNull
    @Length(min = 3,max = 40)
    private String customerName;

    @NotNull
    private String addressLine1;
    private String addressLine2;

    @NotNull
    private String postalCode;

    private String state;
    @NotNull
    private String countryCode;


    @Valid
    private List<CustomerIDDTO> customerIDDTOList;

    // To Support Multiple Addresses during Authorization

    private String primaryPhoneNumber;

    private String primaryEmail;

}
