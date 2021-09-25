package org.movieHub._nextGenEdition._reincarnated._model._enum;

/**
 * @author edgar
 */
public enum EntertainmentType {

    MOVIE('M'), SERIES('S');

    private final char symbol;

    EntertainmentType(char letter) {
        this.symbol = letter;
    }

    public char getSymbol() {
        return symbol;
    }
}
