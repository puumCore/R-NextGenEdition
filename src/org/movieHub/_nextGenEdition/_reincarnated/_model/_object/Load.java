package org.movieHub._nextGenEdition._reincarnated._model._object;

import com.google.gson.Gson;

import java.io.File;
import java.util.List;

/**
 * @author Mandela aka puumInc
 */
public class Load {

    private String name;
    private File destinationFolder;
    private List<Show> showList;
    private transient Double sourceSize;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getDestinationFolder() {
        return destinationFolder;
    }

    public void setDestinationFolder(File destinationFolder) {
        this.destinationFolder = destinationFolder;
    }

    public List<Show> getShowList() {
        return showList;
    }

    public void setShowList(List<Show> showList) {
        this.showList = showList;
    }

    public Double getSourceSize() {
        return sourceSize;
    }

    public void setSourceSize(Double sourceSize) {
        this.sourceSize = sourceSize;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this, Load.class);
    }
}
