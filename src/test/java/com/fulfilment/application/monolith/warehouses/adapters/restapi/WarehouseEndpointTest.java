package com.fulfilment.application.monolith.warehouses.adapters.restapi;

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
public class WarehouseEndpointTest {

  @Test
  @Order(1)
  public void testSimpleListWarehouses() {
    given()
            .when().get("warehouse")
            .then()
            .statusCode(200)
            .body(containsString("MWH.001"), containsString("MWH.012"), containsString("MWH.023"));
  }

  @Test
  @Order(2)
  public void testSimpleCheckingArchivingWarehouses() {
    // Confirm all three present
    given()
            .when().get("warehouse")
            .then()
            .statusCode(200)
            .body(containsString("MWH.001"), containsString("MWH.012"), containsString("MWH.023"));

    // Archive MWH.001
    given().when().delete("warehouse/MWH.001").then().statusCode(204);

    // MWH.001 no longer in active list
    given()
            .when().get("warehouse")
            .then()
            .statusCode(200)
            .body(not(containsString("MWH.001")), containsString("MWH.012"), containsString("MWH.023"));
  }

  // --- NEW TESTS BELOW ---

  @Test
  @Order(3)
  public void testGetSingleWarehouse() {
    // MWH.012 is still active after Order 2
    given()
            .when().get("warehouse/MWH.012")
            .then()
            .statusCode(200)
            .body(containsString("MWH.012"));
  }

  @Test
  @Order(4)
  public void testGetSingleWarehouse_notFound() {
    given()
            .when().get("warehouse/MWH.NONE")
            .then()
            .statusCode(404);
  }

  @Test
  @Order(5)
  public void testCreateWarehouse() {
    // MWH.001 was archived in Order 2, so ZWOLLE-001 slot is free (maxWarehouses=1, maxCapacity=40)
    String body = "{\"businessUnitCode\":\"MWH.100\","
            + "\"location\":\"ZWOLLE-001\","
            + "\"capacity\":30,"
            + "\"stock\":0}";

    given()
            .contentType(ContentType.JSON)
            .body(body)
            .when().post("warehouse")
            .then()
            .statusCode(200)
            .body(containsString("MWH.100"));
  }

  @Test
  @Order(6)
  public void testCreateWarehouse_duplicateBUCode_returns400() {
    // MWH.012 already exists — CreateWarehouseUseCase throws IllegalArgumentException
    // which the WarehouseExceptionMapper converts to 400
    String body = "{\"businessUnitCode\":\"MWH.012\","
            + "\"location\":\"AMSTERDAM-001\","
            + "\"capacity\":10,"
            + "\"stock\":0}";

    given()
            .contentType(ContentType.JSON)
            .body(body)
            .when().post("warehouse")
            .then()
            .statusCode(400);
  }

  @Test
  @Order(7)
  public void testReplaceWarehouse() {
    // MWH.023 is at TILBURG-001, capacity=30, stock=27
    // TILBURG-001 maxCapacity=40, no other active warehouse there after Order 2
    // Replacement: same location, capacity>=stock(27), stock must match existing stock(27)
    String body = "{\"businessUnitCode\":\"MWH.023\","
            + "\"location\":\"TILBURG-001\","
            + "\"capacity\":30,"
            + "\"stock\":27}";

    given()
            .contentType(ContentType.JSON)
            .body(body)
            .when().post("warehouse/MWH.023/replacement")
            .then()
            .statusCode(200)
            .body(containsString("MWH.023"), containsString("TILBURG-001"));
  }
}