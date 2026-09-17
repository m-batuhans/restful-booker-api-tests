package restfulbooker.booking;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import restfulbooker.base.BaseTest;
import restfulbooker.support.BookingPayload;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateBookingTest extends BaseTest {

    @Test
    @Tag("TC-CREATE-001")
    @DisplayName("TC-CREATE-001: Create booking returns 200 with bookingid and a booking object matching the request")
    void createBookingReturnsMatchingBooking() {
        String uniqueLastname = "Brown-" + UUID.randomUUID().toString().substring(0, 8);

        String requestBody = """
                {
                    "firstname": "Jim",
                    "lastname": "%s",
                    "totalprice": 111,
                    "depositpaid": true,
                    "bookingdates": {
                        "checkin": "2026-01-01",
                        "checkout": "2026-01-05"
                    },
                    "additionalneeds": "Breakfast"
                }
                """.formatted(uniqueLastname);

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .body(requestBody)
                .when()
                .post("/booking");

        response.then()
                .log().all()
                .statusCode(200)
                .body("bookingid", instanceOf(Number.class))
                .body("booking.firstname", equalTo("Jim"))
                .body("booking.lastname", equalTo(uniqueLastname))
                .body("booking.totalprice", equalTo(111))
                .body("booking.depositpaid", equalTo(true))
                .body("booking.bookingdates.checkin", equalTo("2026-01-01"))
                .body("booking.bookingdates.checkout", equalTo("2026-01-05"))
                .body("booking.additionalneeds", equalTo("Breakfast"));
    }

    @Test
    @Tag("TC-CREATE-002")
    @DisplayName("TC-CREATE-002: Creating a booking with an XML payload returns a <created-booking> body matching the request")
    void createBookingWithXmlPayloadReturnsMatchingCreatedBooking() {
        BookingPayload payload = BookingPayload.valid();

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .contentType("text/xml")
                .body(payload.buildXml())
                .when()
                .post("/booking");

        response.then()
                .log().all()
                .statusCode(200)
                .body("bookingid", not(emptyOrNullString()))
                .body("booking.firstname", equalTo(payload.currentFirstname()))
                .body("booking.lastname", equalTo(payload.currentLastname()))
                .body("booking.totalprice", equalTo(String.valueOf(payload.currentTotalprice())))
                .body("booking.depositpaid", equalTo(String.valueOf(payload.currentDepositpaid())))
                .body("booking.bookingdates.checkin", equalTo(payload.currentCheckin()))
                .body("booking.bookingdates.checkout", equalTo(payload.currentCheckout()))
                .body("booking.additionalneeds", equalTo(payload.currentAdditionalneeds()));

        assertTrue(response.getContentType().contains("xml"), "Expected an XML content type");
        assertTrue(response.getBody().asString().contains("<created-booking>"), "Expected <created-booking> as the root element");
    }

    @Test
    @Tag("TC-CREATE-003")
    @DisplayName("TC-CREATE-003: Creating a booking with a URL-encoded payload returns a JSON body matching the request")
    void createBookingWithUrlEncodedPayloadReturnsMatchingJsonBooking() {
        BookingPayload payload = BookingPayload.valid();

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .contentType("application/x-www-form-urlencoded")
                .body(payload.buildFormEncoded())
                .when()
                .post("/booking");

        response.then()
                .log().all()
                .statusCode(200)
                .body("booking.firstname", equalTo(payload.currentFirstname()))
                .body("booking.lastname", equalTo(payload.currentLastname()))
                .body("booking.totalprice", equalTo(payload.currentTotalprice()))
                .body("booking.depositpaid", equalTo(payload.currentDepositpaid()))
                .body("booking.bookingdates.checkin", equalTo(payload.currentCheckin()))
                .body("booking.bookingdates.checkout", equalTo(payload.currentCheckout()))
                .body("booking.additionalneeds", equalTo(payload.currentAdditionalneeds()));

        assertTrue(response.getContentType().contains("json"), "Expected a JSON content type");
    }

    @Test
    @Tag("TC-CREATE-004")
    @DisplayName("TC-CREATE-004: Creating a booking without firstname is rejected and nothing is stored")
    void createBookingWithoutFirstnameIsRejectedAndNotStored() {
        BookingPayload payload = BookingPayload.valid().withoutFirstname();

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .body(payload.build())
                .when()
                .post("/booking");

        response.then().log().all().statusCode(400);

        Response verify = given()
                .log().all()
                .spec(requestSpec)
                .queryParam("lastname", payload.currentLastname())
                .when()
                .get("/booking");

        verify.then().log().all().statusCode(200);
        assertTrue(verify.jsonPath().getList("bookingid").isEmpty());
    }

    @Test
    @Tag("TC-CREATE-005")
    @DisplayName("TC-CREATE-005: Creating a booking with an empty additionalneeds stores it as an empty string")
    void createBookingWithEmptyAdditionalneedsStoresEmptyString() {
        BookingPayload payload = BookingPayload.valid().additionalneeds("");

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .body(payload.build())
                .when()
                .post("/booking");

        response.then()
                .log().all()
                .statusCode(200)
                .body("booking.additionalneeds", equalTo(""));
    }

    @Test
    @Tag("TC-CREATE-006")
    @DisplayName("TC-CREATE-006: Creating a booking with totalprice -1 is rejected")
    void createBookingWithNegativeTotalpriceIsRejected() {
        BookingPayload payload = BookingPayload.valid().totalprice(-1);

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .body(payload.build())
                .when()
                .post("/booking");

        response.then().log().all().statusCode(400);
    }

    @Test
    @Tag("TC-CREATE-007")
    @DisplayName("TC-CREATE-007: Creating a booking with a decimal totalprice stores it exactly")
    void createBookingWithDecimalTotalpriceStoresExactValue() {
        BookingPayload payload = BookingPayload.valid().totalprice(150.75);

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .body(payload.build())
                .when()
                .post("/booking");

        response.then().log().all().statusCode(200);
        assertEquals(150.75, response.jsonPath().getDouble("booking.totalprice"), 0.0001);
    }

    @Test
    @Tag("TC-CREATE-008")
    @DisplayName("TC-CREATE-008: Creating a booking with checkout before checkin is rejected")
    void createBookingWithCheckoutBeforeCheckinIsRejected() {
        BookingPayload payload = BookingPayload.valid().checkin("2026-05-10").checkout("2026-05-01");

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .body(payload.build())
                .when()
                .post("/booking");

        response.then().log().all().statusCode(400);
    }

    @Test
    @Tag("TC-CREATE-009")
    @DisplayName("TC-CREATE-009: Creating a booking with a wrong-format checkin date is rejected and nothing is stored")
    void createBookingWithWrongFormatCheckinIsRejectedAndNotStored() {
        BookingPayload payload = BookingPayload.valid().checkin("17-09-2026");

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .body(payload.build())
                .when()
                .post("/booking");

        response.then().log().all().statusCode(400);

        Response verify = given()
                .log().all()
                .spec(requestSpec)
                .queryParam("lastname", payload.currentLastname())
                .when()
                .get("/booking");

        verify.then().log().all().statusCode(200);
        assertTrue(verify.jsonPath().getList("bookingid").isEmpty());
    }
}
