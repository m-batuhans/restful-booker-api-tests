package restfulbooker.smoke;

import io.restassured.RestAssured;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import restfulbooker.base.BaseTest;

@Tag("SMOKE")
class HealthCheckTest extends BaseTest {

    @Test
    @DisplayName("GET /ping returns 201 Created")
    void healthCheckReturnsCreated() {
        RestAssured.given()
                .spec(requestSpec)
                .when()
                .get("/ping")
                .then()
                .statusCode(201);
    }
}
