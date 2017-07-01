package com.codebenders.jsf.p2map.model;

/**
 * Created by fatiho on 25/06/2017.
 */

import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

@Entity("pclatlong")
@Indexes(
        @Index(value = "postcode", fields = @Field("postcode"))
)
@Data
public class Postcode {
    @Id
    private ObjectId id;
    @Property("id")
    private String postcodeId;
    private String postcode;
    private Double latitude;
    private Double longitude;
}