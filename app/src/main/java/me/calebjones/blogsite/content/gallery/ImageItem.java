package me.calebjones.blogsite.content.gallery;

public class ImageItem {
    private String title;
    private String thumbnail;
    private String content;
    private String excerpt;
    private String ID;
    private String postURL;
    private String category;
    private String tags;

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
    public void setCategory(String category){
        this.category = category;
    }
    public String getCategory(){
        return category;
    }
    public void setTags(String tags){
        this.tags = tags;
    }
    public String getTags() {
        return tags;
    }

}
