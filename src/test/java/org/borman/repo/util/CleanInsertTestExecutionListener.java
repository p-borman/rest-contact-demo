package org.borman.repo.util;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Locale;

/**
 * Extends {@link org.springframework.test.context.TestExecutionListener} and provides the ability to set up and tear down an
 * embedded database and load it with data defined in a dataset whose location is defined using the
 * {@code DataSetLocation} annotation.<br>
 * <b>Note</b> This class is not thread safe
 *
 * @see DataSetLocation
 */
public final class CleanInsertTestExecutionListener implements TestExecutionListener {
    private static final Logger LOG = LoggerFactory
            .getLogger(CleanInsertTestExecutionListener.class);

    private IDatabaseConnection dbConn;
    private ReplacementDataSet replaceDataSet;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.test.context.TestExecutionListener#beforeTestMethod(org.springframework
     * .test.context.TestContext)
     */
    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        // trying to find the DbUnit dataset
        String dataSetResourcePath = null;


        // first, the annotation on the test class
        DataSetLocation dsLocation = testContext.getTestInstance().getClass()
                .getAnnotation(DataSetLocation.class);
        if (dsLocation != null) {
            // found the annotation
            dataSetResourcePath = dsLocation.value();
            LOG.info("annotated test, using data set: {}", dataSetResourcePath);
        } else {
            InputStream inputStream = null;
            try {
                // no annotation, let's try with the name of the test
                String tempDsRes = testContext.getTestInstance().getClass().getName();
                tempDsRes = StringUtils.replace(tempDsRes, ".", "/");
                tempDsRes = "/"
                        + tempDsRes + "-dataset.xml";
                inputStream = getClass().getResourceAsStream(tempDsRes);
                if (inputStream != null) {
                    LOG.info("detected default dataset: {}", tempDsRes);
                    dataSetResourcePath = tempDsRes;
                } else {
                    LOG.info("no default dataset");
                }
            } finally {
                if (null != inputStream) {
                    inputStream.close();
                }
            }
        }

        if (dataSetResourcePath != null) {
            Resource dataSetResource = testContext.getApplicationContext().getResource(
                    dataSetResourcePath);
            IDataSet dataSet = new FlatXmlDataSetBuilder().build(dataSetResource.getInputStream());
            replaceDataSet = new ReplacementDataSet(dataSet);
            replaceDataSet.addReplacementObject("[NULL]", null);
            Calendar cal = Calendar.getInstance(Locale.getDefault());
            replaceDataSet.addReplacementObject("[NOW]", cal.getTime());
            DataSource ds = null;
            try {
                // Try and get unique DataSource by type first
                ds = testContext.getApplicationContext().getBean(DataSource.class);
            } catch (NoSuchBeanDefinitionException noSuchEx) {
                // If that didn't work, try and get one named "dataSource". If that doesn't work
                // it's over: just throw an exception
                try {
                    ds = testContext.getApplicationContext().getBean(
                            dsLocation.dataSourceBeanName(), DataSource.class);
                } catch (NoSuchBeanDefinitionException noSuchEx2) {
                    LOG.error(noSuchEx.getMessage(), noSuchEx2);
                    throw noSuchEx2;
                }
            }
            dbConn = new DatabaseDataSourceConnection(ds);
            dbConn.getConfig().setProperty(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, true);
            DatabaseOperation.CLEAN_INSERT.execute(dbConn, replaceDataSet);
        } else {
            LOG.info("{} does not have any data set, no data injection", testContext.getClass()
                    .getName());
        }
    }

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
    }

    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        if (replaceDataSet != null
                && dbConn != null) {
            DatabaseOperation.DELETE_ALL.execute(dbConn, replaceDataSet);
        }
        if (dbConn != null) {
            dbConn.close();
        }
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
    }
}