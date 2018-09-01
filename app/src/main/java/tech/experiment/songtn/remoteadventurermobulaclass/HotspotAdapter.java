package tech.experiment.songtn.remoteadventurermobulaclass;

import android.content.Context;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class HotspotAdapter extends ArrayAdapter<Hotspot> {

    private int resourceId;

    public HotspotAdapter (Context context, int textViewResourceId, List<Hotspot> objects){
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Hotspot hotspot = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        ImageView hotspotImage = (ImageView) view.findViewById(R.id.hotspot_image);
        TextView hotspotName = (TextView) view.findViewById(R.id.hotspot_name);
        hotspotImage.setImageResource(hotspot.getImageId());
        hotspotName.setText(hotspot.getName());
        return view;
    }
}
