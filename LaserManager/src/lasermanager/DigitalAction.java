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
public class DigitalAction {

    public static final int ACTION_NONE = 0;
    public static final int ACTION_OFF = 1;
    public static final int ACTION_ON = 2;

    public int ActionType = ACTION_NONE;

    public int Index;

    private DigitalAction() {
    }

    public DigitalAction(int index, ActionActionListener actionListener) {
        this.Index = index;
    }

    public static class Serializer implements JsonSerializer<DigitalAction> {

        @Override
        public JsonElement serialize(DigitalAction action, Type type, JsonSerializationContext jsc) {
            JsonObject json;
            json = new JsonObject();

            json.add("Index", new JsonPrimitive(action.Index));
            json.add("ActionType", new JsonPrimitive(action.ActionType));
            return json;
        }
    }
}
