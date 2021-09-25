package org.movieHub._nextGenEdition._reincarnated._model._enum;

public enum OperatingSystem {

    WINDOWS("win", "\\"),
    MAC("mac", "/"),
    NIX("nix", "/"),
    NUX("nux", "/"),
    AIX("aix", "/"),
    SOLARIS("sunos", "/");

    private final String os;
    private final String slash;


    OperatingSystem(String myOs, String mySlash) {
        this.os = myOs;
        this.slash = mySlash;
    }

    public String getOs() {
        return os;
    }

    public String getSlash() {
        return slash;
    }
}
