package in.nmaloth.maintenance.model.dto.customer;

import in.nmaloth.entity.customer.CustomerType;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CustomerUpdateDTO {

    @NotNull
    private String customerId;

    @Pattern(regexp = "H|O|P|C|S|1|2|3|4" )
    private String addressType;

    @Pattern(regexp = "O|C" )
    private String customerType;

    @Length(min = 3,max = 40)
    private String customerName;

    private String addressLine1;
    private String addressLine2;

    private String postalCode;

    private String state;

    private String countryCode;


    @Valid
    private List<CustomerIDDTO> customerIDDTOListAdd;
    @Valid
    private List<CustomerIDDTO> customerIDDTOListDelete;


    // To Support Multiple Addresses during Authorization

    private String primaryPhoneNumber;

    private String primaryEmail;
}
