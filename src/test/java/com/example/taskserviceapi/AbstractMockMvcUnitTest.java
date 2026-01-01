package com.example.taskserviceapi;

import com.example.taskserviceapi.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.SpringDataJacksonConfiguration;
import org.springframework.data.web.config.SpringDataWebSettings;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

public abstract class AbstractMockMvcUnitTest {

    protected MockMvc buildStandaloneMockMvc(Object... controllers) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        return configureStandalone(MockMvcBuilders.standaloneSetup(controllers))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    private StandaloneMockMvcBuilder configureStandalone(StandaloneMockMvcBuilder builder) {
        SpringDataWebSettings springDataWebSettings =
                new SpringDataWebSettings(EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO);
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new SpringDataJacksonConfiguration.PageModule(springDataWebSettings));
        MappingJackson2HttpMessageConverter messageConverter =
                new MappingJackson2HttpMessageConverter(objectMapper);

        return builder.setMessageConverters(messageConverter);
    }
}
