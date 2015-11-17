package org.borman.dto.response;

import org.borman.model.Address;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "addressresponse")
public class AddressResponse {
    protected Address address = null;
    protected String message = "";
    protected boolean error = false;

    protected AddressResponse() {
    }

    public AddressResponse(Address address) {
        this.address = address;
    }

    public AddressResponse(String message) {
        this.message = message;
    }

    public AddressResponse(String message, boolean error) {
        this(message);
        this.error = error;
    }

    public AddressResponse(Address address, String message) {
        this(address);
        this.message = message;
    }

    public AddressResponse(Address address, String message, boolean error) {
        this(message, error);
        this.address = address;
    }

    @XmlElement
    public Address getAddress() {
        return address;
    }

    @XmlElement
    public String getMessage() {
        return message;
    }

    @XmlElement
    public boolean isError() {
        return error;
    }

    protected void setAddress(Address address) {
        this.address = address;
    }

    protected void setError(boolean error) {
        this.error = error;
    }

    protected void setMessage(String message) {
        this.message = message;
    }
}
