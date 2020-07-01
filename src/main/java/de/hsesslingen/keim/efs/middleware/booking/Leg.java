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
package de.hsesslingen.keim.efs.middleware.booking;

import java.io.Serializable;
import java.time.Duration;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.hsesslingen.keim.efs.middleware.common.LegBaseItem;
import de.hsesslingen.keim.efs.middleware.common.Place;
import de.hsesslingen.keim.efs.mobility.service.Mode;
import io.swagger.annotations.ApiModel;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * A OpenTripPlanner compatible definition of a leg (see OpenTripPlanner docs
 * for reference)
 *
 * @author boesch, , K.Sivarasah
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "A OpenTripPlanner compatible definition of a leg (see OpenTripPlanner docs for reference)")
public class Leg extends LegBaseItem implements Serializable {

    private static final long serialVersionUID = 1L;

    public Leg(Instant startTime, Place from, Mode mode) {
        super(startTime, from);
        this.mode = mode;
    }

    @NotNull
    @JsonProperty(required = true)
    private Mode mode;

    private Duration departureDelay;
    private Duration arrivalDelay;
    private int distance;
    // private String fare;
    private String route;
    private String routeShortName;
    private String routeLongName;

}
