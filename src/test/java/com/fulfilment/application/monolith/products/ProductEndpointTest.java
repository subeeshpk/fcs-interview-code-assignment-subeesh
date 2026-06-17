package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductEndpointTest {

  @Test
  @Order(1)
  public void testGetSingleProduct() {
    // Product 2 (KALLAX) exists from import.sql seed data
    given()
            .when().get("product/2")
            .then()
            .statusCode(200)
            .body(containsString("KALLAX"));
  }

  @Test
  @Order(2)
  public void testGetSingleProduct_notFound() {
    given()
            .when().get("product/999")
            .then()
            .statusCode(404);
  }

  @Test
  @Order(3)
  public void testCreateProduct() {
    String body = "{\"name\":\"HEMNES\",\"stock\":5}";

    given()
            .contentType(ContentType.JSON)
            .body(body)
            .when().post("product")
            .then()
            .statusCode(201)
            .body(containsString("HEMNES"));
  }

  @Test
  @Order(4)
  public void testCreateProduct_withIdSet_returns422() {
    String body = "{\"id\":1,\"name\":\"INVALID\",\"stock\":0}";

    given()
            .contentType(ContentType.JSON)
            .body(body)
            .when().post("product")
            .then()
            .statusCode(422);
  }

  @Test
  @Order(5)
  public void testUpdateProduct() {
    // Product 3 (BESTÅ) exists from seed data
    String body = "{\"name\":\"BESTÅ-UPDATED\",\"stock\":10}";

    given()
            .contentType(ContentType.JSON)
            .body(body)
            .when().put("product/3")
            .then()
            .statusCode(200)
            .body(containsString("BESTÅ-UPDATED"));
  }

  @Test
  @Order(6)
  public void testUpdateProduct_missingName_returns422() {
    String body = "{\"stock\":5}";

    given()
            .contentType(ContentType.JSON)
            .body(body)
            .when().put("product/3")
            .then()
            .statusCode(422);
  }

  @Test
  @Order(7)
  public void testUpdateProduct_notFound() {
    String body = "{\"name\":\"X\",\"stock\":0}";

    given()
            .contentType(ContentType.JSON)
            .body(body)
            .when().put("product/999")
            .then()
            .statusCode(404);
  }

  @Test
  @Order(100)
  public void testCrudProduct() {
    // Runs last — deletes product 1 which is fine as no later test needs it
    given()
            .when().get("product")
            .then()
            .statusCode(200)
            .body(containsString("TONSTAD"), containsString("KALLAX"));

    given().when().delete("product/1").then().statusCode(204);

    given()
            .when().get("product")
            .then()
            .statusCode(200)
            .body(not(containsString("TONSTAD")), containsString("KALLAX"));
  }
}