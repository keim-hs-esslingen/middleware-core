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
package de.hsesslingen.keim.efs.middleware.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import de.hsesslingen.keim.efs.middleware.exception.InvalidParameterException;
import de.hsesslingen.keim.efs.mobility.Coordinates;
import java.io.Serializable;

import org.springframework.util.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 *
 * @author boesch, K.Sivarasah
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Place extends Coordinates implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Human readable name of the place
     */
    private String name;

    private String stopId;

    private String stopCode;

    public Place(String latCommaLonString) {
        assertPositionIsValid(latCommaLonString);

        String[] split = latCommaLonString.split(",");

        this.setLat(Double.valueOf(split[0]));
        this.setLon(Double.valueOf(split[1]));
    }

    public Place(String latCommaLonString, String name, String stopId, String stopCode) {
        assertPositionIsValid(latCommaLonString);

        String[] split = latCommaLonString.split(",");

        this.setLat(Double.valueOf(split[0]));
        this.setLon(Double.valueOf(split[1]));

        this.name = name;
        this.stopId = stopId;
        this.stopCode = stopCode;
    }

    public Place(String latCommaLonString, String name) {
        assertPositionIsValid(latCommaLonString);

        String[] split = latCommaLonString.split(",");

        this.setLat(Double.valueOf(split[0]));
        this.setLon(Double.valueOf(split[1]));

        this.name = name;
    }

    private void assertPositionIsValid(String pos) {
        if (StringUtils.isEmpty(pos) || StringUtils.countOccurrencesOf(pos, ",") != 1) {
            throw new InvalidParameterException("Invalid format for position " + pos);
        }
    }

    @Override
    public void updateSelfFrom(Object other) {
        super.updateSelfFrom(other);

        if (other instanceof Place) {
            Place o = (Place) other;
            this.name = o.name;
            this.stopCode = o.stopCode;
            this.stopId = o.stopId;
        }
    }

}
