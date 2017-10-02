package com.nightcap.previously;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Created by Paul on 2/10/2017.
 */

class DatabaseMigration implements RealmMigration {

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        // DynamicRealm exposes an editable schema
        RealmSchema schema = realm.getSchema();

        // Migrate to version 1: Add a new class.
        // Example:
        // public Person extends RealmObject {
        //     private String name;
        //     private int age;
        //     // getters and setters left out for brevity
        // }
//        if (oldVersion == 0) {
//            schema.create("Person")
//                    .addField("name", String.class)
//                    .addField("age", int.class);
//            oldVersion++;
//        }

        // Migrate to version 2: Add a primary key + object references
        // Example:
        // public Person extends RealmObject {
        //     private String name;
        //     @PrimaryKey
        //     private int age;
        //     private Dog favoriteDog;
        //     private RealmList<Dog> dogs;
        //     // getters and setters left out for brevity
        // }
        if (oldVersion == 1) {
            schema.get("Event")
                    .addField("category", String.class);
//                    .addRealmObjectField("favoriteDog", schema.get("Dog"))
//                    .addRealmListField("dogs", schema.get("Dog"));
            oldVersion++;
        }
    }

    // Solution for Realm migration error:
    // https://stackoverflow.com/questions/36907001/open-realm-with-new-realmconfiguration
    @Override
    public boolean equals(Object o) {
        return o instanceof DatabaseMigration;
    }
}
