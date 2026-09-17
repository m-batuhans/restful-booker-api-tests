# Bug reports

Source: `mvn test` run on 2026-09-17 (39 tests, 14 failures, 0 errors) and manual exploration for BUG-06. Every request/response below is copied verbatim from that run's logs or from a manual `curl` probe against the same public instance.

---

## BUG-01 — Missing firstname crashes the server (500)

**Severity:** High
**Affected case:** TC-CREATE-004

**Steps:**
```
POST https://restful-booker.herokuapp.com/booking
Content-Type: application/json

{
    "lastname": "Brown-e1c3c61f",
    "totalprice": 111,
    "depositpaid": true,
    "bookingdates": {
        "checkin": "2026-01-01",
        "checkout": "2026-01-05"
    },
    "additionalneeds": "Breakfast"
}
```

**Expected result (source: HTTP rule, per docs/02-review.md oracle policy rule 3):** 400 Bad Request — `firstname` is a required field and every documented example sends it.

**Actual result:**
```
HTTP/1.1 500 Internal Server Error
Content-Type: text/plain; charset=utf-8

Internal Server Error
```

---

## BUG-02 — Invalid body date format is accepted and stored as "0NaN-aN-aN"

**Severity:** High
**Affected case:** TC-CREATE-009

**Steps:**
```
POST https://restful-booker.herokuapp.com/booking
Content-Type: application/json

{
    "firstname": "Jim",
    "lastname": "Brown-c8187a0c",
    "totalprice": 111,
    "depositpaid": true,
    "bookingdates": {
        "checkin": "17-09-2026",
        "checkout": "2026-01-05"
    },
    "additionalneeds": "Breakfast"
}
```

**Expected result (source: HTTP rule, per docs/02-review.md oracle policy rule 3):** 400 Bad Request — invalid input format.

**Actual result:**
```
HTTP/1.1 200 OK

{
    "bookingid": 955,
    "booking": {
        "firstname": "Jim",
        "lastname": "Brown-c8187a0c",
        "totalprice": 111,
        "depositpaid": true,
        "bookingdates": {
            "checkin": "0NaN-aN-aN",
            "checkout": "2026-01-05"
        },
        "additionalneeds": "Breakfast"
    }
}
```
Confirmed the corrupted value is what's actually stored (not just echoed) with a follow-up `GET /booking/955`, which returned the same `"checkin": "0NaN-aN-aN"`. The server evidently tries to parse `17-09-2026` as a date, fails, and stores the string form of an invalid JavaScript `Date` object instead of rejecting the request.

---

## BUG-03 — Decimal totalprice is truncated (150.75 stored as 150)

**Severity:** High
**Affected case:** TC-CREATE-007

**Steps:**
```
POST https://restful-booker.herokuapp.com/booking
Content-Type: application/json

{
    "firstname": "Jim",
    "lastname": "Brown-72fce8b7",
    "totalprice": 150.75,
    "depositpaid": true,
    "bookingdates": {
        "checkin": "2026-01-01",
        "checkout": "2026-01-05"
    },
    "additionalneeds": "Breakfast"
}
```

**Expected result (source: documentation — `totalprice` is typed `Number`, which includes decimals):** 200 OK; `totalprice` stored exactly as 150.75.

**Actual result:**
```
HTTP/1.1 200 OK

{
    "bookingid": 913,
    "booking": {
        "firstname": "Jim",
        "lastname": "Brown-72fce8b7",
        "totalprice": 150,
        ...
    }
}
```
The fractional part is silently dropped.

---

## BUG-04 — PUT, PATCH and DELETE return 405 instead of 404 for a non-existent booking (GET returns 404)

**Severity:** Medium
**Affected cases:** TC-UPDATE-006, TC-UPDATE-009, TC-DELETE-004

All three use the same setup: create a booking, delete it (confirmed 201), then act on the same now-nonexistent ID with a valid token.

**Steps — PUT (TC-UPDATE-009):**
```
PUT https://restful-booker.herokuapp.com/booking/1322
Content-Type: application/json
Cookie: token=<token>

{
    "firstname": "Jim",
    "lastname": "Brown-d9e9803f",
    "totalprice": 111,
    "depositpaid": true,
    "bookingdates": {
        "checkin": "2026-01-01",
        "checkout": "2026-01-05"
    },
    "additionalneeds": "Breakfast"
}
```
**Actual:** `405 Method Not Allowed`

**Steps — PATCH (TC-UPDATE-006):**
```
PATCH https://restful-booker.herokuapp.com/booking/1268
Content-Type: application/json
Cookie: token=<token>

{
    "firstname": "James"
}
```
**Actual:** `405 Method Not Allowed`

**Steps — DELETE (TC-DELETE-004):**
```
DELETE https://restful-booker.herokuapp.com/booking/1015
Cookie: token=<token>
```
**Actual:** `405 Method Not Allowed`

**Expected result (source: HTTP rule, per docs/02-review.md oracle policy rule 3, consistent with the confirmed behavior of GET on the same kind of ID — see TC-GET-005, which does return 404):** 404 Not Found.

For comparison, `GET /booking/<same kind of deleted id>` correctly returns 404 (TC-GET-005 passes). Only PUT, PATCH and DELETE return 405, suggesting the route for a non-existent numeric ID is not matched the same way for these three methods.

---

## BUG-05 — XML responses have Content-Type text/html

**Severity:** Medium
**Affected cases:** TC-CREATE-002, TC-GET-004

**Steps — CreateBooking (TC-CREATE-002):**
```
POST https://restful-booker.herokuapp.com/booking
Content-Type: text/xml
Accept: application/xml

<booking>
  <firstname>Jim</firstname>
  <lastname>Brown-6fa5677d</lastname>
  <totalprice>111</totalprice>
  <depositpaid>true</depositpaid>
  <bookingdates>
    <checkin>2026-01-01</checkin>
    <checkout>2026-01-05</checkout>
  </bookingdates>
  <additionalneeds>Breakfast</additionalneeds>
</booking>
```

**Expected result (source: documentation — the Accept header description says "Can be application/json or application/xml"):** Content-Type header says XML (e.g. `application/xml` or `text/xml`).

**Actual result:**
```
HTTP/1.1 200 OK
Content-Type: text/html; charset=utf-8

<created-booking>
  <bookingid>933</bookingid>
  <booking>
    <firstname>Jim</firstname>
    <lastname>Brown-6fa5677d</lastname>
    <totalprice>111</totalprice>
    <depositpaid>true</depositpaid>
    <bookingdates>
      <checkin>2026-01-01</checkin>
      <checkout>2026-01-05</checkout>
    </bookingdates>
    <additionalneeds>Breakfast</additionalneeds>
  </booking>
</created-booking>
```
The body is well-formed XML with the documented structure, but the Content-Type header says `text/html`, not an XML type. The same happens for GetBooking with `Accept: application/xml`.

---

## BUG-06 — Date filters do not follow "greater than or equal"

**Severity:** Medium
**Affected cases:** TC-FILTER-002, TC-FILTER-006

Explored manually (outside the test suite) to isolate which of `checkin` and `checkout` is at fault. Created one booking with `checkin=2026-04-10`, `checkout=2026-04-15` (bookingid 4592), then queried each filter alone:

| Query | Result count | Booking 4592 included? |
|---|---|---|
| `GET /booking?checkin=2026-04-10` (equal to its own checkin) | 40 | **No** — should be included per "greater than or equal" |
| `GET /booking?checkin=2026-04-11` (one day after its checkin) | 40 | No (correctly excluded) |
| `GET /booking?checkout=2026-04-15` (equal to its own checkout) | 3160 | Yes (correctly included) |
| `GET /booking?checkout=2026-04-16` (one day after its checkout) | 3249 | **Yes** — should be excluded per "greater than or equal" |
| `GET /booking?checkin=2026-04-10&checkout=2026-04-15` (both, exact match) | 3 | **No** |

**Expected result (source: documentation — "checkin"/"checkout" query params "return bookings that have a [...] date greater than or equal to the set date"):** the booking's own ID is included when the filter date equals its checkin/checkout, and excluded once the filter date passes it.

**Actual result:** `checkin` excludes the exact-match case (behaves as strict "greater than", not "greater than or equal to"). `checkout` includes a date one day past the booking's own checkout (behaves as if the upper bound isn't enforced, or as "less than or equal" from the other direction). Combining both filters produces yet a third, smaller result set that also excludes the booking — the two filters do not appear to be applied consistently together either.

TC-FILTER-002 (combined checkin+checkout, exact match) fails because of the `checkin` half. TC-FILTER-006 (checkout one day after, expecting exclusion) fails because of the `checkout` half.

---

## BUG-07 — No input validation (negative price, checkout before checkin, invalid filter date format, login without username all return 200)

**Severity:** Low
**Affected cases:** TC-CREATE-006, TC-CREATE-008, TC-FILTER-003, TC-AUTH-003

Four different kinds of invalid input, all accepted with 200 instead of being rejected.

**Negative totalprice (TC-CREATE-006):**
```
POST https://restful-booker.herokuapp.com/booking
{ "firstname": "Jim", "lastname": "Brown-c3c33a90", "totalprice": -1, "depositpaid": true,
  "bookingdates": { "checkin": "2026-01-01", "checkout": "2026-01-05" }, "additionalneeds": "Breakfast" }
```
Actual: `200 OK`, `"totalprice": -1` stored as sent.

**Checkout before checkin (TC-CREATE-008):**
```
POST https://restful-booker.herokuapp.com/booking
{ "firstname": "Jim", "lastname": "Brown-c82a406c", "totalprice": 111, "depositpaid": true,
  "bookingdates": { "checkin": "2026-05-10", "checkout": "2026-05-01" }, "additionalneeds": "Breakfast" }
```
Actual: `200 OK`, dates stored as sent (checkout before checkin).

**Invalid filter date format (TC-FILTER-003):**
```
GET https://restful-booker.herokuapp.com/booking?checkin=10-03-2026
```
Actual: `200 OK` with a JSON array (no error), even though the documented format is `CCYY-MM-DD`.

**Login without username (TC-AUTH-003):**
```
POST https://restful-booker.herokuapp.com/auth
{ "password": "password123" }
```
Actual: `200 OK`, `{"reason": "Bad credentials"}` — no `token`, but not a 400 either.

**Expected result (source: HTTP rule, per docs/02-review.md oracle policy rule 3):** 400 Bad Request for each case.

**Actual result:** all four return 200 OK; none of them validate their input.
