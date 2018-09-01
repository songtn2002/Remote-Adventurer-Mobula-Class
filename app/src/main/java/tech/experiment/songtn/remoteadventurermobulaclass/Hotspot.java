package tech.experiment.songtn.remoteadventurermobulaclass;

public class Hotspot {

    private String name;
    private int imageId;
    private ConnectActivity.WifiCipherType type;

    public Hotspot (String name, ConnectActivity.WifiCipherType type){
        this.name = name;
        this.type = type;
        if (name.startsWith("A")){
            imageId = R.drawable.adventurer;
        }else if (name.startsWith("M")){
            imageId = R.drawable.mobula;
        }
    }

    public String getName() {
        return name;
    }

    public ConnectActivity.WifiCipherType getType(){
        return type;
    }

    public int getImageId() {
        return imageId;
    }

    public String getPassword(){
        if (this.name.startsWith("A")){
            return "adventurer";
        }else if (this.name.startsWith("M")){
            return "mobula";
        }
        return null;
    }
}
