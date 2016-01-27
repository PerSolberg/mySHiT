package no.shitt.myshit.model;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ListAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.ArrayList;

import no.shitt.myshit.Constants;
import no.shitt.myshit.SHiTApplication;
import no.shitt.myshit.helper.ServerAPI;
import no.shitt.myshit.helper.ServerAPIListener;

public class TripList /* NSObject, SequenceType, NSCoding */ implements ServerAPIListener {
    //typealias Index = Array<AnnotatedTrip>.Index
    //typealias Index = Int
    private static TripList sharedList = new TripList();

    private List<AnnotatedTrip> trips = new ArrayList<>();
    //private var rsRequest: RSTransactionRequest = RSTransactionRequest()
    //private var rsTransGetTripList: RSTransaction = RSTransaction(transactionType: RSTransactionType.GET, baseURL: "https://www.shitt.no/mySHiT", path: "trip", parameters: ["userName":"dummy@default.com","password":"******"])

    // Prevent other classes from instantiating - User is singleton!
    private TripList() {}

    /* Identifiers for keyed archive (iOS only?)
    private struct PropertyKey {
        static let tripsKey = "trips"
    }
    */

    /* Encode for keyed archive (iOS only?)
    // MARK: NSCoding
    func encodeWithCoder(aCoder: NSCoder) {
        aCoder.encodeObject(trips, forKey: PropertyKey.tripsKey)
    }
    */

    /* Decode from keyed archive (iOS only?)
    required init?( coder aDecoder: NSCoder) {
        super.init()
        // NB: use conditional cast (as?) for any optional properties
        trips  = aDecoder.decodeObjectForKey(PropertyKey.tripsKey) as! [AnnotatedTrip]
    }
    */


    // TO DO : Iterators
    /*
        // MARK: SequenceType
        func generate() -> AnyGenerator<AnnotatedTrip> {
        // keep the index of the next trip in the iteration
        var nextIndex = 0

        // Construct a AnyGenerator<AnnotatedTrip> instance, passing a closure that returns the next car in the iteration
        return anyGenerator {
        if (nextIndex >= self.trips.count) {
        return nil
        }
        return self.trips[nextIndex++]
        }
        }


        func reverse() -> AnyGenerator<AnnotatedTrip> {
        // keep the index of the next trip in the iteration
        var nextIndex = trips.count-1

        // Construct a AnyGenerator<AnnotatedTrip> instance, passing a closure that returns the next car in the iteration
        return anyGenerator {
        if (nextIndex < 0) {
        return nil
        }
        return self.trips[nextIndex--]
        }
        }


        var indices:Range<Int> {
        return trips.indices
        }

        // MARK: Indexable
        subscript(position: Int) -> AnnotatedTrip? {
        if position >= trips.count {
        return nil
        }
        return trips[position]
        }


        // MARK: CollectionType
        var count: Index.Distance {
        return trips.count
        }
    */

    // Functions
    public static TripList getSharedList() {
        return sharedList;
    }

    public void getFromServer(/*ServerAPIListener responseHandler*/) {
        //let userCred = User.sharedUser.getCredentials();

        //assert( userCred.name != nil );
        //assert( userCred.password != nil );
        //assert( userCred.urlsafePassword != nil );

        //Set the parameters for the RSTransaction object
        //rsTransGetTripList.parameters = [ "userName":userCred.name!,
        //        "password":userCred.urlsafePassword!,
        //        "sectioned":"0",
        //        "details":"non-historic"]

        //Send request
        new ServerAPI(this).execute("http://www.shitt.no/mySHiT/trip?userName=persolberg@hotmail.com&password=Vertex70&sectioned=0&details=non-historic");
        return;
    }

    // Copy data received from server to memory structure
    public void copyServerData(JSONArray serverData) {
        // Clear current data and repopulate from server data
        List<AnnotatedTrip> newTripList = new ArrayList<>();

        Log.d("TripList copyServerData", "Copy elements");
        for (int i = 0; i < serverData.length(); i++) {
            Trip newTrip = new Trip(serverData.optJSONObject(i));
            Log.d("TripList copyServerData", "Element #" + String.valueOf(i));
            newTripList.add(new AnnotatedTrip(TripListSection.HISTORIC, newTrip, ChangeState.UNCHANGED));
        }

        // Determine changes
        Log.d("TripList copyServerData", "Determine changes");
        if (!trips.isEmpty()) {
            for (int i = 0; i < newTripList.size(); i++) {
                AnnotatedTrip newTrip = newTripList.get(i);
                AnnotatedTrip oldTrip = null;
                for (int j = 0; j < trips.size(); j++) {
                    if (trips.get(i).trip.id == newTrip.trip.id) {
                        oldTrip = trips.get(j);
                        break;
                    }
                }
                if (oldTrip == null) {
                    newTrip.modified = ChangeState.NEW;
                } else {
                    newTrip.trip.compareTripElements(oldTrip.trip);
                    if (!newTrip.trip.isEqual(oldTrip.trip)) {
                        newTrip.modified = ChangeState.CHANGED;
                    }
                }
            }
        }

        trips =  newTripList;
    }

    public void onRemoteCallComplete(JSONObject response) {
        Log.d("TripList", "Trip list retrieved - building model");
        copyServerData(response.optJSONArray(Constants.JSON.QUERY_RESULTS));

        Intent intent = new Intent("tripsLoaded");
        //intent.putExtra("message", "SHiT trips loaded");
        LocalBroadcastManager.getInstance(SHiTApplication.getContext()).sendBroadcast(intent);
    }

    public void onRemoteCallFailed() {
        //
    }

    // Load from keyed archive
    /*
    func loadFromArchive(path:String) {
        let newTripList = NSKeyedUnarchiver.unarchiveObjectWithFile(path) as? [AnnotatedTrip]
        trips = newTripList ?? [AnnotatedTrip]()
    }
    */

    /*
    func saveToArchive(path:String) {
        let isSuccessfulSave = NSKeyedArchiver.archiveRootObject(trips, toFile: path)
        if !isSuccessfulSave {
            print("Failed to save trips...")
        } else {
            print("Trips saved to iOS keyed archive")
        }
    }
    */

    public int tripCount() {
        if (trips == null)
            return 0;
        else
            return trips.size();
    }

    public AnnotatedTrip tripById(int tripId) {
        for (int i = 0; i < trips.size(); i++) {
            if (tripId == trips.get(i).trip.id) {
                return trips.get(i);
            }
        }
        return null;
    }

    public AnnotatedTrip tripByPosition(int position) {
        if (position >= 0 && position < trips.size())
            return trips.get(position);
        else
            return null;
    }

    public AnnotatedTrip tripByCode(String tripCode) {
        for (int i = 0; i < trips.size(); i++) {
            if (tripCode.equals(trips.get(i).trip.code)) {
                return trips.get(i);
            }
        }
        return null;
    }

    public void clear() {
        trips = new ArrayList<>();
    }
}
