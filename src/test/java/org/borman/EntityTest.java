package org.borman;


import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class EntityTest<T> {

    protected static Validator validator;
    protected static Logger LOG = LoggerFactory.getLogger("org.borman");

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    protected final void assertEntityIsInvalid(final T entity, final String... expectedErrors) {
        final List<String> errors = getErrors(entity);
        final List<String> expectedErrorsList = Arrays.asList(expectedErrors);
        assertThat(expectedErrorsList).isNotNull().isNotEmpty().overridingErrorMessage("No errors were expected for an invalid object.");
        assertThat(errors).hasSize(expectedErrorsList.size()).overridingErrorMessage("The correct number of expected errors were not returned.");

        errors.forEach(e -> assertThat(expectedErrorsList.contains(e)).isTrue());
        expectedErrorsList.forEach(e -> assertThat(errors.contains(e)).isTrue());
        LOG.error(printErrors(errors));
    }

    protected final void assertEntityIsValid(final T entity) {
        final List<String> errors = getErrors(entity);
        assertThat(errors).hasSize(0).overridingErrorMessage(printErrors(errors));
    }

    private String printErrors(final List<String> errors) {
        final StringBuilder stringBuilder = new StringBuilder();

        errors.forEach(e -> {
            stringBuilder.append(e);
            stringBuilder.append("\n");
        });

        return stringBuilder.toString();
    }

    private List<String> getErrors(final T entity) {
        final Set<ConstraintViolation<T>> constraintViolations = validator.validate(entity);
        List<String> errors = new ArrayList<String>();

        constraintViolations.forEach(v -> errors.add(v.getMessage()));

        return errors;
    }

}