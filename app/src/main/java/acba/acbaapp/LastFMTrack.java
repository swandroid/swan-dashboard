package acba.acbaapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Alex on 24-May-16.
 */
public class LastFMTrack {

    private String name;
    private int listeners;
    private String url;
    private String artistName;
    private String artistUrl;
    private String imageUrl;

    public LastFMTrack(JSONObject jsonObject) {
        try {
            name = jsonObject.getString("name");
            listeners = jsonObject.getInt("listeners");
            url = jsonObject.getString("url");
            JSONObject artist = jsonObject.getJSONObject("artist");
            artistName = artist.getString("name");
            artistUrl = artist.getString("url");
            JSONArray images = jsonObject.getJSONArray("image");
            imageUrl = images.getJSONObject(0).getString("#text");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public int getListeners() {
        return listeners;
    }

    public String getUrl() {
        return url;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getArtistUrl() {
        return artistUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
