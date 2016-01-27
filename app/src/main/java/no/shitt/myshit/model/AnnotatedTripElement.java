package no.shitt.myshit.model;

public class AnnotatedTripElement /* NSObject, NSCoding */ {
    public TripElement tripElement;
    public ChangeState modified;

    /* Identifiers for keyed archive (iOS only?)
    struct PropertyKey {
        static let modifiedKey = "modified"
        static let tripElementKey = "tripElement"
    }
    */

    /* Encode for keyed archive (iOS only?)
    // MARK: NSCoding
    func encodeWithCoder(aCoder: NSCoder) {
        aCoder.encodeObject(modified.rawValue, forKey: PropertyKey.modifiedKey)
        aCoder.encodeObject(tripElement, forKey: PropertyKey.tripElementKey)
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
        let tripElement = aDecoder.decodeObjectForKey(PropertyKey.tripElementKey) as! TripElement

        // Must call designated initializer.
        self.init(tripElement: tripElement, modified: modified)
    }
    */

    AnnotatedTripElement(TripElement tripElement) {
        super();
        // Initialize stored properties.
        this.modified = ChangeState.UNCHANGED;
        this.tripElement = tripElement;
    }

    AnnotatedTripElement(TripElement tripElement, ChangeState modified) {
        super();
        // Initialize stored properties.
        this.modified = modified;
        this.tripElement = tripElement;
    }
}
