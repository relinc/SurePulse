package net.relinc.libraries.application;

import org.json.simple.JSONObject;

import java.util.Optional;

// Quick wrapper for using Optionals instead of null.
public class JsonReader {
    private JSONObject ob;

    public JsonReader(JSONObject ob) {
        if(ob == null) {
            this.ob = new JSONObject();
        } else {
            this.ob = ob;
        }
    }

    public Optional<Object> get(Object key) {
        Object res = this.ob.get(key);
        if(res == null) {
            return Optional.empty();
        } else {
            return Optional.of(res);
        }
    }
}
