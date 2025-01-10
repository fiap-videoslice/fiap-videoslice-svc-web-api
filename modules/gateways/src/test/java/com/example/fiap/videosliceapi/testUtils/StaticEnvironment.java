package com.example.fiap.archburgers.testUtils;

import org.springframework.core.env.AbstractEnvironment;

import java.util.HashMap;
import java.util.Map;

public class StaticEnvironment extends AbstractEnvironment {
    private final Map<String, String> properties;

    public StaticEnvironment(Map<String, String> properties) {
        this.properties = new HashMap<>(properties);
    }

    @Override
    public String getProperty(String key) {
        return properties.get(key);
    }
}
