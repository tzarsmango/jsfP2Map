This repository contains the source code for an article posted in [koders.co](http://www.koders.co/programming/a-tribute-to-jsf-postcode-to-map/)


I felt it's time to pay a tribute to my old friend JSF (which actually caused lots of pain) 
and see how I can play with it and may be add some flavour from current technologies down the road.

# The application
I wanted the application to be simple and somewhat useful. In one of my previous project we implemented an 
"Address Finder" micro service which used another paid service in the backend. We added a local cache to save 
requests and went to the other service only it is a cash miss, or if the user experiences a problem finding 
his address or if the cache expires. Nowadays, I have been working with maps and addresses frequently so I 
wanted to have a basic application that shows the location based on postcodes.

# Setting the stage
For building a JSF application, I preferred using PrimeFaces which is pretty solid and since I had used PrimeFaces in my previous JSF projects.
I used mongoDB since the data at hand is pretty static and the key for searching is clear. In fact, I just wanted to play with mongoDB :) 
I used Wildfly as the application server and Maven to bring together everything.

## Acquiring and Importing the Data
For my application I needed data so I made a google search to get the post office vs geographical location data. There are several sources available and I chose the one in the Free Map Tools web site (https://www.freemaptools.com/download-uk-postcode-lat-lng.htm)
After I downloaded and extracted the data, I imported the CSV file into mongo using the following command. The data is uploaded within a minute.

```
fatiho@sardis:~/Downloads/postcodeData$ mongoimport --host=127.0.0.1:27017 -d postcodes -c pclatlong --type csv --file ukpostcodes.csv --headerline
2017-06-25T20:42:51.478+0100	connected to: 127.0.0.1:27017
2017-06-25T20:42:54.473+0100	[####....................] postcodes.pclatlong	15.8MB/90.9MB (17.4%)
2017-06-25T20:42:57.475+0100	[########................] postcodes.pclatlong	33.0MB/90.9MB (36.3%)
2017-06-25T20:43:00.474+0100	[#############...........] postcodes.pclatlong	50.4MB/90.9MB (55.5%)
2017-06-25T20:43:03.473+0100	[#################.......] postcodes.pclatlong	67.9MB/90.9MB (74.7%)
2017-06-25T20:43:06.473+0100	[######################..] postcodes.pclatlong	85.3MB/90.9MB (93.9%)
2017-06-25T20:43:07.454+0100	[########################] postcodes.pclatlong	90.9MB/90.9MB (100.0%)
2017-06-25T20:43:07.454+0100	imported 1741532 documents
```
## Mongo DB Configuration
In order to simplify things I pulled a docker image with mongo DB and import the CSV data directly to the image. `
The command for pulling the image and the output is as follows:
```
fatiho@sardis:~$docker pull mongo
Using default tag: latest
latest: Pulling from library/mongo
f5cc0ee7a6f6: Pull complete
d99b18c5f0ce: Pull complete
2abe504e5492: Pull complete
010336760fd5: Pull complete
1bebc569a09b: Pull complete
99973eccc29a: Pull complete
3caee68cdf37: Pull complete
2f024b88f543: Pull complete
74f877b8f67d: Pull complete
72dc91cfe502: Pull complete
d610498cfcc7: Pull complete
Digest:     sha256:f1ae736ea5f115822cf6fcef6458839d87bdaea06f40b97934ad913ed348f67d
Status: Downloaded newer image for mongo:latest
```
Then run the downloaded image with the following command:
```
fatiho@sardis:~$ docker run --name ukpostcodes -p:27017:27017 -d mongo
dbc9b88d7b0843025ac761faccb8d0424639b7806ead3c2f766c95069080c552
```
After you import the data you may optionally create an index on the postcode field in order to reach the data faster. `
To do that first you connect to mongodb within your shell, switch to ukpostcodes db and execute a create index operation.
```
fatiho@sardis:~$ docker exec -it ukpostcodes mongo admin
MongoDB shell version v3.4.5
connecting to: mongodb://127.0.0.1:27017/admin
MongoDB server version: 3.4.5
Welcome to the MongoDB shell.
For interactive help, type “help”.
For more comprehensive documentation, see
http://docs.mongodb.org/
Questions? Try the support group
http://groups.google.com/group/mongodb-user
> use postcodes
switched to db postcodes
> show collections
pclatlong
> db.pclatlong.createIndex( {postcode: 1} )
{
	"createdCollectionAutomatically" : false,
	"numIndexesBefore" : 1,
	"numIndexesAfter" : 2,
	"ok" : 1
}
```
## Workspace Configuration
I started with putting a set of JSF dependencies in place. As usual, it took some time to find the correct set of libraries that work together. Then a second round of compatibility check was required after putting the current version (6.2) of PrimeFaces. The resulting dependency mix that worked for me is as follows: 
```
        <dependency>
            <groupId>org.primefaces</groupId>
            <artifactId>primefaces</artifactId>
            <version>6.1</version>
        </dependency>
        <dependency>
            <groupId>com.sun.faces</groupId>
            <artifactId>jsf-api</artifactId>
            <version>2.0.1</version>
        </dependency>
        <dependency>
            <groupId>com.sun.faces</groupId>
            <artifactId>jsf-impl</artifactId>
            <version>2.0.1</version>
        </dependency>
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>jstl</artifactId>
            <version>1.2</version>
        </dependency>
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>servlet-api</artifactId>
            <version>2.5</version>
        </dependency>
        <dependency>
            <groupId>javax.servlet.jsp</groupId>
            <artifactId>jsp-api</artifactId>
            <version>2.1</version>
        </dependency>
        <dependency>
            <groupId>org.primefaces.extensions</groupId>
            <artifactId>all-themes</artifactId>
            <version>1.0.8</version>
        </dependency>
```

I chose to get all themes of prime faces to play with it, you may chose the one you like to minimise the deployment package.

# Page Implementation
I downloaded the bootstrap template and put a navigation bar at the top and a footer. After adding some descriptive text I 
placed my form in a div (as suggested in PrimeFaces demo)so that it is responsive. I used "spacers" from PrimeFaces to make 
the form look like what I expected.   
```
    <div class="ui-fluid">
		 <h:form>
			<p:panelGrid columns="2" columnClasses="ui-grid-col-2,ui-grid-col-10" layout="grid" styleClass="ui-panelgrid-blank">
				<p:spacer width="10"/>
				<p:outputLabel id="display" value="#{postcodeBean.resultMessage}"/>
				<p:outputLabel for="searchText" value="Enter postcode" />
				<p:inputText id="searchText" value="#{postcodeBean.searchText}"/>
				<p:spacer width="10"/>
				<p:commandButton value="Submit" update="display, pmap" icon="ui-icon-check" 
					action="#{postcodeBean.findPostcodeLocation}"/>
			</p:panelGrid>
		</h:form>
	</div>

```
The page needs a backing bean to keep the data of the front end. Another backing bean is required for keeping track of the 
Map markers. I marked both beans as @ManagedBean so that the application knows how to access the beans as needed. 
Thanks to the "@ManagedProperty" annotation, I didn't have to write XML configuration for accessing one bean from another. 
Note that I use the postcode object initialized in the constructor to center the map over London city center.

```
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
   	...
```
At first I just wanted a plain query from the DB and printout the coordinates of the matching postcode if found any. So
the code for searching the MongoDB is as follows:
````
	...
	String result = "";
	MongoClient mongo = new MongoClient( "localhost" , 27017 );
	DB db = mongo.getDB("postcodes");
	DBCollection table = db.getCollection("pclatlong");
	BasicDBObject searchQuery = new BasicDBObject();
	searchQuery.put("postcode", searchText);
	DBCursor cursor = table.find(searchQuery);
	while (cursor.hasNext()) {
		result = result + cursor.next();
	}
	if (result == null || result.isEmpty()) {
		resultMessage = "No location found for " + searchText;
	} else {
		resultMessage = result;
	}
	...
````
Then I though "why not put the coordinates in a better shape?". PrimeFaces has a really good component for the purpose. 
I used GMap component by adding the following lines just after the from on my page.
````
	...
	<p:spacer height="20"/>
	<p:gmap id="pmap" center="#{postcodeBean.postcode.latitude},#{postcodeBean.postcode.longitude}"
			zoom="15" type="MAP" style="width:100%;height:400px" model="#{mapBean.model}"/>
	<p:spacer height="20"/>
	...
````
I had some difficulty while setting up the GMap component according to the user manual. But then I figured 
out that I needed to add a script line including my API_KEY for Google Maps which worked fine. 
```
    <script src="http://maps.google.com/maps/api/js?key=MY_API_KEY" type="text/javascript"></script>
```
# Adding Morphia Support
There are always better and elegant ways to play with data so I made a quick inquiry about how I can implement an ORM 
tool for MongoDB. Among several alternatives like MongoJack, MJORM, and Jongo I chose Morphia which is supported by 
MongoDB Inc. You just need to define your entity classes, highlight your indexes and Id fields. The rest is pretty straight forrward.

I added the dependency to the pom.xml
```
        <dependency>
            <groupId>org.mongodb.morphia</groupId>
            <artifactId>morphia</artifactId>
            <version>1.3.2</version>
        </dependency>
```
Created the Postcode entity object:
```
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
```
And used it in my action call replacing all the previous MongoDB calls:
```
		//Morphia way
		final Morphia morphia = new Morphia();

		// tell Morphia where to find your classes
		morphia.mapPackage("com.codebenders.jsf.p2map.model");

		final Datastore datastore = morphia.createDatastore(new MongoClient(), "postcodes");
		datastore.ensureIndexes();
		final Query<Postcode> query = datastore.createQuery(Postcode.class).field("postcode").equal(searchText.toUpperCase());
		final List<Postcode> postcodes = query.asList();
		if (postcodes.isEmpty()) {
			resultMessage = "No location found for " + searchText;
		} else {
			this.postcode = postcodes.get(0);
			mapBean.addMarker(new LatLng(postcode.getLatitude(), postcode.getLongitude()), postcode.getPostcode());
			resultMessage = "Map refreshed to " + searchText;
		}
```
The source code can be found on [my github page](https://github.com/tzarsmango/jsfP2Map) and the final look and feel of this applicaiton is as follows:

![p2map JSF application](https://i0.wp.com/www.koders.co/wp-content/uploads/2017/06/P2Map.png?zoom=2&fit=1906%2C1162)

