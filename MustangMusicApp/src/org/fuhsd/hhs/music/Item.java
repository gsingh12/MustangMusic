package org.fuhsd.hhs.music;

/**
 * Represents the item displayed in a List.
 */
public class Item {

    private String name;
    private String link;
    private String date;
    private String imgUrl;

    public Item(String name, String link, String date) {
        this(name, link, date, null);
    }

    public Item(String name, String link, String date, String imgUrl) {
        this.name = name;
        this.link = link;
        this.date = date;
        this.imgUrl = imgUrl;
    }
    
    public String getName() {
        return name;
    }
    
    public String getLink() {
        return link;
    }
    
    public String getDate() {
        return date;
    }
    
    public String getImgUrl() {
        return imgUrl;
    }
    
    public String toString() {
        return name + " (" + link + ")" + (date != null? " [" + date + "]": "") + (imgUrl != null? " [" + imgUrl + "]": "");
    }
}
