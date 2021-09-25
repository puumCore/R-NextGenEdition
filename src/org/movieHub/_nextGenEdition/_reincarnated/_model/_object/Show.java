package org.movieHub._nextGenEdition._reincarnated._model._object;

import com.google.gson.Gson;
import org.movieHub._nextGenEdition._reincarnated._model._enum.EntertainmentType;

import java.io.File;
import java.util.Objects;

/**
 * @author edgar
 */
public class Show {

    private File source;
    private EntertainmentType entertainmentType;

    public File getSource() {
        return source;
    }

    public void setSource(File source) {
        this.source = source;
    }

    public EntertainmentType getEntertainmentType() {
        return entertainmentType;
    }

    public void setEntertainmentType(EntertainmentType entertainmentType) {
        this.entertainmentType = entertainmentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Show show = (Show) o;
        return this.source.equals(show.source) && this.entertainmentType == show.entertainmentType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.source, this.entertainmentType);
    }

    @Override
    public String toString() {
        return new Gson().toJson(this, Show.class);
    }
}
