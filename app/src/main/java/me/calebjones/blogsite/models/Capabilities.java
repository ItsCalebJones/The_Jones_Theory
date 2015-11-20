
package me.calebjones.blogsite.models;

import java.util.HashMap;
import java.util.Map;

public class Capabilities {

    private Boolean publishPost;
    private Boolean deletePost;
    private Boolean editPost;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The publishPost
     */
    public Boolean getPublishPost() {
        return publishPost;
    }

    /**
     * 
     * @param publishPost
     *     The publish_post
     */
    public void setPublishPost(Boolean publishPost) {
        this.publishPost = publishPost;
    }

    /**
     * 
     * @return
     *     The deletePost
     */
    public Boolean getDeletePost() {
        return deletePost;
    }

    /**
     * 
     * @param deletePost
     *     The delete_post
     */
    public void setDeletePost(Boolean deletePost) {
        this.deletePost = deletePost;
    }

    /**
     * 
     * @return
     *     The editPost
     */
    public Boolean getEditPost() {
        return editPost;
    }

    /**
     * 
     * @param editPost
     *     The edit_post
     */
    public void setEditPost(Boolean editPost) {
        this.editPost = editPost;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
