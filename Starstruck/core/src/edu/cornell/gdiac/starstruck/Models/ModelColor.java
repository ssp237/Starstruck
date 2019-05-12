package edu.cornell.gdiac.starstruck.Models;

public enum ModelColor {
    PINK,
    BLUE;

    public String getName() {
        switch (this) {
            case PINK: return "pink";
            case BLUE: return "blue";
        }
        return null; //Should not get here.
    }
}
