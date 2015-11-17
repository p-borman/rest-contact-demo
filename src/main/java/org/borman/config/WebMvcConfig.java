package org.borman.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import org.borman.dto.page.SerializableAddressPage;
import org.borman.dto.request.AddressRequest;
import org.borman.dto.response.AddressListResponse;
import org.borman.dto.response.AddressPageResponse;
import org.borman.dto.response.AddressResponse;
import org.borman.model.Address;
import org.borman.util.TimestampPrinter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.xml.bind.Marshaller;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
//@ComponentScan(basePackages = "org.borman.)
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = "org.borman.repo")
public class WebMvcConfig extends WebMvcConfigurationSupport {

    @Bean
    public MarshallingHttpMessageConverter marshallingMessageConverter() {
        MarshallingHttpMessageConverter converter = new MarshallingHttpMessageConverter();
        converter.setMarshaller(marshaller());
        converter.setUnmarshaller(marshaller());
        return converter;
    }

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(SerializableAddressPage.class, AddressRequest.class,
                AddressResponse.class, AddressListResponse.class, AddressPageResponse.class,
                Address.class);
        Map<String, Object> props = new HashMap<>();
        props.put(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setMarshallerProperties(props);
        return marshaller;
    }

    @Bean(name = "objectMapper")
    public ObjectMapper objectMapper() {
        Hibernate4Module hibernate4Module = new Hibernate4Module();
        hibernate4Module.configure(Hibernate4Module.Feature.USE_TRANSIENT_ANNOTATION, false);

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE);

        objectMapper.registerModule(hibernate4Module);

        return objectMapper;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(marshallingMessageConverter());
        addDefaultHttpMessageConverters(converters);
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = (MappingJackson2HttpMessageConverter) converter;
                ObjectMapper objectMapper = new ObjectMapper();
                /* support converting hibernate proxy objects to JSON */
                Hibernate4Module hibernate4Module = new Hibernate4Module();
                hibernate4Module
                        .configure(Hibernate4Module.Feature.USE_TRANSIENT_ANNOTATION, false);


                objectMapper
                        .registerModules(hibernate4Module);//,new JodaModule(), new EnumModule());
                mappingJackson2HttpMessageConverter.setObjectMapper(objectMapper);
            }
        }
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception ex) {
            return "localhost";
        }
    }

    @Bean
    public TimestampPrinter timestampPrinter() {
        return new TimestampPrinter();
    }

}
