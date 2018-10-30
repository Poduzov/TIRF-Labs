/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lasermanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
public class SequenceStep {

    public int Index = 1;

    SequenceTrigger Trigger;
    AnalogAction AnalogActions[] = new AnalogAction[Configuration.AO_COUNT];
    DigitalAction DigitalActions[] = new DigitalAction[Configuration.DO_COUNT];

    public SequenceStep(SequenceStep step) {
        this.Trigger = step.Trigger;
        this.AnalogActions = step.AnalogActions;
        this.DigitalActions = step.DigitalActions;
        this.Index = step.Index;
    }

    private SequenceStep() {
    }

    public SequenceStep(int valueType) {

        for (int i = 0; i < AnalogActions.length; i++) {
            AnalogActions[i] = new AnalogAction(i);
            AnalogActions[i].ValueType = valueType;
        }

        for (int i = 0; i < DigitalActions.length; i++) {
            DigitalActions[i] = new DigitalAction(i, null);
        }

        Trigger = new SequenceTrigger();
    }

    public static class Serializer implements JsonSerializer<SequenceStep> {

        @Override
        public JsonElement serialize(SequenceStep step, Type type, JsonSerializationContext jsc) {

            GsonBuilder builder;
            builder = new GsonBuilder();
            builder.registerTypeAdapter(SequenceTrigger.class, new SequenceTrigger.Serializer());
            builder.registerTypeAdapter(AnalogAction.class, new AnalogAction.Serializer());
            builder.registerTypeAdapter(DigitalAction.class, new DigitalAction.Serializer());

            Gson gson;
            gson = builder.create();

            JsonObject json;
            json = new JsonObject();

            json.add("Index", new JsonPrimitive(step.Index));
            json.add("Trigger", gson.toJsonTree(step.Trigger));
            json.add("AnalogActions", gson.toJsonTree(step.AnalogActions));
            json.add("DigitalActions", gson.toJsonTree(step.DigitalActions));

            return json;
        }
    }
}
