package org.borman.model;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@Entity

@Table(name = Address.table_name, uniqueConstraints = @UniqueConstraint(name = "UK_ADDRESS", columnNames = {
        Address.street_vlu, Address.street_2_vlu, Address.city_name, Address.state_cd,
        Address.zip_cd}))
@NamedQueries({
        @NamedQuery(name = "Address.findActiveAddresses", query = "select a from Address a where a.active = 1 order by a.state, a.city, a.zip, a.street, a.street2"),
        @NamedQuery(name = "Address.findAllAddresses", query = "select a from Address a order by a.state, a.city, a.zip, a.street, a.street2"),
        @NamedQuery(name = "Address.countExistingMatchingAddress", query = "select count(a) from Address a where a.street = :street and a.street2 = :street2 and a.city = :city and a.state = :state and a.zip = :zip group by a.state, a.city, a.street, a.street2, a.zip"),
})
@XmlRootElement(name = "address")
public class Address
{
    static final String table_name = "ADDRESS";

    static final String id_name      = table_name + "_ID";
    static final String street_vlu   = "STREET_VLU";
    static final String street_2_vlu = "STREET_2_VLU";
    static final String city_name    = "CITY_NAM";
    static final String state_cd     = "STATE_CD";
    static final String zip_cd       = "ZIP_CD";
    static final String active_flag  = "ACTIVE_FLG";

    public static final String MISSING_STREET_ERROR = "Street must be provided for an address.";
    public static final String MISSING_CITY_ERROR = "City must be provided for an address.";
    public static final String INVALID_STATE_ERROR = "State must be 2 letters, ex. 'NY'.";
    public static final String MISSING_STATE_ERROR = "Zip code must be provided for an address.";
    public static final String INVALID_ZIP_CODE_ERROR = "Zip code must be 5 numbers long. ex. 12345.";
    public static final String MISSING_ZIP_CODE_ERROR = "Zip code must be provided for an address.";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = id_name)
    protected Long id;
    @Column(name = street_vlu, length = 80, nullable = false)
    @NotBlank(message = MISSING_STREET_ERROR)
    protected String street;

    @Column(name = street_2_vlu, length = 50, nullable = true)
    protected String street2 = "";

    @Column(name = city_name, nullable = false, length = 50)
    @NotBlank(message = MISSING_CITY_ERROR)
    protected String city;

    @Column(name = state_cd, nullable = false, length = 2)
    @NotBlank(message = MISSING_STATE_ERROR)
    @Pattern(regexp = "^[a-zA-Z]{2}$", message = INVALID_STATE_ERROR)
    protected String state;

    @Column(name = zip_cd, nullable = false)
    @Max(value = 99999, message = INVALID_ZIP_CODE_ERROR)
    @Min(value = 10000, message = INVALID_ZIP_CODE_ERROR)
    @NotNull(message = MISSING_ZIP_CODE_ERROR)
    protected Integer zip;

    @Column(name = active_flag, nullable = false)
    @Type(type = "org.hibernate.type.NumericBooleanType")
    protected boolean active;

    protected Address()
    {
    }

    public Address(final String street, final String street2, final String city, final String state, final Integer zip,
                   final boolean active)
    {
        this();
        setCity(city);
        setState(state);
        setStreet2(street2);
        setStreet(street);
        setZip(zip);
        setActive(active);
    }

    public Address(final String street, final String city, final String state, final Integer zip, final boolean active)
    {
        this(street, null, city, state, zip, active);
    }


    //GETTERS
    @XmlAttribute
    public String getCity()
    {
        return city == null ? "" : city.toUpperCase();
    }

    @XmlAttribute
    public Long getId() {
        return id;
    }

    @XmlAttribute
    public String getState()
    {
        return state == null ? "" : state.toUpperCase();
    }

    @XmlAttribute
    public String getStreet2()
    {
        return street2 == null ? "" : street2.toUpperCase();
    }

    @XmlAttribute
    public String getStreet()
    {
        return street == null ? "" : street.toUpperCase();
    }

    @XmlAttribute
    public Integer getZip()
    {
        return zip;
    }

    @XmlElement
    public boolean isActive()
    {
        return active;
    }


    //SETTERS
    public final void setId(final Long id)
    {
        this.id = id;
    }

    protected final void setCity(final String city)
    {
        if (city != null)
        {
            this.city = city.toUpperCase();
        }
    }

    protected final void setState(final String state)
    {
        if (state != null)
        {
            this.state = state.toUpperCase();
        }
    }

    protected final void setStreet2(final String street2)
    {
        if (street2 != null)
        {
            this.street2 = street2.toUpperCase();
        }
    }

    protected final void setStreet(final String street)
    {
        if (street != null)
        {
            this.street = street.toUpperCase();
        }
    }

    protected final void setZip(final Integer zip)
    {
        this.zip = zip;
    }

    protected final void setActive(final boolean active)
    {
        this.active = active;
    }

    public String toString()
    {
        ReflectionToStringBuilder bldr = new ReflectionToStringBuilder(this);
        bldr.setAppendTransients(true);
        return bldr.toString();
    }
}
