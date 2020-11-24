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

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.hsesslingen.keim.efs.middleware.validation.OnCreate;
import de.hsesslingen.keim.efs.mobility.service.Mode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import de.hsesslingen.keim.efs.middleware.validation.IsInFutureOrNull;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Contains the route data about a mobility option. Information about the asset
 * that is associated to this mobility option is contained in the property asset
 * or can be retrieved using the providers Asset API with the assetID in this
 * object.
 *
 * @author boesch, K.Sivarasah
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Leg implements Serializable {

    private static final long serialVersionUID = 1L;

    public Leg(Place from, ZonedDateTime startTime, Mode mode) {
        this.from = from;
        this.startTime = startTime;
        this.mode = mode;
    }

    public Leg(Place from, ZonedDateTime startTime) {
        this.from = from;
        this.startTime = startTime;
    }

    @IsInFutureOrNull(groups = OnCreate.class)
    private ZonedDateTime startTime;

    @IsInFutureOrNull(groups = OnCreate.class)
    private ZonedDateTime endTime;

    @NotNull
    @JsonProperty(required = true)
    private Place from;

    private Place to;

    /**
     * An optional assetId that identifies the optional {@link asset} that
     * belongs to this leg. See {@link asset} for more details. If
     * {@link assetId} is not given, this ID might still be available in the
     * {@link asset} object, if that one is present.
     */
    private String assetId;

    /**
     * An optional asset that belongs to this leg. This means that this asset
     * will/can/must be used to move along this leg.
     */
    private Asset asset;

    public String getAssetId() {
        if (assetId != null) {
            return assetId;
        } else if (asset != null) {
            return asset.getId();
        }

        return null;
    }

    /**
     * An optional list of sub-legs that describe this leg in higher detail.
     */
    private List<Leg> subLegs;

    /**
     * A list of coordinates describing the path of this leg. The resolution of
     * this path is unspecified and can therefore be very rough or highly
     * detailed. If sub legs are present, those sub legs can again contain a
     * path, allowing this leg to contain are rather rough or symbolic
     * representation of the path and leaving the higher resolution to the sub
     * legs.
     */
    private List<Coordinates> geoPath;

    /**
     * The mode of this leg.
     */
    private Mode mode;

    /**
     * Distance/length of this leg in meter.
     */
    private Integer distanceMeter;

    public Leg updateSelfFrom(Leg other) {
        this.startTime = other.startTime;
        this.endTime = other.endTime;
        this.from = other.from;
        this.to = other.to;
        this.assetId = other.assetId;
        this.asset = other.asset;
        this.subLegs = other.subLegs;
        this.geoPath = other.geoPath;
        this.mode = other.mode;
        this.distanceMeter = other.distanceMeter;
        return this;
    }

}
