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
package middleware.provider.credentials;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import de.hsesslingen.keim.efs.middleware.provider.credentials.CredentialsUtils;
import de.hsesslingen.keim.efs.mobility.utils.EfsRequest;

/**
 * @author k.sivarasah
 * 12 Nov 2019
 */
public class CredentialUtilsTest {

	private static final String ID_KEY = "id";
	private static final String ID_VALUE = "id_001";
	private static final String LOGINKEY_KEY = "demoLoginKey";
	private static final String LOGINKEY_VALUE = "loginkey_001";
	private static final String PROVIDERID_KEY = "serviceId";
	private static final String PROVIDERID_VALUE = "demo";
	
	private static Map<String, String> credentialMap;
	private static TestCredential credentialObject;
	private static String credentialString;
	
	@BeforeClass
	public static void setUp() {
		credentialMap = new HashMap<>();
		credentialMap.put(ID_KEY, ID_VALUE);
		credentialMap.put(LOGINKEY_KEY, LOGINKEY_VALUE);
		credentialMap.put(PROVIDERID_KEY, PROVIDERID_VALUE);
		
		credentialObject = new TestCredential(ID_VALUE, LOGINKEY_VALUE);
		credentialObject.setServiceId(PROVIDERID_VALUE);
		credentialString = String.format("{\"%s\":\"%s\",\"%s\":\"%s\",\"%s\":\"%s\"}", 
				PROVIDERID_KEY, PROVIDERID_VALUE, ID_KEY, ID_VALUE, LOGINKEY_KEY, LOGINKEY_VALUE);
		
	}
	
	@Test
	public void testFromMapToObject() {
		TestCredential result = CredentialsUtils.toCredentials(credentialMap, TestCredential.class);
		assertNotNull(result);
		assertEquals(credentialObject, result);
	}
	
	@Test
	public void testFromMapToObject_null() {
		TestCredential result = CredentialsUtils.toCredentials((Map<String, String>) null, TestCredential.class);
		assertNull(result);
	}
	
	@Test
	public void testFromStringToObject() {
		TestCredential result = CredentialsUtils.toCredentials(credentialString, TestCredential.class);
		assertNotNull(result);
		assertEquals(credentialObject, result);
	}
	
	@Test
	public void testFromStringToObject_null() {
		TestCredential result = CredentialsUtils.toCredentials((String) null, TestCredential.class);
		assertNull(result);
	}
	
	@Test
	public void testFromObjectToString() {
		String result = CredentialsUtils.toJsonString(credentialObject);
		assertNotNull(result);
		assertEquals(credentialString, result);
	}
	
	@Test
	public void testFromObjectToString_null() {
		String result = CredentialsUtils.toJsonString(null);
		assertNull(result);
	}
	
	@Test
	public void testFromStringToMap() {
		Map<String, String> result = CredentialsUtils.toMap(credentialString);
		assertNotNull(result);
		assertEquals(credentialMap, result);
	}
	
	@Test
	public void testFromStringToMap_null() {
		Map<String, String> result = CredentialsUtils.toMap(null);
		assertNull(result);
	}
	
	@Test
	public void testFromObjectToHeaderMap() {
		Map<String, String> result = CredentialsUtils.toHttpHeaderElement(credentialObject);
		assertNotNull(result);
		Map<String, String> headerMap = new HashMap<>();
		headerMap.put(EfsRequest.CREDENTIALS_HEADER, credentialString);
		assertEquals(headerMap, result);
	}
	
	@Test
	public void testFromObjectToHeaderMap_null() {
		Map<String, String> result = CredentialsUtils.toHttpHeaderElement(null);
		assertNull(result);
	}
}
