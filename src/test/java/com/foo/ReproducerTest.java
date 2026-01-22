package com.foo;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.ReturnDocument;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.Morphia;
import dev.morphia.query.FindOptions;
import dev.morphia.query.updates.UpdateOperators;
import org.bson.UuidRepresentation;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.MongoDBContainer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static com.mongodb.MongoClientSettings.builder;

public class ReproducerTest {

    private MongoDBContainer mongoDBContainer;
    private String connectionString;

    private Datastore datastore;

    @Test
    public void reproduce() {

        final MyEntity entity = new MyEntity()
                .setName("Test Entity")
                .setValue(42)
                .setNickname("Nickname123");

        // Upsert the entity
        datastore.find(MyEntity.class, new FindOptions().limit(1))
                .modify(new ModifyOptions().upsert(true).returnDocument(ReturnDocument.AFTER),
                        UpdateOperators.setOnInsert(Map.of("id", entity.getId())),
                        dev.morphia.query.updates.UpdateOperators.set("name", entity.getName()),
                        dev.morphia.query.updates.UpdateOperators.set("value", entity.getValue()),
                        dev.morphia.query.updates.UpdateOperators.set("nickname", entity.getNickname())
                );

        // Retrieve the entity
        final MyEntity retrievedEntity = datastore.find(MyEntity.class)
                .filter(dev.morphia.query.filters.Filters.eq("id", entity.getId()))
                .first();

        // Verify that the retrieved entity matches the original
        assert entity.equals(retrievedEntity) : "Retrieved entity does not match the original";

        // check for discriminator field presence
        final org.bson.Document rawDocument = datastore.getDatabase()
                .getCollection("myEntity")
                .find(new org.bson.Document("_id", entity.getId()))
                .first();

        assert rawDocument != null;
        System.out.println("Raw Document: " + rawDocument.toJson());
        assert rawDocument.containsKey("_t") : "Discriminator field '_t' should be present in the document";
    }

    @Test
    public void saveCreatesDiscriminatorField() {
        final MyEntity entity = new MyEntity()
                .setName("Another Test Entity")
                .setValue(100)
                .setNickname("AnotherNickname");

        // Save the entity
        datastore.save(entity);

        // Retrieve the raw document from the database
        final org.bson.Document rawDocument = datastore.getDatabase()
                .getCollection("myEntity")
                .find(new org.bson.Document("_id", entity.getId()))
                .first();

        assert rawDocument != null;
        System.out.println("Raw Document after save: " + rawDocument.toJson());
        assert rawDocument.containsKey("_t") : "Discriminator field '_t' should be present in the document after save";
    }

    @NotNull
    public String databaseName() {
        return "morphia_repro";
    }

    @NotNull
    public String dockerImageName() {
        return "mongo:7";
    }

    @BeforeClass
    private void setup() {
        mongoDBContainer = new MongoDBContainer(dockerImageName());
        mongoDBContainer.start();
        connectionString = mongoDBContainer.getReplicaSetUrl(databaseName());
        System.out.println("*** MongoDB Connection String: " + connectionString);

        MongoClient mongoClient = MongoClients.create(builder()
                                                  .uuidRepresentation(UuidRepresentation.STANDARD)
                                                  .applyConnectionString(new ConnectionString(connectionString))
                                                  .build());

        datastore = Morphia.createDatastore(mongoClient, databaseName());
    }
}
