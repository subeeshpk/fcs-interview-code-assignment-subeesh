package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StoreResourceTest {

    @Test
    public void testCrudStore() {
        final String path = "store";

        given()
                .when()
                .get(path)
                .then()
                .statusCode(200)
                .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));

        String newStoreJson = "{\"name\":\"DELFT-001\",\"quantityProductsInStock\":7}";

        Long newId =
                given()
                        .contentType(ContentType.JSON)
                        .body(newStoreJson)
                        .when()
                        .post(path)
                        .then()
                        .statusCode(201)
                        .body(containsString("DELFT-001"))
                        .extract()
                        .jsonPath()
                        .getLong("id");

        given()
                .when()
                .get(path + "/" + newId)
                .then()
                .statusCode(200)
                .body(containsString("DELFT-001"));

        String updateJson = "{\"name\":\"DELFT-002\",\"quantityProductsInStock\":9}";

        given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when()
                .put(path + "/" + newId)
                .then()
                .statusCode(200)
                .body(containsString("DELFT-002"));

        String patchJson = "{\"name\":\"DELFT-002\",\"quantityProductsInStock\":12}";

        given()
                .contentType(ContentType.JSON)
                .body(patchJson)
                .when()
                .patch(path + "/" + newId)
                .then()
                .statusCode(200)
                .body(containsString("12"));

        given().when().delete(path + "/" + newId).then().statusCode(204);

        given()
                .when()
                .get(path)
                .then()
                .statusCode(200)
                .body(not(containsString("DELFT-002")));
    }

    @Test
    public void testGetSingleStore_notFound() {
        given().when().get("store/999999").then().statusCode(404);
    }

    @Test
    public void testCreateStore_withIdSet_returns422() {
        String invalidJson = "{\"id\":1,\"name\":\"X\",\"quantityProductsInStock\":1}";
        given()
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post("store")
                .then()
                .statusCode(422);
    }

    @Test
    public void testUpdateStore_notFound() {
        String json = "{\"name\":\"X\",\"quantityProductsInStock\":1}";
        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .put("store/999999")
                .then()
                .statusCode(404);
    }

    @Test
    public void testUpdateStore_missingName_returns422() {
        String json = "{\"quantityProductsInStock\":1}";
        given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .put("store/1")
                .then()
                .statusCode(422);
    }

    @Test
    public void testDeleteStore_notFound() {
        given().when().delete("store/999999").then().statusCode(404);
    }
}