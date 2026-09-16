You are a QA engineer. Design API test cases for Restful-Booker based ONLY on its official documentation, which follows this prompt.

Requirements:
- Cover all endpoints in the documentation.
- Include positive, negative and boundary cases.
- Base expected results only on the documentation, not on assumptions about how the API "probably" behaves.

Output format: a single Markdown table with these columns:
ID | Endpoint | Scenario | Type (Positive/Negative/Boundary) | Preconditions | Expected result

ID format: TC-<AREA>-<NNN>, areas: AUTH, CREATE, GET, FILTER, UPDATE, DELETE.

After the table, list any points where the documentation is unclear or incomplete.
