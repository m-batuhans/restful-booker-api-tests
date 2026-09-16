package restfulbooker.get;

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

class GetBookingTest extends BaseTest {

    @Test
    @Tag("TC-GET-001")
    @DisplayName("TC-GET-001: Retrieved booking matches the values sent at creation")
    void retrievedBookingMatchesCreatedValues() {
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
                .body("totalprice", equalTo(payload.currentTotalprice()))
                .body("depositpaid", equalTo(payload.currentDepositpaid()))
                .body("bookingdates.checkin", equalTo(payload.currentCheckin()))
                .body("bookingdates.checkout", equalTo(payload.currentCheckout()))
                .body("additionalneeds", equalTo(payload.currentAdditionalneeds()));
    }

    @Test
    @Tag("TC-GET-002")
    @DisplayName("TC-GET-002: Retrieving a booking with Accept: application/xml returns an XML body with the booking's fields")
    void retrievedBookingAsXmlContainsFields() {
        BookingApi api = new BookingApi(requestSpec);
        BookingPayload payload = BookingPayload.valid();
        int bookingId = api.createBooking(payload.build());

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .accept("application/xml")
                .when()
                .get("/booking/" + bookingId);

        response.then().log().all().statusCode(200);

        assertTrue(response.getContentType().contains("xml"), "Expected an XML content type");
        String body = response.getBody().asString();
        assertTrue(body.contains(payload.currentLastname()), "XML body should contain the lastname");
        assertTrue(body.contains(String.valueOf(payload.currentTotalprice())), "XML body should contain the totalprice");
        assertTrue(body.contains(payload.currentCheckin()), "XML body should contain the checkin date");
        assertTrue(body.contains(payload.currentCheckout()), "XML body should contain the checkout date");
        assertTrue(body.contains(payload.currentAdditionalneeds()), "XML body should contain the additionalneeds");
    }

    @Test
    @Tag("TC-GET-003")
    @DisplayName("TC-GET-003: Retrieving a non-existent booking returns 404")
    void retrievingNonExistentBookingReturns404() {
        BookingApi api = new BookingApi(requestSpec);
        int nonExistentId = api.guaranteedNonExistentId();

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .when()
                .get("/booking/" + nonExistentId);

        response.then().log().all().statusCode(404);
    }
}
