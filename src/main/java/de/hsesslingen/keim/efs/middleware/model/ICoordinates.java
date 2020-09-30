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

import de.hsesslingen.keim.efs.middleware.exception.InvalidParameterException;
import org.springframework.util.StringUtils;

/**
 * This interface describes the stereotype of 2 dimensional geo coordinates.
 * <p>
 * It can be used on classes that contian such coordinates to make them
 * compatible with some generic functions.
 * <p>
 * This interface also contains some static methods to calculate the distance in
 * kilometers between two coordinates and to convert between degrees and
 * radians.
 *
 * @author boesch
 */
public interface ICoordinates {

    /**
     * Used in distance calculations between coordinates.
     */
    public static final double DOUBLE_EARTH_RADIUS_KM = 2.0 * 6371.0;

    /**
     * Used to convert degrees to radians.
     */
    public static final double PI_BY_D180 = Math.PI / 180d;

    /**
     * Used to convert radians to degrees.
     */
    public static final double D180_BY_PI = 180d / Math.PI;

    /**
     * Get the latitude value of this coordinates pair.
     *
     * @return
     */
    public Double getLat();

    /**
     * Get the longitude value of this coordinates pair.
     *
     * @return
     */
    public Double getLon();

    /**
     * Calculates the distance in kilometers between two coordinates using the
     * haversine formula. (https://en.wikipedia.org/wiki/Haversine_formula)
     *
     * @param a ICoordinates
     * @param b ICoordinates
     * @return The distance between two coordinates in kilometers.
     */
    public static double distanceKmBetween(ICoordinates a, ICoordinates b) {
        double latA = degreesToRadians(a.getLat());
        double latB = degreesToRadians(b.getLat());
        double lonA = degreesToRadians(a.getLon());
        double lonB = degreesToRadians(b.getLon());

        double latDiff = latB - latA;
        double lonDiff = lonB - lonA;

        double sinOfLat = Math.sin(latDiff / 2d);
        double sinOfLon = Math.sin(lonDiff / 2d);

        double cosLatA = Math.cos(latA);
        double cosLatB = Math.cos(latB);

        double result = DOUBLE_EARTH_RADIUS_KM * Math.asin(Math.sqrt((sinOfLat * sinOfLat) + (sinOfLon * sinOfLon * cosLatA * cosLatB)));
        return result;
    }

    /**
     * Converts degrees to radians. Can be universally used.
     *
     * @param deg
     * @return
     */
    public static double degreesToRadians(double deg) {
        return deg * PI_BY_D180;
    }

    /**
     * Converts radians to degrees. Can be universally used.
     *
     * @param rad
     * @return
     */
    public static double radiansToDegrees(double rad) {
        return rad * D180_BY_PI;
    }

    public static boolean positionIsValid(String latLonString) {
        return !StringUtils.isEmpty(latLonString) && StringUtils.countOccurrencesOf(latLonString, ",") == 1;
    }

    public static boolean isValid(ICoordinates coordinates) {
        return isValid(coordinates.getLat(), coordinates.getLon());
    }

    public static boolean isValid(Double lat, Double lon) {
        return lat != null && lon != null
                && lat >= -90.0 && lat <= 90.0
                && lon >= -180.0 && lon <= 180.0;
    }

    public static void assertPositionIsValid(String latLonString) {
        if (!positionIsValid(latLonString)) {
            throw new InvalidParameterException("Invalid format for position " + latLonString);
        }
    }

}
