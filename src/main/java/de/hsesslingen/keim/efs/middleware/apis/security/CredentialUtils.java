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
package de.hsesslingen.keim.efs.middleware.apis.security;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.hsesslingen.keim.efs.middleware.config.ApiConstants;

/**
 * Utility class for converting credential data from and to various representations
 * 
 * @author k.sivarasah
 * 8 Nov 2019
 */
public class CredentialUtils {

	private final static ObjectMapper mapper = new ObjectMapper();
	private final static Logger log = LoggerFactory.getLogger(CredentialUtils.class);
	
	/**
	 * Converts the given map to a credential-object of wished type.
	 *   
	 * @param credentials credential information as Map
	 * @param credentialType return type
	 * @param <T> The type for the credentials object
	 * @return credential-object as wished or null
	 */
	public static <T> T toCredentials(Map<String, String> credentials, Class<T> credentialType) {
		if(credentials == null || credentialType == null) {
			return null;
		}
		
		try {
			return mapper.convertValue(credentials, credentialType);
		} catch (Exception e) {
			log.error("Credential-Map could not be converted to Type {}", credentialType, e);
		}
		return null;
	}
	
	/**
	 * Converts the given string to a credential-object of wished type.
	 * @param credentials credential information as JSON content String
	 * @param credentialType return type
	 * @return credential-object as wished or null
	 */
	public static <T> T toCredentials(String credentials, Class<T> credentialType) {
		if(!StringUtils.hasText(credentials) || credentialType == null) {
			return null;
		}
		
		try {
			return mapper.readValue(credentials, credentialType);
		} catch (Exception e) {
			log.error("Credential-String could not be converted to Type {}", credentialType, e);
		}
		return null;
	}
	
	/**
	 * Converts the given credential-object to a JSON content String
	 * @param credentialObject The credential-object 
	 * @return JSON content String or null
	 */
	public static String toJsonString(Object credentialObject) {
		if(credentialObject == null) {
			return null;
		}
		
		try {
			return mapper.writeValueAsString(credentialObject);
		} catch (Exception e) {
			log.error("Credential Object could not be converted to String", e);
		}
		return null;
	}
	
	/**
	 * Converts the given string to a Map
	 * @param credentials credential information as JSON content String
	 * @return credential information as Map or null
	 */
	public static Map<String, String> toMap(String credentials) {
		if(!StringUtils.hasText(credentials)) {
			return null;
		}
		
		try {
			return mapper.readValue(credentials, new TypeReference<Map<String, String>>(){});
		} catch (Exception e) {
			log.error("Credential-String could not be converted to Map", e);
		}
		return null;
	}
	
	/**
	 * Converts the given credential-object to a Map that can be used as header parameter
	 * @param credentialObject The credential-object 
	 * @return Map with "x-credentials" as key and JSON content String of credential-object as value
	 */
	public static Map<String, String> toHttpHeaderElement(Object credentialObject) {
		if(credentialObject == null) {
			return null;
		}
		
		Map<String, String> headerElement = new HashMap<>();
		headerElement.put(ApiConstants.CREDENTIALS_HEADER_NAME, toJsonString(credentialObject));
		return headerElement;
	}
	
}
