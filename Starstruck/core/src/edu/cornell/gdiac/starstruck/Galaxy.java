package edu.cornell.gdiac.starstruck;

/**
 *  Represents the different Galaxies in the game. Used to keep track of what
 *  assets/enemies to use.
 */
public enum Galaxy {
    DEFAULT("de"), //Galaxy from first game play prototype.
    WHIRLPOOL("wp"),
    MILKYWAY("mw"),
    SOMBRERO("so"),
    CIRCINUS("ci"),
    LEVELSELECT("ls");

    private final String chars;

    Galaxy(String chars) {
        this.chars = chars;
    }

    public String getChars(){
        return chars;
    }

    /**
     * Return the galaxy associated with the given characters
     * @param chars The characters to look up
     * @return The galaxy associated with chars
     */
    public static Galaxy fromString(String chars) {
        if (chars.equals("whirlpool")) {
            return WHIRLPOOL;
        } else if (chars.equals("milky way")) {
            return MILKYWAY;
        } else if (chars.equals("sombrero")) {
            return SOMBRERO;
        } else if (chars.equals("circinus")) {
            return CIRCINUS;
        } else if (chars.equals("level select")) {
            return LEVELSELECT;
        }
        return DEFAULT;
    }

    public String fullName() {
        switch (this) {
            case WHIRLPOOL: return "whirlpool";
            case MILKYWAY: return "milky way";
            case SOMBRERO: return "sombrero";
            case LEVELSELECT: return "level select";
            case CIRCINUS: return "circinus";
            default: return "default";
        }
    }

    public String getUrchinPrefix() {
        return "spike";
    }


}
