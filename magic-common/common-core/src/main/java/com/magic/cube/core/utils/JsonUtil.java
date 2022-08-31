package com.magic.cube.core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class JsonUtil {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    private JsonUtil() {}

    public static ObjectMapper getInstance() {
        return OBJECT_MAPPER;
    }

    /**
     * 转为json字符串
     */
    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("===[JsonUtil][toJson]: ", e);
        }
        return "";
    }

    /**
     * 将json字符串格式化后输出
     */
    public static String toJsonFormat(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("===[JsonUtil][toJsonFormat]: ", e);
        }
        return "";
    }

    public static String getNodeValue(String jsonStr, String key) {
        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(jsonStr);
            return jsonNode.findValue(key).asText();
        } catch (JsonProcessingException e) {
            log.error("===[JsonUtil][getNodeValue]: ", e);
        }
        return null;
    }

    /**
     * 转为Class对象
     */
    public static <T> T fromJson(String jsonString, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            log.error("===[JsonUtil][fromJson]: ", e);
        }
        return null;
    }

    /**
     * json转List
     *
     * @param jsonArrayStr json串
     * @param clazz        集合中的元素类型
     */
    public static <T> List<T> fromJsonToList(String jsonArrayStr, Class<T> clazz) {

        JavaType javaType = getCollectionType(ArrayList.class, clazz);
        try {
            return OBJECT_MAPPER.readValue(jsonArrayStr, javaType);
        } catch (JsonProcessingException e) {
            log.error("===[JsonUtil][fromJsonToList]: ", e);
        }
        return new ArrayList<>();
    }

    /**
     * json转List
     * @param jsonArrayStr json串
     * @return 默认泛型: List<Map<String, Object>>
     */
    public static List<Map<String, Object>> fromJsonToListMap(String jsonArrayStr) {

        JavaType javaType = getCollectionType(ArrayList.class);
        try {
            return OBJECT_MAPPER.readValue(jsonArrayStr, javaType);
        } catch (JsonProcessingException e) {
            log.error("===[JsonUtil][fromJsonToListMap]: ", e);
        }
        return new ArrayList<>();
    }

    /**
     * json转为Map
     *
     * @param jsonMapStr json串
     */
    public static Map<String, Object> fromJsonToMap(String jsonMapStr) {

        JavaType javaType = getMapType(Map.class, String.class, Object.class);
        try {
            return OBJECT_MAPPER.readValue(jsonMapStr, javaType);
        } catch (JsonProcessingException e) {
            log.error("===[JsonUtil][fromJsonToMap]: ", e);
        }
        return new HashMap<>();
    }

    /**
     * json转Map, 指定泛型
     * @param jsonMapStr json串
     * @param keyClass key类型
     * @param valueClass value类型
     */
    public static <K, V> Map<K, V> fromJsonToMap(String jsonMapStr, Class<K> keyClass, Class<V> valueClass) {

        JavaType javaType = getMapType(Map.class, keyClass, valueClass);
        try {
            return OBJECT_MAPPER.readValue(jsonMapStr, javaType);
        } catch (JsonProcessingException e) {
            log.error("===[JsonUtil][fromJsonToMap]: ", e);
        }
        return new HashMap<>();
    }

    public static <T> T toBean(Object obj, Class<T> clazz) {
        return OBJECT_MAPPER.convertValue(obj, clazz);
    }



    // ============================================== private

    /**
     * 获取collection中的泛型
     *
     * @param collectionClass 泛型的Collection
     * @param elementClass    集中中的元素类型
     * @return JavaType Java类型
     */
    private static JavaType getCollectionType(Class<?> collectionClass, Class<?> elementClass) {
        return OBJECT_MAPPER.getTypeFactory().constructCollectionLikeType(collectionClass, elementClass);
    }

    private static JavaType getCollectionType(Class<? extends Collection> collectionClass) {
        JavaType mapType = getMapType(Map.class, String.class, Object.class);
        return OBJECT_MAPPER.getTypeFactory().constructCollectionType(collectionClass, mapType);
    }

    /**
     * 获取Map中的泛型
     *
     * @param mapClass   泛型的Collection
     * @param keyClass   Map中key类型
     * @param valueClass Map中value类型
     * @return JavaType Java类型
     */
    private static JavaType getMapType(Class<?> mapClass, Class<?> keyClass, Class<?> valueClass) {
        return OBJECT_MAPPER.getTypeFactory().constructMapLikeType(Map.class, keyClass, valueClass);
    }
}
