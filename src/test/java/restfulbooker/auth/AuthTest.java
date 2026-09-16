package restfulbooker.auth;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import restfulbooker.base.BaseTest;
import restfulbooker.config.ConfigLoader;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AuthTest extends BaseTest {

    @Test
    @Tag("TC-AUTH-001")
    @DisplayName("TC-AUTH-001: Valid credentials return a non-empty token")
    void validCredentialsReturnToken() {
        String requestBody = """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(ConfigLoader.username(), ConfigLoader.password());

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .body(requestBody)
                .when()
                .post("/auth");

        response.then()
                .log().all()
                .statusCode(200)
                .body("token", not(nullValue()))
                .body("token", not(""));
    }

    @Test
    @Tag("TC-AUTH-002")
    @DisplayName("TC-AUTH-002: Invalid credentials do not return a token")
    void invalidCredentialsDoNotReturnToken() {
        String requestBody = """
                {
                    "username": "%s",
                    "password": "definitely-wrong-password"
                }
                """.formatted(ConfigLoader.username());

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .body(requestBody)
                .when()
                .post("/auth");

        response.then().log().all();

        // Policy 4: the documentation does not define a status code for this case, so only the
        // invariant (no token issued) is asserted. The actual status is recorded, not asserted.
        System.out.println("TC-AUTH-002 observed status code: " + response.statusCode());

        assertFalse(response.getBody().asString().contains("\"token\""), "Response must not contain a token field");
    }

    @Test
    @Tag("TC-AUTH-003")
    @DisplayName("TC-AUTH-003: Empty request body is rejected")
    void emptyRequestBodyIsRejected() {
        Response response = given()
                .log().all()
                .spec(requestSpec)
                .body("")
                .when()
                .post("/auth");

        response.then().log().all().statusCode(400);

        assertFalse(response.getBody().asString().contains("\"token\""), "Response must not contain a token field");
    }
}
