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
public class SequenceTrigger {

    public static final int TRIGGER_RISING_1 = 0;
    public static final int TRIGGER_FALLING_1 = 1;
    public static final int TRIGGER_RISING_2 = 2;
    public static final int TRIGGER_FALLING_2 = 3;
    public static final int TRIGGER_USB = 4;
    public static final int TRIGGER_TIMER = 5;

    public int Type = TRIGGER_USB;
    public int TimerValue = 0;

    public static class Serializer implements JsonSerializer<SequenceTrigger> {

        @Override
        public JsonElement serialize(SequenceTrigger trigger, Type type, JsonSerializationContext jsc) {
            JsonObject json;
            json = new JsonObject();

            json.add("Type", new JsonPrimitive(trigger.Type));
            json.add("TimerValue", new JsonPrimitive(trigger.TimerValue));

            return json;
        }

    }
}
