package restfulbooker.update;

import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import restfulbooker.base.BaseTest;
import restfulbooker.support.BookingApi;
import restfulbooker.support.BookingPayload;
import restfulbooker.support.HttpCalls;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

        int statusCode = HttpCalls.statusCode(() -> given()
                .log().all()
                .spec(requestSpec)
                .body(attempted.build())
                .when()
                .put("/booking/" + bookingId));

        assertTrue(statusCode == 401 || statusCode == 403, "Expected 401 or 403 but got " + statusCode);

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
    @DisplayName("TC-UPDATE-004: Full update with totalprice missing from the body is rejected and the booking is unchanged")
    void fullUpdateWithoutTotalpriceIsRejectedAndUnchanged() {
        BookingApi api = new BookingApi(requestSpec);
        BookingPayload original = BookingPayload.valid();
        int bookingId = api.createBooking(original.build());
        String token = api.createToken();

        BookingPayload incomplete = BookingPayload.valid().withoutTotalprice();

        int statusCode = HttpCalls.statusCode(() -> given()
                .log().all()
                .spec(requestSpec)
                .cookie("token", token)
                .body(incomplete.build())
                .when()
                .put("/booking/" + bookingId));

        assertEquals(400, statusCode);

        Response verify = given()
                .log().all()
                .spec(requestSpec)
                .when()
                .get("/booking/" + bookingId);

        verify.then()
                .log().all()
                .statusCode(200)
                .body("totalprice", equalTo(original.currentTotalprice()));
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
    @DisplayName("TC-UPDATE-006: Partially updating a non-existent booking returns 404")
    @Disabled("Known bug: BUG-04, see docs/03-bug-reports.md")
    void partialUpdateOfNonExistentBookingReturns404() {
        BookingApi api = new BookingApi(requestSpec);
        int nonExistentId = api.guaranteedNonExistentId();
        String token = api.createToken();

        String partialBody = """
                {
                    "firstname": "James"
                }
                """;

        int statusCode = HttpCalls.statusCode(() -> given()
                .log().all()
                .spec(requestSpec)
                .cookie("token", token)
                .body(partialBody)
                .when()
                .patch("/booking/" + nonExistentId));

        assertEquals(404, statusCode);
    }

    @Test
    @Tag("TC-UPDATE-007")
    @DisplayName("TC-UPDATE-007: Updating a booking with an invalid token is rejected and the booking is unchanged")
    void updateBookingWithInvalidTokenIsRejectedAndUnchanged() {
        BookingApi api = new BookingApi(requestSpec);
        BookingPayload original = BookingPayload.valid();
        int bookingId = api.createBooking(original.build());

        int statusCode = HttpCalls.statusCode(() -> given()
                .log().all()
                .spec(requestSpec)
                .cookie("token", "invalid")
                .body(BookingPayload.valid().firstname("Intruder").build())
                .when()
                .put("/booking/" + bookingId));

        assertTrue(statusCode == 401 || statusCode == 403, "Expected 401 or 403 but got " + statusCode);

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
    @DisplayName("TC-UPDATE-008: Partial update without auth is rejected and the booking is unchanged")
    void partialUpdateWithoutAuthIsRejectedAndUnchanged() {
        BookingApi api = new BookingApi(requestSpec);
        BookingPayload original = BookingPayload.valid();
        int bookingId = api.createBooking(original.build());

        String attemptedBody = """
                {
                    "firstname": "Intruder"
                }
                """;

        int statusCode = HttpCalls.statusCode(() -> given()
                .log().all()
                .spec(requestSpec)
                .body(attemptedBody)
                .when()
                .patch("/booking/" + bookingId));

        assertTrue(statusCode == 401 || statusCode == 403, "Expected 401 or 403 but got " + statusCode);

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
    @Tag("TC-UPDATE-009")
    @DisplayName("TC-UPDATE-009: Updating a non-existent booking returns 404")
    @Disabled("Known bug: BUG-04, see docs/03-bug-reports.md")
    void updateNonExistentBookingReturns404() {
        BookingApi api = new BookingApi(requestSpec);
        int nonExistentId = api.guaranteedNonExistentId();
        String token = api.createToken();

        int statusCode = HttpCalls.statusCode(() -> given()
                .log().all()
                .spec(requestSpec)
                .cookie("token", token)
                .body(BookingPayload.valid().build())
                .when()
                .put("/booking/" + nonExistentId));

        assertEquals(404, statusCode);
    }
}
