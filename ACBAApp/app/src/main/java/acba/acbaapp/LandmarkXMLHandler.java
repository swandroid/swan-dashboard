package acba.acbaapp;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

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
            double latMin = Double.parseDouble(attributes.getValue("LATMIN"));
            double lngMin = Double.parseDouble(attributes.getValue("LNGMIN"));
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