package com.io.begstd.slot.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonParseUtils {
    public static String parseToJson(Object obj) throws JsonProcessingException {
        ObjectMapper objectMapper = BeanUtils.getBean("objectMapper", ObjectMapper.class);
        return objectMapper.writeValueAsString(obj);
    }
    
    public static String serializeToJson(Object obj) throws JsonProcessingException {
        ObjectMapper objectSerialize = BeanUtils.getBean("objectSerialize", ObjectMapper.class);
        return objectSerialize.writeValueAsString(obj);
    }
    
    public static <T> T parseFromJson(Class<T> clazz, String playSessionJson) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper objectMapper = BeanUtils.getBean("objectMapper", ObjectMapper.class);
        return objectMapper.readValue(playSessionJson, clazz);
    }
    
    public static <T> T deserializeFromJson(Class<T> clazz, String playSessionJson) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper objectSerialize = BeanUtils.getBean("objectSerialize", ObjectMapper.class);
        return objectSerialize.readValue(playSessionJson, clazz);
    }
}
