package com.codebenders.jsf.p2map;


import com.codebenders.jsf.p2map.MapBean;
import com.codebenders.jsf.p2map.PostcodeBean;
import com.mongodb.MongoClient;
import org.junit.Before;
import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.primefaces.model.map.LatLng;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by fatiho on 30/06/2017.
 */
public class SearchTester {
    double latitude = 20.0;
    double longitude = 20.0;
    String label = "TestLabel";

    MapBean mapBean;

    PostcodeBean postcodeBean;

    @Before
    public void initialize() {
        mapBean = new MapBean();
        postcodeBean = new PostcodeBean();
        postcodeBean.setMapBean(mapBean);
    }

    @Test
    public void shouldFindPostCodeLocation() {
        String searchText = "E9 5PN";
        postcodeBean.setSearchText(searchText);
        postcodeBean.findPostcodeLocation();
        assertThat(postcodeBean.getResultMessage()).isEqualToIgnoringWhitespace("Map refreshed to " + searchText);
    }

    @Test
    public void addMarkerShouldReplaceMarker() {
        LatLng markerLocation = new LatLng(latitude,longitude);
        mapBean.addMarker(markerLocation, label);
        assertThat(postcodeBean.getMapBean().getModel().getMarkers().size()).isEqualTo(1);
        assertThat(postcodeBean.getMapBean().getModel().getMarkers().get(0).getLatlng().getLat()).isEqualTo(latitude);
        assertThat(postcodeBean.getMapBean().getModel().getMarkers().get(0).getLatlng().getLng()).isEqualTo(longitude);
        assertThat(postcodeBean.getMapBean().getModel().getMarkers().get(0).getTitle()).isEqualTo(label);
    }

    @Test
    public void resetToInitialReturnsMarkerToLondonCenter() {
        LatLng markerLocation = new LatLng(latitude,longitude);
        String label = "label";
        mapBean.addMarker(markerLocation, label);
        mapBean.resetToInitial();
        assertThat(postcodeBean.getMapBean().getModel().getMarkers().size()).isEqualTo(1);
        assertThat(postcodeBean.getMapBean().getModel().getMarkers().get(0).getLatlng().getLat()).isEqualTo(Constants.LONDON_LATITUDE);
        assertThat(postcodeBean.getMapBean().getModel().getMarkers().get(0).getLatlng().getLng()).isEqualTo(Constants.LONDON_LONGITUDE);
        assertThat(postcodeBean.getMapBean().getModel().getMarkers().get(0).getTitle()).isEqualTo(Constants.LONDON_LABEL);
    }
}