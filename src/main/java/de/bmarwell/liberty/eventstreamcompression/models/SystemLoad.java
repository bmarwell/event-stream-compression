package de.bmarwell.liberty.eventstreamcompression.models;


import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.config.PropertyVisibilityStrategy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public record SystemLoad(
    @JsonbProperty("hostname")    String hostname,
    @JsonbProperty("loadAverage") Number loadAverage) {

    private static final PropertyVisibilityStrategy visibilityStrategy = new PropertyVisibilityStrategy() {
        @Override
        public boolean isVisible(Field field) {
            return true;
        }

        @Override
        public boolean isVisible(Method method) {
            return false;
        }
    };


    private static final JsonbConfig jsonbConfig = new JsonbConfig()
        .withFormatting(false)
        .withPropertyVisibilityStrategy(visibilityStrategy);

    private static final Jsonb JSONB = JsonbBuilder.create(jsonbConfig);

    @Override
    public String toString() {
        return "CpuLoadAverage: " + JSONB.toJson(this);
    }

}
