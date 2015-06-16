package me.calebjones.blogsite.feed;

public class FeedItem {
    private String title;
    private String thumbnail;
    private String content;
    private String excerpt;
    private String ID;
    private String postURL;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getID(){
        return ID;
    }
    public void setID(String ID){
        this.ID = ID;
    }
    public void setpostURL(String postURL){
        this.postURL = postURL;
    }
    public String getpostURL(){
        return postURL;
    }

}
