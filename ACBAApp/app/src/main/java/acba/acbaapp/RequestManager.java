package acba.acbaapp;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;

/**
 * Created by Alex on 24-May-16.
 */
public class RequestManager extends AsyncTask<String, Void, String> {
    private Context context;
    private String  url;
    private Coordinates coordinates;
    private RequestManagerHandlers handlers;

    public RequestManager(Context context, String url, RequestManagerHandlers handlers) {
        this.context = context;
        this.url = url;
        this.handlers = handlers;
    }

    public RequestManager(Context context, String formattedUrl, Coordinates coordinates, RequestManagerHandlers handlers) {
        this.context = context;
        this.url = formattedUrl;
        this.coordinates = coordinates;
        this.handlers = handlers;
    }

    @Override
    protected String doInBackground(String... params) {
        StringBuilder sb = new StringBuilder();

        try {
            URL url;
            if (coordinates != null) {
                url = new URL(String.format(this.url, coordinates.getLatitude(),
                        coordinates.getLongitude()));
            } else {
                url = new URL(this.url);
            }

//                try {
//                    InetAddress i = InetAddress.getByName("www.google.com");
//                } catch (UnknownHostException uhe) {
//                    uhe.printStackTrace();
//                }

            HttpURLConnection urlConnection =
                    (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Accept", "application/json");

            InputStream in = urlConnection.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }
            urlConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        handlers.onPostExecute(context, result);
    }
}
