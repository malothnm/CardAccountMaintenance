package in.nmaloth.maintenance.model.dto.customer;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CustomerDTO {

    private String customerNumber;
    private String addressType;
    private String customerType;

    private String customerName;

    private String addressLine1;
    private String addressLine2;

    private String postalCode;
    private String state;
    private String countryCode;

    private List<CustomerIDDTO> customerIDDTOList;

    private String primaryPhoneNumber;

    private String primaryEmail;
}
