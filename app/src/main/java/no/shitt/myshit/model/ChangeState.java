package no.shitt.myshit.model;

public enum ChangeState {
    NEW("N"),
    CHANGED("C"),
    DELETED("D"),
    UNCHANGED("U");

    private final String rawValue;

    ChangeState(String rawValue) {
        this.rawValue = rawValue;
    }
}
