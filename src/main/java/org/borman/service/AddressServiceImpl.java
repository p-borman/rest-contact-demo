package org.borman.service;

import org.borman.dto.page.SerializableAddressPage;
import org.borman.model.Address;
import org.borman.repo.AddressRepository;
import org.borman.util.AddressComparator;
import org.borman.util.LogWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class AddressServiceImpl extends LogWrapper implements AddressService {

    private AddressRepository addressRepository;

    @Autowired
    public AddressServiceImpl(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public SerializableAddressPage getAllActiveAddresses(PageRequest pageRequest) {
        Page<Address> activeAddresses = addressRepository.findActiveAddresses(pageRequest);
        return sortAddressPage(activeAddresses);
    }

    @Override
    public List<Address> getAllActiveAddresses() {
        return sortAddresses(addressRepository.findActiveAddresses());
    }

    @Override
    public SerializableAddressPage getAllOrderedAddresses(Pageable pageable) {
        Page<Address> allAddresses = addressRepository.findAllAddresses(pageable);
        return sortAddressPage(allAddresses);
    }

    @Override
    public List<Address> getAllOrderedAddresses() {
        return sortAddresses(addressRepository.findAllAddresses());
    }

    @Override
    public Address getAddressById(long addressId) {
        return addressRepository.findOne(addressId);
    }

    @Override
    public void deleteAddress(long addressId) {
        addressRepository.delete(addressId);
    }

    @Override
    public Address saveNewAddress(Address address) {
        verifyAddressNotInDatabaseAlready(address);
        return addressRepository.save(address);
    }

    @Override
    public List<Address> saveNewAddresses(Iterable<Address> addresses) {
        for (Address address : addresses) {
            verifyAddressNotInDatabaseAlready(address);
        }
        return addressRepository.save(addresses);
    }

    private void verifyAddressNotInDatabaseAlready(Address address) {
        final Integer existingAddresses = addressRepository.countExistingMatchingAddress(address.getStreet(), address.getStreet2(), address.getCity(), address.getState(), address.getZip());
        if (existingAddresses != null && existingAddresses > 0) {
            String message = String.format("Address: %s was already in the database", address.toString());
            LOG.warn(String.format("Failed to save address: %s", message));
            throw new RuntimeException(message);
        }
    }

    protected List<Address> sortAddresses(Collection<Address> addressesToSort) {
        final List<Address> addresses = new ArrayList<>(addressesToSort);
        Collections.sort(addresses, new AddressComparator());
        return addresses;
    }

    protected SerializableAddressPage sortAddressPage(Page<Address> activeAddresses) {
        SerializableAddressPage addressPage = new SerializableAddressPage(activeAddresses);
        addressPage.setContent(sortAddresses(addressPage.getContent()));
        return addressPage;
    }

}
