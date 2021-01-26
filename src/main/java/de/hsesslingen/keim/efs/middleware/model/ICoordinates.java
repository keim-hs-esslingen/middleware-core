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

import static java.lang.Math.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import static org.springframework.util.StringUtils.countOccurrencesOf;
import static org.springframework.util.StringUtils.isEmpty;

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

        double sinOfLat = sin(latDiff / 2d);
        double sinOfLon = sin(lonDiff / 2d);

        double cosLatA = cos(latA);
        double cosLatB = cos(latB);

        double result = DOUBLE_EARTH_RADIUS_KM * asin(sqrt((sinOfLat * sinOfLat) + (sinOfLon * sinOfLon * cosLatA * cosLatB)));
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

    /**
     * Checks whether the given lat and lon values are not null and within their
     * allowed boundaries.
     *
     * @param lat
     * @param lon
     * @return
     */
    public static boolean isValid(Double lat, Double lon) {
        return lat != null && lon != null
                && lat >= -90.0 && lat <= 90.0
                && lon >= -180.0 && lon <= 180.0;
    }

    /**
     * Checks whether the lat and lon values of the given coordinates are not
     * null and within their allowed boundaries.
     *
     * @param coordinates
     * @return
     */
    public static boolean isValid(ICoordinates coordinates) {
        return isValid(coordinates.getLat(), coordinates.getLon());
    }

    /**
     * Checks whether coordinates are not null and then whether the lat and lon
     * values of the given coordinates are not null and within their allowed
     * boundaries.
     *
     * @param coordinates
     * @return
     */
    public static boolean isValidAndNotNull(ICoordinates coordinates) {
        return coordinates != null && isValid(coordinates);
    }

    /**
     * Converts {@link coordinates} to a lat,lon string with no space after the
     * comma.
     *
     * @param coordinates
     * @return
     */
    public static String toLatLonString(ICoordinates coordinates) {
        return toLatLonString(coordinates, false);
    }

    /**
     * Converts {@link coordinates} to a lat,lon string with an optional space
     * after the comma.
     *
     * @param coordinates
     * @param includeSpace
     * @return
     */
    public static String toLatLonString(ICoordinates coordinates, boolean includeSpace) {
        return coordinates.getLat() + "," + (includeSpace ? " " : "") + coordinates.getLon();
    }

    /**
     * Tries to parse the given coordinates string and if successful passes the
     * parse latitude and longitude to the given {@link utilizer}. Upon fail,
     * the {@link fallbackOnFail} supplier is called and its value returned.
     * <p>
     * The {@link fallbackOnFail} supplier can also be used to throw an
     * exception.
     *
     * @param <C>
     * @param latCommaLonString
     * @param utilizer
     * @param onFailValueSupplier
     * @return
     */
    public static <C> C parse(String latCommaLonString, BiFunction<Double, Double, C> utilizer, Supplier<C> onFailValueSupplier) {
        if (isEmpty(latCommaLonString) || countOccurrencesOf(latCommaLonString, ",") != 1) {
            return onFailValueSupplier.get();
        }

        String[] split = latCommaLonString.split(",");

        try {
            return utilizer.apply(Double.valueOf(split[0]), Double.valueOf(split[1]));
        } catch (NumberFormatException ex) {
            return onFailValueSupplier.get();
        }
    }

    /**
     * Parses the given coordinates and passes them to the given
     * {@link utilizer} for further handling. If the string is incorrectly
     * formatted, an exception is thrown. This method does not detect whether
     * latitude or longitude exceed their allowed boundaries. Use
     * {@link ICoordinates#parseVali(String, BiFunction)} for this.
     *
     * @param <C>
     * @param latCommaLonString
     * @param utilizer
     * @return
     */
    public static <C> C parse(String latCommaLonString, BiFunction<Double, Double, C> utilizer) {
        return parse(latCommaLonString, utilizer, () -> {
            throw new IllegalArgumentException("Invalid format for position \"" + latCommaLonString + "\".");
        });
    }

    /**
     * Tries to parse the given coordinates string and validate the result and
     * if successful passes the parse latitude and longitude to the given
     * {@link utilizer}.Upon fail, the {@link fallbackOnFail} supplier is called
     * and its value returned.
     * <p>
     * The {@link fallbackOnFail} supplier can also be used to throw an
     * exception.
     *
     * @param <C>
     * @param latCommaLonString
     * @param utilizer
     * @param onFailValueSupplier
     * @return
     */
    public static <C> C parseAndValidate(String latCommaLonString, BiFunction<Double, Double, C> utilizer, Supplier<C> onFailValueSupplier) {
        return parse(latCommaLonString, (lat, lon) -> {
            if (!isValid(lat, lon)) {
                return onFailValueSupplier.get();
            }

            return utilizer.apply(lat, lon);
        }, onFailValueSupplier);
    }

    /**
     * Parses the given coordinates and passes them to the given
     * {@link utilizer} for further handling. If the string is incorrectly
     * formatted, or if the values of latitude or longitude exceed their allowed
     * boundaries, an exception is thrown.
     *
     * @param <C>
     * @param latCommaLonString
     * @param utilizer
     * @return
     */
    public static <C> C parseAndValidate(String latCommaLonString, BiFunction<Double, Double, C> utilizer) {
        return parse(latCommaLonString, (lat, lon) -> {
            if (!isValid(lat, lon)) {
                throw new IllegalArgumentException("The values of either latitude or longitude or both exceed their allowed boundaries.");
            }

            return utilizer.apply(lat, lon);
        });
    }

    /**
     * Parses the given lat,lon string to an anonymous implementation of
     * {@link ICoordinates}.
     *
     * @param latLonString
     * @return
     */
    public static ICoordinates parse(String latLonString) {
        return parse(latLonString, (lat, lon) -> {
            return new ICoordinates() {
                @Override
                public Double getLat() {
                    return lat;
                }

                @Override
                public Double getLon() {
                    return lon;
                }
            };
        });
    }

    /**
     * Parses the given lat,lon string to an anonymous implementation of
     * {@link ICoordinates}.
     *
     * @param latLonString
     * @param onFailValueSupplier
     * @return
     */
    public static ICoordinates parseAndValidate(String latLonString, Supplier<ICoordinates> onFailValueSupplier) {
        return parseAndValidate(latLonString, (lat, lon) -> {
            return new ICoordinates() {
                @Override
                public Double getLat() {
                    return lat;
                }

                @Override
                public Double getLon() {
                    return lon;
                }
            };
        }, onFailValueSupplier);
    }
}
