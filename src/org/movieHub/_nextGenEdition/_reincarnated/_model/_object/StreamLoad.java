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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StreamLoad that = (StreamLoad) o;

        if (!name.equals(that.name)) return false;
        if (!key.equals(that.key)) return false;
        return showStreamList.equals(that.showStreamList);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + key.hashCode();
        result = 31 * result + showStreamList.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this, StreamLoad.class);
    }
}
