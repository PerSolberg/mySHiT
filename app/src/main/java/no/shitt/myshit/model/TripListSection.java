package no.shitt.myshit.model;

public enum TripListSection {
    FUTURE      ("Future"),
    UPCOMING    ("Upcoming"),
    CURRENT     ("Current"),
    HISTORIC    ("Historic");

    // Iteration support
    //static let allValues = [Future, Upcoming, Current, Historic]

    private final String rawValue;
    TripListSection(String rawValue) {
        this.rawValue = rawValue;
    }
}
