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

import static de.hsesslingen.keim.efs.middleware.model.ICoordinates.assertPositionIsValid;
import java.io.Serializable;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author keim
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coordinates implements Serializable, ICoordinates {

    private static final long serialVersionUID = 1L;

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

    public static Coordinates parse(String latCommaLonString) {
        assertPositionIsValid(latCommaLonString);
        String[] split = latCommaLonString.split(",");
        return new Coordinates(Double.valueOf(split[0]), Double.valueOf(split[1]));
    }

    public static Coordinates copy(ICoordinates other) {
        return new Coordinates(other.getLat(), other.getLon());
    }
}
