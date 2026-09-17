package restfulbooker.get;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import restfulbooker.base.BaseTest;
import restfulbooker.config.ConfigLoader;
import restfulbooker.support.BookingApi;
import restfulbooker.support.BookingPayload;
import restfulbooker.support.HttpCalls;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetBookingTest extends BaseTest {

    @Test
    @Tag("TC-GET-003")
    @DisplayName("TC-GET-003: Retrieving a booking with Accept: application/json returns the values sent at creation")
    void retrievedBookingAsJsonMatchesCreatedValues() {
        BookingApi api = new BookingApi(requestSpec);
        BookingPayload payload = BookingPayload.valid();
        int bookingId = api.createBooking(payload.build());

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .accept("application/json")
                .when()
                .get("/booking/" + bookingId);

        response.then()
                .log().all()
                .statusCode(200)
                .body("firstname", equalTo(payload.currentFirstname()))
                .body("lastname", equalTo(payload.currentLastname()))
                .body("totalprice", equalTo(payload.currentTotalprice()))
                .body("depositpaid", equalTo(payload.currentDepositpaid()))
                .body("bookingdates.checkin", equalTo(payload.currentCheckin()))
                .body("bookingdates.checkout", equalTo(payload.currentCheckout()))
                .body("additionalneeds", equalTo(payload.currentAdditionalneeds()));
    }

    @Test
    @Tag("TC-GET-004")
    @DisplayName("TC-GET-004: Retrieving a booking with Accept: application/xml returns an XML <booking> body matching the created values")
    void retrievedBookingAsXmlMatchesCreatedValues() {
        BookingApi api = new BookingApi(requestSpec);
        BookingPayload payload = BookingPayload.valid();
        int bookingId = api.createBooking(payload.build());

        // The documented GetBooking example sends only an Accept header, no Content-Type. Bypass
        // requestSpec (which would add Content-Type: application/json) to match it exactly.
        Response response = given()
                .log().all()
                .baseUri(ConfigLoader.baseUrl())
                .accept("application/xml")
                .when()
                .get("/booking/" + bookingId);

        // XmlPath resolves body() paths starting from the document root, whose tag is <booking>
        // here - so each field must be prefixed with "booking." (unlike JsonPath, where the
        // object itself has no name). Without the prefix, "firstname" does not match anything
        // and XmlPath falls back to describing the whole root node.
        response.then()
                .log().all()
                .statusCode(200)
                .body("booking.firstname", equalTo(payload.currentFirstname()))
                .body("booking.lastname", equalTo(payload.currentLastname()))
                .body("booking.totalprice", equalTo(String.valueOf(payload.currentTotalprice())))
                .body("booking.depositpaid", equalTo(String.valueOf(payload.currentDepositpaid())))
                .body("booking.bookingdates.checkin", equalTo(payload.currentCheckin()))
                .body("booking.bookingdates.checkout", equalTo(payload.currentCheckout()))
                .body("booking.additionalneeds", equalTo(payload.currentAdditionalneeds()));

        assertTrue(response.getContentType().contains("xml"), "Expected an XML content type");
        assertTrue(response.getBody().asString().trim().startsWith("<booking>"), "Expected <booking> as the root element");
    }

    @Test
    @Tag("TC-GET-005")
    @DisplayName("TC-GET-005: Retrieving a non-existent booking returns 404")
    void retrievingNonExistentBookingReturns404() {
        BookingApi api = new BookingApi(requestSpec);
        int nonExistentId = api.guaranteedNonExistentId();

        int statusCode = HttpCalls.statusCode(() -> given()
                .log().all()
                .spec(requestSpec)
                .when()
                .get("/booking/" + nonExistentId));

        assertEquals(404, statusCode);
    }

    @Test
    @Tag("TC-GET-006")
    @DisplayName("TC-GET-006: Retrieving a booking without an Accept header defaults to JSON")
    void retrievingBookingWithoutAcceptHeaderDefaultsToJson() {
        BookingApi api = new BookingApi(requestSpec);
        BookingPayload payload = BookingPayload.valid();
        int bookingId = api.createBooking(payload.build());

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .when()
                .get("/booking/" + bookingId);

        response.then()
                .log().all()
                .statusCode(200)
                .body("firstname", equalTo(payload.currentFirstname()))
                .body("lastname", equalTo(payload.currentLastname()))
                .body("additionalneeds", equalTo(payload.currentAdditionalneeds()));
    }
}
