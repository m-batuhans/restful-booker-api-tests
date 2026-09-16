package restfulbooker.support;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Builder for a booking request body. Defaults match the documented CreateBooking example,
 * with a unique lastname per instance. Fields can be overridden, and firstname/lastname can be
 * dropped entirely to build a payload for a missing-required-field test.
 */
public final class BookingPayload {

    private String firstname = "Jim";
    private String lastname = uniqueValue("Brown");
    private Object totalprice = 111;
    private boolean depositpaid = true;
    private String checkin = "2026-01-01";
    private String checkout = "2026-01-05";
    private String additionalneeds = "Breakfast";

    private boolean includeFirstname = true;
    private boolean includeLastname = true;
    private boolean includeTotalprice = true;
    private boolean includeDepositpaid = true;
    private boolean includeCheckin = true;
    private boolean includeCheckout = true;
    private boolean includeAdditionalneeds = true;

    private BookingPayload() {
    }

    public static BookingPayload valid() {
        return new BookingPayload();
    }

    public static String uniqueValue(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public BookingPayload firstname(String value) {
        this.firstname = value;
        return this;
    }

    public BookingPayload lastname(String value) {
        this.lastname = value;
        return this;
    }

    public BookingPayload totalprice(Object value) {
        this.totalprice = value;
        return this;
    }

    public BookingPayload depositpaid(boolean value) {
        this.depositpaid = value;
        return this;
    }

    public BookingPayload checkin(String value) {
        this.checkin = value;
        return this;
    }

    public BookingPayload checkout(String value) {
        this.checkout = value;
        return this;
    }

    public BookingPayload additionalneeds(String value) {
        this.additionalneeds = value;
        return this;
    }

    public BookingPayload withoutFirstname() {
        this.includeFirstname = false;
        return this;
    }

    public BookingPayload withoutLastname() {
        this.includeLastname = false;
        return this;
    }

    public String currentFirstname() {
        return firstname;
    }

    public String currentLastname() {
        return lastname;
    }

    public Object currentTotalprice() {
        return totalprice;
    }

    public boolean currentDepositpaid() {
        return depositpaid;
    }

    public String currentCheckin() {
        return checkin;
    }

    public String currentCheckout() {
        return checkout;
    }

    public String currentAdditionalneeds() {
        return additionalneeds;
    }

    public String build() {
        Map<String, Object> body = new LinkedHashMap<>();
        if (includeFirstname) {
            body.put("firstname", firstname);
        }
        if (includeLastname) {
            body.put("lastname", lastname);
        }
        if (includeTotalprice) {
            body.put("totalprice", totalprice);
        }
        if (includeDepositpaid) {
            body.put("depositpaid", depositpaid);
        }
        Map<String, Object> dates = new LinkedHashMap<>();
        if (includeCheckin) {
            dates.put("checkin", checkin);
        }
        if (includeCheckout) {
            dates.put("checkout", checkout);
        }
        body.put("bookingdates", dates);
        if (includeAdditionalneeds) {
            body.put("additionalneeds", additionalneeds);
        }
        return Json.toJson(body);
    }

    public String buildXml() {
        StringBuilder xml = new StringBuilder("<booking>");
        if (includeFirstname) {
            xml.append("<firstname>").append(firstname).append("</firstname>");
        }
        if (includeLastname) {
            xml.append("<lastname>").append(lastname).append("</lastname>");
        }
        if (includeTotalprice) {
            xml.append("<totalprice>").append(totalprice).append("</totalprice>");
        }
        if (includeDepositpaid) {
            xml.append("<depositpaid>").append(depositpaid).append("</depositpaid>");
        }
        xml.append("<bookingdates>");
        if (includeCheckin) {
            xml.append("<checkin>").append(checkin).append("</checkin>");
        }
        if (includeCheckout) {
            xml.append("<checkout>").append(checkout).append("</checkout>");
        }
        xml.append("</bookingdates>");
        if (includeAdditionalneeds) {
            xml.append("<additionalneeds>").append(additionalneeds).append("</additionalneeds>");
        }
        xml.append("</booking>");
        return xml.toString();
    }
}
