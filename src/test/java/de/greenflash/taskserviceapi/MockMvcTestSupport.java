package de.greenflash.taskserviceapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.SpringDataJacksonConfiguration;
import org.springframework.data.web.config.SpringDataWebSettings;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

final class MockMvcTestSupport {

    private MockMvcTestSupport() {
    }

    static StandaloneMockMvcBuilder configureStandalone(StandaloneMockMvcBuilder builder) {
        SpringDataWebSettings springDataWebSettings =
                new SpringDataWebSettings(EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO);
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(new SpringDataJacksonConfiguration.PageModule(springDataWebSettings));
        MappingJackson2HttpMessageConverter messageConverter =
                new MappingJackson2HttpMessageConverter(objectMapper);

        return builder.setMessageConverters(messageConverter);
    }

    static DefaultMockMvcBuilder configureWebApp(DefaultMockMvcBuilder builder) {
        return builder;
    }

    
}
