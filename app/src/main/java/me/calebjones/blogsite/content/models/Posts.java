
package me.calebjones.blogsite.content.models;

public class Posts {

    private Integer PostID;
    private Integer ID;
    private String date;
    private String modified;
    private String title;
    private String URL;
    private String content;
    private String excerpt;
    private Boolean parent;
    private String type;
    private String featuredImage;
    private String format;
    private String tags;
    private String categories;
    private transient boolean favourite;

    /**
     *
     * @return
     *     The PostID
     */
    public Integer getID() {
        return ID;
    }

    /**
     *
     * @param ID
     *     The PostID
     */
    public void setID(Integer ID) {
        this.ID = ID;
    }

    /**
     * 
     * @return
     *     The PostID
     */
    public Integer getPostID() {
        return PostID;
    }

    /**
     * 
     * @param ID
     *     The PostID
     */
    public void setPostID(Integer ID) {
        this.PostID = ID;
    }

    /**
     * 
     * @return
     *     The date
     */
    public String getDate() {
        return date;
    }

    /**
     * 
     * @param date
     *     The date
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * 
     * @return
     *     The modified
     */
    public String getModified() {
        return modified;
    }

    /**
     * 
     * @param modified
     *     The modified
     */
    public void setModified(String modified) {
        this.modified = modified;
    }

    /**
     * 
     * @return
     *     The title
     */
    public String getTitle() {
        return title;
    }

    /**
     * 
     * @param title
     *     The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 
     * @return
     *     The URL
     */
    public String getURL() {
        return URL;
    }

    /**
     * 
     * @param URL
     *     The URL
     */
    public void setURL(String URL) {
        this.URL = URL;
    }

    /**
     * 
     * @return
     *     The content
     */
    public String getContent() {
        return content;
    }

    /**
     * 
     * @param content
     *     The content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 
     * @return
     *     The excerpt
     */
    public String getExcerpt() {
        return excerpt;
    }

    /**
     * 
     * @param excerpt
     *     The excerpt
     */
    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }
    /**
     * 
     * @return
     *     The parent
     */
    public Boolean getParent() {
        return parent;
    }

    /**
     * 
     * @param parent
     *     The parent
     */
    public void setParent(Boolean parent) {
        this.parent = parent;
    }

    /**
     * 
     * @return
     *     The type
     */
    public String getType() {
        return type;
    }

    /**
     * 
     * @param type
     *     The type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     * @return
     *     The featuredImage
     */
    public String getFeaturedImage() {
        return featuredImage;
    }

    /**
     *
     * @param featuredImage
     *     The featured_image
     */
    public void setFeaturedImage(String featuredImage) {
        this.featuredImage = featuredImage;
    }

    /**
     * 
     * @return
     *     The format
     */
    public String getFormat() {
        return format;
    }

    /**
     * 
     * @param format
     *     The format
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * 
     * @return
     *     The tags
     */
    public String getTags() {
        return tags;
    }

    /**
     * 
     * @param tags
     *     The tags
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * 
     * @return
     *     The categories
     */
    public String getCategories() {
        return categories;
    }

    /**
     * 
     * @param categories
     *     The categories
     */
    public void setCategories(String categories) {
        this.categories = categories;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

}
