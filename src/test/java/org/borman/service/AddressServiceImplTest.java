package org.borman.service;


import org.borman.dto.page.SerializableAddressPage;
import org.borman.model.Address;
import org.borman.repo.AddressRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AddressServiceImplTest {

    private final Address address1 = new Address("street1", "street1-2", "city1", "NY", 12341, true);
    private final Address address2 = new Address("street1", "street1-1", "city1", "NY", 12341, true);
    private final Address address3 = new Address("street2", "street2-2", "city2", "CT", 12342, false);
    private final Address address4 = new Address("street2", "street2-1", "city2", "CT", 12342, false);
    private final List<Address> allAddresses = new ArrayList<Address>() {{
        add(address1);
        add(address2);
        add(address3);
        add(address4);
    }};
    private final List<Address> activeAddresses = new ArrayList<Address>() {{
        add(address1);
        add(address2);
    }};
    private final List<Address> inactiveAddresses = new ArrayList<Address>() {{
        add(address3);
        add(address4);
    }};

    @Mock
    private AddressRepository addressRepository;

    private AddressService addressService;

    @Before
    public void setup() {
        address1.setId(1L);
        address2.setId(2L);
        address3.setId(3L);
        address4.setId(4L);
        MockitoAnnotations.initMocks(this);
        addressService = new AddressServiceImpl(addressRepository);
    }

    @After
    public void teardown() {
        verifyNoMoreInteractions(addressRepository);
    }

    @Test
    public void testShouldGetAllActiveAddresses() {
        when(addressRepository.findActiveAddresses()).thenReturn(activeAddresses);

        final List<Address> allActiveAddresses = addressService.getAllActiveAddresses();

        verify(addressRepository, times(1)).findActiveAddresses();
        assertThat(allActiveAddresses).isNotNull().isNotEmpty().hasSize(activeAddresses.size());
        allActiveAddresses.forEach(a -> assertThat(activeAddresses).contains(a));
        assertThat(allActiveAddresses.get(0)).isNotNull().isEqualTo(address2);
        assertThat(allActiveAddresses.get(1)).isNotNull().isEqualTo(address1);
    }

    @Test
    public void testShouldGetAllActiveAddressesPaged() {
        final PageRequest pageRequest = new PageRequest(1, activeAddresses.size());
        when(addressRepository.findActiveAddresses(pageRequest)).thenReturn(new PageImpl<>(activeAddresses));

        final SerializableAddressPage allActiveAddresses = addressService.getAllActiveAddresses(pageRequest);

        verify(addressRepository, times(1)).findActiveAddresses(pageRequest);
        assertThat(allActiveAddresses).isNotNull().isNotEmpty().hasSize(activeAddresses.size());
        allActiveAddresses.forEach(a -> assertThat(activeAddresses).contains(a));
        assertThat(allActiveAddresses.getContent().get(0)).isNotNull().isEqualTo(address2);
        assertThat(allActiveAddresses.getContent().get(1)).isNotNull().isEqualTo(address1);
    }

    @Test
    public void testShouldGetAllAddresses() {
        when(addressRepository.findAllAddresses()).thenReturn(allAddresses);

        final List<Address> allOrderedAddresses = addressService.getAllOrderedAddresses();

        verify(addressRepository, times(1)).findAllAddresses();
        assertThat(allOrderedAddresses).isNotNull().isNotEmpty().hasSize(allAddresses.size());
        allOrderedAddresses.forEach(a -> assertThat(allAddresses).contains(a));
        assertThat(allOrderedAddresses.get(0)).isNotNull().isEqualTo(address4);
        assertThat(allOrderedAddresses.get(1)).isNotNull().isEqualTo(address3);
        assertThat(allOrderedAddresses.get(2)).isNotNull().isEqualTo(address2);
        assertThat(allOrderedAddresses.get(3)).isNotNull().isEqualTo(address1);
    }

    @Test
    public void testShouldGetAllAddressesPaged() {
        final PageRequest pageRequest = new PageRequest(1, allAddresses.size());
        when(addressRepository.findAllAddresses(pageRequest)).thenReturn(new PageImpl<>(allAddresses));

        final SerializableAddressPage allOrderedAddresses = addressService.getAllOrderedAddresses(pageRequest);

        verify(addressRepository, times(1)).findAllAddresses(pageRequest);
        assertThat(allOrderedAddresses).isNotNull().isNotEmpty().hasSize(allAddresses.size());
        allOrderedAddresses.forEach(a -> assertThat(allAddresses).contains(a));
        assertThat(allOrderedAddresses.getContent().get(0)).isNotNull().isEqualTo(address4);
        assertThat(allOrderedAddresses.getContent().get(1)).isNotNull().isEqualTo(address3);
        assertThat(allOrderedAddresses.getContent().get(2)).isNotNull().isEqualTo(address2);
        assertThat(allOrderedAddresses.getContent().get(3)).isNotNull().isEqualTo(address1);
    }

    @Test
    public void testShouldFindAddressById() {
        when(addressRepository.findOne(anyLong())).thenAnswer(invocation -> {
            final Long id = invocation.getArgumentAt(0, Long.class);
            return allAddresses.stream().filter(a -> a.getId().equals(id)).findFirst().get();
        });

        Address addressById = addressService.getAddressById(address3.getId());

        assertThat(addressById).isNotNull().isEqualTo(address3);
        verify(addressRepository, times(1)).findOne(address3.getId());
    }

    @Test
    public void testShouldDeleteAddressById() {
        addressService.deleteAddress(address3.getId());

        verify(addressRepository, times(1)).delete(address3.getId());
    }

    @Test
    public void testShouldSaveAddress() {
        when(addressRepository.countExistingMatchingAddress(anyString(), anyString(), anyString(), anyString(), anyInt())).thenReturn(0);
        final Address newAddress = new Address("new", "new", "new", "new", 98765, true);
        newAddress.setId(999L);
        when(addressRepository.save(any(Address.class))).thenReturn(newAddress);

        Address savedAddress = addressService.saveNewAddress(newAddress);

        verify(addressRepository, times(1)).countExistingMatchingAddress(newAddress.getStreet(), newAddress.getStreet2(), newAddress.getCity(), newAddress.getState(), newAddress.getZip());
        verify(addressRepository, times(1)).save(newAddress);
        assertThat(savedAddress).isNotNull().isEqualTo(newAddress);
    }

    @Test
    public void testShouldNotSaveAddressIfAlreadyInRepo() {
        when(addressRepository.countExistingMatchingAddress(anyString(), anyString(), anyString(), anyString(), anyInt())).thenReturn(1);
        final Address newAddress = new Address("new", "new", "new", "new", 98765, true);
        boolean errorCaught = false;

        try {
            addressService.saveNewAddress(newAddress);
        } catch (Exception e) {
            assertThat(e.getMessage())
                    .isNotNull().isNotEmpty()
                    .isEqualTo(String.format("Address: %s was already in the database", newAddress.toString()));
            errorCaught = true;
        }

        assertThat(errorCaught).isTrue();
        verify(addressRepository, times(1)).countExistingMatchingAddress(newAddress.getStreet(), newAddress.getStreet2(), newAddress.getCity(), newAddress.getState(), newAddress.getZip());
        verify(addressRepository, times(0)).save(newAddress);
    }

    @Test
    public void testShouldSaveAddresses() {
        when(addressRepository.countExistingMatchingAddress(anyString(), anyString(), anyString(), anyString(), anyInt())).thenReturn(0);
        final Address newAddress1 = new Address("new1", "new1", "new1", "new1", 98765, true);
        newAddress1.setId(999L);
        final Address newAddress2 = new Address("new2", "new2", "new2", "new2", 98765, true);
        newAddress1.setId(998L);
        List<Address> newAddresses = Arrays.asList(newAddress1, newAddress2);
        when(addressRepository.save(anyListOf(Address.class))).thenReturn(newAddresses);

        List<Address> savedAddresses = addressService.saveNewAddresses(newAddresses);

        verify(addressRepository, times(1)).countExistingMatchingAddress(newAddress1.getStreet(), newAddress1.getStreet2(), newAddress1.getCity(), newAddress1.getState(), newAddress1.getZip());
        verify(addressRepository, times(1)).countExistingMatchingAddress(newAddress2.getStreet(), newAddress2.getStreet2(), newAddress2.getCity(), newAddress2.getState(), newAddress2.getZip());
        verify(addressRepository, times(1)).save(newAddresses);
        assertThat(savedAddresses).isNotNull().isEqualTo(newAddresses);
    }

    @Test
    public void testShouldNotSaveAddressesIfAlreadyInRepo() {
        when(addressRepository.countExistingMatchingAddress(anyString(), anyString(), anyString(), anyString(), anyInt())).thenReturn(1);
        final Address newAddress1 = new Address("new1", "new1", "new1", "new1", 98765, true);
        final Address newAddress2 = new Address("new2", "new2", "new2", "new2", 98765, true);
        List<Address> newAddresses = Arrays.asList(newAddress1, newAddress2);
        boolean errorCaught = false;

        try {
            addressService.saveNewAddresses(newAddresses);
        } catch (Exception e) {
            assertThat(e.getMessage())
                    .isNotNull().isNotEmpty()
                    .isEqualTo(String.format("Address: %s was already in the database", newAddress1.toString()));
            errorCaught = true;
        }

        assertThat(errorCaught).isTrue();
        verify(addressRepository, times(1)).countExistingMatchingAddress(newAddress1.getStreet(), newAddress1.getStreet2(), newAddress1.getCity(), newAddress1.getState(), newAddress1.getZip());
        verify(addressRepository, times(0)).countExistingMatchingAddress(newAddress2.getStreet(), newAddress2.getStreet2(), newAddress2.getCity(), newAddress2.getState(), newAddress2.getZip());
        verify(addressRepository, times(0)).save(newAddresses);
    }
}