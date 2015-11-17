package org.borman.repo.util;

import java.lang.annotation.*;

/**
 * To be used on test classes to set the location of a dataset.
 * <p>
 * DbUnit inserts and updates rows in the order they are found in your dataset; deletes on the other
 * hand are done in reverse order. You must therefore order your tables and rows appropriately in
 * your datasets to prevent foreign keys constraint violation.
 * <p>
 * If you follow the ordering of empty-dataset.xml you will be fine.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
public @interface DataSetLocation {
    /**
     * The location of the dataset to be used.
     *
     * @return
     */
    String value();

    /**
     * Optional parameter specifying the bean name of the data source to be used with this dataset.
     * <p>
     * This parameter does not need to be specified if there is only one data source defined in the
     * application context. In this case the only existing data source will be used. In the event
     * there are multiple data sources the data source specified here will be used or it will
     * default to the bean named 'dataSource' if not provided.
     *
     * @return
     */
    String dataSourceBeanName() default "dataSource";
}