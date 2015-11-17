package org.borman.dto.response;

import org.borman.model.Address;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "addresslist")
public class AddressListResponse {
    protected List<Address> addresses = new ArrayList<Address>();
    protected String message = "";
    protected boolean error = false;

    protected AddressListResponse() {
    }

    public AddressListResponse(List<Address> addresses) {
        this.addresses = addresses;
    }

    public AddressListResponse(String message) {
        this.message = message;
    }

    public AddressListResponse(String message, boolean error) {
        this(message);
        this.error = error;
    }

    public AddressListResponse(List<Address> addresses, String message) {
        this(addresses);
        this.message = message;
    }

    public AddressListResponse(List<Address> addresses, String message, boolean error) {
        this(message, error);
        this.addresses = addresses;
    }

    @XmlElement
    public List<Address> getAddresses() {
        return addresses;
    }

    @XmlElement
    public String getMessage() {
        return message;
    }

    @XmlElement
    public boolean isError() {
        return error;
    }

    protected void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    protected void setError(boolean error) {
        this.error = error;
    }

    protected void setMessage(String message) {
        this.message = message;
    }
}
