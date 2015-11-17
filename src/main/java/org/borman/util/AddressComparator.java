package org.borman.util;

import org.borman.model.Address;

import java.util.Comparator;

public class AddressComparator implements Comparator<Address> {
    AlphanumComparator alphanumComparator = new AlphanumComparator();

    @Override
    public int compare(Address o1, Address o2) {
        return compareState(o1, o2);
    }

    private int compareState(Address o1, Address o2) {
        final int stateCompare = o1.getState().compareTo(o2.getState());
        if (stateCompare == 0) {
            return compareCity(o1, o2);
        }
        return stateCompare;
    }

    private int compareCity(Address o1, Address o2) {
        final int cityCompare = o1.getCity().compareTo(o2.getCity());
        if (cityCompare == 0) {
            return compareZip(o1, o2);
        }
        return cityCompare;
    }

    private int compareZip(Address o1, Address o2) {
        final int zipCompare = o1.getZip().compareTo(o2.getZip());
        if (zipCompare == 0) {
            return compareStreet(o1, o2);
        }
        return zipCompare;
    }

    private int compareStreet(Address o1, Address o2) {
        final int streetCompare = alphanumComparator.compare(o1.getStreet(), o2.getStreet());
        if (streetCompare == 0) {
            return compareStreet2(o1, o2);
        }
        return streetCompare;
    }

    private int compareStreet2(Address o1, Address o2) {
        return alphanumComparator.compare(o1.getStreet2(), o2.getStreet2());
    }
}
