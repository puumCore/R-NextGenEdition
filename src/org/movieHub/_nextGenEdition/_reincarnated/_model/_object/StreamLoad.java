package org.movieHub._nextGenEdition._reincarnated._model._object;

import com.google.gson.Gson;

import java.util.List;

/**
 * @author edgar
 */
public class StreamLoad {

    private String name;
    private String key;
    private List<ShowStream> showStreamList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<ShowStream> getShowStreamList() {
        return showStreamList;
    }

    public void setShowStreamList(List<ShowStream> showStreamList) {
        this.showStreamList = showStreamList;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this, StreamLoad.class);
    }
}
