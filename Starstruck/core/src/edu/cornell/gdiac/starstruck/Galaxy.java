package edu.cornell.gdiac.starstruck;

/**
 *  Represents the different Galaxies in the game. Used to keep track of what
 *  assets/enemies to use.
 */
public enum Galaxy {
    DEFAULT("de"), //Galaxy from first game play prototype.
    WHIRLPOOL("wp"),
    MILKYWAY("mw");

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
        }
        return DEFAULT;
    }

    public String fullName() {
        switch (this) {
            case WHIRLPOOL: return "whirlpool";
            case MILKYWAY: return "milky way";
            default: return "default";
        }
    }

    public String getUrchinPrefix() {
        return "spike";
    }


}
