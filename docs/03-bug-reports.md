# Bug reports

Source: `mvn test` run on 2026-09-17 (39 tests, 14 failures, 0 errors) and manual exploration for BUG-06. Every request/response below is copied verbatim from that run's logs or from a manual `curl` probe against the same public instance.

---

## BUG-01 — Missing firstname crashes the server (500)

**Severity:** High
**Affected case:** TC-CREATE-004
**Issue:** [#1](https://github.com/m-batuhans/restful-booker-api-tests/issues/1)

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
**Issue:** [#2](https://github.com/m-batuhans/restful-booker-api-tests/issues/2)

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
**Issue:** [#3](https://github.com/m-batuhans/restful-booker-api-tests/issues/3)

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
**Issue:** [#4](https://github.com/m-batuhans/restful-booker-api-tests/issues/4)

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
**Issue:** [#5](https://github.com/m-batuhans/restful-booker-api-tests/issues/5)

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
**Issue:** [#7](https://github.com/m-batuhans/restful-booker-api-tests/issues/7)

An earlier version of this exploration (booking 4592) could not be trusted: the shared public instance resets itself periodically, and the booking no longer existed by the time part of the exploration queried it, so a "not found" result could not be told apart from "the filter excluded it". This run re-verified the booking still existed immediately after the last query below.

Booking 4164: `checkin=2026-04-10`, `checkout=2026-04-15`. Verified to still exist after the last query.

| Query | Booking in result | Documentation says |
|---|---|---|
| `GET /booking?checkin=2026-04-09` | yes | yes |
| `GET /booking?checkin=2026-04-10` | no | yes (equal) |
| `GET /booking?checkin=2026-04-11` | no | no |
| `GET /booking?checkout=2026-04-14` | no | yes |
| `GET /booking?checkout=2026-04-15` | yes | yes (equal) |
| `GET /booking?checkout=2026-04-16` | yes | no |

**Expected result (source: documentation — "checkin"/"checkout" query params "return bookings that have a [...] date greater than or equal to the set date"):** each row's "Documentation says" column, derived from the booking's own checkin/checkout compared with "greater than or equal to" the filter date.

**Actual result — two separate defects in the same bug:**
1. The `checkin` filter excludes the equal date: it behaves as a strict "greater than", not "greater than or equal to".
2. The `checkout` filter compares in the opposite direction: it returns bookings whose checkout date is less than or equal to the given date, not greater than or equal to it.

TC-FILTER-002 (checkin and checkout both equal to the booking's own dates) fails because of defect 1. TC-FILTER-006 (checkout one day after the booking's checkout, expecting exclusion) fails because of defect 2.

**Note:** on this shared instance, a filter result cannot be interpreted at face value. The booking used to probe it must be independently verified to still exist (e.g. with a `GET /booking/<id>`) at the time of each query, since the public data resets periodically and a "not found" result can mean either "the filter excluded it" or "the booking is gone".

---

## BUG-07 — No input validation (negative price, checkout before checkin, invalid filter date format, login without username all return 200)

**Severity:** Low
**Affected cases:** TC-CREATE-006, TC-CREATE-008, TC-FILTER-003, TC-AUTH-003
**Issue:** [#6](https://github.com/m-batuhans/restful-booker-api-tests/issues/6)

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
