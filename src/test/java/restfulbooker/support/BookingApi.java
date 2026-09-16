package restfulbooker.support;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import restfulbooker.config.ConfigLoader;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static io.restassured.RestAssured.given;

/**
 * Shared booking operations used across test classes: creating a booking, obtaining a token
 * (Cookie and Basic auth forms), and producing a booking ID guaranteed not to exist.
 */
public final class BookingApi {

    private final RequestSpecification spec;

    public BookingApi(RequestSpecification spec) {
        this.spec = spec;
    }

    public int createBooking(String payloadJson) {
        Response response = given()
                .log().all()
                .spec(spec)
                .body(payloadJson)
                .when()
                .post("/booking");

        response.then().log().all().statusCode(200);
        return response.jsonPath().getInt("bookingid");
    }

    public String createToken() {
        String credentials = """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(ConfigLoader.username(), ConfigLoader.password());

        Response response = given()
                .log().all()
                .spec(spec)
                .body(credentials)
                .when()
                .post("/auth");

        response.then().log().all().statusCode(200);
        String token = response.jsonPath().getString("token");
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Auth token was not returned for the configured credentials.");
        }
        return token;
    }

    public String basicAuthHeader() {
        String credentials = ConfigLoader.username() + ":" + ConfigLoader.password();
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates and immediately deletes a booking, per the review's global rule for obtaining a
     * guaranteed non-existent ID. If the delete does not return 201, this throws instead of
     * returning a possibly-still-existing ID.
     */
    public int guaranteedNonExistentId() {
        int bookingId = createBooking(BookingPayload.valid().build());
        String token = createToken();
        deleteBooking(bookingId, token);
        return bookingId;
    }

    private void deleteBooking(int bookingId, String token) {
        Response response = given()
                .log().all()
                .spec(spec)
                .cookie("token", token)
                .when()
                .delete("/booking/" + bookingId);

        response.then().log().all().statusCode(201);
    }
}
