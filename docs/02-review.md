# Review of AI-generated test cases

Source: docs/01-ai-generated-cases.md (model: gemini-3.6-flash)
Reviewer: Mevlüt Batuhan Saar

## Summary
- AI-generated cases: 27
- Keep: 6 · Fix: 20 · Drop: 1
- Added cases (missed by AI): 9
- AI documentation findings: 7 confirmed, 0 rejected
- Documentation findings added by reviewer: 2

## Oracle policy
Order of precedence when deciding an expected result:

1. **Documented behavior.** If the documentation states it, it is the expected result.
2. **Documentation contradicts itself.** The raw HTTP response example wins over section headers, because it shows the actual response. The contradiction is logged under Documentation findings.
3. **Documentation is silent, HTTP semantics decide.** Non-existent resource → 404 Not Found. Invalid input (missing required field, wrong format, invalid value) → 400 Bad Request. Missing or invalid credentials on a protected endpoint → 401 Unauthorized or 403 Forbidden (both accepted; the documentation does not choose).
4. **Documentation is silent and HTTP semantics do not decide.** Assert the invariant that must hold regardless of status code (e.g. no token issued, data not modified) and record the actual status code as an observation.

Global rules (applied to every case, not counted as changes):
- A precondition such as "booking exists" means the test creates that booking itself. Tests never rely on seeded data.
- Names used in filter tests get a unique suffix, so other users of the shared instance cannot affect the result.
- A body field in CreateBooking or UpdateBooking counts as required unless marked optional. PartialUpdateBooking explicitly marks its fields optional, so the missing label elsewhere is read as required (DOC-5).
- A guaranteed non-existent booking ID is obtained by creating and then deleting a booking in setup. If deletion fails, setup fails and the test reports an error, not a false result.

## Decisions
| ID | Decision (Keep/Fix/Drop) | Reason | Final expected result |
|---|---|---|---|
| TC-AUTH-001 | Keep | Documented. | 200 OK; body contains a non-empty `token` string. |
| TC-AUTH-002 | Fix | Expected result was undocumented. Status codes for login failure are not decided by HTTP semantics, so policy 4 applies. | No `token` field in the response body. Actual status code recorded. |
| TC-AUTH-003 | Fix | Type corrected Boundary → Negative: a missing body is an invalid partition, not an edge value. The "default value" labels on username/password are examples, not behavior (DOC-9). Policy 3. | 400 Bad Request; no `token` field in the response body. |
| TC-FILTER-001 | Keep | Verifiable as written. | 200 OK; array of objects with numeric `bookingid`, including the booking created in setup. |
| TC-FILTER-002 | Fix | "Matching" was not verifiable. With a unique firstname the exact result is known. | 200 OK; array contains exactly one item, the created booking's ID. |
| TC-FILTER-003 | Fix | Same as FILTER-002, for lastname. | 200 OK; array contains exactly one item, the created booking's ID. |
| TC-FILTER-004 | Fix | The documentation says "greater than or equal"; the equal case is the boundary and was not covered. Scenario changed: filter date = the booking's checkin date. Excluded side added as FILTER-007. | 200 OK; the created booking's ID is in the result. |
| TC-FILTER-005 | Fix | Same as FILTER-004, for checkout. Excluded side added as FILTER-008. | 200 OK; the created booking's ID is in the result. |
| TC-FILTER-006 | Fix | Type corrected Boundary → Negative: a wrong format is an invalid partition. The format is documented for query parameters (CCYY-MM-DD). Policy 3. | 400 Bad Request. |
| TC-GET-001 | Fix | Field presence alone does not prove the data is correct. | 200 OK; every field equals the value sent at creation. |
| TC-GET-002 | Keep | Documented (Accept: application/xml). | 200 OK; XML body with the booking's fields. |
| TC-GET-003 | Fix | Policy 3. Non-existent ID obtained by the global rule. | 404 Not Found. |
| TC-GET-004 | Drop | Already covered by the existing smoke test (`HealthCheckTest`). It was also filed under the wrong area. The 200/201 contradiction is logged as DOC-2. | — |
| TC-CREATE-001 | Fix | The response must reflect the request, not just contain fields. | 200 OK; numeric `bookingid`; `booking` object equal to the request payload. |
| TC-CREATE-002 | Keep | Documented (Content-Type: text/xml). | 200 OK; XML body with `bookingid` and the booking's fields. |
| TC-CREATE-003 | Keep | No minimum is documented; 0 is the lowest non-negative value. Invalid side added as CREATE-005. | 200 OK; `totalprice` stored as 0. |
| TC-CREATE-004 | Fix | "Missing required fields" named no field. Narrowed to one field (`firstname`) so a failure has a single clear cause. Policy 3. | 400 Bad Request; no booking exists with the test's unique lastname. |
| TC-UPDATE-001 | Fix | A 200 response does not prove the update was stored. | 200 OK with updated values; a subsequent GET returns the updated values. |
| TC-UPDATE-002 | Keep | Covers the second documented auth method. Storage is already verified in UPDATE-001. | 200 OK with updated values. |
| TC-UPDATE-003 | Fix | Policy 3. The real risk is unauthorized modification, so the data check matters more than the status code. | 401 or 403; a subsequent GET shows the booking unchanged. |
| TC-UPDATE-004 | Fix | Policy 3, with a valid token. Non-existent ID obtained by the global rule. | 404 Not Found. |
| TC-UPDATE-005 | Fix | The defining property of PATCH is that untouched fields stay the same; this was not asserted. | 200 OK; `firstname` and `lastname` updated, all other fields unchanged. |
| TC-UPDATE-006 | Fix | Same as UPDATE-003, for PATCH. The documentation never states that PATCH requires auth (DOC-8); it is treated as protected because it lists the same auth headers as PUT and modifies the same data. | 401 or 403; a subsequent GET shows the booking unchanged. |
| TC-DELETE-001 | Fix | "201 (or 200)" is not a single expected result; resolved by policy 2. A status code alone does not prove deletion. | 201 Created; a subsequent GET returns 404. |
| TC-DELETE-002 | Fix | Ambiguous status resolved by policy 2. Deletion is already verified in DELETE-001. | 201 Created. |
| TC-DELETE-003 | Fix | Policy 3. | 401 or 403; a subsequent GET still returns the booking. |
| TC-DELETE-004 | Fix | Policy 3, with a valid token. Non-existent ID obtained by the global rule. | 404 Not Found. |

## Added cases (missed by AI)
| ID | Endpoint | Scenario | Type | Preconditions | Expected result | Why it matters |
|---|---|---|---|---|---|---|
| TC-FILTER-007 | GET /booking | Filter by checkin date one day after the booking's checkin date | Boundary | Booking created with a known checkin date | 200 OK; the created booking's ID is not in the result | Excluded side of the ">=" boundary. Without it, a filter that ignores the date entirely would still pass FILTER-004. |
| TC-FILTER-008 | GET /booking | Filter by checkout date one day after the booking's checkout date | Boundary | Booking created with a known checkout date | 200 OK; the created booking's ID is not in the result | Same as FILTER-007, for checkout. |
| TC-CREATE-005 | POST /booking | Create booking with `totalprice: -1` | Boundary | None | 400 Bad Request | Invalid side of the price boundary; CREATE-003 covers only the valid side. |
| TC-CREATE-006 | POST /booking | Create booking with a decimal `totalprice` (150.75) | Positive | None | 200 OK; `totalprice` stored exactly as 150.75 | The documented type is Number, which includes decimals, but every example and every AI case used whole numbers only. |
| TC-CREATE-007 | POST /booking | Create booking with checkout date earlier than checkin date | Negative | None | 400 Bad Request | Checks the logical relation between two fields; the AI tested each field only in isolation. |
| TC-CREATE-008 | POST /booking | Create booking with checkin in the wrong format (`17-09-2026`) | Negative | None | 400 Bad Request; no booking exists with the test's unique lastname | The AI tested date format only for the query parameter (FILTER-006), not for the request body, where bad data would be stored. The body format is not documented (DOC-7); the query parameter format and all examples are applied. |
| TC-UPDATE-007 | PUT /booking/:id | Update booking with an invalid token (`Cookie: token=invalid`) | Negative | Booking created | 401 or 403; a subsequent GET shows the booking unchanged | The AI covered only missing auth. A wrong token is a different partition. |
| TC-UPDATE-008 | PUT /booking/:id | Full update with `lastname` missing from the body | Negative | Booking created; valid token | 400 Bad Request; a subsequent GET shows the booking unchanged | PUT replaces the whole resource; an incomplete body must not partially overwrite it. |
| TC-DELETE-005 | DELETE /booking/:id | Delete booking with an invalid token (`Cookie: token=invalid`) | Negative | Booking created | 401 or 403; a subsequent GET still returns the booking | Same partition as UPDATE-007, for DELETE. |

## Documentation findings
The AI output ends with a list of documentation problems. Each one was checked against `api-docs/restful-booker.md`. DOC-8 and DOC-9 were found by the reviewer.

| # | Source | Decision | Evidence in documentation | Impact on tests |
|---|---|---|---|---|
| DOC-1 | AI | Confirmed | Every endpoint has only a "Success 200" section; no error sections exist | Oracle policy rules 3 and 4 |
| DOC-2 | AI | Confirmed | Ping and DeleteBooking: header "Success 200", description "Default HTTP 201 response", example `HTTP/1.1 201 Created` | Policy 2 → 201 (DELETE-001, DELETE-002) |
| DOC-3 | AI | Confirmed | PartialUpdateBooking is defined as `patch`, but its cURL example uses `curl -X PUT` | Tests use PATCH |
| DOC-4 | AI | Confirmed | Authorization description starts with `YWRtaW46cGFzc3dvcmQxMjM=]` | The value is standard Basic auth (base64 of `admin:password123`); tests use the standard format |
| DOC-5 | AI | Confirmed | Only PartialUpdateBooking body fields are marked optional; CreateBooking and UpdateBooking fields have no label | Global required-field rule |
| DOC-6 | AI | Confirmed | DeleteBooking `id` description: "ID for the booking you want to update" | None |
| DOC-7 | AI | Confirmed, extended | Invalid format behavior is not documented. The format CCYY-MM-DD is stated only for query parameters; body `checkin`/`checkout` are typed "Date" with no format | FILTER-006, CREATE-008 |
| DOC-8 | Reviewer | — | CreateToken: token is "to use for access to the PUT and DELETE /booking". PATCH is not mentioned, yet PartialUpdateBooking lists the Cookie and Authorization headers | UPDATE-006 treats PATCH as protected |
| DOC-9 | Reviewer | — | "Default value" labels appear on fields where they cannot be runtime defaults, e.g. Cookie: `token=<token_value>` | Default values are read as examples, not behavior (AUTH-003) |
