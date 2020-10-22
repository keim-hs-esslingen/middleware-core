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
package conditionalbeans.provider;

import conditionalbeans.BeansCollector;
import conditionalbeans.provider.dummybeans.bookingservice.DummyBookingService;
import conditionalbeans.provider.dummybeans.credentialsdeserializer.DummyCredentialsDeserializer;
import conditionalbeans.provider.dummybeans.credentialsservice.DummyCredentialsService;
import conditionalbeans.provider.dummybeans.placesservice.DummyPlacesService;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SomeProviderBeansTest_2.class})
@SpringBootApplication
@ComponentScan(
        basePackages = "de.hsesslingen.keim.efs.middleware.provider",
        basePackageClasses = {
            DummyPlacesService.class,
            //DummyOptionsService.class,
            DummyBookingService.class,
            DummyCredentialsService.class,
            DummyCredentialsDeserializer.class
        }
)
@ActiveProfiles("beans-provider")
public class SomeProviderBeansTest_2 extends BeansCollector {

    public static void main(String[] args) {
        SpringApplication.run(SomeProviderBeansTest_2.class, args);
    }

    @Test
    public void testBeanExistence() {
        assertNotNull(placesService);
        assertNotNull(placesApi);

        assertNull(optionsApi);
        assertNull(optionsService);

        assertNotNull(bookingApi);
        assertNotNull(bookingService);

        assertNotNull(credentialsApi);
        assertNotNull(credentialsService);
        assertNotNull(credentialsDeserializer);

        assertNotNull(serviceInfoApi);

        assertNull(sdProxy);
    }

}
