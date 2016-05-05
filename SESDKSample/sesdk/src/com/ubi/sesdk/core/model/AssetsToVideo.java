package com.ubi.sesdk.core.model;

/**
 * @author João Pedro Pedrosa, SE on 27/04/2016.
 */
public class AssetsToVideo {
    private int id;
    private String pathVideo;
    private String pathImage;
    private String key;

    public AssetsToVideo(int id, String pathVideo, String pathImage, String key) {
        this.id = id;
        this.pathVideo = pathVideo;
        this.pathImage = pathImage;
        this.key = key;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPathVideo() {
        return pathVideo;
    }

    public void setPathVideo(String pathVideo) {
        this.pathVideo = pathVideo;
    }

    public String getPathImage() {
        return pathImage;
    }

    public void setPathImage(String pathImage) {
        this.pathImage = pathImage;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
