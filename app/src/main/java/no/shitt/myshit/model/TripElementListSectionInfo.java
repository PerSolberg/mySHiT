package no.shitt.myshit.model;

public class TripElementListSectionInfo /* NSObject, NSCoding */ {
    private final boolean visible;
    private final String title;
    private final int firstTripElement;

    /* Identifiers for keyed archive (iOS only?)
    struct PropertyKey {
        static let visibleKey = "visible"
        static let titleKey = "title"
        static let firstTripElementKey = "firstTripElement"
    }
    */

    /* Encode for keyed archive (iOS only?)
    // MARK: NSCoding
    func encodeWithCoder(aCoder: NSCoder) {
        aCoder.encodeObject(visible, forKey: PropertyKey.visibleKey)
        aCoder.encodeObject(title, forKey: PropertyKey.titleKey)
        aCoder.encodeObject(firstTripElement, forKey: PropertyKey.firstTripElementKey)
    }
    */

    /* Decode from keyed archive (iOS only?)
    required convenience init?(coder aDecoder: NSCoder) {
        // NB: use conditional cast (as?) for any optional properties
        let visible  = aDecoder.decodeObjectForKey(PropertyKey.visibleKey) as! Bool
        let title = aDecoder.decodeObjectForKey(PropertyKey.titleKey) as! String
        let firstTripElement = aDecoder.decodeIntegerForKey(PropertyKey.firstTripElementKey)

        // Must call designated initializer.
        self.init(visible: visible, title: title, firstTripElement: firstTripElement)
    }
    */

    TripElementListSectionInfo(boolean visible, String title, int firstTripElement) {
        super();
        // Initialize stored properties.
        this.visible = visible;
        this.title = title;
        this.firstTripElement = firstTripElement;

        // Initialization should fail if there is no name
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Section title must be filled in!");
        }
    }

}
