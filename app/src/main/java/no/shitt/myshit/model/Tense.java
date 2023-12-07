package no.shitt.myshit.model;

public enum Tense {
    PAST    ("past"),
    PRESENT ("present"),
    FUTURE  ("future"),
    //UNKNOWN ("unknown")
    ;

    private final String rawValue;
    Tense(String rawValue) {
        this.rawValue = rawValue;
    }

    @SuppressWarnings("unused")
    public String getRawValue() {
        return rawValue;
    }
}