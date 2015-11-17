package org.borman.dto.response;

import org.borman.dto.page.SerializableAddressPage;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "addresspage")
public class AddressPageResponse {
    protected SerializableAddressPage addresses = null;
    protected String message = "";
    protected boolean error = false;

    protected AddressPageResponse() {
    }

    public AddressPageResponse(SerializableAddressPage addresses) {
        this.addresses = addresses;
    }

    public AddressPageResponse(String message) {
        this.message = message;
    }

    public AddressPageResponse(String message, boolean error) {
        this(message);
        this.error = error;
    }

    public AddressPageResponse(SerializableAddressPage addresses, String message) {
        this(addresses);
        this.message = message;
    }

    public AddressPageResponse(SerializableAddressPage addresses, String message,
                               boolean error) {
        this(message, error);
        this.addresses = addresses;
    }

    @XmlElement
    public SerializableAddressPage getAddresses() {
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

    protected void setAddresses(SerializableAddressPage addresses) {
        this.addresses = addresses;
    }

    protected void setError(boolean error) {
        this.error = error;
    }

    protected void setMessage(String message) {
        this.message = message;
    }
}
