package umm3601.goal;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.json.JsonReader;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import umm3601.ControllerSuperSpec;
import umm3601.user.UserControllerSpec;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class GoalControllerSpec extends ControllerSuperSpec{
    private GoalController goalController;
    private ObjectId mattsId;

    @Before
    public void clearAndPopulateDB() throws IOException {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase db = mongoClient.getDatabase("test");
        MongoCollection<Document> emojiDocuments = db.getCollection("goals");
        emojiDocuments.drop();
        List<Document> testGoals = new ArrayList<>();
        testGoals.add(Document.parse("{\n" +
            "                    owner: \"Ahnaf\",\n" +
            "                    name: \"Do the Dishes\",\n" +
            "                    creation_date: \"8/20/2015 20:00\",\n" +
            "                }"));
        testGoals.add(Document.parse("{\n" +
            "                    owner: \"Aurora\",\n" +
            "                    name: \"Call Mom\",\n" +
            "                    creation_date: \"7/13/2016 08:00\",\n" +
            "                }"));
        testGoals.add(Document.parse("{\n" +
            "                    owner: \"Ethan\",\n" +
            "                    name: \"Fold Laundry\",\n" +
            "                    creation_date: \"2/10/2017 12:00\",\n" +
            "                }"));

        mattsId = new ObjectId();
        BasicDBObject matt = new BasicDBObject("_id", mattsId);
        matt = matt.append("owner", "Matt")
            .append("name", "Eat Breakfast")
            .append("creation_date", "11/11/2011 09:00");



        emojiDocuments.insertMany(testGoals);
        emojiDocuments.insertOne(Document.parse(matt.toJson()));

        // It might be important to construct this _after_ the DB is set up
        // in case there are bits in the constructor that care about the state
        // of the database.
        goalController = new GoalController(db);
    }


    private static String getOwner(BsonValue val) {
        BsonDocument doc = val.asDocument();
        return ((BsonString) doc.get("owner")).getValue();
    }

    @Test
    public void getAllUsers() {
        Map<String, String[]> emptyMap = new HashMap<>();
        String jsonResult = goalController.getItems(emptyMap);
        BsonArray docs = parseJsonArray(jsonResult);

        assertEquals("Should be 4 users", 4, docs.size());
        List<String> names = docs
            .stream()
            .map(GoalControllerSpec::getOwner)
            .sorted()
            .collect(Collectors.toList());
        List<String> expectedNames = Arrays.asList("Ahnaf", "Aurora", "Ethan", "Matt");
        assertEquals("Names should match", expectedNames, names);
    }
}
