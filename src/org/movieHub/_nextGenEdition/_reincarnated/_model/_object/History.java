package org.movieHub._nextGenEdition._reincarnated._model._object;

import com.google.gson.Gson;
import org.movieHub._nextGenEdition._reincarnated._model._enum.LoadPurpose;
import org.movieHub._nextGenEdition._reincarnated._model._enum.LoadStatus;

/**
 * @author Mandela aka puumInc
 */
public class History {

    private transient final Gson gson = new Gson();
    private String date;
    private String timeWhenItStarted;
    private String timeWhenItStopped;
    private Load load;
    private LoadPurpose loadPurpose;
    private LoadStatus loadStatus;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTimeWhenItStarted() {
        return timeWhenItStarted;
    }

    public void setTimeWhenItStarted(String timeWhenItStarted) {
        this.timeWhenItStarted = timeWhenItStarted;
    }

    public String getTimeWhenItStopped() {
        return timeWhenItStopped;
    }

    public void setTimeWhenItStopped(String timeWhenItStopped) {
        this.timeWhenItStopped = timeWhenItStopped;
    }

    public Load getLoad() {
        return load;
    }

    public void setLoad(Load load) {
        this.load = load;
    }

    public LoadPurpose getLoadPurpose() {
        return loadPurpose;
    }

    public void setLoadPurpose(LoadPurpose loadPurpose) {
        this.loadPurpose = loadPurpose;
    }

    public LoadStatus getLoadStatus() {
        return loadStatus;
    }

    public void setLoadStatus(LoadStatus loadStatus) {
        this.loadStatus = loadStatus;
    }

    @Override
    public String toString() {
        return gson.toJson(this, History.class);
    }
}
