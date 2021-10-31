package com.example.demo;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtil {
	
	private static final Logger log = LoggerFactory.getLogger(JsonUtil.class.getName());

    private static ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JsonUtil() {
      throw new UnsupportedOperationException(
              "You are not allowed to instantiate " + this.getClass().getName() + " class");
    }


    /**
     * Output JSON String from POJO
     *
     * @param object any java object that needs to be serialized to json string
     * @return String
     */
    public static String toJson(final Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception exp) {
            log.error(exp.getMessage(), exp);
            throw new JsonParsingFailureException(exp);
        }
    }

    /**
     * Get POJO from Json String
     *
     * @param json input JSON String
     * @param clazz Marshalling class
     * @return Object
     */
    public static <T> T fromJson(final String json, final Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception exp) {
            log.error(exp.getMessage(), exp);
            throw new JsonParsingFailureException(exp);
        }
    }

}