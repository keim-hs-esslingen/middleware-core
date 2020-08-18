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
import java.time.Instant;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.hsesslingen.keim.efs.middleware.utils.InstantEpochMilliDeserializer;
import de.hsesslingen.keim.efs.middleware.utils.InstantEpochMilliSerializer;
import de.hsesslingen.keim.efs.middleware.validation.OnCreate;
import de.hsesslingen.keim.efs.middleware.validation.TimeIsInFutureInstant;
import de.hsesslingen.keim.efs.mobility.service.Mode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Contains the route data about a mobility option.
 *
 * @author boesch, K.Sivarasah
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Leg implements Serializable {

    private static final long serialVersionUID = 1L;

    public Leg(Instant startTime, Place from, Mode mode) {
        this.startTime = startTime;
        this.from = from;
        this.mode = mode;
    }

    public Leg(Instant startTime, Place from) {
        this.startTime = startTime;
        this.from = from;
    }

    @JsonSerialize(using = InstantEpochMilliSerializer.class)
    @JsonDeserialize(using = InstantEpochMilliDeserializer.class)
    @TimeIsInFutureInstant(groups = OnCreate.class)
    private Instant startTime;

    @JsonSerialize(using = InstantEpochMilliSerializer.class)
    @JsonDeserialize(using = InstantEpochMilliDeserializer.class)
    @TimeIsInFutureInstant(groups = OnCreate.class)
    private Instant endTime;

    @NotNull
    @Valid
    @JsonProperty(required = true)
    private Place from;

    @Valid
    private Place to;

    @NotEmpty
    @JsonProperty(required = true)
    private String serviceId;

    private Mode mode;

    /**
     * Distance/length of this leg in meter.
     */
    private Integer distance;
}
