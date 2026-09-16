package restfulbooker.booking;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import restfulbooker.base.BaseTest;
import restfulbooker.support.BookingPayload;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
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
    @DisplayName("TC-CREATE-002: Creating a booking with an XML payload returns an XML body with bookingid and the booking's fields")
    void createBookingWithXmlPayloadReturnsXmlBody() {
        BookingPayload payload = BookingPayload.valid();

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .contentType("text/xml")
                .accept("application/xml")
                .body(payload.buildXml())
                .when()
                .post("/booking");

        response.then().log().all().statusCode(200);

        assertTrue(response.getContentType().contains("xml"), "Expected an XML content type");
        String body = response.getBody().asString();
        assertTrue(body.contains(payload.currentLastname()), "XML body should contain the lastname");
        assertTrue(body.contains(payload.currentAdditionalneeds()), "XML body should contain the additionalneeds");
    }

    @Test
    @Tag("TC-CREATE-003")
    @DisplayName("TC-CREATE-003: Creating a booking with totalprice 0 stores it as 0")
    void createBookingWithZeroTotalpriceStoresZero() {
        BookingPayload payload = BookingPayload.valid().totalprice(0);

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .body(payload.build())
                .when()
                .post("/booking");

        response.then().log().all().statusCode(200);
        assertEquals(0, response.jsonPath().getInt("booking.totalprice"));
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
    @DisplayName("TC-CREATE-005: Creating a booking with totalprice -1 is rejected")
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
    @Tag("TC-CREATE-006")
    @DisplayName("TC-CREATE-006: Creating a booking with a decimal totalprice stores it exactly")
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
    @Tag("TC-CREATE-007")
    @DisplayName("TC-CREATE-007: Creating a booking with checkout before checkin is rejected")
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
    @Tag("TC-CREATE-008")
    @DisplayName("TC-CREATE-008: Creating a booking with a wrong-format checkin date is rejected and nothing is stored")
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
