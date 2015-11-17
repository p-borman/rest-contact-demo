package org.borman.repo;

import org.borman.model.Address;

import java.util.List;

public interface AddressRepositoryCustom {
    List<Address> findAddressesByCityAndState(String city, String stateCode);

    long changeZipCodeOfCity(String cityName, String stateCode, int newZipCode);
}
