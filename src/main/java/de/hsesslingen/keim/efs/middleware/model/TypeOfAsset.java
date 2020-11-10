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

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.hsesslingen.keim.efs.mobility.service.Mode;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Contains information about a particular type of assets. This type is provider
 * specific and not standardizied, which means that types with equal ids of
 * different providers do NOT necessarily represent the same type.
 *
 * @author boesch, K.Sivarasah
 */
@Data
@Accessors(chain = true)
@Deprecated(since = "3.0.2", forRemoval = true)
public class TypeOfAsset {

    /**
     * unique identifier of a type.
     */
    @NotEmpty
    @JsonProperty(required = true)
    private String typeID;

    /**
     * The name of the asset type.
     */
    @JsonProperty(required = true)
    @NotEmpty
    private String name;

    /**
     * The mode of this asset type.
     */
    private Mode mode;

    /**
     * true indicates asset is allowed to travel abroad
     */
    private Boolean travelAbroad;

    /**
     * true indicates airconditioning required
     */
    private Boolean airconditioning;

    /**
     * classification of the asset
     */
    private String assetClass;

    /**
     * brand of the asset
     */
    private String brand;

    /**
     * true indicates cabrio required
     */
    private Boolean cabrio;

    /**
     * Colour of the asset.
     */
    private String colour;

    /**
     * describes options to carry cargo
     */
    private String cargo;

    /**
     * describes if asset is or needs to be easily accessible
     */
    private AccessibilityArrangement easyAccessibility;

    /**
     * number of gears of the asset
     */
    private Integer gears;

    /**
     * type of gearbox
     */
    private Gearbox gearbox;

    /**
     * URL to image of this asset type.
     */
    private String image;

    /**
     * true indicates infant seat available.
     */
    private Boolean infantseat;

    /**
     * Number of persons able to use one particular asset of this type.
     */
    private Integer persons;

    /**
     * true indicates pets are allowed on assets of this type.
     */
    private Boolean pets;

    /**
     * How this asset type is powered.
     */
    private Propulsion propulsion;

    /**
     * true indicates smoking is allowed on assets of this type.
     */
    private Boolean smoking;

    /**
     * percentage of charge available. (0-100).
     */
    private Integer stateofcharge;

    /**
     * true indicates towing hook required
     */
    private Boolean towinghook;

    /**
     * true indicates underground parking is allowed with asset
     */
    private Boolean undergroundparking;

    /**
     * true indicates winter tires are mounted.
     */
    private Boolean wintertires;

    /**
     * free text to describe asset
     */
    private String other;

}
