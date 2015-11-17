package org.borman.model;


import org.borman.EntityTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AddressTest extends EntityTest<Address> {


    @Test
    public void testShouldBuildAnInvalidAddress() {
        Address address = new Address();

        assertEntityIsInvalid(address, Address.MISSING_ZIP_CODE_ERROR, Address.MISSING_CITY_ERROR, Address.MISSING_STATE_ERROR, Address.MISSING_STREET_ERROR);
    }

    @Test
    public void testShouldBuildAddressThatHasInvalidState() {
        Address address = new Address("1234 fake st", "#42", "New York", "New York", 12345, true);

        assertEntityIsInvalid(address, Address.INVALID_STATE_ERROR);
    }

    @Test
    public void testShouldBuildAddressThatHasInvalidZip_short() {
        Address address = new Address("1234 fake st", "#42", "New York", "NY", 1234, true);

        assertEntityIsInvalid(address, Address.INVALID_ZIP_CODE_ERROR);
    }

    @Test
    public void testShouldBuildAddressThatHasInvalidZip_long() {
        Address address = new Address("1234 fake st", "#42", "New York", "NY", 123456, true);

        assertEntityIsInvalid(address, Address.INVALID_ZIP_CODE_ERROR);
    }

    @Test
    public void testShouldBuildAddress() {
        String street = "1234 fake st";
        String street2 = "#42";
        String city = "New York";
        String state = "NY";
        int zip = 12345;
        boolean active = true;

        Address address = new Address(street, street2, city, state, zip, active);
        address.setId(33L);

        assertEntityIsValid(address);
        assertThat(address.getId()).isNotNull().isEqualTo(33L);
        assertThat(address.getStreet()).isNotNull().isNotEmpty().isEqualTo(street.toUpperCase());
        assertThat(address.getStreet2()).isNotNull().isNotEmpty().isEqualTo(street2.toUpperCase());
        assertThat(address.getState()).isNotNull().isNotEmpty().isEqualTo(state.toUpperCase());
        assertThat(address.getCity()).isNotNull().isNotEmpty().isEqualTo(city.toUpperCase());
        assertThat(address.getZip()).isNotNull().isEqualTo(zip);
        assertThat(address.isActive()).isNotNull().isEqualTo(active);
    }

}