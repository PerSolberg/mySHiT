package no.shitt.myshit.model;

public enum Tense {
    PAST    ("past"),
    PRESENT ("present"),
    FUTURE  ("future");

    private final String rawValue;
    Tense(String rawValue) {
        this.rawValue = rawValue;
    }
}