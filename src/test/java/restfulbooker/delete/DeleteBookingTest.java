package restfulbooker.delete;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import restfulbooker.base.BaseTest;
import restfulbooker.support.BookingApi;
import restfulbooker.support.BookingPayload;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeleteBookingTest extends BaseTest {

    @Test
    @Tag("TC-DELETE-001")
    @DisplayName("TC-DELETE-001: Deleting a booking with a valid Cookie token removes it")
    void deleteBookingWithCookieTokenRemovesIt() {
        BookingApi api = new BookingApi(requestSpec);
        int bookingId = api.createBooking(BookingPayload.valid().build());
        String token = api.createToken();

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .cookie("token", token)
                .when()
                .delete("/booking/" + bookingId);

        response.then().log().all().statusCode(201);

        Response verify = given()
                .log().all()
                .spec(requestSpec)
                .when()
                .get("/booking/" + bookingId);

        verify.then().log().all().statusCode(404);
    }

    @Test
    @Tag("TC-DELETE-002")
    @DisplayName("TC-DELETE-002: Deleting a booking with a valid Basic auth header succeeds")
    void deleteBookingWithBasicAuthSucceeds() {
        BookingApi api = new BookingApi(requestSpec);
        int bookingId = api.createBooking(BookingPayload.valid().build());

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .header("Authorization", api.basicAuthHeader())
                .when()
                .delete("/booking/" + bookingId);

        response.then().log().all().statusCode(201);
    }

    @Test
    @Tag("TC-DELETE-003")
    @DisplayName("TC-DELETE-003: Deleting a booking without auth is rejected and the booking still exists")
    void deleteBookingWithoutAuthIsRejectedAndStillExists() {
        BookingApi api = new BookingApi(requestSpec);
        int bookingId = api.createBooking(BookingPayload.valid().build());

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .when()
                .delete("/booking/" + bookingId);

        response.then().log().all();
        assertTrue(response.statusCode() == 401 || response.statusCode() == 403,
                "Expected 401 or 403 but got " + response.statusCode());

        Response verify = given()
                .log().all()
                .spec(requestSpec)
                .when()
                .get("/booking/" + bookingId);

        verify.then().log().all().statusCode(200);
    }

    @Test
    @Tag("TC-DELETE-004")
    @DisplayName("TC-DELETE-004: Deleting a non-existent booking returns 404")
    void deleteNonExistentBookingReturns404() {
        BookingApi api = new BookingApi(requestSpec);
        int nonExistentId = api.guaranteedNonExistentId();
        String token = api.createToken();

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .cookie("token", token)
                .when()
                .delete("/booking/" + nonExistentId);

        response.then().log().all().statusCode(404);
    }

    @Test
    @Tag("TC-DELETE-005")
    @DisplayName("TC-DELETE-005: Deleting a booking with an invalid token is rejected and the booking still exists")
    void deleteBookingWithInvalidTokenIsRejectedAndStillExists() {
        BookingApi api = new BookingApi(requestSpec);
        int bookingId = api.createBooking(BookingPayload.valid().build());

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .cookie("token", "invalid")
                .when()
                .delete("/booking/" + bookingId);

        response.then().log().all();
        assertTrue(response.statusCode() == 401 || response.statusCode() == 403,
                "Expected 401 or 403 but got " + response.statusCode());

        Response verify = given()
                .log().all()
                .spec(requestSpec)
                .when()
                .get("/booking/" + bookingId);

        verify.then().log().all().statusCode(200);
    }
}
