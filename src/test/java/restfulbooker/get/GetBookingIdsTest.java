package restfulbooker.get;

import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import restfulbooker.base.BaseTest;
import restfulbooker.support.BookingApi;
import restfulbooker.support.BookingPayload;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetBookingIdsTest extends BaseTest {

    @Test
    @Tag("TC-GET-002")
    @DisplayName("TC-GET-002: All booking IDs include a newly created booking")
    void allBookingIdsIncludeCreatedBooking() {
        BookingApi api = new BookingApi(requestSpec);
        int bookingId = api.createBooking(BookingPayload.valid().build());

        Response response = given()
                .log().all()
                .spec(requestSpec)
                .when()
                .get("/booking");

        response.then().log().all().statusCode(200);

        List<Integer> ids = response.jsonPath().getList("bookingid", Integer.class);
        assertTrue(ids.contains(bookingId));
    }
}
