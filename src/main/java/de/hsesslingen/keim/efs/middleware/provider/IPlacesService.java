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
package de.hsesslingen.keim.efs.middleware.provider;

import de.hsesslingen.keim.efs.middleware.model.ICoordinates;
import de.hsesslingen.keim.efs.middleware.model.Place;
import de.hsesslingen.keim.efs.middleware.provider.credentials.AbstractCredentials;
import java.util.List;

/**
 * This interface represents the API, that the places rest controller uses to
 * perform the possible actions provided by the placse API. That API will only
 * be available if this interface is implemented and provided as a spring bean.
 *
 * @author keim
 * @param <C>
 */
public interface IPlacesService<C extends AbstractCredentials> {

    /**
     * API for searching provider specific places by text.The text is used as a
     * query to find places, whose properties match this text at least
     * partially.This can be understood as a way to find places by arbitrary
     * text searches, such as names of places, or addresses or even coordinates
     * or ids.
     *
     * @param query The text that is to be used as query for searching places.
     * @param areaCenter An optional geo-location that defines the center of a
     * circular search area contrained by param {@link radiusMeter}. If no
     * radius is given, a default radius is chosen by the provider.
     * @param radiusMeter A radius in unit meter, that serves as a constraint
     * for param {@link areaCenter}. Only applied together with areaCenter.
     * @param limitTo An optional upper limit of results for the response.
     * @param credentials The credentials needed to authenticate and authorize
     * oneself to perform this action
     * @return
     */
    public List<Place> search(
            String query,
            ICoordinates areaCenter,
            Integer radiusMeter,
            Integer limitTo,
            C credentials
    );

}
