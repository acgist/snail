package com.acgist.snail.utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * utils - JSON
 */
public class JSONUtils {

	public static final Logger LOGGER = LoggerFactory.getLogger(JSONUtils.class);

	/**
	 * JAVA对象转JSON字符串
	 */
	public static final String toJSON(Object object) {
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
	 * JSON字符串转List对象
	 */
	public static final <T> List<T> toList(String json, Class<T> clazz) {
		if (json == null) {
			return null;
		}
		final ObjectMapper mapper = new ObjectMapper();
		try {
			final JavaType type = mapper.getTypeFactory().constructParametricType(List.class, clazz);
			return mapper.readValue(json, type);
		} catch (IOException e) {
			LOGGER.error("JSON转List异常，内容：" + json, e);
		}
		return null;
	}

	/**
	 * JSON字符串转MAP对象
	 */
	public static final Map<String, Object> toMap(String json) {
		if (json == null) {
			return null;
		}
		final ObjectMapper mapper = new ObjectMapper();
		try {
//			final Map<String, Object> data = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
			final JavaType type = mapper.getTypeFactory().constructParametricType(Map.class, String.class, Object.class);
			return mapper.readValue(json, type);
		} catch (IOException e) {
			LOGGER.error("JSON转Map异常，内容：" + json, e);
		}
		return null;
	}
	
	/**
	 * JSON字符串转JAVA对象
	 */
	public static final <T> T toJava(String json, Class<T> clazz) {
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
