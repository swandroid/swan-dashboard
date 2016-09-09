package swan.dashboard.services;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import swan.dashboard.models.Coordinates;
import swan.dashboard.models.MapMarker;

/**
 * Created by Alex on 24-May-16.
 */
public class LandmarkXMLHandler extends DefaultHandler {
    private ArrayList<MapMarker> markers;
    private MapMarker marker;

    public LandmarkXMLHandler() {
        markers = new ArrayList<>();
    }

    public ArrayList<MapMarker> getMarkers() {
        return markers;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        if(qName.equalsIgnoreCase("object")) {
            String label = attributes.getValue("LABEL");
            String lat = attributes.getValue("LATMIN");
            String lng = attributes.getValue("LNGMIN");
            double latMin, lngMin;
            if (lat == null || lng == null){
                String coords = attributes.getValue("COORDS").replace("||", "");
                int index = coords.indexOf(',');

                lngMin = Double.parseDouble(coords.substring(0, index - 1));
                latMin = Double.parseDouble(coords.substring(index + 1));
            } else {
                latMin = Double.parseDouble(lat);
                lngMin = Double.parseDouble(lng);
            }

            marker = new MapMarker(label, new Coordinates(latMin, lngMin));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equalsIgnoreCase("object")) {
            markers.add(marker);
        }
    }
}