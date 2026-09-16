<!--
Model: gemini-3.6-flash
Generated: 2026-09-17 00:01:54
Prompt file: prompts\generate-cases.md
-->

| ID | Endpoint | Scenario | Type (Positive/Negative/Boundary) | Preconditions | Expected result |
|---|---|---|---|---|---|
| TC-AUTH-001 | POST /auth | Create auth token with valid credentials (`admin` / `password123`) | Positive | None | HTTP 200 OK with JSON response containing `"token": "<string>"`. |
| TC-AUTH-002 | POST /auth | Attempt authentication with invalid credentials | Negative | None | Undocumented in spec (Spec does not define HTTP status code or payload for authentication failure). |
| TC-AUTH-003 | POST /auth | Attempt authentication with an empty request body | Boundary | None | Undocumented in spec (Spec does not define error handling or response format for missing credentials). |
| TC-FILTER-001 | GET /booking | Retrieve all booking IDs without filter parameters | Positive | Bookings exist in the system | HTTP 200 OK with JSON array of objects, each containing `bookingid` (Number). |
| TC-FILTER-002 | GET /booking | Filter booking IDs by `firstname` | Positive | A booking with the specified firstname exists | HTTP 200 OK with JSON array of matching `bookingid` objects. |
| TC-FILTER-003 | GET /booking | Filter booking IDs by `lastname` | Positive | A booking with the specified lastname exists | HTTP 200 OK with JSON array of matching `bookingid` objects. |
| TC-FILTER-004 | GET /booking | Filter booking IDs by `checkin` date | Positive | Bookings with checkin date >= specified date exist | HTTP 200 OK with JSON array of matching `bookingid` objects. |
| TC-FILTER-005 | GET /booking | Filter booking IDs by `checkout` date | Positive | Bookings with checkout date >= specified date exist | HTTP 200 OK with JSON array of matching `bookingid` objects. |
| TC-FILTER-006 | GET /booking | Filter booking IDs using invalid `checkin` date format (e.g. `DD-MM-YYYY`) | Boundary | None | Undocumented in spec (Spec states format must be `CCYY-MM-DD`, but no error response is specified). |
| TC-GET-001 | GET /booking/:id | Retrieve specific booking with default/JSON Accept header | Positive | Booking with specified ID exists | HTTP 200 OK with JSON body containing `firstname`, `lastname`, `totalprice`, `depositpaid`, `bookingdates` (`checkin`, `checkout`), and `additionalneeds`. |
| TC-GET-002 | GET /booking/:id | Retrieve specific booking with `Accept: application/xml` header | Positive | Booking with specified ID exists | HTTP 200 OK with XML formatted response containing booking details. |
| TC-GET-003 | GET /booking/:id | Retrieve booking details with non-existent booking ID | Negative | Booking ID does not exist | Undocumented in spec (Spec does not define HTTP 404 or error payload structure). |
| TC-GET-004 | GET /ping | Perform server health check ping | Positive | Server is running | HTTP 201 Created with response body string "OK" (or HTTP 200 OK per header section). |
| TC-CREATE-001 | POST /booking | Create a new booking with valid JSON payload | Positive | None | HTTP 200 OK returning `bookingid` (Number) and full `booking` object in JSON format. |
| TC-CREATE-002 | POST /booking | Create a new booking with `Content-Type: text/xml` and XML payload | Positive | None | HTTP 200 OK with XML formatted response body containing created `bookingid` and `booking` object. |
| TC-CREATE-003 | POST /booking | Create booking with numeric boundary value `totalprice: 0` | Boundary | None | HTTP 200 OK returning created booking with `totalprice: 0`. |
| TC-CREATE-004 | POST /booking | Create booking with missing required request body fields | Negative | None | Undocumented in spec (Spec does not specify required vs optional fields or error handling for missing fields). |
| TC-UPDATE-001 | PUT /booking/:id | Update booking using valid `Cookie: token=<token_value>` auth header | Positive | Booking ID exists; Auth token obtained | HTTP 200 OK with updated JSON booking object. |
| TC-UPDATE-002 | PUT /booking/:id | Update booking using valid `Authorization: Basic ...` header | Positive | Booking ID exists | HTTP 200 OK with updated JSON booking object. |
| TC-UPDATE-003 | PUT /booking/:id | Update booking without providing Cookie or Authorization headers | Negative | Booking ID exists | Undocumented in spec (Spec indicates auth is required to access PUT endpoint, but does not define the error status code/body). |
| TC-UPDATE-004 | PUT /booking/:id | Update booking for a non-existent booking ID | Negative | Booking ID does not exist | Undocumented in spec (Spec does not define error response for invalid/non-existent IDs). |
| TC-UPDATE-005 | PATCH /booking/:id | Partially update booking fields (e.g. `firstname` and `lastname` only) | Positive | Booking ID exists; Auth provided | HTTP 200 OK with complete booking object reflecting the updated fields. |
| TC-UPDATE-006 | PATCH /booking/:id | Partially update booking without authorization headers | Negative | Booking ID exists | Undocumented in spec (Spec does not define error response for missing authorization). |
| TC-DELETE-001 | DELETE /booking/:id | Delete booking using valid `Cookie: token=<token_value>` header | Positive | Booking ID exists; Auth token obtained | HTTP 201 Created with body string "OK" (or HTTP 200 OK per header section). |
| TC-DELETE-002 | DELETE /booking/:id | Delete booking using valid `Authorization: Basic ...` header | Positive | Booking ID exists | HTTP 201 Created with body string "OK" (or HTTP 200 OK per header section). |
| TC-DELETE-003 | DELETE /booking/:id | Delete booking without authorization header | Negative | Booking ID exists | Undocumented in spec (Spec indicates auth is required, but error status code is not specified). |
| TC-DELETE-004 | DELETE /booking/:id | Delete a non-existent booking ID | Negative | Booking ID does not exist | Undocumented in spec (Spec does not define error response for non-existent IDs). |

---

### Documentation Ambiguities and Incompleteness

1. **Complete Absence of Error Response Specifications**:
   - The documentation defines **only** 200/201 success responses across all endpoints.
   - It does not document HTTP error status codes (e.g., `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `500 Internal Server Error`) or error payload formats for invalid requests, missing auth headers, bad credentials, or missing resource IDs.

2. **Contradictory HTTP Status Codes for Success**:
   - **`DeleteBooking`**: The response section header states `Success 200`, but the text description states `"Default HTTP 201 response"` and the raw response example explicitly shows `HTTP/1.1 201 Created`.
   - **`Ping - HealthCheck`**: The section header states `Success 200`, but the description states `"Default HTTP 201 response"` and the raw example shows `HTTP/1.1 201 Created`.

3. **Method Mismatch in `PartialUpdateBooking` cURL Example**:
   - The endpoint definition states the HTTP method is `patch` (`PATCH /booking/:id`), but the provided cURL command example uses `curl -X PUT`.

4. **Malformed/Unclear Syntax for Basic Authorization Header**:
   - The `Authorization` header description in `UpdateBooking`, `PartialUpdateBooking`, and `DeleteBooking` contains broken/unparsed text: `YWRtaW46cGFzc3dvcmQxMjM=] Basic authorization header to access...`
   - It is unclear whether the expected format is standard `Authorization: Basic YWRtaW46cGFzc3dvcmQxMjM=` or something else due to the orphaned closing bracket `]`.

5. **Unspecified Mandatory vs. Optional Body Fields**:
   - For `CreateBooking` and `UpdateBooking`, no fields in the request body are marked as optional or required. Only `PartialUpdateBooking` explicitly marks fields as `opsiyonel` (optional).

6. **Incorrect Parameter Description in `DeleteBooking`**:
   - In `DeleteBooking`, the description for the URL parameter `id` is stated as `"ID for the booking you want to update"` instead of "...you want to delete".

7. **Date Format Validation Errors Unspecified**:
   - The documentation specifies `checkin` and `checkout` query parameters must follow `CCYY-MM-DD` format, but does not specify the API's behavior or error response when an invalid date string/format is passed.