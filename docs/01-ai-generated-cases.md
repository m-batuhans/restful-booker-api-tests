<!--
Model: gemini-3.6-flash
Generated: 2026-09-17 01:45:37
Prompt file: prompts\generate-cases.md
-->

### Test Cases

| ID | Endpoint | Scenario | Type (Positive/Negative/Boundary) | Preconditions | Expected result |
|---|---|---|---|---|---|
| TC-AUTH-001 | POST `/auth` | Create auth token with valid credentials (`admin` / `password123`) | Positive | None | HTTP 200 OK; Response body contains JSON object with `token` (String). |
| TC-AUTH-002 | POST `/auth` | Create auth token with invalid password | Negative | None | Undocumented behavior (documentation does not specify status code or error payload for authentication failure). |
| TC-AUTH-003 | POST `/auth` | Omit required field `username` from body | Negative | None | Undocumented behavior (documentation marks field as non-optional but specifies no error response). |
| TC-AUTH-004 | POST `/auth` | Omit required `Content-Type` header | Negative | None | Undocumented behavior (documentation marks header as non-optional but specifies no error response). |
| TC-GET-001 | GET `/ping` | Send health check request | Positive | Server is running | Response status code 201 Created (per success example) or 200 OK (per section header). Body contains default response. |
| TC-GET-002 | GET `/booking` | Retrieve all booking IDs without query parameters | Positive | Bookings exist in system | HTTP 200 OK; Response contains array of objects with field `bookingid` (Number). |
| TC-GET-003 | GET `/booking/:id` | Retrieve booking by valid ID with `Accept: application/json` | Positive | Booking with ID exists | HTTP 200 OK; JSON payload returned with `firstname`, `lastname`, `totalprice`, `depositpaid`, `bookingdates` (`checkin`, `checkout`), and `additionalneeds`. |
| TC-GET-004 | GET `/booking/:id` | Retrieve booking by valid ID with `Accept: application/xml` | Positive | Booking with ID exists | HTTP 200 OK; XML payload `<booking>` returned with all booking fields. |
| TC-GET-005 | GET `/booking/:id` | Retrieve booking using non-existent ID | Negative | ID does not exist | Undocumented behavior (documentation specifies no status code or payload for non-existent IDs). |
| TC-GET-006 | GET `/booking/:id` | Omit non-optional `Accept` header | Negative | Booking with ID exists | Undocumented behavior (documentation marks `Accept` header as mandatory but specifies no error handling). |
| TC-FILTER-001 | GET `/booking` | Filter booking IDs by `firstname` and `lastname` | Positive | Matching booking exists | HTTP 200 OK; Returns array of matching `bookingid` objects. |
| TC-FILTER-002 | GET `/booking` | Filter booking IDs by `checkin` and `checkout` dates in `CCYY-MM-DD` format | Positive | Matching booking exists | HTTP 200 OK; Returns array of `bookingid` objects matching dates greater than or equal to params. |
| TC-FILTER-003 | GET `/booking` | Filter using invalid date format for `checkin` (e.g., `DD-MM-YYYY`) | Boundary / Negative | None | Undocumented behavior (documentation requires `CCYY-MM-DD` format but specifies no failure response). |
| TC-FILTER-004 | GET `/booking` | Filter using query parameter `firstname` only | Positive | Matching booking exists | HTTP 200 OK; Returns array of matching `bookingid` objects. |
| TC-CREATE-001 | POST `/booking` | Create a booking with valid JSON payload (`Content-Type: application/json`, `Accept: application/json`) | Positive | None | HTTP 200 OK; JSON response containing generated `bookingid` (Number) and full `booking` object. |
| TC-CREATE-002 | POST `/booking` | Create a booking with valid XML payload (`Content-Type: text/xml`, `Accept: application/xml`) | Positive | None | HTTP 200 OK; XML response wrapped in `<created-booking>` containing `<bookingid>` and `<booking>`. |
| TC-CREATE-003 | POST `/booking` | Create a booking with `Content-Type: application/x-www-form-urlencoded` | Positive | None | HTTP 200 OK; Form URL-encoded string response containing `bookingid` and `booking` parameters. |
| TC-CREATE-004 | POST `/booking` | Omit required field `firstname` in request body | Negative | None | Undocumented behavior (documentation marks `firstname` as non-optional but defines no error response). |
| TC-CREATE-005 | POST `/booking` | Create booking with empty string for `additionalneeds` | Boundary | None | HTTP 200 OK; Booking created with `additionalneeds: ""`. |
| TC-UPDATE-001 | PUT `/booking/:id` | Update booking using valid `Cookie: token=<token_value>` header | Positive | Booking ID exists; Valid auth token generated | HTTP 200 OK; Response body contains updated booking details matching input. |
| TC-UPDATE-002 | PUT `/booking/:id` | Update booking using valid `Authorization: Basic YWRtaW46cGFzc3dvcmQxMjM=` header | Positive | Booking ID exists | HTTP 200 OK; Response body contains updated booking details. |
| TC-UPDATE-003 | PUT `/booking/:id` | Omit mandatory authorization headers (`Cookie` and `Authorization`) | Negative | Booking ID exists | Undocumented behavior (documentation lists authorization as required for access but does not define 401/403 status code or error payload). |
| TC-UPDATE-004 | PUT `/booking/:id` | Omit required field `totalprice` in request payload | Negative | Booking ID exists; Valid authorization provided | Undocumented behavior (documentation lists payload fields as mandatory for PUT but specifies no validation error response). |
| TC-UPDATE-005 | PATCH `/booking/:id` | Partial update of `firstname` and `lastname` using Cookie token auth | Positive | Booking ID exists; Valid auth token generated | HTTP 200 OK; Response body contains updated booking object with modified fields updated and untouched fields retained. |
| TC-UPDATE-006 | PATCH `/booking/:id` | Partial update on non-existent booking ID | Negative | Booking ID does not exist; Valid authorization provided | Undocumented behavior (documentation does not define error code or message for missing resources). |
| TC-DELETE-001 | DELETE `/booking/:id` | Delete booking using `Cookie: token=<token_value>` header | Positive | Booking ID exists; Valid auth token generated | Response status code 201 Created (per example) or 200 OK (per table header). Body contains `OK`. |
| TC-DELETE-002 | DELETE `/booking/:id` | Delete booking using `Authorization: Basic YWRtaW46cGFzc3dvcmQxMjM=` header | Positive | Booking ID exists | Response status code 201 Created (per example) or 200 OK (per table header). Body contains `OK`. |
| TC-DELETE-003 | DELETE `/booking/:id` | Delete booking without providing Cookie or Authorization header | Negative | Booking ID exists | Undocumented behavior (authorization requirement noted, but error status/payload is missing). |
| TC-DELETE-004 | DELETE `/booking/:id` | Delete booking using a non-existent ID | Negative | Booking ID does not exist; Valid authorization provided | Undocumented behavior (no 404 or error payload documented). |

---

### Documentation Ambiguities and Omissions

1. **Conflicting HTTP Status Codes**:
   * **Ping - HealthCheck**: Section header lists `Success 200`, but the response example shows `HTTP/1.1 201 Created`.
   * **Booking - DeleteBooking**: Section header lists `Success 200`, but the response example shows `HTTP/1.1 201 Created`.

2. **Complete Absence of Error Documentation**:
   * No HTTP error codes (e.g., `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `415 Unsupported Media Type`) or error response bodies are defined for any endpoint.

3. **Incorrect HTTP Method in Examples**:
   * **Booking - PartialUpdateBooking**: All `curl` request examples (JSON, XML, URL-encoded) use `curl -X PUT` instead of `curl -X PATCH`.

4. **Malformed Parameter Descriptions / Documentation Typos**:
   * In **PUT** and **PATCH** headers, the description for `Authorization` contains an unclosed trailing bracket and snippet: `YWRtaW46cGFzc3dvcmQxMjM=] Basic authorization header...`.
   * In **Booking - DeleteBooking**, the URL Parameter table lists `id` with the description *"ID for the booking you want to update"* instead of "delete". Furthermore, the endpoint URL header literally hardcodes `DELETE booking/1` rather than standard parameterized format `DELETE booking/:id`.

5. **Unclear Mandatory Header Requirements**:
   * **GET `/booking/:id`**: Lists `Accept` header as `Optional: no`. Standard HTTP GET behavior when `Accept` is omitted is not specified.
   * **POST `/booking`**: Lists `Content-Type` and `Accept` as `Optional: no`. Default/fallback behaviors for missing headers are not documented.

6. **Missing Data Type & Validation Constraints**:
   * Date fields (`bookingdates.checkin`, `bookingdates.checkout`) in body parameters are described as type `Date`, but specific formatting (e.g., ISO 8601 `YYYY-MM-DD`) is only mentioned in query parameter descriptions (`CCYY-MM-DD`).
   * No string length limits, numeric value ranges (e.g., negative `totalprice`), or regex formats are provided for input parameters.
   * `additionalneeds` is marked as `Optional: no` in POST/PUT endpoints, meaning empty/null behavior is left unspecified.