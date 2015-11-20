
package me.calebjones.blogsite.models;

import java.util.HashMap;
import java.util.Map;


public class Author {

    private Integer ID;
    private Boolean email;
    private String name;
    private String URL;
    private String avatarURL;
    private String profileURL;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The ID
     */
    public Integer getID() {
        return ID;
    }

    /**
     * 
     * @param ID
     *     The ID
     */
    public void setID(Integer ID) {
        this.ID = ID;
    }

    /**
     * 
     * @return
     *     The email
     */
    public Boolean getEmail() {
        return email;
    }

    /**
     * 
     * @param email
     *     The email
     */
    public void setEmail(Boolean email) {
        this.email = email;
    }

    /**
     * 
     * @return
     *     The name
     */
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name
     *     The name
     */
    public void setName(String name) {
        this.name = name;
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
     *     The avatarURL
     */
    public String getAvatarURL() {
        return avatarURL;
    }

    /**
     * 
     * @param avatarURL
     *     The avatar_URL
     */
    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

    /**
     * 
     * @return
     *     The profileURL
     */
    public String getProfileURL() {
        return profileURL;
    }

    /**
     * 
     * @param profileURL
     *     The profile_URL
     */
    public void setProfileURL(String profileURL) {
        this.profileURL = profileURL;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
