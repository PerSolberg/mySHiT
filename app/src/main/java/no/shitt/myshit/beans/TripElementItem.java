package no.shitt.myshit.beans;

class TripElementItem {
    private int trip_id;
    private int id;
    private int imageId;
    private String name;
    private String start;
    private String end;
    private String desc;

    public TripElementItem(int trip_id, int id, int imageId, String name, String start, String end, String desc) {
        this.trip_id = trip_id;
        this.id      = id;
        this.imageId = imageId;
        this.name    = name;
        this.start   = start;
        this.end     = end;
        this.desc    = desc;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getTripId() {
        return trip_id;
    }
    public void setTripId(int trip_id) {
        this.trip_id = trip_id;
    }
    public int getImageId() {
        return imageId;
    }
    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getStart() {
        return start;
    }
    public void setStart(String start) {
        this.start = start;
    }
    public String getEnd() {
        return end;
    }
    public void setEnd(String end) {
        this.end = end;
    }
    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    @Override
    public String toString() {
        return name + "\n" + desc;
    }
}