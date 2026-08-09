package com.novaerp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import java.util.LinkedHashMap;

/**
 * Shared test utilities: produces a pre-configured MockMvcBuilders setup
 * with an ObjectMapper that can serialize Spring Data Page<T> correctly.
 */
public final class TestMvcConfig {

    private TestMvcConfig() {}

    /**
     * Returns an ObjectMapper configured with JavaTimeModule and Page serialization support.
     */
    public static ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    /**
     * Wraps a controller in a StandaloneMockMvcBuilder with all required converters.
     */
    public static StandaloneMockMvcBuilder standaloneSetup(Object controller, ObjectMapper om) {
        return MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(om));
    }
}
