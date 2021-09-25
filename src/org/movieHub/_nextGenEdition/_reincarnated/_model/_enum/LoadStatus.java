package org.movieHub._nextGenEdition._reincarnated._model._enum;

/**
 * @author edgar
 */
public enum LoadStatus {

    COMPLETE(0, "Complete"),
    INCOMPLETE(1, "Incomplete"),
    STOPPED(2, "Stopped"),
    SYSTEM_ERROR(3, "System Error");

    private final int id;
    private final String status;

    LoadStatus(int i, String s) {
        this.id = i;
        this.status = s;
    }

    public int getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }
}
