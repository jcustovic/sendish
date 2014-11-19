package com.sendish.repository.model.jpa;

import com.sendish.repository.model.jpa.listener.LocationAware;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import java.io.Serializable;
import java.math.BigDecimal;

@Embeddable
public class Location implements LocationAware, Serializable {

    private static final long serialVersionUID = 1L;

    @Transient
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

    @Column(name = "loc_latitude", nullable = false)
    private BigDecimal latitude;

    @Column(name = "loc_longitude", nullable = false)
    private BigDecimal longitude;

    @Type(type = "org.hibernate.spatial.GeometryType")
    @Column(name = "loc_location", nullable = false)
    private Point location;

    public Location() {
        // Hibernate
    }

    public Location(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public void buildLocation() {
        location = geometryFactory.createPoint(new Coordinate(longitude.doubleValue(), latitude.doubleValue()));
    }

    // Getters & setters

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public Point getLocation() {
        return location;
    }

}
