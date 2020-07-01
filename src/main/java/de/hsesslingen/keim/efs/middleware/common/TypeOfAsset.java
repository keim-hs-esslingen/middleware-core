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

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.hsesslingen.keim.efs.mobility.service.Mode;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Contains meta data about an asset.
 *
 * @author boesch, K.Sivarasah
 */
@Data
@Accessors(chain = true)
public class TypeOfAsset {

    /**
     * unique identifier of a type
     */
    @NotEmpty
    @JsonProperty(required = true)
    private String typeID;

    /**
     * name of asset type
     */
    @JsonProperty(required = true)
    @NotEmpty
    private String name;

    private Mode mode;

    /**
     * true indicates asset is allowed to travel abroad
     */
    private boolean travelAbroad;

    /**
     * true indicates airconditioning required
     */
    private boolean airconditioning;

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
    private boolean cabrio;

    /**
     * colour of the asset
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
    private int gears;

    /**
     * type of gearbox
     */
    private Gearbox gearbox;

    /**
     * URL
     */
    private String image;

    /**
     * true indicates infant seat required
     */
    private boolean infantseat;

    /**
     * number of persons able to use the asset
     */
    private int persons;

    /**
     * true indicates pets are allowed on asset
     */
    private boolean pets;

    /**
     * way in which the asset is powered
     */
    private Propulsion propulsion;

    /**
     * true indicates smoking is allowed on asset
     */
    private boolean smoking;

    /**
     * percentage of charge available. (0-100)
     */
    private int stateofcharge;

    /**
     * true indicates towing hook required
     */
    private boolean towinghook;

    /**
     * true indicates underground parking is allowed with asset
     */
    private boolean undergroundparking;

    /**
     * true indicates winter tires required
     */
    private boolean wintertires;

    /**
     * free text to describe asset
     */
    private String other;

}
