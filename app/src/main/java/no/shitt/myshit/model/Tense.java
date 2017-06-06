package no.shitt.myshit.model;

public enum Tense {
    PAST    ("past"),
    PRESENT ("present"),
    FUTURE  ("future"),
    UNKNOWN ("unknown");

    private final String rawValue;
    Tense(String rawValue) {
        this.rawValue = rawValue;
    }

    public String getRawValue() {
        return rawValue;
    }

    public static Tense fromString(String text) {
        if (text != null) {
            for (Tense t : Tense.values()) {
                if (text.equalsIgnoreCase(t.rawValue)) {
                    return t;
                }
            }
        }
        return null;
    }
}