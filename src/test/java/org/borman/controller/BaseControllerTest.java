package org.borman.controller;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import org.borman.dto.page.SerializableAddressPage;
import org.borman.dto.request.AddressRequest;
import org.borman.dto.response.AddressListResponse;
import org.borman.dto.response.AddressPageResponse;
import org.borman.dto.response.AddressResponse;
import org.borman.model.Address;
import org.borman.util.LogWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

public abstract class BaseControllerTest {

    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected MockMvc mockMvc;
    private Jaxb2Marshaller marshaller;

    protected void buildMockMvc(LogWrapper controller) throws JAXBException {

        marshaller = marshaller();
        objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE);

        Hibernate4Module hibernate4Module = new Hibernate4Module();
        hibernate4Module.configure(Hibernate4Module.Feature.USE_TRANSIENT_ANNOTATION, false);
        objectMapper.registerModule(hibernate4Module);

        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
        stringConverter.setWriteAcceptCharset(false);

        mockMvc = standaloneSetup(controller).setMessageConverters(stringConverter,
                new AllEncompassingFormHttpMessageConverter(),
                new Jaxb2RootElementHttpMessageConverter(),
                new MappingJackson2HttpMessageConverter(objectMapper),
                marshallingMessageConverter()).build();
    }


    public MarshallingHttpMessageConverter marshallingMessageConverter() {
        MarshallingHttpMessageConverter converter = new MarshallingHttpMessageConverter();
        final Jaxb2Marshaller marshaller = marshaller();
        converter.setMarshaller(marshaller);
        converter.setUnmarshaller(marshaller);
        return converter;
    }

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

    protected <T> String serializeToJson(T model) throws JsonProcessingException {
        ObjectWriter ow = objectMapper.writer();
        return ow.writeValueAsString(model);
    }

    protected <OT> OT deserializeFromJson(String json, TypeReference<OT> typeReference)
            throws IOException {
        return objectMapper.readValue(json, typeReference);
    }

    protected <T> String serializeToXml(T model) throws JAXBException {
        Marshaller marshaller = JAXBContext
                .newInstance(SerializableAddressPage.class, AddressRequest.class,
                        AddressResponse.class, AddressListResponse.class, AddressPageResponse.class,
                        Address.class)
                .createMarshaller();
        StringWriter sw = new StringWriter();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.marshal(model, sw);

        return sw.toString();
    }

    protected <OT> OT deserializeFromXml(String xml, TypeReference<OT> typeReference)
            throws JAXBException {
        Unmarshaller unmarshaller = JAXBContext
                .newInstance(SerializableAddressPage.class, AddressRequest.class,
                        AddressResponse.class, AddressListResponse.class, AddressPageResponse.class,
                        Address.class)
                .createUnmarshaller();

        StringReader reader = new StringReader(xml);
        return (OT) unmarshaller.unmarshal(reader);
    }

    protected ResultActions performMvcAction(MockHttpServletRequestBuilder request,
                                             HttpStatus status) throws Exception {
        final ResultActions resultActions = mockMvc.perform(request);
        resultActions.andExpect(status().is(status.value()));
        return resultActions;
    }

    protected ResultActions performMvcAction(MockHttpServletRequestBuilder request) throws Exception {
        return performMvcAction(request, HttpStatus.OK);
    }

    protected <OT> OT performMvcAction(
            MockHttpServletRequestBuilder request, String errorValue,
            TypeReference<OT> typeReference) throws Exception {
        return getResponseContainer(performMvcAction(request).andReturn(), typeReference);
    }

    protected <OT> OT performMvcJsonAction(
            MockHttpServletRequestBuilder request,
            TypeReference<OT> typeReference) throws Exception {
        return getResponseContainer(performMvcAction(request).andReturn(), typeReference);
    }

    protected <OT> OT performMvcXmlAction(
            MockHttpServletRequestBuilder request,
            TypeReference<OT> typeReference) throws Exception {
        return getResponseContainerXml(performMvcAction(request).andReturn(), typeReference);
    }

    protected ResultActions performMvcAction(MockHttpServletRequestBuilder request,
                                             Map.Entry... entries) throws Exception {
        final ResultActions resultActions = performMvcAction(request);
        for (Map.Entry entry : entries) {
            resultActions.andExpect(jsonPath("$." + entry.getKey(), is(entry.getValue())));
        }
        return resultActions;
    }

    protected <OT> OT getResponseContainer(MvcResult mvcResult,
                                           TypeReference<OT> typeReference) throws java.io.IOException {
        return deserializeFromJson(mvcResult.getResponse().getContentAsString(), typeReference);
    }

    protected <OT> OT getResponseContainerXml(MvcResult mvcResult,
                                              TypeReference<OT> typeReference) throws JAXBException, UnsupportedEncodingException {
        return deserializeFromXml(mvcResult.getResponse().getContentAsString(), typeReference);
    }
}
