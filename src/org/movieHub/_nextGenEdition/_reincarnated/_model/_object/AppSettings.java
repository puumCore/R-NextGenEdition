package org.movieHub._nextGenEdition._reincarnated._model._object;

import com.google.gson.Gson;

/**
 * @author edgar
 */
public class AppSettings {

    private Double price = 30.0;

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this, AppSettings.class);
    }
}
