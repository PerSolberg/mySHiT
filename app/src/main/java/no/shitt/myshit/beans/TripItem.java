package no.shitt.myshit.beans;

public class TripItem {
    private int id;
    private int imageId;
    private String name;
    private String desc;

    public TripItem(int id, int imageId, String name, String desc) {
        this.id = id;
        this.imageId = imageId;
        this.name = name;
        this.desc = desc;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getImageId() {
        return imageId;
    }
    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    @Override
    public String toString() {
        return name + "\n" + desc;
    }
}