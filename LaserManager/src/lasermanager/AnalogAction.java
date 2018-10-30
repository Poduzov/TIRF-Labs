/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lasermanager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

/**
 *
 * @author user
 */
public class AnalogAction {

    public static final int ACTION_NONE = 0;
    public static final int ACTION_CHANGE = 1;

    public static final int VALUE_VOLTAGE = 0;
    public static final int VALUE_WATTAGE = 1;

    public int ActionType = ACTION_NONE;
    public int ValueType = VALUE_VOLTAGE;
    public float Value = 0.0f;

    public int Index;

    private AnalogAction() {
    }

    public AnalogAction(int i) {
        Index = i;
    }

    public static class Serializer implements JsonSerializer<AnalogAction> {

        @Override
        public JsonElement serialize(AnalogAction action, Type type, JsonSerializationContext jsc) {
            JsonObject json;
            json = new JsonObject();

            json.add("Index", new JsonPrimitive(action.Index));
            json.add("ActionType", new JsonPrimitive(action.ActionType));
            json.add("ValueType", new JsonPrimitive(action.ValueType));
            json.add("Value", new JsonPrimitive(action.Value));
            return json;
        }
    }
}
