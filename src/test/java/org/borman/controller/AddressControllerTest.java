package org.borman.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import org.borman.dto.page.SerializableAddressPage;
import org.borman.dto.request.AddressRequest;
import org.borman.dto.response.AddressListResponse;
import org.borman.dto.response.AddressPageResponse;
import org.borman.dto.response.AddressResponse;
import org.borman.model.Address;
import org.borman.service.AddressService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class AddressControllerTest extends BaseControllerTest {
    private final TypeReference<AddressPageResponse> addressPageTypeReference = new TypeReference<AddressPageResponse>() {
    };
    private final String applicationJsonValue = MediaType.APPLICATION_JSON_VALUE;
    private final String applicationXmlValue = MediaType.APPLICATION_XML_VALUE;
    private final SerializableAddressPage emptyAddressPage = new SerializableAddressPage(new PageImpl<Address>(new ArrayList<Address>()));
    @Mock
    AddressService addressService;
    @Captor
    private ArgumentCaptor<List<Address>> arrayListCaptor;
    private Address savedAddress1;
    private Address savedAddress2;
    private ArrayList<Address> savedAddresses;
    private ArrayList<Address> addresses;
    private SerializableAddressPage serializableAddressPage;
    private final Address address1 = new Address("street1", "street1-2", "city1", "NY", 12341, true);
    private final Address address2 = new Address("street2", "street2-2", "city2", "CT", 12342, false);
    private final Address invalidAddress = new Address(null, null, null, null, 0, true);
    private final long addressId = 44;
    private final int pageSize = 1;

    private final int pageNumber = 0;
    private final TypeReference<AddressResponse> addressResponseTypeReference = new TypeReference<AddressResponse>() {
    };
    private final TypeReference<AddressListResponse> addressListTypeReference = new TypeReference<AddressListResponse>() {
    };
    private final TypeReference<AddressPageResponse> addressPageResponseTypeReference = new TypeReference<AddressPageResponse>() {
    };
    private final RuntimeException runtimeException = new RuntimeException("I'm sorry Dave");

    @Before
    public void setup() throws JAXBException {

        savedAddress1 = new Address(address1.getStreet(), address1.getStreet2(), address1.getCity(), address1.getState(), address1.getZip(), address1.isActive());
        savedAddress1.setId(addressId);
        savedAddress2 = new Address(address2.getStreet(), address2.getStreet2(), address2.getCity(), address2.getState(), address2.getZip(), address2.isActive());
        savedAddress2.setId(addressId + 22);

        addresses = new ArrayList<Address>() {{
            add(address1);
            add(address2);
        }};

        savedAddresses = new ArrayList<Address>() {{
            add(savedAddress1);
            add(savedAddress2);
        }};

        PageRequest pageable = new PageRequest(pageNumber, pageSize);
        List<Address> addresses = savedAddresses.subList(0, pageSize);
        PageImpl<Address> basePage = new PageImpl<Address>(addresses, pageable, savedAddresses.size());
        serializableAddressPage = new SerializableAddressPage(basePage);

        MockitoAnnotations.initMocks(this);
        buildMockMvc(new AddressController(addressService));
    }

    @After
    public void afterEach() {
        verifyNoMoreInteractions(addressService);
    }

    //INDEX
    @Test
    public void testShouldGetIndex() throws Exception {
        performMvcAction(get("/")
                .accept(MediaType.TEXT_HTML_VALUE))
                .andExpect(view().name("addresses"));
    }

    @Test
    public void testShouldGetAddressesPage() throws Exception {
        performMvcAction(get("/addresses/")
                .accept(MediaType.TEXT_HTML_VALUE))
                .andExpect(view().name("addresses"));
    }


    //SAVE SEVERAL ADDRESSES

    /**
     * test for {@link AddressController#saveNewAddress(Address, org.springframework.validation.BindingResult)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldSaveAddressesJson() throws Exception {
        when(addressService.saveNewAddresses(anyListOf(Address.class))).thenReturn(savedAddresses);

        final AddressListResponse resultActions = performMvcJsonAction(postAddressesRequest(applicationJsonValue, serializeToJson(new AddressRequest(addresses))), addressListTypeReference);

        assertThat(resultActions.isError()).isFalse();
        assertThat(resultActions.getMessage()).isNotNull().isEmpty();
        assertThat(resultActions.getAddresses()).isNotNull().hasSize(savedAddresses.size());
        verify(addressService, times(1)).saveNewAddresses(arrayListCaptor.capture());
        assertThat(arrayListCaptor.getValue()).isNotNull().hasSize(addresses.size());
    }

    /**
     * test for {@link AddressController#saveNewAddress(Address, org.springframework.validation.BindingResult)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldSaveAddressesXml() throws Exception {
        when(addressService.saveNewAddresses(anyListOf(Address.class))).thenReturn(savedAddresses);

        final AddressListResponse resultActions = performMvcXmlAction(
                postAddressesRequest(applicationXmlValue, serializeToXml(new AddressRequest(addresses))), addressListTypeReference);

        assertThat(resultActions.isError()).isFalse();
        assertThat(resultActions.getMessage()).isNotNull().isEmpty();
        assertThat(resultActions.getAddresses()).isNotNull().hasSize(savedAddresses.size());
        verify(addressService, times(1)).saveNewAddresses(arrayListCaptor.capture());
        assertThat(arrayListCaptor.getValue()).isNotNull().hasSize(addresses.size());
    }

    @Test
    public void testShouldFailToSaveInvalidAddressListJson() throws Exception {
        final AddressListResponse resultActions = performMvcJsonAction(
                postAddressesRequest(applicationJsonValue, serializeToJson(new AddressRequest(new ArrayList<Address>()))), addressListTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage())
                .isNotNull().isNotEmpty()
                .contains("Addresses must be provided for this request.");
        assertThat(resultActions.getAddresses()).isNotNull().isEmpty();
    }

    /**
     * test for {@link AddressController#saveNewAddress(Address, org.springframework.validation.BindingResult)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldFailToSaveInvalidAddressListXml() throws Exception {
        final AddressListResponse resultActions = performMvcXmlAction(
                postAddressesRequest(applicationXmlValue, serializeToXml(new AddressRequest(new ArrayList<Address>()))), addressListTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage())
                .isNotNull().isNotEmpty()
                .contains("Addresses must be provided for this request.");
        assertThat(resultActions.getAddresses()).isNotNull().isEmpty();
    }

    @Test
    public void testShouldFailToSaveInvalidAddressesJson() throws Exception {
        final ArrayList<Address> invalidAddresses = new ArrayList<Address>() {{
            add(invalidAddress);
            add(invalidAddress);
        }};

        final AddressListResponse resultActions = performMvcJsonAction(
                postAddressesRequest(applicationJsonValue, serializeToJson(new AddressRequest(invalidAddresses))), addressListTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage()).isNotNull().isNotEmpty()
                .contains(Address.INVALID_ZIP_CODE_ERROR)
                .contains(Address.MISSING_STATE_ERROR)
                .contains(Address.MISSING_STREET_ERROR)
                .contains(Address.MISSING_CITY_ERROR)
                .contains(Address.INVALID_STATE_ERROR);
        assertThat(resultActions.getAddresses()).isNotNull().isEmpty();
    }

    /**
     * test for {@link AddressController#saveNewAddress(Address, org.springframework.validation.BindingResult)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldFailToSaveInvalidAddressesXml() throws Exception {
        final ArrayList<Address> invalidAddresses = new ArrayList<Address>() {{
            add(invalidAddress);
            add(invalidAddress);
        }};

        final AddressListResponse resultActions = performMvcXmlAction(
                postAddressesRequest(applicationXmlValue, serializeToXml(new AddressRequest(invalidAddresses))), addressListTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage()).isNotNull().isNotEmpty()
                .contains(Address.INVALID_ZIP_CODE_ERROR)
                .contains(Address.MISSING_STATE_ERROR)
                .contains(Address.MISSING_STREET_ERROR)
                .contains(Address.MISSING_CITY_ERROR)
                .contains(Address.INVALID_STATE_ERROR);
        assertThat(resultActions.getAddresses()).isNotNull().isEmpty();
    }

    /**
     * test for {@link AddressController#saveNewAddress(Address, org.springframework.validation.BindingResult)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldFailToSaveAddressesRepoFailsJson() throws Exception {
        when(addressService.saveNewAddresses(anyListOf(Address.class))).thenThrow(new RuntimeException("boom"));

        final AddressListResponse resultActions = performMvcJsonAction(
                postAddressesRequest(applicationJsonValue, serializeToJson(new AddressRequest(addresses))), addressListTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage().replaceAll("@[0-9a-fA-F]+\\[", "@["))
                .isNotNull()
                .isNotEmpty().isEqualTo("Failed to save addresses: " + addresses.toString().replaceAll("@[0-9a-fA-F]+\\[", "@["));
        assertThat(resultActions.getAddresses()).isNotNull().isEmpty();
        verify(addressService, times(1)).saveNewAddresses(anyListOf(Address.class));
    }

    /**
     * test for {@link AddressController#saveNewAddress(Address, org.springframework.validation.BindingResult)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldFailToSaveAddressesRepoFailsXml() throws Exception {
        when(addressService.saveNewAddresses(anyListOf(Address.class))).thenThrow(new RuntimeException("boom"));

        final AddressListResponse resultActions = performMvcXmlAction(
                postAddressesRequest(applicationXmlValue, serializeToXml(new AddressRequest(addresses))), addressListTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage().replaceAll("@[0-9a-fA-F]+\\[", "@[")).isNotNull()
                .isNotEmpty().isEqualTo(
                "Failed to save addresses: " +
                        addresses.toString().replaceAll("@[0-9a-fA-F]+\\[", "@["));
        assertThat(resultActions.getAddresses()).isNotNull().isEmpty();
        verify(addressService, times(1)).saveNewAddresses(anyListOf(Address.class));
    }


    //SAVE AN ADDRESS

    /**
     * test for {@link AddressController#saveNewAddress(Address, org.springframework.validation.BindingResult)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldSaveAddressJson() throws Exception {
        when(addressService.saveNewAddress(any(Address.class))).thenReturn(savedAddress1);

        final AddressResponse resultActions = performMvcJsonAction(
                postAddressRequest(applicationJsonValue, serializeToJson(address1)), addressResponseTypeReference);

        assertThat(resultActions.isError()).isFalse();
        assertThat(resultActions.getMessage()).isNotNull().isEmpty();
        assertThat(resultActions.getAddress()).isNotNull();
        verifyAddressWasSaved(address1);
        verifySavedAddress(resultActions.getAddress(), savedAddress1);
    }

    /**
     * test for {@link AddressController#saveNewAddress(Address, org.springframework.validation.BindingResult)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldSaveAddressXml() throws Exception {
        final Address savedAddress = new Address(address1.getStreet(), address1.getStreet2(),
                address1.getCity(),
                address1.getState(), address1.getZip(), address1.isActive());
        savedAddress.setId(addressId);
        when(addressService.saveNewAddress(any(Address.class))).thenReturn(savedAddress);

        final AddressResponse resultActions = performMvcXmlAction(
                postAddressRequest(applicationXmlValue, serializeToXml(address1)), addressResponseTypeReference);

        assertThat(resultActions.isError()).isFalse();
        assertThat(resultActions.getMessage()).isNotNull().isEmpty();
        assertThat(resultActions.getAddress()).isNotNull();
        verifyAddressWasSaved(address1);
        verifySavedAddress(resultActions.getAddress(), savedAddress1);
    }

    /**
     * test for {@link AddressController#saveNewAddress(Address, org.springframework.validation.BindingResult)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldFailToSaveInvalidAddressJson() throws Exception {
        final AddressResponse resultActions = performMvcJsonAction(
                postAddressRequest(applicationJsonValue, serializeToJson(invalidAddress)), addressResponseTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage()).isNotNull().isNotEmpty()
                .contains(Address.INVALID_ZIP_CODE_ERROR)
                .contains(Address.MISSING_STATE_ERROR)
                .contains(Address.MISSING_STREET_ERROR)
                .contains(Address.MISSING_CITY_ERROR)
                .contains(Address.INVALID_STATE_ERROR);
        assertThat(resultActions.getAddress()).isNull();
    }

    /**
     * test for {@link AddressController#saveNewAddress(Address, org.springframework.validation.BindingResult)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldFailToSaveInvalidAddressXml() throws Exception {
        final AddressResponse resultActions = performMvcXmlAction(
                postAddressRequest(applicationXmlValue, serializeToXml(invalidAddress)), addressResponseTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage()).isNotNull().isNotEmpty()
                .contains(Address.INVALID_ZIP_CODE_ERROR)
                .contains(Address.MISSING_STATE_ERROR)
                .contains(Address.MISSING_STREET_ERROR)
                .contains(Address.MISSING_CITY_ERROR)
                .contains(Address.INVALID_STATE_ERROR);
        assertThat(resultActions.getAddress()).isNull();
    }

    /**
     * test for {@link AddressController#saveNewAddress(Address, org.springframework.validation.BindingResult)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldFailToSaveAddressIfRepoFailsJson() throws Exception {
        when(addressService.saveNewAddress(any(Address.class))).thenThrow(runtimeException);

        final AddressResponse resultActions = performMvcJsonAction(
                postAddressRequest(applicationJsonValue, serializeToJson(address1)), addressResponseTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage().replaceAll("@[0-9a-fA-F]+\\[", "@["))
                .isNotNull()
                .isEqualTo("Failed to save address: " + address1.toString().replaceAll("@[0-9a-fA-F]+\\[", "@["));
        assertThat(resultActions.getAddress()).isNull();
        verifyAddressWasSaved(address1);
    }

    /**
     * test for {@link AddressController#saveNewAddress(Address, org.springframework.validation.BindingResult)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldFailToSaveAddressIfRepoFailsXml() throws Exception {
        when(addressService.saveNewAddress(any(Address.class))).thenThrow(runtimeException);

        final AddressResponse resultActions = performMvcXmlAction(
                postAddressRequest(applicationXmlValue, serializeToXml(address1)), addressResponseTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage().replaceAll("@[0-9a-fA-F]+\\[", "@[")).isNotNull()
                .isEqualTo(
                        "Failed to save address: " +
                                address1.toString().replaceAll("@[0-9a-fA-F]+\\[", "@["));
        assertThat(resultActions.getAddress()).isNull();
        verifyAddressWasSaved(address1);
    }


    //GET ADDRESS BY ID

    /**
     * test for {@link AddressController#getAddressById(long)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetAddressJson() throws Exception {
        when(addressService.getAddressById(addressId)).thenReturn(savedAddress1);

        final AddressResponse resultActions = performMvcJsonAction(
                getAddressRequest(applicationJsonValue, addressId), addressResponseTypeReference);

        verifyAddressLookup(resultActions);
        verify(addressService, times(1)).getAddressById(addressId);
    }

    /**
     * test for {@link AddressController#getAddressById(long)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetAddressXml() throws Exception {
        when(addressService.getAddressById(addressId)).thenReturn(savedAddress1);

        final AddressResponse resultActions = performMvcXmlAction(
                getAddressRequest(applicationXmlValue, addressId), addressResponseTypeReference);

        verifyAddressLookup(resultActions);
        verify(addressService, times(1)).getAddressById(addressId);
    }

    /**
     * test for {@link AddressController#getAddressById(long)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetErrorIfNoAddressToGetXml() throws Exception {
        when(addressService.getAddressById(addressId)).thenReturn(null);

        final AddressResponse resultActions = performMvcXmlAction(
                getAddressRequest(applicationXmlValue, addressId), addressResponseTypeReference);

        assertThat(resultActions.getAddress()).isNull();
        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage())
                .isNotNull().isNotEmpty()
                .isEqualTo(String.format("No address found for id: %s", addressId));
        verify(addressService, times(1)).getAddressById(addressId);
    }

    /**
     * test for {@link AddressController#getAddressById(long)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetErrorIfNoAddressToGetJson() throws Exception {
        when(addressService.getAddressById(addressId)).thenReturn(null);

        final AddressResponse resultActions = performMvcJsonAction(getAddressRequest(
                applicationJsonValue, addressId), addressResponseTypeReference);

        assertThat(resultActions.getAddress()).isNull();
        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage())
                .isNotNull().isNotEmpty()
                .isEqualTo(String.format("No address found for id: %s", addressId));
        verify(addressService, times(1)).getAddressById(addressId);
    }

    /**
     * test for {@link AddressController#getAddressById(long)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetErrorIfRepoFailsForGetAddressXml() throws Exception {
        when(addressService.getAddressById(addressId)).thenThrow(runtimeException);

        final AddressResponse resultActions = performMvcXmlAction(getAddressRequest(
                applicationXmlValue, addressId), addressResponseTypeReference);

        assertThat(resultActions.getAddress()).isNull();
        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage())
                .isNotNull().isNotEmpty()
                .isEqualTo("Failed to find address: " + addressId);
        verify(addressService, times(1)).getAddressById(addressId);
    }

    /**
     * test for {@link AddressController#getAddressById(long)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetErrorIfRepoFailsForGetAddressJson() throws Exception {
        when(addressService.getAddressById(addressId)).thenThrow(runtimeException);

        final AddressResponse resultActions = performMvcJsonAction(getAddressRequest(
                applicationJsonValue, addressId), addressResponseTypeReference);

        assertThat(resultActions.getAddress()).isNull();
        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage())
                .isNotNull().isNotEmpty()
                .isEqualTo("Failed to find address: " + addressId);
        verify(addressService, times(1)).getAddressById(addressId);
    }


    //DELETE ADDRESS BY ID

    /**
     * test for {@link AddressController#deleteAddress(long)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldDeleteAddressJson() throws Exception {
        final AddressResponse resultActions = performMvcJsonAction(deleteAddressRequest(
                applicationJsonValue, addressId), addressResponseTypeReference);

        assertThat(resultActions.isError()).isFalse();
        assertThat(resultActions.getMessage()).isNotNull().isEqualTo(AddressController.SUCCESS);
        assertThat(resultActions.getAddress()).isNull();
        verify(addressService, times(1)).deleteAddress(addressId);
    }

    /**
     * test for {@link AddressController#deleteAddress(long)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldDeleteAddressXml() throws Exception {
        final AddressResponse resultActions = performMvcXmlAction(deleteAddressRequest(
                applicationXmlValue, addressId), addressResponseTypeReference);

        assertThat(resultActions.isError()).isFalse();
        assertThat(resultActions.getMessage()).isNotNull().isEqualTo(AddressController.SUCCESS);
        assertThat(resultActions.getAddress()).isNull();
        verify(addressService, times(1)).deleteAddress(addressId);
    }

    /**
     * test for {@link AddressController#deleteAddress(long)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetErrorMessageWhenFailToDeleteAddressJson() throws Exception {
        doThrow(runtimeException).when(addressService).deleteAddress(anyLong());

        final AddressResponse resultActions = performMvcJsonAction(deleteAddressRequest(
                applicationJsonValue, addressId), addressResponseTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage()).isNotNull()
                .isEqualTo("Failed to delete address: " + addressId);
        assertThat(resultActions.getAddress()).isNull();
        verify(addressService, times(1)).deleteAddress(addressId);
    }

    /**
     * test for {@link AddressController#deleteAddress(long)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetErrorMessageWhenFailToDeleteAddressXml() throws Exception {
        doThrow(runtimeException).when(addressService).deleteAddress(anyLong());

        final AddressResponse resultActions = performMvcXmlAction(deleteAddressRequest(
                applicationXmlValue, addressId), addressResponseTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage()).isNotNull()
                .isEqualTo("Failed to delete address: " + addressId);
        assertThat(resultActions.getAddress()).isNull();
        verify(addressService, times(1)).deleteAddress(addressId);
    }


    //GET ALL ADDRESSES

    /**
     * test for {@link AddressController#getAllAddresses()}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetAllAddressesJson() throws Exception {
        when(addressService.getAllOrderedAddresses()).thenReturn(savedAddresses);

        final AddressListResponse resultActions = performMvcJsonAction(
                getAllAddressListRequest(
                        applicationJsonValue), addressListTypeReference);

        assertThat(resultActions.isError()).isFalse();
        assertThat(resultActions.getMessage()).isNotNull().isEmpty();
        assertThat(resultActions.getAddresses())
                .isNotNull().isNotEmpty()
                .hasSize(savedAddresses.size());


        verifyFindAllOrdered();
    }

    /**
     * test for {@link AddressController#getAllAddresses()}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetAllAddressesXml() throws Exception {
        when(addressService.getAllOrderedAddresses()).thenReturn(savedAddresses);

        final AddressListResponse resultActions = performMvcXmlAction(
                getAllAddressListRequest(
                        applicationXmlValue), addressListTypeReference);

        assertThat(resultActions.isError()).isFalse();
        assertThat(resultActions.getMessage()).isNotNull().isEmpty();
        assertThat(resultActions.getAddresses())
                .isNotNull().isNotEmpty()
                .hasSize(savedAddresses.size());
        verifyFindAllOrdered();
    }

    /**
     * test for {@link AddressController#getAllActiveAddresses(boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetErrorForAllAddressesWhenNoneJson() throws Exception {
        when(addressService.getAllOrderedAddresses()).thenReturn(new ArrayList<Address>());

        final AddressListResponse resultActions = performMvcJsonAction(getAllAddressListRequest(applicationJsonValue), addressListTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage())
                .isNotNull().isNotEmpty()
                .isEqualTo(AddressController.NO_ADDRESSES_FOUND);
        assertThat(resultActions.getAddresses()).isNotNull().isEmpty();
        verify(addressService, times(1)).getAllOrderedAddresses();
    }

    /**
     * test for {@link AddressController#getAllActiveAddresses(boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetErrorForAllAddressesWhenNoneXml() throws Exception {
        when(addressService.getAllOrderedAddresses()).thenReturn(new ArrayList<Address>());

        final AddressListResponse resultActions = performMvcXmlAction(getAllAddressListRequest(applicationXmlValue), addressListTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage())
                .isNotNull().isNotEmpty()
                .isEqualTo(AddressController.NO_ADDRESSES_FOUND);
        assertThat(resultActions.getAddresses()).isNotNull().isEmpty();
        verify(addressService, times(1)).getAllOrderedAddresses();
    }

    /**
     * test for {@link AddressController#getAllActiveAddresses(boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetErrorForAllAddressesWhenRepoFailsJson() throws Exception {
        when(addressService.getAllOrderedAddresses()).thenThrow(runtimeException);

        final AddressListResponse resultActions = performMvcJsonAction(getAllAddressListRequest(applicationJsonValue), addressListTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage())
                .isNotNull().isNotEmpty()
                .isEqualTo(AddressController.ALL_ADDRESS_LOOKUP_FAILURE_MESSAGE);
        assertThat(resultActions.getAddresses()).isNotNull().isEmpty();
        verify(addressService, times(1)).getAllOrderedAddresses();
    }

    /**
     * test for {@link AddressController#getAllActiveAddresses(boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetErrorForAllAddressesWhenRepoFailsXml() throws Exception {
        when(addressService.getAllOrderedAddresses()).thenThrow(runtimeException);

        final AddressListResponse resultActions = performMvcXmlAction(getAllAddressListRequest(applicationXmlValue), addressListTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage())
                .isNotNull().isNotEmpty()
                .isEqualTo(AddressController.ALL_ADDRESS_LOOKUP_FAILURE_MESSAGE);
        assertThat(resultActions.getAddresses()).isNotNull().isEmpty();
        verify(addressService, times(1)).getAllOrderedAddresses();
    }


    //GET ALL ADDRESSES PAGED

    /**
     * test for {@link AddressController#getAllAddresses(int, int)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetAllAddressesPagedXml() throws Exception {
        when(addressService.getAllOrderedAddresses(any(PageRequest.class))).thenReturn(serializableAddressPage);

        final AddressPageResponse resultActions = performMvcXmlAction(getPagedAddressRequest(applicationXmlValue, pageNumber, pageSize), addressPageResponseTypeReference);

        verifyPageContent(resultActions);
        verifyAllPagedLookup(pageNumber, pageSize);
    }

    /**
     * test for {@link AddressController#getAllAddresses(int, int)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetAllAddressesPagedJson() throws Exception {
        when(addressService.getAllOrderedAddresses(any(PageRequest.class))).thenReturn(serializableAddressPage);

        final AddressPageResponse resultActions = performMvcJsonAction(getPagedAddressRequest(applicationJsonValue, pageNumber, pageSize), addressPageResponseTypeReference);

        verifyPageContent(resultActions);
        verifyAllPagedLookup(pageNumber, pageSize);
    }

    /**
     * test for {@link AddressController#getAllActiveAddresses(boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetErrorForAllAddressesPagedWhenNoneJson() throws Exception {
        when(addressService.getAllOrderedAddresses(any(PageRequest.class))).thenReturn(emptyAddressPage);

        final AddressPageResponse resultActions = performMvcJsonAction(getPagedAddressRequest(applicationJsonValue, pageNumber, pageSize), addressPageResponseTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage())
                .isNotNull().isNotEmpty()
                .isEqualTo(AddressController.NO_ADDRESSES_FOUND);
        assertThat(resultActions.getAddresses()).isNull();
        verify(addressService, times(1)).getAllOrderedAddresses(any(PageRequest.class));
    }

    /**
     * test for {@link AddressController#getAllActiveAddresses(boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetErrorForAllAddressesPagedWhenNoneXml() throws Exception {
        when(addressService.getAllOrderedAddresses(any(PageRequest.class))).thenReturn(emptyAddressPage);

        final AddressPageResponse resultActions = performMvcXmlAction(getPagedAddressRequest(applicationXmlValue, pageNumber, pageSize), addressPageResponseTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage())
                .isNotNull().isNotEmpty()
                .isEqualTo(AddressController.NO_ADDRESSES_FOUND);
        assertThat(resultActions.getAddresses()).isNull();
        verify(addressService, times(1)).getAllOrderedAddresses(any(PageRequest.class));
    }

    /**
     * test for {@link AddressController#getAllActiveAddresses(boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetErrorForAllAddressesPagedWhenRepoFailsJson() throws Exception {
        when(addressService.getAllOrderedAddresses(any(PageRequest.class))).thenThrow(runtimeException);

        final AddressPageResponse resultActions = performMvcJsonAction(getPagedAddressRequest(applicationJsonValue, pageNumber, pageSize), addressPageResponseTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage())
                .isNotNull().isNotEmpty()
                .isEqualTo(AddressController.ALL_ADDRESS_LOOKUP_FAILURE_MESSAGE);
        assertThat(resultActions.getAddresses()).isNull();
        verify(addressService, times(1)).getAllOrderedAddresses(any(PageRequest.class));
    }

    /**
     * test for {@link AddressController#getAllActiveAddresses(boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetErrorForAllAddressesPagedWhenRepoFailsXml() throws Exception {
        when(addressService.getAllOrderedAddresses(any(PageRequest.class))).thenThrow(runtimeException);

        final AddressPageResponse resultActions = performMvcXmlAction(getPagedAddressRequest(applicationXmlValue, pageNumber, pageSize), addressPageResponseTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage())
                .isNotNull().isNotEmpty()
                .isEqualTo(AddressController.ALL_ADDRESS_LOOKUP_FAILURE_MESSAGE);
        assertThat(resultActions.getAddresses()).isNull();
        verify(addressService, times(1)).getAllOrderedAddresses(any(PageRequest.class));
    }


    //GET ALL ACTIVE ADDRESSES

    /**
     * test for {@link AddressController#getAllActiveAddresses(boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetActiveAddressesJson() throws Exception {
        when(addressService.getAllActiveAddresses()).thenReturn(savedAddresses);
        final boolean activeOnly = true;

        final AddressListResponse resultActions = performMvcJsonAction(getAllActiveAddressListRequest(applicationJsonValue, activeOnly), addressListTypeReference);

        assertThat(resultActions.isError()).isFalse();
        assertThat(resultActions.getMessage()).isNotNull().isEmpty();
        assertThat(resultActions.getAddresses())
                .isNotNull().isNotEmpty()
                .hasSize(savedAddresses.size());
        verify(addressService, times(1)).getAllActiveAddresses();
    }

    /**
     * test for {@link AddressController#getAllActiveAddresses(boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetActiveAddressesXml() throws Exception {
        when(addressService.getAllActiveAddresses()).thenReturn(savedAddresses);
        final boolean activeOnly = true;

        final AddressListResponse resultActions = performMvcXmlAction(getAllActiveAddressListRequest(applicationXmlValue, activeOnly), addressListTypeReference);

        assertThat(resultActions.isError()).isFalse();
        assertThat(resultActions.getMessage()).isNotNull().isEmpty();
        assertThat(resultActions.getAddresses())
                .isNotNull().isNotEmpty()
                .hasSize(savedAddresses.size());
        verify(addressService, times(1)).getAllActiveAddresses();
    }

    @Test
    public void testShouldGetAllWhenLookingForActiveAddressesJson() throws Exception {
        when(addressService.getAllOrderedAddresses()).thenReturn(savedAddresses);
        final boolean activeOnly = false;

        final AddressListResponse resultActions = performMvcJsonAction(getAllActiveAddressListRequest(applicationJsonValue, activeOnly), addressListTypeReference);

        assertThat(resultActions.isError()).isFalse();
        assertThat(resultActions.getMessage()).isNotNull().isEmpty();
        assertThat(resultActions.getAddresses())
                .isNotNull().isNotEmpty()
                .hasSize(savedAddresses.size());
        verify(addressService, times(1)).getAllOrderedAddresses();
    }

    /**
     * test for {@link AddressController#getAllActiveAddresses(boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetAllWhenLookingForActiveAddressesXml() throws Exception {
        when(addressService.getAllOrderedAddresses()).thenReturn(savedAddresses);
        final boolean activeOnly = false;

        final AddressListResponse resultActions = performMvcXmlAction(getAllActiveAddressListRequest(applicationXmlValue, activeOnly), addressListTypeReference);

        assertThat(resultActions.isError()).isFalse();
        assertThat(resultActions.getMessage()).isNotNull().isEmpty();
        assertThat(resultActions.getAddresses())
                .isNotNull().isNotEmpty()
                .hasSize(savedAddresses.size());
        verify(addressService, times(1)).getAllOrderedAddresses();
    }

    /**
     * test for {@link AddressController#getAllActiveAddresses(boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetErrorForActiveAddressesWhenNoneJson() throws Exception {
        when(addressService.getAllActiveAddresses()).thenReturn(new ArrayList<Address>());
        final boolean activeOnly = true;

        final AddressListResponse resultActions = performMvcJsonAction(getAllActiveAddressListRequest(applicationJsonValue, activeOnly), addressListTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage())
                .isNotNull().isNotEmpty()
                .isEqualTo(AddressController.NO_ADDRESSES_FOUND);
        assertThat(resultActions.getAddresses()).isNotNull().isEmpty();
        verify(addressService, times(1)).getAllActiveAddresses();
    }

    /**
     * test for {@link AddressController#getAllActiveAddresses(boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetErrorForActiveAddressesWhenNoneXml() throws Exception {
        when(addressService.getAllActiveAddresses()).thenReturn(new ArrayList<Address>());
        final boolean activeOnly = true;

        final AddressListResponse resultActions = performMvcXmlAction(getAllActiveAddressListRequest(applicationXmlValue, activeOnly), addressListTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage())
                .isNotNull().isNotEmpty()
                .isEqualTo(AddressController.NO_ADDRESSES_FOUND);
        assertThat(resultActions.getAddresses()).isNotNull().isEmpty();
        verify(addressService, times(1)).getAllActiveAddresses();
    }

    /**
     * test for {@link AddressController#getAllActiveAddresses(boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetErrorForActiveAddressesWhenRepoFailsJson() throws Exception {
        when(addressService.getAllActiveAddresses()).thenThrow(runtimeException);
        final boolean activeOnly = true;

        final AddressListResponse resultActions = performMvcJsonAction(getAllActiveAddressListRequest(applicationJsonValue, activeOnly), addressListTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage())
                .isNotNull().isNotEmpty()
                .isEqualTo(AddressController.ACTIVE_ADDRESS_LOOKUP_FAILURE_MESSAGE);
        assertThat(resultActions.getAddresses()).isNotNull().isEmpty();
        verify(addressService, times(1)).getAllActiveAddresses();
    }

    /**
     * test for {@link AddressController#getAllActiveAddresses(boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetErrorForActiveAddressesWhenRepoFailsXml() throws Exception {
        when(addressService.getAllActiveAddresses()).thenThrow(runtimeException);
        final boolean activeOnly = true;

        final AddressListResponse resultActions = performMvcXmlAction(getAllActiveAddressListRequest(applicationXmlValue, activeOnly), addressListTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage())
                .isNotNull().isNotEmpty()
                .isEqualTo(AddressController.ACTIVE_ADDRESS_LOOKUP_FAILURE_MESSAGE);
        assertThat(resultActions.getAddresses()).isNotNull().isEmpty();
        verify(addressService, times(1)).getAllActiveAddresses();
    }


    //GET ALL ACTIVE ADDRESSES PAGED

    /**
     * test for {@link AddressController#getAllActiveAddresses(int, int, boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetActiveAddressesPagedXml() throws Exception {
        when(addressService.getAllActiveAddresses(any(PageRequest.class))).thenReturn(serializableAddressPage);
        final boolean activeOnly = true;

        final AddressPageResponse resultActions = performMvcXmlAction(
                getPagedActiveAddressRequest(applicationXmlValue, activeOnly, pageNumber, pageSize), addressPageResponseTypeReference);

        verifyPageContent(resultActions);
        verifyActivePagedLookup(pageNumber, pageSize);
    }

    /**
     * test for {@link AddressController#getAllActiveAddresses(int, int, boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetActiveAddressesPagedJson() throws Exception {
        when(addressService.getAllActiveAddresses(any(PageRequest.class))).thenReturn(serializableAddressPage);
        final boolean activeOnly = true;

        final AddressPageResponse resultActions = performMvcJsonAction(
                getPagedActiveAddressRequest(applicationJsonValue, activeOnly, pageNumber, pageSize), addressPageResponseTypeReference);

        verifyPageContent(resultActions);
        verifyActivePagedLookup(pageNumber, pageSize);
    }

    /**
     * test for {@link AddressController#getAllActiveAddresses(int, int, boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetAllWhenLookingForActiveAddressesPagedXml() throws Exception {
        when(addressService.getAllOrderedAddresses(any(PageRequest.class))).thenReturn(serializableAddressPage);
        final boolean activeOnly = false;

        final AddressPageResponse resultActions = performMvcXmlAction(
                getPagedActiveAddressRequest(applicationXmlValue, activeOnly, pageNumber, pageSize), addressPageResponseTypeReference);

        verifyPageContent(resultActions);
        verifyAllPagedLookup(pageNumber, pageSize);
    }

    /**
     * test for {@link AddressController#getAllActiveAddresses(int, int, boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetAllWhenLookingForActiveAddressesPagedJson() throws Exception {
        when(addressService.getAllOrderedAddresses(any(PageRequest.class))).thenReturn(serializableAddressPage);
        final boolean activeOnly = false;

        final AddressPageResponse resultActions = performMvcJsonAction(
                getPagedActiveAddressRequest(applicationJsonValue, activeOnly, pageNumber, pageSize), addressPageResponseTypeReference);

        verifyPageContent(resultActions);
        verifyAllPagedLookup(pageNumber, pageSize);
    }

    /**
     * test for {@link AddressController#getAllActiveAddresses(boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetErrorForActiveAddressesPagedWhenNoneJson() throws Exception {
        when(addressService.getAllActiveAddresses(any(PageRequest.class))).thenReturn(emptyAddressPage);
        final boolean activeOnly = true;

        final AddressPageResponse resultActions = performMvcJsonAction(
                getPagedActiveAddressRequest(applicationJsonValue, activeOnly, pageNumber, pageSize), addressPageResponseTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage())
                .isNotNull().isNotEmpty()
                .isEqualTo(AddressController.NO_ADDRESSES_FOUND);
        assertThat(resultActions.getAddresses()).isNull();
        verify(addressService, times(1)).getAllActiveAddresses(any(PageRequest.class));
    }

    /**
     * test for {@link AddressController#getAllActiveAddresses(boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetErrorForActiveAddressesPagedWhenNoneXml() throws Exception {
        when(addressService.getAllActiveAddresses(any(PageRequest.class))).thenReturn(emptyAddressPage);
        final boolean activeOnly = true;

        final AddressPageResponse resultActions = performMvcXmlAction(
                getPagedActiveAddressRequest(applicationXmlValue, activeOnly, pageNumber, pageSize), addressPageResponseTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage())
                .isNotNull().isNotEmpty()
                .isEqualTo(AddressController.NO_ADDRESSES_FOUND);
        assertThat(resultActions.getAddresses()).isNull();
        verify(addressService, times(1)).getAllActiveAddresses(any(PageRequest.class));
    }

    /**
     * test for {@link AddressController#getAllActiveAddresses(boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetErrorForActiveAddressesPagedWhenRepoFailsJson() throws Exception {
        when(addressService.getAllActiveAddresses(any(PageRequest.class))).thenThrow(runtimeException);
        final boolean activeOnly = true;

        final AddressPageResponse resultActions = performMvcJsonAction(
                getPagedActiveAddressRequest(applicationJsonValue, activeOnly, pageNumber, pageSize), addressPageResponseTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage())
                .isNotNull().isNotEmpty()
                .isEqualTo(AddressController.ACTIVE_ADDRESS_LOOKUP_FAILURE_MESSAGE);
        assertThat(resultActions.getAddresses()).isNull();
        verify(addressService, times(1)).getAllActiveAddresses(any(PageRequest.class));
    }

    /**
     * test for {@link AddressController#getAllActiveAddresses(boolean)}
     *
     * @throws Exception
     */
    @Test
    public void testShouldGetErrorForActiveAddressesPagedWhenRepoFailsXml() throws Exception {
        when(addressService.getAllActiveAddresses(any(PageRequest.class))).thenThrow(runtimeException);
        final boolean activeOnly = true;

        final AddressPageResponse resultActions = performMvcXmlAction(
                getPagedActiveAddressRequest(applicationXmlValue, activeOnly, pageNumber, pageSize), addressPageResponseTypeReference);

        assertThat(resultActions.isError()).isTrue();
        assertThat(resultActions.getMessage())
                .isNotNull().isNotEmpty()
                .isEqualTo(AddressController.ACTIVE_ADDRESS_LOOKUP_FAILURE_MESSAGE);
        assertThat(resultActions.getAddresses()).isNull();
        verify(addressService, times(1)).getAllActiveAddresses(any(PageRequest.class));
    }


    //REQUESTS
    private MockHttpServletRequestBuilder getPagedAddressRequest(String mimeType, int pageNumber, int pageSize) {
        return get("/addresses/all/" + pageNumber + "/" + pageSize).accept(mimeType);
    }

    private MockHttpServletRequestBuilder getPagedActiveAddressRequest(String mimeType, boolean activeOnly, int pageNumber, int pageSize) {
        return get("/addresses/" + pageNumber + "/" + pageSize + "/" + activeOnly).accept(mimeType);
    }

    private MockHttpServletRequestBuilder getAllActiveAddressListRequest(String mimeType, boolean activeOnly) {
        return get("/addresses/" + activeOnly).accept(mimeType);
    }

    private MockHttpServletRequestBuilder getAllAddressListRequest(String mimeType) {
        return get("/addresses/all").accept(mimeType);
    }

    private MockHttpServletRequestBuilder deleteAddressRequest(String mimeType, long idOfAddressToDelete) {
        return delete("/address/" + idOfAddressToDelete).accept(mimeType);
    }

    private MockHttpServletRequestBuilder postAddressRequest(String mimeType, String content)
            throws javax.xml.bind.JAXBException {
        return post("/address").content(content)
                .accept(mimeType).contentType(mimeType);
    }

    private MockHttpServletRequestBuilder postAddressesRequest(String mimeType, String content) throws javax.xml.bind.JAXBException {
        return post("/addresses").content(content)
                .accept(mimeType).contentType(mimeType);
    }

    private MockHttpServletRequestBuilder getAddressRequest(String mimeType, long addressId) {
        return get("/address/" + addressId).accept(mimeType);
    }


    //VERIFICATION
    private void verifyAllPagedLookup(int pageNumber, int pageSize) {
        final ArgumentCaptor<PageRequest> pageRequestArgumentCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(addressService, times(1)).getAllOrderedAddresses(pageRequestArgumentCaptor.capture());
        assertThat(pageRequestArgumentCaptor.getValue().getPageNumber()).isEqualTo(pageNumber);
        assertThat(pageRequestArgumentCaptor.getValue().getPageSize()).isEqualTo(pageSize);
    }

    private void verifyActivePagedLookup(int pageNumber, int pageSize) {
        final ArgumentCaptor<PageRequest> pageRequestArgumentCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(addressService, times(1)).getAllActiveAddresses(pageRequestArgumentCaptor.capture());
        assertThat(pageRequestArgumentCaptor.getValue().getPageNumber()).isEqualTo(pageNumber);
        assertThat(pageRequestArgumentCaptor.getValue().getPageSize()).isEqualTo(pageSize);
    }

    private void verifyPageContent(AddressPageResponse resultActions) {
        assertThat(resultActions.isError()).isFalse();
        assertThat(resultActions.getMessage()).isNotNull().isEmpty();
        assertThat(resultActions.getAddresses()).isNotNull();
        assertThat(resultActions.getAddresses().getContent())
                .isNotNull().isNotEmpty()
                .hasSize(pageSize);
        assertThat(resultActions.getAddresses().getNumber()).isEqualTo(pageNumber);
        assertThat(resultActions.getAddresses().getTotalPages()).isEqualTo(savedAddresses.size() / pageSize);
        assertThat(resultActions.getAddresses().getTotalElements()).isEqualTo(savedAddresses.size());
        assertThat(resultActions.getAddresses().getNumberOfElements()).isEqualTo(pageSize);
    }

    private void verifySavedAddress(Address savedAddress, Address compareTo) {
        assertThat(savedAddress.getId()).isEqualTo(compareTo.getId());
        assertThat(savedAddress.getStreet()).isEqualTo(compareTo.getStreet());
        assertThat(savedAddress.getStreet2()).isEqualTo(compareTo.getStreet2());
        assertThat(savedAddress.getCity()).isEqualTo(compareTo.getCity());
        assertThat(savedAddress.getState()).isEqualTo(compareTo.getState());
        assertThat(savedAddress.getZip()).isEqualTo(compareTo.getZip());
        assertThat(savedAddress.isActive()).isEqualTo(compareTo.isActive());
    }

    private void verifyAddressWasSaved(Address addressToSave) {
        final ArgumentCaptor<Address> addressArgumentCaptor = ArgumentCaptor.forClass(Address.class);
        verify(addressService, times(1)).saveNewAddress(addressArgumentCaptor.capture());
        final Address address = addressArgumentCaptor.getValue();
        assertThat(address.getId()).isEqualTo(addressToSave.getId());
        assertThat(address.getStreet()).isEqualTo(addressToSave.getStreet());
        assertThat(address.getStreet2()).isEqualTo(addressToSave.getStreet2());
        assertThat(address.getCity()).isEqualTo(addressToSave.getCity());
        assertThat(address.getState()).isEqualTo(addressToSave.getState());
        assertThat(address.getZip()).isEqualTo(addressToSave.getZip());
        assertThat(address.isActive()).isEqualTo(addressToSave.isActive());
    }

    private void verifyAddressLookup(AddressResponse resultActions) {
        assertThat(resultActions.isError()).isFalse();
        assertThat(resultActions.getMessage()).isNotNull().isEmpty();
        assertThat(resultActions.getAddress()).isNotNull();
        assertThat(resultActions.getAddress().getId()).isEqualTo(addressId);
        assertThat(resultActions.getAddress().getStreet()).isEqualTo(address1.getStreet());
        assertThat(resultActions.getAddress().getStreet2()).isEqualTo(address1.getStreet2());
        assertThat(resultActions.getAddress().getCity()).isEqualTo(address1.getCity());
        assertThat(resultActions.getAddress().getState()).isEqualTo(address1.getState());
        assertThat(resultActions.getAddress().getZip()).isEqualTo(address1.getZip());
        assertThat(resultActions.getAddress().isActive()).isEqualTo(address1.isActive());
    }

    private void verifyFindAllOrdered() {
        verify(addressService, times(1)).getAllOrderedAddresses();
    }
}
