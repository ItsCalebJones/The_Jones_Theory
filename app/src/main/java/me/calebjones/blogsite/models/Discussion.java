
package me.calebjones.blogsite.models;

import java.util.HashMap;
import java.util.Map;

public class Discussion {

    private Boolean commentsOpen;
    private String commentStatus;
    private Boolean pingsOpen;
    private String pingStatus;
    private Integer commentCount;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The commentsOpen
     */
    public Boolean getCommentsOpen() {
        return commentsOpen;
    }

    /**
     * 
     * @param commentsOpen
     *     The comments_open
     */
    public void setCommentsOpen(Boolean commentsOpen) {
        this.commentsOpen = commentsOpen;
    }

    /**
     * 
     * @return
     *     The commentStatus
     */
    public String getCommentStatus() {
        return commentStatus;
    }

    /**
     * 
     * @param commentStatus
     *     The comment_status
     */
    public void setCommentStatus(String commentStatus) {
        this.commentStatus = commentStatus;
    }

    /**
     * 
     * @return
     *     The pingsOpen
     */
    public Boolean getPingsOpen() {
        return pingsOpen;
    }

    /**
     * 
     * @param pingsOpen
     *     The pings_open
     */
    public void setPingsOpen(Boolean pingsOpen) {
        this.pingsOpen = pingsOpen;
    }

    /**
     * 
     * @return
     *     The pingStatus
     */
    public String getPingStatus() {
        return pingStatus;
    }

    /**
     * 
     * @param pingStatus
     *     The ping_status
     */
    public void setPingStatus(String pingStatus) {
        this.pingStatus = pingStatus;
    }

    /**
     * 
     * @return
     *     The commentCount
     */
    public Integer getCommentCount() {
        return commentCount;
    }

    /**
     * 
     * @param commentCount
     *     The comment_count
     */
    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
