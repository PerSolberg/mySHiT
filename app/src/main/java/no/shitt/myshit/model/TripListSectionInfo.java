package no.shitt.myshit.model;

public class TripListSectionInfo /* NSObject, NSCoding */ {
    boolean visible;
    TripListSection type;
    int firstTrip;

    /* Identifiers for keyed archive (iOS only?)
    struct PropertyKey {
        static let visibleKey = "visible"
        static let typeKey = "type"
        static let firstTripKey = "firstTrip"
    }
    */

    /* Encode for keyed archive (iOS only?)
    // MARK: NSCoding
    func encodeWithCoder(aCoder: NSCoder) {
        aCoder.encodeObject(visible, forKey: PropertyKey.visibleKey)
        aCoder.encodeObject(type.rawValue, forKey: PropertyKey.typeKey)
        aCoder.encodeInteger(firstTrip, forKey: PropertyKey.firstTripKey)
    }
    */

    // MARK: Constructors
    /* Decode from keyed archive (iOS only?)
    required convenience init?(coder aDecoder: NSCoder) {
        // NB: use conditional cast (as?) for any optional properties
        let visible  = aDecoder.decodeObjectForKey(PropertyKey.visibleKey) as! Bool
        let type = TripListSection(rawValue: aDecoder.decodeObjectForKey(PropertyKey.typeKey) as! String)!
                let firstTrip = aDecoder.decodeIntegerForKey(PropertyKey.firstTripKey)

        // Must call designated initializer.
        self.init(visible: visible, type: type, firstTrip: firstTrip)
    }
    */

    TripListSectionInfo(boolean visible, TripListSection type, int firstTrip) {
        super();
        // Initialize stored properties.
        this.visible = visible;
        this.type = type;
        this.firstTrip = firstTrip;
    }

}
