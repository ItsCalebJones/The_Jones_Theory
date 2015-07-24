
package me.calebjones.blogsite.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Posts {

    private Integer PostID;
    private Integer siteID;
    private Integer ID;
    private Author author;
    private String date;
    private String modified;
    private String title;
    private String URL;
    private String shortURL;
    private String content;
    private String excerpt;
    private String slug;
    private String guid;
    private String status;
    private Boolean sticky;
    private String password;
    private Boolean parent;
    private String type;
    private String discussion;
    private Boolean likesEnabled;
    private Boolean sharingEnabled;
    private Integer likeCount;
    private Boolean iLike;
    private Boolean isReblogged;
    private Boolean isFollowing;
    private String globalID;
    private String featuredImage;
    private PostThumbnail postThumbnail;
    private String format;
    private Boolean geo;
    private Integer menuOrder;
    private String pageTemplate;
    private List<Object> publicizeURLs = new ArrayList<Object>();
    private String tags;
    private String categories;
    private String attachments;
    private Integer attachmentCount;
    private List<Metadatum> metadata = new ArrayList<Metadatum>();
    private String[] meta;
    private Capabilities capabilities;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
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
     *     The siteID
     */
    public Integer getSiteID() {
        return siteID;
    }

    /**
     * 
     * @param siteID
     *     The site_ID
     */
    public void setSiteID(Integer siteID) {
        this.siteID = siteID;
    }

    /**
     * 
     * @return
     *     The author
     */
    public Author getAuthor() {
        return author;
    }

    /**
     * 
     * @param author
     *     The author
     */
    public void setAuthor(Author author) {
        this.author = author;
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
     *     The shortURL
     */
    public String getShortURL() {
        return shortURL;
    }

    /**
     * 
     * @param shortURL
     *     The short_URL
     */
    public void setShortURL(String shortURL) {
        this.shortURL = shortURL;
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
     *     The slug
     */
    public String getSlug() {
        return slug;
    }

    /**
     * 
     * @param slug
     *     The slug
     */
    public void setSlug(String slug) {
        this.slug = slug;
    }

    /**
     * 
     * @return
     *     The guid
     */
    public String getGuid() {
        return guid;
    }

    /**
     * 
     * @param guid
     *     The guid
     */
    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * 
     * @return
     *     The status
     */
    public String getStatus() {
        return status;
    }

    /**
     * 
     * @param status
     *     The status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 
     * @return
     *     The sticky
     */
    public Boolean getSticky() {
        return sticky;
    }

    /**
     * 
     * @param sticky
     *     The sticky
     */
    public void setSticky(Boolean sticky) {
        this.sticky = sticky;
    }

    /**
     * 
     * @return
     *     The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * 
     * @param password
     *     The password
     */
    public void setPassword(String password) {
        this.password = password;
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
     *     The discussion
     */
    public String getDiscussion() {
        return discussion;
    }

    /**
     * 
     * @param discussion
     *     The discussion
     */
    public void setDiscussion(String discussion) {
        this.discussion = discussion;
    }

    /**
     * 
     * @return
     *     The likesEnabled
     */
    public Boolean getLikesEnabled() {
        return likesEnabled;
    }

    /**
     * 
     * @param likesEnabled
     *     The likes_enabled
     */
    public void setLikesEnabled(Boolean likesEnabled) {
        this.likesEnabled = likesEnabled;
    }

    /**
     * 
     * @return
     *     The sharingEnabled
     */
    public Boolean getSharingEnabled() {
        return sharingEnabled;
    }

    /**
     * 
     * @param sharingEnabled
     *     The sharing_enabled
     */
    public void setSharingEnabled(Boolean sharingEnabled) {
        this.sharingEnabled = sharingEnabled;
    }

    /**
     * 
     * @return
     *     The likeCount
     */
    public Integer getLikeCount() {
        return likeCount;
    }

    /**
     * 
     * @param likeCount
     *     The like_count
     */
    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    /**
     * 
     * @return
     *     The iLike
     */
    public Boolean getILike() {
        return iLike;
    }

    /**
     * 
     * @param iLike
     *     The i_like
     */
    public void setILike(Boolean iLike) {
        this.iLike = iLike;
    }

    /**
     * 
     * @return
     *     The isReblogged
     */
    public Boolean getIsReblogged() {
        return isReblogged;
    }

    /**
     * 
     * @param isReblogged
     *     The is_reblogged
     */
    public void setIsReblogged(Boolean isReblogged) {
        this.isReblogged = isReblogged;
    }

    /**
     * 
     * @return
     *     The isFollowing
     */
    public Boolean getIsFollowing() {
        return isFollowing;
    }

    /**
     * 
     * @param isFollowing
     *     The is_following
     */
    public void setIsFollowing(Boolean isFollowing) {
        this.isFollowing = isFollowing;
    }

    /**
     * 
     * @return
     *     The globalID
     */
    public String getGlobalID() {
        return globalID;
    }

    /**
     * 
     * @param globalID
     *     The global_ID
     */
    public void setGlobalID(String globalID) {
        this.globalID = globalID;
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
     *     The postThumbnail
     */
    public PostThumbnail getPostThumbnail() {
        return postThumbnail;
    }

    /**
     * 
     * @param postThumbnail
     *     The post_thumbnail
     */
    public void setPostThumbnail(PostThumbnail postThumbnail) {
        this.postThumbnail = postThumbnail;
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
     *     The geo
     */
    public Boolean getGeo() {
        return geo;
    }

    /**
     * 
     * @param geo
     *     The geo
     */
    public void setGeo(Boolean geo) {
        this.geo = geo;
    }

    /**
     * 
     * @return
     *     The menuOrder
     */
    public Integer getMenuOrder() {
        return menuOrder;
    }

    /**
     * 
     * @param menuOrder
     *     The menu_order
     */
    public void setMenuOrder(Integer menuOrder) {
        this.menuOrder = menuOrder;
    }

    /**
     * 
     * @return
     *     The pageTemplate
     */
    public String getPageTemplate() {
        return pageTemplate;
    }

    /**
     * 
     * @param pageTemplate
     *     The page_template
     */
    public void setPageTemplate(String pageTemplate) {
        this.pageTemplate = pageTemplate;
    }

    /**
     * 
     * @return
     *     The publicizeURLs
     */
    public List<Object> getPublicizeURLs() {
        return publicizeURLs;
    }

    /**
     * 
     * @param publicizeURLs
     *     The publicize_URLs
     */
    public void setPublicizeURLs(List<Object> publicizeURLs) {
        this.publicizeURLs = publicizeURLs;
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

    /**
     * 
     * @return
     *     The attachments
     */
    public String getAttachments() {
        return attachments;
    }

    /**
     * 
     * @param attachments
     *     The attachments
     */
    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    /**
     * 
     * @return
     *     The attachmentCount
     */
    public Integer getAttachmentCount() {
        return attachmentCount;
    }

    /**
     * 
     * @param attachmentCount
     *     The attachment_count
     */
    public void setAttachmentCount(Integer attachmentCount) {
        this.attachmentCount = attachmentCount;
    }

    /**
     * 
     * @return
     *     The metadata
     */
    public List<Metadatum> getMetadata() {
        return metadata;
    }

    /**
     * 
     * @param metadata
     *     The metadata
     */
    public void setMetadata(List<Metadatum> metadata) {
        this.metadata = metadata;
    }

    /**
     * 
     * @return
     *     The meta
     */
    public String[] getMeta() {
        return meta;
    }

    /**
     * 
     * @param meta
     *     The meta
     */
    public void setMeta(String[] meta) {
        this.meta = meta;
    }

    /**
     * 
     * @return
     *     The capabilities
     */
    public Capabilities getCapabilities() {
        return capabilities;
    }

    /**
     * 
     * @param capabilities
     *     The capabilities
     */
    public void setCapabilities(Capabilities capabilities) {
        this.capabilities = capabilities;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

}
