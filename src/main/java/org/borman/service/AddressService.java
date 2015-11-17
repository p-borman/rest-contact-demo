package org.borman.service;

import org.borman.dto.page.SerializableAddressPage;
import org.borman.model.Address;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AddressService {
    SerializableAddressPage getAllActiveAddresses(PageRequest pageRequest);

    List<Address> getAllActiveAddresses();

    SerializableAddressPage getAllOrderedAddresses(Pageable pageable);

    List<Address> getAllOrderedAddresses();

    Address getAddressById(long addressId);

    void deleteAddress(long addressId);

    Address saveNewAddress(Address address);

    List<Address> saveNewAddresses(Iterable<Address> addresses);
}
