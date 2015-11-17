package org.borman.repo;

import com.mysema.query.types.expr.BooleanExpression;
import org.borman.model.Address;
import org.borman.model.QAddress;
import org.borman.repo.util.DataSetLocation;
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.io.InvalidObjectException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataSetLocation("classpath:/datasets/address-dataset.xml")
public class AddressRepositoryTest extends EntityRepositoryTestBase<Address, AddressRepository> {
    private final String nj = "NJ";
    private final String gotham = "GOTHAM";
    private final String de = "DE";
    private final String metropolis = "METROPOLIS";

    final BooleanExpression inNj = QAddress.address.state.toUpperCase().eq(nj.trim().toUpperCase());
    final BooleanExpression inGotham = QAddress.address.city.toUpperCase().eq(gotham.trim().toUpperCase());
    final BooleanExpression inDe = QAddress.address.state.toUpperCase().eq(de.trim().toUpperCase());
    final BooleanExpression inMetropolis = QAddress.address.city.toUpperCase().eq(metropolis.trim().toUpperCase());

    final BooleanExpression isActive = QAddress.address.active.isTrue();
    final BooleanExpression isInactive = QAddress.address.active.isFalse();

    @Override
    protected Long getItemId(Address item) {
        return item.getId();
    }

    @Override
    protected Address buildNewItem() throws InvalidObjectException {
        return new Address("1007 Mountain Drive", gotham, nj, 54321, true);
    }

    @Override
    protected int expectedItemsForFindAll() {
        return 4;
    }

    @Test
    public void testShouldGetAllOrderedActiveAddresses() {
        final List<Address> allActiveOrdered = repository.findActiveAddresses();

        assertThat(allActiveOrdered).hasSize(2);
        List<Long> ids = new ArrayList<Long>();
        for (Address address : allActiveOrdered) {
            ids.add(address.getId());
        }
        assertThat(ids).contains(-1L, -4L);
        assertThat(allActiveOrdered.get(0).getId()).isEqualTo(-4);
        assertThat(allActiveOrdered.get(1).getId()).isEqualTo(-1);
    }

    @Test
    public void testShouldGetPagedOrderedActiveAddresses() {
        final PageRequest pageable = new PageRequest(0, 1);

        final Page<Address> addressPage = repository.findActiveAddresses(pageable);

        assertThat(addressPage.getContent()).hasSize(1);
        assertThat(addressPage.getContent().get(0).getId()).isEqualTo(-4L);
        assertThat(addressPage.getTotalPages()).isEqualTo(2);
        assertThat(addressPage.getTotalElements()).isEqualTo(2);
        assertThat(addressPage.getSize()).isEqualTo(1);
        assertThat(addressPage.getNumber()).isEqualTo(0);
        assertThat(addressPage.isFirst()).isTrue();
        assertThat(addressPage.isLast()).isFalse();
    }

    @Test
    public void testShouldAllOrderedAddresses() {
        final List<Address> allOrdered = repository.findAllAddresses();

        assertThat(allOrdered).hasSize(expectedItemsForFindAll());
        assertThat(allOrdered.get(0).getId()).isEqualTo(-3);
        assertThat(allOrdered.get(1).getId()).isEqualTo(-4);
        assertThat(allOrdered.get(2).getId()).isEqualTo(-2);
        assertThat(allOrdered.get(3).getId()).isEqualTo(-1);
    }

    @Test
    public void testShouldPagedOrderedAddresses() {
        final int pageSize = 1;
        final PageRequest pageable = new PageRequest(0, pageSize);

        final Page<Address> addressPage = repository.findAllAddresses(pageable);

        assertThat(addressPage.getContent()).hasSize(1);
        assertThat(addressPage.getContent().get(0).getId()).isEqualTo(-3L);
        assertThat(addressPage.getTotalPages()).isEqualTo((int) (expectedItemsForFindAll() / pageSize));
        assertThat(addressPage.getTotalElements()).isEqualTo(expectedItemsForFindAll());
        assertThat(addressPage.getSize()).isEqualTo(1);
        assertThat(addressPage.getNumber()).isEqualTo(0);
        assertThat(addressPage.isFirst()).isTrue();
        assertThat(addressPage.isLast()).isFalse();
    }

    @Test
    public void testShouldReturnNullForUnsavedAddress() {
        final Integer existingAddressCount = repository.countExistingMatchingAddress("1qaz", "2wsx", "3edc", "gr", 00000);

        assertThat(existingAddressCount).isNull();
    }

    @Test
    public void testShouldReturnOneForSavedAddress() {
        final Integer existingAddressCount = repository.countExistingMatchingAddress("1 STREET RD", "SUITE 1", metropolis, de, 12345);

        assertThat(existingAddressCount).isNotNull().isEqualTo(1);
    }

    @Test
    public void testShouldFailToSaveExistingAddress() {
        final Address address = new Address("1 STREET RD", "SUITE 1", metropolis, de, 12345,
                false);

        boolean constraintViolationThrown = resaveExistingAddress(address);

        assertThat(constraintViolationThrown).isTrue();
    }

    @Test
    public void testShouldGetAddressByCityAndState() {
        final List<Address> addressesByCityAndState = repository.findAddressesByCityAndState(" GOTHAM    ", "  NJ  ");

        assertThat(addressesByCityAndState).hasSize(2);
    }

    @Test
    public void testShouldGetAddressByCityAndStateUsingExpression() {
        final Iterable addressesByCityAndState = repository.findAll(inNj.and(inGotham));

        assertThat(addressesByCityAndState).hasSize(2);
    }

    @Test
    public void testShouldGetActiveAddressByCityAndState() {
        final BooleanExpression activeGothamAddresses = inNj.and(inGotham).and(isActive);

        final Iterable addressesByCityAndState = repository.findAll(activeGothamAddresses);

        assertThat(addressesByCityAndState).hasSize(1);
    }

    @Test
    public void testShouldGetActiveAddressByCityAndStates() {
        final BooleanExpression inactiveMetropolisAddress = inDe.and(inMetropolis).and(isInactive);
        final BooleanExpression activeGothamAddresses = inNj.and(inGotham).and(isActive);

        final Iterable addressesByCityAndState = repository.findAll(activeGothamAddresses.or(inactiveMetropolisAddress));

        assertThat(addressesByCityAndState).hasSize(2);
    }

    @Test
    public void testShouldChangeZipCodeOfCity() {
        final long changedAddresses = repository.changeZipCodeOfCity(gotham, nj, 11235);

        assertThat(changedAddresses).isEqualTo(2);
        final List<Address> allGotham = (List<Address>) repository.findAll(inGotham);
        assertThat(allGotham).hasSize(2);
        allGotham.forEach(a -> assertThat(a.getZip()).isEqualTo(11235));

    }

    private boolean resaveExistingAddress(Address address) {
        try {
            repository.save(address);
            return false;
        } catch (Exception e) {
            return lookForConstraintViolation(e);
        }
    }

    private boolean lookForConstraintViolation(Throwable e) {
        final Throwable cause = e.getCause();
        if (cause != null) {
            if (SQLException.class.isInstance(cause)) {
                assertThat(((SQLException) cause).getMessage()).isEqualTo(
                        "The statement was aborted because it would have caused a duplicate key value in a unique or primary key constraint or unique index identified by 'UK_ADDRESS' defined on 'ADDRESS'.");
                return true;
            }
            return lookForConstraintViolation(cause);
        }
        return false;
    }
}
