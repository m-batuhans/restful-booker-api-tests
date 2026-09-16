package restfulbooker.update;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import restfulbooker.base.BaseTest;
import restfulbooker.support.BookingApi;
import restfulbooker.support.BookingPayload;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateBookingTest extends BaseTest {

    @Test
    @Tag("TC-UPDATE-001")
    @DisplayName("TC-UPDATE-001: Updating a booking with a valid Cookie token stores the new values")
    void updateBookingWithCookieTokenStoresNewValues() {
        BookingApi api = new BookingApi(requestSpec);
        int bookingId = api.createBooking(BookingPayload.valid().build());
        String token = api.createToken();

        BookingPayload updated = BookingPayload.valid()
                .firstname("James")
                .checkin("2026-02-01")
                .checkout("2026-02-10");

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .cookie("token", token)
                .body(updated.build())
                .when()
                .put("/booking/" + bookingId);

        response.then()
                .log().all()
                .statusCode(200)
                .body("firstname", equalTo("James"))
                .body("lastname", equalTo(updated.currentLastname()))
                .body("bookingdates.checkin", equalTo("2026-02-01"))
                .body("bookingdates.checkout", equalTo("2026-02-10"));

        Response verify = given()
                .log().all()
                .spec(requestSpec)
                .when()
                .get("/booking/" + bookingId);

        verify.then()
                .log().all()
                .statusCode(200)
                .body("firstname", equalTo("James"))
                .body("bookingdates.checkin", equalTo("2026-02-01"))
                .body("bookingdates.checkout", equalTo("2026-02-10"));
    }

    @Test
    @Tag("TC-UPDATE-002")
    @DisplayName("TC-UPDATE-002: Updating a booking with a valid Basic auth header stores the new values")
    void updateBookingWithBasicAuthReturnsUpdatedValues() {
        BookingApi api = new BookingApi(requestSpec);
        int bookingId = api.createBooking(BookingPayload.valid().build());

        BookingPayload updated = BookingPayload.valid().firstname("James");

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .header("Authorization", api.basicAuthHeader())
                .body(updated.build())
                .when()
                .put("/booking/" + bookingId);

        response.then()
                .log().all()
                .statusCode(200)
                .body("firstname", equalTo("James"))
                .body("lastname", equalTo(updated.currentLastname()));
    }

    @Test
    @Tag("TC-UPDATE-003")
    @DisplayName("TC-UPDATE-003: Updating a booking without auth is rejected and the booking is unchanged")
    void updateBookingWithoutAuthIsRejectedAndUnchanged() {
        BookingApi api = new BookingApi(requestSpec);
        BookingPayload original = BookingPayload.valid();
        int bookingId = api.createBooking(original.build());

        BookingPayload attempted = BookingPayload.valid().firstname("Intruder");

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .body(attempted.build())
                .when()
                .put("/booking/" + bookingId);

        response.then().log().all();
        assertTrue(response.statusCode() == 401 || response.statusCode() == 403,
                "Expected 401 or 403 but got " + response.statusCode());

        Response verify = given()
                .log().all()
                .spec(requestSpec)
                .when()
                .get("/booking/" + bookingId);

        verify.then()
                .log().all()
                .statusCode(200)
                .body("firstname", equalTo(original.currentFirstname()))
                .body("lastname", equalTo(original.currentLastname()));
    }

    @Test
    @Tag("TC-UPDATE-004")
    @DisplayName("TC-UPDATE-004: Updating a non-existent booking returns 404")
    void updateNonExistentBookingReturns404() {
        BookingApi api = new BookingApi(requestSpec);
        int nonExistentId = api.guaranteedNonExistentId();
        String token = api.createToken();

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .cookie("token", token)
                .body(BookingPayload.valid().build())
                .when()
                .put("/booking/" + nonExistentId);

        response.then().log().all().statusCode(404);
    }

    @Test
    @Tag("TC-UPDATE-005")
    @DisplayName("TC-UPDATE-005: Partially updating firstname and lastname leaves other fields unchanged")
    void partialUpdateChangesOnlyGivenFields() {
        BookingApi api = new BookingApi(requestSpec);
        BookingPayload original = BookingPayload.valid();
        int bookingId = api.createBooking(original.build());
        String token = api.createToken();

        String newLastname = BookingPayload.uniqueValue("Updated");
        String partialBody = """
                {
                    "firstname": "James",
                    "lastname": "%s"
                }
                """.formatted(newLastname);

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .cookie("token", token)
                .body(partialBody)
                .when()
                .patch("/booking/" + bookingId);

        response.then()
                .log().all()
                .statusCode(200)
                .body("firstname", equalTo("James"))
                .body("lastname", equalTo(newLastname))
                .body("totalprice", equalTo(original.currentTotalprice()))
                .body("depositpaid", equalTo(original.currentDepositpaid()))
                .body("bookingdates.checkin", equalTo(original.currentCheckin()))
                .body("bookingdates.checkout", equalTo(original.currentCheckout()))
                .body("additionalneeds", equalTo(original.currentAdditionalneeds()));
    }

    @Test
    @Tag("TC-UPDATE-006")
    @DisplayName("TC-UPDATE-006: Partial update without auth is rejected and the booking is unchanged")
    void partialUpdateWithoutAuthIsRejectedAndUnchanged() {
        BookingApi api = new BookingApi(requestSpec);
        BookingPayload original = BookingPayload.valid();
        int bookingId = api.createBooking(original.build());

        String attemptedBody = """
                {
                    "firstname": "Intruder"
                }
                """;

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .body(attemptedBody)
                .when()
                .patch("/booking/" + bookingId);

        response.then().log().all();
        assertTrue(response.statusCode() == 401 || response.statusCode() == 403,
                "Expected 401 or 403 but got " + response.statusCode());

        Response verify = given()
                .log().all()
                .spec(requestSpec)
                .when()
                .get("/booking/" + bookingId);

        verify.then()
                .log().all()
                .statusCode(200)
                .body("firstname", equalTo(original.currentFirstname()));
    }

    @Test
    @Tag("TC-UPDATE-007")
    @DisplayName("TC-UPDATE-007: Updating a booking with an invalid token is rejected and the booking is unchanged")
    void updateBookingWithInvalidTokenIsRejectedAndUnchanged() {
        BookingApi api = new BookingApi(requestSpec);
        BookingPayload original = BookingPayload.valid();
        int bookingId = api.createBooking(original.build());

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .cookie("token", "invalid")
                .body(BookingPayload.valid().firstname("Intruder").build())
                .when()
                .put("/booking/" + bookingId);

        response.then().log().all();
        assertTrue(response.statusCode() == 401 || response.statusCode() == 403,
                "Expected 401 or 403 but got " + response.statusCode());

        Response verify = given()
                .log().all()
                .spec(requestSpec)
                .when()
                .get("/booking/" + bookingId);

        verify.then()
                .log().all()
                .statusCode(200)
                .body("firstname", equalTo(original.currentFirstname()));
    }

    @Test
    @Tag("TC-UPDATE-008")
    @DisplayName("TC-UPDATE-008: Full update with lastname missing from the body is rejected and the booking is unchanged")
    void fullUpdateWithoutLastnameIsRejectedAndUnchanged() {
        BookingApi api = new BookingApi(requestSpec);
        BookingPayload original = BookingPayload.valid();
        int bookingId = api.createBooking(original.build());
        String token = api.createToken();

        BookingPayload incomplete = BookingPayload.valid().withoutLastname();

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .cookie("token", token)
                .body(incomplete.build())
                .when()
                .put("/booking/" + bookingId);

        response.then().log().all().statusCode(400);

        Response verify = given()
                .log().all()
                .spec(requestSpec)
                .when()
                .get("/booking/" + bookingId);

        verify.then()
                .log().all()
                .statusCode(200)
                .body("lastname", equalTo(original.currentLastname()));
    }
}
