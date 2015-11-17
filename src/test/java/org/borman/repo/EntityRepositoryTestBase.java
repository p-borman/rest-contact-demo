package org.borman.repo;

import org.borman.repo.util.BaseSpringDbIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.InvalidObjectException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class EntityRepositoryTestBase<T, TD extends JpaRepository<T, Long>> extends BaseSpringDbIntegrationTest<T> {

    @Autowired
    protected TD repository;

    @Test
    public void testShouldSaveAndDeleteNewItem() throws InvalidObjectException {
        final T entity = buildNewItem();
        T item = repository.save(entity);
        final int expectedItems = expectedItemsForFindAll();
        assertThat(repository.count()).isEqualTo(expectedItems + 1);

        repository.delete(getItemId(item));

        assertThat(repository.count()).isEqualTo(expectedItems);
    }

    protected abstract Long getItemId(T item);

    protected abstract T buildNewItem() throws InvalidObjectException;

    protected abstract int expectedItemsForFindAll();

    @Test
    public void testShouldGetAllItemsFromRepository() {
        final List<T> items = repository.findAll();

        assertThat(items).hasSize(expectedItemsForFindAll());
        items.forEach(this::assertEntityIsValid);
    }
}
