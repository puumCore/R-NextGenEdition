package org.movieHub._nextGenEdition._reincarnated._model._object;

import com.google.gson.Gson;

/**
 * @author Mandela aka puumInc
 */

public class ShowStream {

    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this, ShowStream.class);
    }
}
