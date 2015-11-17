package org.borman.repo;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.jpa.impl.JPAUpdateClause;
import com.mysema.query.types.expr.BooleanExpression;
import org.borman.model.Address;
import org.borman.model.QAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

public class AddressRepositoryImpl implements AddressRepositoryCustom {
    @Autowired
    EntityManager entityManager;
    private QAddress address = QAddress.address;

    @Override
    public List<Address> findAddressesByCityAndState(String city, String stateCode) {
        final BooleanExpression cityMatches = cityMatches(city);
        final BooleanExpression stateMatches = stateMatches(stateCode);
        return getJpaQuery().from(address).where(cityMatches.and(stateMatches)).list(address);
    }


    @Override
    @Transactional(readOnly = false)
    public long changeZipCodeOfCity(String cityName, String stateCode, int newZipCode) {
        return getJpaUpdateClause()
                .set(address.zip, newZipCode)
                .where(cityMatches(cityName).and(stateMatches(stateCode)))
                .execute();
    }

    private JPAUpdateClause getJpaUpdateClause() {
        return new JPAUpdateClause(entityManager, address);
    }

    public BooleanExpression stateMatches(String stateCode) {
        return address.state.toUpperCase().eq(stateCode.trim().toUpperCase());
    }

    public BooleanExpression cityMatches(String city) {
        return address.city.toUpperCase().eq(city.trim().toUpperCase());
    }

    private JPAQuery getJpaQuery() {
        return new JPAQuery(entityManager);
    }
}
