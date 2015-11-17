package org.borman.controller;

import org.borman.dto.page.SerializableAddressPage;
import org.borman.dto.request.AddressRequest;
import org.borman.dto.response.AddressListResponse;
import org.borman.dto.response.AddressPageResponse;
import org.borman.dto.response.AddressResponse;
import org.borman.model.Address;
import org.borman.service.AddressService;
import org.borman.util.LogWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
public class AddressController extends LogWrapper {
    public static final String SUCCESS = "Success!";
    public static final String ALL_ADDRESS_LOOKUP_FAILURE_MESSAGE = "Failed to retrieve all addresses.";
    public static final String ACTIVE_ADDRESS_LOOKUP_FAILURE_MESSAGE = "Failed to retrieve active addresses.";
    public static final String NO_ADDRESSES_FOUND = "No addresses found";

    protected AddressService addressService;

    @Autowired
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    //WEB PAGE REQUESTS
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return "addresses";
    }

    @RequestMapping(value = "/addresses", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String addressesPage() {
        return "addresses";
    }


    //GET ACTIVE ADDRESSES BASED ON TOGGLE
    @RequestMapping(value = "/addresses/{pageNumber}/{pageSize}/{activeOnly}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public AddressPageResponse getAllActiveAddresses(@PathVariable int pageNumber, @PathVariable int pageSize, @PathVariable boolean activeOnly) {
        final PageRequest pageRequest = new PageRequest(pageNumber, pageSize);
        try {
            final SerializableAddressPage addresses = activeOnly ? addressService.getAllActiveAddresses(pageRequest) : addressService.getAllOrderedAddresses(pageRequest);

            if (addresses == null || addresses.getContent() == null || addresses.getContent().isEmpty()) {
                LOG.warn(NO_ADDRESSES_FOUND);
                return new AddressPageResponse(NO_ADDRESSES_FOUND, true);
            }
            return new AddressPageResponse(addresses);
        } catch (Exception e) {
            LOG.error(ACTIVE_ADDRESS_LOOKUP_FAILURE_MESSAGE, e);
            return new AddressPageResponse(ACTIVE_ADDRESS_LOOKUP_FAILURE_MESSAGE, true);
        }
    }

    @RequestMapping(value = "/addresses/{activeOnly}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public AddressListResponse getAllActiveAddresses(@PathVariable boolean activeOnly) {
        try {
            final List<Address> addresses = activeOnly ? addressService.getAllActiveAddresses() : addressService.getAllOrderedAddresses();

            if (addresses == null || addresses.isEmpty()) {
                LOG.warn(NO_ADDRESSES_FOUND);
                return new AddressListResponse(NO_ADDRESSES_FOUND, true);
            }
            return new AddressListResponse(addresses);

        } catch (Exception e) {
            return logAndReturnListError(e, ACTIVE_ADDRESS_LOOKUP_FAILURE_MESSAGE);
        }
    }


    //GET ALL ADDRESSES
    @RequestMapping(value = "/addresses/all/{pageNumber}/{pageSize}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public AddressPageResponse getAllAddresses(@PathVariable int pageNumber, @PathVariable int pageSize) {
        try {
            final SerializableAddressPage addresses = addressService.getAllOrderedAddresses(new PageRequest(pageNumber, pageSize));
            if (addresses == null || addresses.getContent() == null ||
                    addresses.getContent().isEmpty()) {
                LOG.warn(NO_ADDRESSES_FOUND);
                return new AddressPageResponse(NO_ADDRESSES_FOUND, true);
            }
            return new AddressPageResponse(addresses);
        } catch (Exception e) {
            LOG.error(ALL_ADDRESS_LOOKUP_FAILURE_MESSAGE, e);
            return new AddressPageResponse(ALL_ADDRESS_LOOKUP_FAILURE_MESSAGE, true);
        }
    }

    @RequestMapping(value = "/addresses/all", method = RequestMethod.GET, produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public AddressListResponse getAllAddresses() {
        try {
            final List<Address> addresses = addressService.getAllOrderedAddresses();
            if (addresses == null || addresses.isEmpty()) {
                LOG.warn(NO_ADDRESSES_FOUND);
                return new AddressListResponse(NO_ADDRESSES_FOUND, true);
            }
            return new AddressListResponse(addresses);
        } catch (Exception e) {
            return logAndReturnListError(e, ALL_ADDRESS_LOOKUP_FAILURE_MESSAGE);
        }
    }


    //LOOKUP ADDRESS BY ID
    @RequestMapping(value = "/address/{addressId}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public AddressResponse getAddressById(@PathVariable long addressId) {

        try {
            final Address address = addressService.getAddressById(addressId);
            final AddressResponse noAddressWithId = returnErrorIfNoAddressFound(addressId, address);
            if (noAddressWithId != null) return noAddressWithId;
            return new AddressResponse(address);
        } catch (Exception e) {
            return logAndReturnError(e, "Failed to find address: " + addressId);
        }
    }

    private AddressResponse returnErrorIfNoAddressFound(@PathVariable long addressId, Address address) {
        if (address == null) {
            final String noAddressWithId = String.format("No address found for id: %s", addressId);
            LOG.warn(noAddressWithId);
            return new AddressResponse(noAddressWithId, true);
        }
        return null;
    }


    //DELETE ADDRESS BY ID
    @RequestMapping(value = "/address/{addressId}", method = RequestMethod.DELETE, produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public AddressResponse deleteAddress(@PathVariable long addressId) {
        try {
            addressService.deleteAddress(addressId);
            return new AddressResponse(SUCCESS);
        } catch (Exception e) {
            final String message = "Failed to delete address: " + addressId;
            return logAndReturnError(e, message);
        }
    }


    //SAVE NEW ADDRESS
    @RequestMapping(value = "/address", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public AddressResponse saveNewAddress(@Valid @RequestBody Address address, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new AddressResponse(getValidationErrorString(bindingResult), true);
        } else {
            try {
                return new AddressResponse(addressService.saveNewAddress(address));
            } catch (Exception e) {
                String message = String.format("Failed to save address: %s", address.toString());
                return logAndReturnError(e, message);
            }

        }
    }

    @RequestMapping(value = "/addresses", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public AddressListResponse saveNewAddresses(@Valid @RequestBody AddressRequest request, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new AddressListResponse(getValidationErrorString(bindingResult), true);
        } else {
            try {
                return new AddressListResponse(addressService.saveNewAddresses(request.getAddresses()));
            } catch (Exception e) {
                final String message = "Failed to save addresses: " + request.getAddresses().toString();
                return logAndReturnListError(e, message);
            }
        }
    }

    private AddressResponse logAndReturnError(Exception e, String message) {
        LOG.error(message, e);
        return new AddressResponse(message, true);
    }

    private AddressListResponse logAndReturnListError(Exception e, String message) {
        LOG.error(message, e);
        return new AddressListResponse(message, true);
    }

    private String getValidationErrorString(BindingResult bindingResult) {
        StringBuilder stringBuilder = new StringBuilder();
        for (ObjectError objectError : bindingResult.getAllErrors()) {
            stringBuilder.append(objectError.getDefaultMessage()).append('\n');
        }
        return stringBuilder.toString();
    }

}
