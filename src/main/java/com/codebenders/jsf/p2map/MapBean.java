package com.codebenders.jsf.p2map;

import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import static com.codebenders.jsf.p2map.Constants.LONDON_LABEL;
import static com.codebenders.jsf.p2map.Constants.LONDON_LATITUDE;
import static com.codebenders.jsf.p2map.Constants.LONDON_LONGITUDE;

/**
 * Created by fatiho on 26/06/2017.
 */
@ManagedBean
@ViewScoped
public class MapBean {
    private MapModel model = new DefaultMapModel();

    public MapBean() {
        resetToInitial();
    }

    public void resetToInitial() {
        model.getMarkers().clear();
        model.addOverlay(new Marker(new LatLng(LONDON_LATITUDE, LONDON_LONGITUDE), LONDON_LABEL));
    }

    public void addMarker(LatLng markerLocation, String label) {
        model.getMarkers().clear();
        model.addOverlay(new Marker(markerLocation, label));
    }

    public MapModel getModel() {
        return this.model;
    }
}
