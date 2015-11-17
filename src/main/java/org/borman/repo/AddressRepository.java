package org.borman.repo;

import org.borman.model.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Transactional(readOnly = true)
public interface AddressRepository extends JpaRepository<Address, Long>, AddressRepositoryCustom, QueryDslPredicateExecutor {
    Page<Address> findActiveAddresses(Pageable pageable);

    List<Address> findActiveAddresses();

    Page<Address> findAllAddresses(Pageable pageable);

    List<Address> findAllAddresses();

    Integer countExistingMatchingAddress(@Param("street") String street,
                                         @Param("street2") String street2,
                                         @Param("city") String city,
                                         @Param("state") String state,
                                         @Param("zip") int zip);
}
