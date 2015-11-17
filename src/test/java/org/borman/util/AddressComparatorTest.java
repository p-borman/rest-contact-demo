package org.borman.util;

import org.borman.model.Address;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AddressComparatorTest {
    private final AddressComparator addressComparator = new AddressComparator();
    Address address1 = new Address("2 test st", "test city", "NY", 12345, true);
    Address address2 = new Address("11 test st", "test city", "NY", 12345, true);
    Address address3 = new Address("2 test st", "test city", "NY", 12346, true);
    Address address4 = new Address("2 test st", "different city", "NY", 12345, true);

    @Test
    public void testShouldCompareAddressesOnSameStreet() {
        int compare = addressComparator.compare(address1, address2);

        assertThat(compare).isLessThan(0);
    }

    @Test
    public void testShouldCompareAddresses() {
        int compare = addressComparator.compare(address2, address1);

        assertThat(compare).isGreaterThan(0);
    }

    @Test
    public void testShouldCompareSameAddress() {
        int compare = addressComparator.compare(address1, address1);

        assertThat(compare).isEqualTo(0);
    }

    @Test
    public void testShouldCompareAddressesInDifferentZips() {
        int compare = addressComparator.compare(address1, address3);

        assertThat(compare).isLessThan(0);
    }

    @Test
    public void testShouldCompareAddressesInDifferentCitys() {
        int compare = addressComparator.compare(address1, address4);

        assertThat(compare).isGreaterThan(0);
    }
}