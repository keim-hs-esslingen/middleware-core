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
import java.util.List;
import static java.util.stream.Collectors.joining;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Contains meta data about an asset.
 *
 * @author boesch, K.Sivarasah
 */
@Data
@Accessors(chain = true)
public class Asset {

    /**
     * Unique but provider-specific id of asset.
     */
    private String id;

    /**
     * Human readable name of this asset.
     */
    @JsonProperty(required = true)
    @NotEmpty
    private String name;

    /**
     * Mode of this asset.
     */
    private Mode mode;

    /**
     * A value that identifies the type of this asset. The type is defined by
     * the provider and can only be expected to be recognized in the scope of
     * this provider.
     */
    private String typeId;

    /**
     * A humand readable name for the type which is identified by
     * {@link typeId}.
     */
    private String typeName;

    /**
     * Color of the asset.
     */
    private String color;

    /**
     * URL to image of this asset.
     */
    private String imageUrl;

    /**
     * Percentage of charge available (0-100). This value only exists if it is
     * applicable to this asset. This is e.g. not the case for solely
     * muscle-powered bikes, but IS the case for electrical powered bikes.
     */
    @Min(0)
    @Max(100)
    private Integer stateOfCharge;

    /**
     * True indicates whether winter tires are mounted on this asset. This
     * property is only present if applicable to this asset and if this asset
     * can be expected to be used in environments where winter tires are
     * necessary.
     */
    private Boolean hasWinterTires;

    /**
     * Whether this asset has air conditioning if this is applicable. A value of
     * null does not mean that there is no air conditioning. Instead this either
     * means that this value is not applicable to this asset, or it wasn't
     * transfered in the query in which this object was received. If this asset
     * was queried using the asset API, which will be added later, and this
     * value is {@code null}, than this property can be considered to be not
     * applicable to this asset.
     */
    private Boolean hasAirConditioning;

    /**
     * Whether this asset is a cabrio or not.
     */
    private Boolean isCabrio;

    /**
     * A human readable name of the brand of this asset. This represent the
     * brand of the manufacturer who built this asset, not the provider (except
     * both are the same).
     */
    private String brandName;

    /**
     * A human readable name of the model of this asset. This represents the
     * model of the manufacturer, who built this asset.
     */
    private String modelName;

    /**
     * What kind of gear system is present in this asset.
     */
    private Gearbox gearbox;

    /**
     * How many gears this asset has, if applicable.
     */
    private Integer numberOfGears;

    /**
     * How this asset is powered.
     */
    private Propulsion propulsion;

    /**
     * Whether this asset has an infantseat ready to be used.
     */
    private Boolean hasInfantseat;

    /**
     * How many persons are allowed to use this asset simultaneously.
     */
    private Integer allowedNumberOfPersons;

    /**
     * Whether pets are allowed in this asset.
     */
    private Boolean arePetsAllowed;

    /**
     * Whether smoking is allowed in this asset.
     */
    private Boolean isSmokingAllowed;

    /**
     * Whether this asset has a towing hook.
     */
    private Boolean hasTowingHook;

    /**
     * The maximum amount of cargo in liters that can be loaded to this asset.
     */
    private Integer maxCargoLiter;

    /**
     * The maximum amount of cargo in kilograms that can be loaded to this
     * asset.
     */
    private Integer maxCargoKg;

    /**
     * Arrangements of interest for people with disabilities given for this
     * asset.
     */
    private List<AccessibilityArrangement> accessibilityArangements;

    /**
     * Whether users are allowed to park this asset in garages.
     */
    private Boolean isParkingInGarageAllowed;

    /**
     * A short, human readable summary about this asset. Max length is 140
     * characters.
     */
    private String summary;

    /**
     * Free and human readable text that contains additional information about
     * this asset.
     */
    private String description;

    /**
     * Unspecified object that extends information about this asset.
     */
    private Object other;

    @Deprecated(since = "3.0.2", forRemoval = true)
    public TypeOfAsset toTypeOfAsset() {
        return new TypeOfAsset()
                .setMode(mode)
                .setTypeID(typeId)
                .setName(typeName)
                .setColour(color)
                .setImage(imageUrl)
                .setStateofcharge(stateOfCharge)
                .setWintertires(hasWinterTires)
                .setAirconditioning(hasAirConditioning)
                .setCabrio(isCabrio)
                .setBrand(brandName)
                .setAssetClass(modelName)
                .setGearbox(gearbox)
                .setGears(numberOfGears)
                .setPropulsion(propulsion)
                .setInfantseat(hasInfantseat)
                .setPersons(allowedNumberOfPersons)
                .setPets(arePetsAllowed)
                .setSmoking(isSmokingAllowed)
                .setTowinghook(hasTowingHook)
                .setUndergroundparking(isParkingInGarageAllowed)
                .setCargo(
                        List.of(
                                maxCargoKg != null ? maxCargoKg + "kg" : null,
                                maxCargoLiter != null ? maxCargoLiter + "l" : null
                        ).stream().filter(n -> n != null).collect(joining(", "))
                )
                .setOther(
                        List.of(
                                summary != null ? summary : null,
                                description != null ? description : null
                        ).stream().filter(n -> n != null).collect(joining("\n\n"))
                )
                .setEasyAccessibility(
                        this.accessibilityArangements == null
                                ? null
                                : accessibilityArangements.stream().findFirst().orElse(null)
                );
    }
}
