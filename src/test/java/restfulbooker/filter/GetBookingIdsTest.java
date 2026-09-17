package restfulbooker.filter;

import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import restfulbooker.base.BaseTest;
import restfulbooker.support.BookingApi;
import restfulbooker.support.BookingPayload;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetBookingIdsTest extends BaseTest {

    @Test
    @Tag("TC-FILTER-001")
    @DisplayName("TC-FILTER-001: Filtering by a unique firstname and lastname returns exactly the created booking")
    void filterByUniqueFirstnameAndLastnameReturnsExactlyOneBooking() {
        BookingApi api = new BookingApi(requestSpec);
        String uniqueFirstname = BookingPayload.uniqueValue("Filter");
        BookingPayload payload = BookingPayload.valid().firstname(uniqueFirstname);
        int bookingId = api.createBooking(payload.build());

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .queryParam("firstname", uniqueFirstname)
                .queryParam("lastname", payload.currentLastname())
                .when()
                .get("/booking");

        response.then().log().all().statusCode(200);

        List<Integer> ids = response.jsonPath().getList("bookingid", Integer.class);
        assertEquals(List.of(bookingId), ids);
    }

    @Test
    @Tag("TC-FILTER-002")
    @DisplayName("TC-FILTER-002: Filtering by checkin and checkout dates equal to the booking's own dates includes it")
    @Disabled("Known bug: BUG-06, see docs/03-bug-reports.md")
    void filterByCheckinAndCheckoutEqualToBookingDatesIncludesIt() {
        BookingApi api = new BookingApi(requestSpec);
        BookingPayload payload = BookingPayload.valid().checkin("2026-03-10").checkout("2026-03-15");
        int bookingId = api.createBooking(payload.build());

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .queryParam("checkin", "2026-03-10")
                .queryParam("checkout", "2026-03-15")
                .when()
                .get("/booking");

        response.then().log().all().statusCode(200);

        List<Integer> ids = response.jsonPath().getList("bookingid", Integer.class);
        assertTrue(ids.contains(bookingId));
    }

    @Test
    @Tag("TC-FILTER-003")
    @DisplayName("TC-FILTER-003: Filtering with an invalid checkin date format is rejected")
    @Disabled("Known bug: BUG-07, see docs/03-bug-reports.md")
    void filterWithInvalidCheckinFormatIsRejected() {
        Response response = given()
                .log().all()
                .spec(requestSpec)
                .queryParam("checkin", "10-03-2026")
                .when()
                .get("/booking");

        response.then().log().all().statusCode(400);
    }

    @Test
    @Tag("TC-FILTER-004")
    @DisplayName("TC-FILTER-004: Filtering by a unique firstname only returns exactly the created booking")
    void filterByUniqueFirstnameOnlyReturnsExactlyOneBooking() {
        BookingApi api = new BookingApi(requestSpec);
        String uniqueFirstname = BookingPayload.uniqueValue("Filter");
        int bookingId = api.createBooking(BookingPayload.valid().firstname(uniqueFirstname).build());

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .queryParam("firstname", uniqueFirstname)
                .when()
                .get("/booking");

        response.then().log().all().statusCode(200);

        List<Integer> ids = response.jsonPath().getList("bookingid", Integer.class);
        assertEquals(List.of(bookingId), ids);
    }

    @Test
    @Tag("TC-FILTER-005")
    @DisplayName("TC-FILTER-005: Filtering by checkin date one day after the booking's checkin excludes it")
    void filterByCheckinDateAfterBookingCheckinExcludesIt() {
        BookingApi api = new BookingApi(requestSpec);
        BookingPayload payload = BookingPayload.valid().checkin("2026-03-10").checkout("2026-03-15");
        int bookingId = api.createBooking(payload.build());

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .queryParam("checkin", "2026-03-11")
                .when()
                .get("/booking");

        response.then().log().all().statusCode(200);

        List<Integer> ids = response.jsonPath().getList("bookingid", Integer.class);
        assertFalse(ids.contains(bookingId));
    }

    @Test
    @Tag("TC-FILTER-006")
    @DisplayName("TC-FILTER-006: Filtering by checkout date one day after the booking's checkout excludes it")
    @Disabled("Known bug: BUG-06, see docs/03-bug-reports.md")
    void filterByCheckoutDateAfterBookingCheckoutExcludesIt() {
        BookingApi api = new BookingApi(requestSpec);
        BookingPayload payload = BookingPayload.valid().checkin("2026-03-10").checkout("2026-03-15");
        int bookingId = api.createBooking(payload.build());

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .queryParam("checkout", "2026-03-16")
                .when()
                .get("/booking");

        response.then().log().all().statusCode(200);

        List<Integer> ids = response.jsonPath().getList("bookingid", Integer.class);
        assertFalse(ids.contains(bookingId));
    }
}
