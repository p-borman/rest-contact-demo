package org.borman.repo.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.borman.EntityTest;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@ContextConfiguration(classes = {DbTestConfig.class})
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, CleanInsertTestExecutionListener.class})
@DataSetLocation("classpath:/datasets/empty-dataset.xml")
@ActiveProfiles("db-unit-test")
public abstract class BaseSpringDbIntegrationTest<T> extends EntityTest<T> {
    @BeforeClass
    public static void classSetup() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);
    }
}
