package com.codebenders.jsf.p2map;

import com.codebenders.jsf.p2map.model.Postcode;
import com.mongodb.*;
import lombok.Data;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.primefaces.model.map.LatLng;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.List;

import static com.codebenders.jsf.p2map.Constants.LONDON_LATITUDE;
import static com.codebenders.jsf.p2map.Constants.LONDON_LONGITUDE;

/**
 * Created by fatiho on 24/06/2017.
 */

@ManagedBean
@ViewScoped
@Data
public class PostcodeBean implements Serializable {
    private String searchText;
    private String resultMessage = "";
    private Postcode postcode;

    @ManagedProperty(value="#{mapBean}")
    MapBean mapBean;

    public PostcodeBean() {
        this.postcode = new Postcode();
        postcode.setLatitude(LONDON_LATITUDE);
        postcode.setLongitude(LONDON_LONGITUDE);
    }

    public void findPostcodeLocation() {
        if (searchText == null || searchText.isEmpty()) {
            resultMessage = "Please enter a value";
            this.postcode = new Postcode();
            postcode.setLatitude(LONDON_LATITUDE);
            postcode.setLongitude(LONDON_LONGITUDE);
            mapBean.resetToInitial();
        } else {
//        Old school way of doing it
//        String result = "";
//        MongoClient mongo = new MongoClient( "localhost" , 27017 );
//        DB db = mongo.getDB("postcodes");
//        DBCollection table = db.getCollection("pclatlong");
//        BasicDBObject searchQuery = new BasicDBObject();
//        searchQuery.put("postcode", searchText);
//        DBCursor cursor = table.find(searchQuery);
//        while (cursor.hasNext()) {
//            result = result + cursor.next();
//        }
//        if (result == null || result.isEmpty()) {
//            resultMessage = "No location found for " + searchText;
//        } else {
//            resultMessage = result;
//        }
            //Morphia way
            final Morphia morphia = new Morphia();

            // tell Morphia where to find your classes
            morphia.mapPackage("com.codebenders.jsf.p2map.model");

            // create the Datastore connecting to the default port on the local host
            final Datastore datastore = morphia.createDatastore(new MongoClient(), "postcodes");
            datastore.ensureIndexes();

            // do the query
            final Query<Postcode> query = datastore.createQuery(Postcode.class).field("postcode").equal(searchText.toUpperCase());
            final List<Postcode> postcodes = query.asList();

            if (postcodes.isEmpty()) {
                resultMessage = "No location found for " + searchText;
            } else {
                this.postcode = postcodes.get(0);
                mapBean.addMarker(new LatLng(postcode.getLatitude(), postcode.getLongitude()), postcode.getPostcode());
                resultMessage = "Map refreshed to " + searchText;
            }
        }
    }
}