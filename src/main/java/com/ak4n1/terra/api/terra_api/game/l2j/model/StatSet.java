package com.ak4n1.terra.api.terra_api.game.l2j.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase simplificada de StatSet del core L2J.
 * 
 * <p>Implementación mínima que solo contiene lo necesario para parsear XMLs de items.
 * Proporciona métodos para almacenar y recuperar valores tipados (String, int, long, boolean)
 * desde un mapa interno. Es la clase RECOMENDADA para manejar datos parseados de XMLs.
 * 
 * @see ItemXmlParser
 * @author ak4n1
 * @since 1.0
 */
public class StatSet {
    private final Map<String, Object> _set = new HashMap<>();
    
    public void set(String key, Object value) {
        _set.put(key, value);
    }
    
    public String getString(String key) {
        return getString(key, null);
    }
    
    public String getString(String key, String defaultValue) {
        Object obj = _set.get(key);
        return obj != null ? obj.toString() : defaultValue;
    }
    
    public int getInt(String key) {
        return getInt(key, 0);
    }
    
    public int getInt(String key, int defaultValue) {
        Object obj = _set.get(key);
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    public long getLong(String key) {
        return getLong(key, 0L);
    }
    
    public long getLong(String key, long defaultValue) {
        Object obj = _set.get(key);
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        Object obj = _set.get(key);
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        if (obj instanceof String) {
            return Boolean.parseBoolean((String) obj);
        }
        return defaultValue;
    }
    
    public boolean contains(String key) {
        return _set.containsKey(key);
    }
    
    public Map<String, Object> getAll() {
        return new HashMap<>(_set);
    }
}

