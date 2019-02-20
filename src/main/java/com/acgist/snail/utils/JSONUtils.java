package com.acgist.snail.utils;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON工具
 */
public class JSONUtils {

	public static final Logger LOGGER = LoggerFactory.getLogger(JSONUtils.class);

	/**
	 * JAVA对象转JSON字符串
	 */
	public static final String javaToJson(Object object) {
		if (object == null) {
			return null;
		}
		final ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.setSerializationInclusion(Include.NON_NULL); // 使用注解：@JsonInclude(Include.NON_NULL)
			return mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			LOGGER.error("JAVA对象转JSON异常，内容：{}", object, e);
		}
		return null;
	}

	/**
	 * JSON字符串转MAP对象
	 * TODO 优化泛型
	 */
	@SuppressWarnings("unchecked")
	public static final Map<String, Object> jsonToMap(String json) {
		if (json == null) {
			return null;
		}
		final ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(json, Map.class);
		} catch (IOException e) {
			LOGGER.error("JSON转MAP异常，内容：" + json, e);
		}
		return null;
	}
	
	/**
	 * JSON字符串转JAVA对象
	 */
	public static final <T> T jsonToJava(String json, Class<T> clazz) {
		if(json == null || clazz == null) {
			return null;
		}
		final ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 使用注解：@JsonIgnoreProperties(ignoreUnknown = true)
			return mapper.readValue(json, clazz);
		} catch (IOException e) {
			LOGGER.error("JSON转JAVA对象异常", e);
		}
		return null;
	}

}
