/*
 * MIT License
 * 
 * Copyright (c) 2020 Hochschule Esslingen
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE. 
 */
package de.hsesslingen.keim.efs.middleware.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import static de.hsesslingen.keim.efs.middleware.model.ICoordinates.isValid;
import java.io.Serializable;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * An object representing a place, described not only by coordinates.
 *
 * @author boesch, K.Sivarasah
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Place implements ICoordinates, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The ID of the mobility service, that this place belongs to.
     * <p>
     * This value is not constrained to be non-null or non-empty because this
     * class can also be used to describe places that do not belong to any
     * mobility service.
     */
    private String serviceId;

    /**
     * minimum: -90
     * <p>
     * maximum: 90
     * <p>
     * example: 52.376883 The field value must be a valid WGS84 latitude in
     * decimal degrees format.
     */
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double lat;

    /**
     * minimum: -180
     * <p>
     * maximum: 180
     * <p>
     * example: 4.90017 The field value must be a valid WGS84 longitude in
     * decimal degrees format.
     */
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double lon;

    /**
     * A unique identifier for this place. This ID is provider specific. This
     * means that this ID can only be expected to be recognized by that
     * provider, from which this place originated.
     */
    private String id;

    /**
     * Human readable name of the place
     */
    private String name;

    /**
     * If this place belongs to a hierarichal location system, this field can
     * contain the id of the parent place.
     */
    private String parentId;

    /**
     * If this place belongs to a hierarichal location system, this field can
     * contain a human friendly name of the parent place.
     */
    private String parentName;

    public Place(String id) {
        this.id = id;
    }

    /**
     * Updates this instance with the latitude and longitude values of the given
     * ICoordinates.If null is passed, nothing happens.
     *
     * @param coordinates
     * @return
     */
    @JsonIgnore
    public Place setCoordinates(ICoordinates coordinates) {
        if (coordinates != null) {
            this.lat = coordinates.getLat();
            this.lon = coordinates.getLon();
        }

        return this;
    }

    /**
     * Updates this instance with the given latitude and longitude values.
     *
     * @param lat
     * @param lon
     * @return
     */
    @JsonIgnore
    public Place setCoordinates(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        return this;
    }

    @JsonIgnore
    public boolean hasCoordinates() {
        return isValid(this);
    }

    @JsonIgnore
    public void updateSelfFrom(Place other) {
        this.setCoordinates(other);

        this.id = other.id;
        this.name = other.name;
        this.parentId = other.parentId;
        this.parentName = other.parentName;
    }

    public static Place fromCoordinates(double lat, double lon) {
        var place = new Place();
        place.setCoordinates(lat, lon);
        return place;
    }

    public static Place fromCoordinates(ICoordinates coordinates) {
        var place = new Place();
        place.setCoordinates(coordinates);
        return place;
    }

    public static Place fromCoordinates(String latCommaLonString) {
        return ICoordinates.parseAndValidate(latCommaLonString, Place::fromCoordinates);
    }

}
