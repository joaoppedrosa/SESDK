package com.ubi.sesdk.core.providers;

import com.ubi.sesdk.core.model.AssetsToVideo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author João Pedro Pedrosa, SE on 27/04/2016.
 */
public class VuforiaVideoProvider {

    private List<AssetsToVideo> assetsToVideos = new ArrayList<>();
    private String dataFile;

    public VuforiaVideoProvider(){}

    public void setDataFile(String dataFile){
        this.dataFile = dataFile;
    }

    public String getDataFile(){
        return this.dataFile;
    }

    public void addToList(AssetsToVideo assetsToVideo){
        this.assetsToVideos.add(assetsToVideo);
    }

    public void addToList(int id, String videoPath, String imagePath, String key){
        this.assetsToVideos.add(new AssetsToVideo(id, videoPath, imagePath, key));
    }

    public String getVideoFile(int key){
        return this.assetsToVideos.get(key).getPathVideo();
    }

    public String getImageFile(int key){
        return this.assetsToVideos.get(key).getPathImage();
    }

    public int getSize(){
        return this.assetsToVideos.size();
    }

    public int getKeyVideo(String key){
        for (AssetsToVideo assets : this.assetsToVideos) {
            if(key.equals(assets.getKey())){
                return assets.getId();
            }
        }
        return 0;
    }
}
