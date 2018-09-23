package com.doxee.pvideo.dashboard.aws.lambda.handler.utils;

import com.doxee.pvideo.dashboard.aws.lambda.handler.exception.ValidationException;
//import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.log4j.Logger;

public class JsonManager {

    // private static final Gson gson = new Gson();
    // private static final Validator validator = new Validator();
    // private static final Logger log = Logger.getLogger(JsonManager.class);
    //
    // /**
    // * This method returns a GenericEvent object containing the serialized data (based on class
    // * annotations) given in the input. Each Java object that wraps a JSON is then validated using the
    // * rules specified in the class annotation.
    // *
    // * @param json JSON object to serialize in a Java Object of type GenericEvent
    // * @return event serialized event that contains received JSON properties, correctly mapped.
    // */
    // public <T> T deserialize(String json, Class<T> type) throws ValidationException {
    // log.info("deserialize ...");
    // T event = gson.fromJson(json, type);
    // validator.validate(event);
    // return event;
    // }
    //
    // /**
    // * This method is a custom deserializer for this object. An EmailOpeningEvent is made of a
    // * Geolocalization Event and a QueryString Event.
    // *
    // * @param immutableJson is the part of the event taken from the query string (no property is
    // * overridden by others with the same name).
    // * @param mutableJson is the part of the event take from the geolocalization.
    // * @return event
    // */
    // public <T> T deserialize(String immutableJson, String mutableJson, Class<T> type)
    // throws ValidationException {
    // String json;
    // Map<?, ?> immutableMap = deserialize(immutableJson, HashMap.class);
    // Map<?, ?> mutableMap = deserialize(mutableJson, HashMap.class);
    // Map<?, ?> merged = Stream
    // .concat(immutableMap.entrySet().stream(), mutableMap.entrySet().stream())
    // .collect(Collectors.toMap(
    // entry -> entry.getKey(),
    // entry -> entry.getValue(),
    // (immutableEl, mutableEl) -> {
    // log.warn("A duplicate key was found. It is: " + mutableEl);
    // return immutableEl;
    // }
    // ));
    // json = new Gson().toJson(merged);
    // return deserialize(json, type);
    // }
    //
    // /**
    // * This method acts with the reverse scope of deserialization. It wraps a Java object in a JSON
    // * object.
    // *
    // * @return json JSON object that wraps GenericEvent information.
    // */
    // public String serialize(Object obj) {
    // log.info("serialize ...");
    // return gson.toJson(obj);
    // }

}
