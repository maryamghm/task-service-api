package de.greenflash.taskserviceapi;

import de.greenflash.taskserviceapi.exception.GlobalExceptionHandler;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

public abstract class AbstractMockMvcUnitTest {

    protected MockMvc buildStandaloneMockMvc(Object... controllers) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        return MockMvcTestSupport.configureStandalone(MockMvcBuilders.standaloneSetup(controllers))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }
}
