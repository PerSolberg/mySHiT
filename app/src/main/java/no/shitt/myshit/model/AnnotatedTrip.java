package no.shitt.myshit.model;

public class AnnotatedTrip /* NSObject, NSCoding */ {
    public TripListSection section;
    public Trip trip;
    public ChangeState modified;

    /* Identifiers for keyed archive (iOS only?)
    struct PropertyKey {
        static let modifiedKey = "modified"
        static let sectionKey = "section"
        static let tripKey = "trip"
    }
    */

    /* Encode for keyed archive (iOS only?)
    // MARK: NSCoding
    func encodeWithCoder(aCoder: NSCoder) {
        aCoder.encodeObject(modified.rawValue, forKey: PropertyKey.modifiedKey)
        aCoder.encodeObject(section.rawValue, forKey: PropertyKey.sectionKey)
        aCoder.encodeObject(trip, forKey: PropertyKey.tripKey)
    }
    */

    // MARK: Constructors
    /* Decode from keyed archive (iOS only?)
    required convenience init?(coder aDecoder: NSCoder) {
        // NB: use conditional cast (as?) for any optional properties
        let _modified   = aDecoder.decodeObjectForKey(PropertyKey.modifiedKey) as? String
        var modified:ChangeState = .Unchanged
        if (_modified != nil) {
            modified  = ChangeState(rawValue: _modified!)!
        }

        let section  = aDecoder.decodeObjectForKey(PropertyKey.sectionKey) as? TripListSection ?? .Historic
        let trip = aDecoder.decodeObjectForKey(PropertyKey.tripKey) as! Trip

        // Must call designated initializer.
        self.init(section: section, trip: trip, modified: modified)
    }
    */

    AnnotatedTrip(TripListSection section, Trip trip, ChangeState modified) {
        super();
        // Initialize stored properties.
        this.modified = modified;
        this.section = section;
        this.trip = trip;
    }
}
