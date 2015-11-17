package org.borman.dto.request;

import org.borman.model.Address;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "request")
public class AddressRequest {
    @NotEmpty(message = "Addresses must be provided for this request.")
    @Valid
    protected List<Address> addresses = new ArrayList<Address>();

    protected AddressRequest() {
    }

    public AddressRequest(List<Address> addresses) {
        this.addresses = addresses;
    }

    @XmlElement(name = "address")
    public List<Address> getAddresses() {
        return addresses;
    }

    protected void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }
}
