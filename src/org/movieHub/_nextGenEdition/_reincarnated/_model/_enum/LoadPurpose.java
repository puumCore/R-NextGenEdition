package org.movieHub._nextGenEdition._reincarnated._model._enum;

/**
 * @author edgar
 */
public enum LoadPurpose {

    REMOVABLE_DRIVE("Removable Drive"), STREAMING("Streaming");

    final String purpose;

    LoadPurpose(String s) {
        this.purpose = s;
    }

    public String asString() {
        return purpose;
    }
}
