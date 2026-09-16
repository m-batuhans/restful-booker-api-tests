
Filter...
x
Auth
CreateToken
Booking
GetBookingIds
GetBooking
CreateBooking
UpdateBooking
PartialUpdateBooking
DeleteBooking
Ping
HealthCheck
restful-booker
API documentation for the playground API restful-booker. Click here to go back to Home
Auth
Auth - CreateToken
Creates a new auth token to use for access to the PUT and DELETE /booking

post
https://restful-booker.herokuapp.com/auth
Example 1:
curl -X POST \
  https://restful-booker.herokuapp.com/auth \
  -H 'Content-Type: application/json' \
  -d '{
    "username" : "admin",
    "password" : "password123"
}'
Header
Alan	Tip	Açıklama
Content-Type	string	
Sets the format of payload you are sending

Varsayılan değer: application/json

Request body
Alan	Tip	Açıklama
username	String	
Username for authentication

Varsayılan değer: admin

password	String	
Password for authentication

Varsayılan değer: password123

Success 200
Alan	Tip	Açıklama
token	String	
Token to use in future requests

Response:
HTTP/1.1 200 OK

{
    "token": "abc123"
}
Booking
Booking - GetBookingIds
Returns the ids of all the bookings that exist within the API. Can take optional query strings to search and return a subset of booking ids.

get
https://restful-booker.herokuapp.com/booking
Example 1 (All IDs):
Example 2 (Filter by name):
Example 3 (Filter by checkin/checkout date):
curl -i https://restful-booker.herokuapp.com/booking
Parametre
Alan	Tip	Açıklama
firstnameopsiyonel	String	
Return bookings with a specific firstname

lastnameopsiyonel	String	
Return bookings with a specific lastname

checkinopsiyonel	date	
Return bookings that have a checkin date greater than or equal to the set checkin date. Format must be CCYY-MM-DD

checkoutopsiyonel	date	
Return bookings that have a checkout date greater than or equal to the set checkout date. Format must be CCYY-MM-DD

Success 200
Alan	Tip	Açıklama
object	object[]	
Array of objects that contain unique booking IDs

  bookingid	number	
ID of a specific booking that matches search criteria

Response:
HTTP/1.1 200 OK

[
  {
    "bookingid": 1
  },
  {
    "bookingid": 2
  },
  {
    "bookingid": 3
  },
  {
    "bookingid": 4
  }
]
Booking - GetBooking
Returns a specific booking based upon the booking id provided

get
https://restful-booker.herokuapp.com/booking/:id
Example 1 (Get booking):
curl -i https://restful-booker.herokuapp.com/booking/1
Header
Alan	Tip	Açıklama
Accept	string	
Sets what format the response body is returned in. Can be application/json or application/xml

Varsayılan değer: application/json

Url Parameter
Alan	Tip	Açıklama
id	String	
The id of the booking you would like to retrieve

Success 200
Alan	Tip	Açıklama
firstname	String	
Firstname for the guest who made the booking

lastname	String	
Lastname for the guest who made the booking

totalprice	Number	
The total price for the booking

depositpaid	Boolean	
Whether the deposit has been paid or not

bookingdates	Object	
Sub-object that contains the checkin and checkout dates

  checkin	Date	
Date the guest is checking in

  checkout	Date	
Date the guest is checking out

additionalneeds	String	
Any other needs the guest has

JSON Response:
XML Response:
URL Response:
HTTP/1.1 200 OK

{
    "firstname": "Sally",
    "lastname": "Brown",
    "totalprice": 111,
    "depositpaid": true,
    "bookingdates": {
        "checkin": "2013-02-23",
        "checkout": "2014-10-23"
    },
    "additionalneeds": "Breakfast"
}
Booking - CreateBooking
Creates a new booking in the API

post
https://restful-booker.herokuapp.com/booking
JSON example usage:
XML example usage:
URLencoded example usage:
curl -X POST \
  https://restful-booker.herokuapp.com/booking \
  -H 'Content-Type: application/json' \
  -d '{
    "firstname" : "Jim",
    "lastname" : "Brown",
    "totalprice" : 111,
    "depositpaid" : true,
    "bookingdates" : {
        "checkin" : "2018-01-01",
        "checkout" : "2019-01-01"
    },
    "additionalneeds" : "Breakfast"
}'
Header
Alan	Tip	Açıklama
Content-Type	string	
Sets the format of payload you are sending. Can be application/json or text/xml

Varsayılan değer: application/json

Accept	string	
Sets what format the response body is returned in. Can be application/json or application/xml

Varsayılan değer: application/json

Request body
Alan	Tip	Açıklama
firstname	String	
Firstname for the guest who made the booking

lastname	String	
Lastname for the guest who made the booking

totalprice	Number	
The total price for the booking

depositpaid	Boolean	
Whether the deposit has been paid or not

  checkin	Date	
Date the guest is checking in

  checkout	Date	
Date the guest is checking out

additionalneeds	String	
Any other needs the guest has

Success 200
Alan	Tip	Açıklama
bookingid	Number	
ID for newly created booking

booking	Object	
Object that contains

  firstname	String	
Firstname for the guest who made the booking

  lastname	String	
Lastname for the guest who made the booking

  totalprice	Number	
The total price for the booking

  depositpaid	Boolean	
Whether the deposit has been paid or not

  bookingdates	Object	
Sub-object that contains the checkin and checkout dates

    checkin	Date	
Date the guest is checking in

    checkout	Date	
Date the guest is checking out

  additionalneeds	String	
Any other needs the guest has

JSON Response:
XML Response:
URL Response:
HTTP/1.1 200 OK

{
    "bookingid": 1,
    "booking": {
        "firstname": "Jim",
        "lastname": "Brown",
        "totalprice": 111,
        "depositpaid": true,
        "bookingdates": {
            "checkin": "2018-01-01",
            "checkout": "2019-01-01"
        },
        "additionalneeds": "Breakfast"
    }
}
Booking - UpdateBooking
Updates a current booking

put
https://restful-booker.herokuapp.com/booking/:id
JSON example usage:
XML example usage:
URLencoded example usage:
curl -X PUT \
  https://restful-booker.herokuapp.com/booking/1 \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -H 'Cookie: token=abc123' \
  -d '{
    "firstname" : "James",
    "lastname" : "Brown",
    "totalprice" : 111,
    "depositpaid" : true,
    "bookingdates" : {
        "checkin" : "2018-01-01",
        "checkout" : "2019-01-01"
    },
    "additionalneeds" : "Breakfast"
}'
Header
Alan	Tip	Açıklama
Content-Type	string	
Sets the format of payload you are sending. Can be application/json or text/xml

Varsayılan değer: application/json

Accept	string	
Sets what format the response body is returned in. Can be application/json or application/xml

Varsayılan değer: application/json

Cookieopsiyonel	string	
Sets an authorization token to access the PUT endpoint, can be used as an alternative to the Authorization

Varsayılan değer: token=<token_value>

Authorizationopsiyonel	string	
YWRtaW46cGFzc3dvcmQxMjM=] Basic authorization header to access the PUT endpoint, can be used as an alternative to the Cookie header

Varsayılan değer: Basic

Url Parameter
Alan	Tip	Açıklama
id	Number	
ID for the booking you want to update

Request body
Alan	Tip	Açıklama
firstname	String	
Firstname for the guest who made the booking

lastname	String	
Lastname for the guest who made the booking

totalprice	Number	
The total price for the booking

depositpaid	Boolean	
Whether the deposit has been paid or not

  checkin	Date	
Date the guest is checking in

  checkout	Date	
Date the guest is checking out

additionalneeds	String	
Any other needs the guest has

Success 200
Alan	Tip	Açıklama
firstname	String	
Firstname for the guest who made the booking

lastname	String	
Lastname for the guest who made the booking

totalprice	Number	
The total price for the booking

depositpaid	Boolean	
Whether the deposit has been paid or not

bookingdates	Object	
Sub-object that contains the checkin and checkout dates

  checkin	Date	
Date the guest is checking in

  checkout	Date	
Date the guest is checking out

additionalneeds	String	
Any other needs the guest has

JSON Response:
XML Response:
URL Response:
HTTP/1.1 200 OK

{
    "firstname" : "James",
    "lastname" : "Brown",
    "totalprice" : 111,
    "depositpaid" : true,
    "bookingdates" : {
        "checkin" : "2018-01-01",
        "checkout" : "2019-01-01"
    },
    "additionalneeds" : "Breakfast"
}
Booking - PartialUpdateBooking
Updates a current booking with a partial payload

patch
https://restful-booker.herokuapp.com/booking/:id
JSON example usage:
XML example usage:
URLencoded example usage:
curl -X PUT \
  https://restful-booker.herokuapp.com/booking/1 \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -H 'Cookie: token=abc123' \
  -d '{
    "firstname" : "James",
    "lastname" : "Brown"
}'
Header
Alan	Tip	Açıklama
Content-Type	string	
Sets the format of payload you are sending. Can be application/json or text/xml

Varsayılan değer: application/json

Accept	string	
Sets what format the response body is returned in. Can be application/json or application/xml

Varsayılan değer: application/json

Cookieopsiyonel	string	
Sets an authorization token to access the PUT endpoint, can be used as an alternative to the Authorization

Varsayılan değer: token=<token_value>

Authorizationopsiyonel	string	
YWRtaW46cGFzc3dvcmQxMjM=] Basic authorization header to access the PUT endpoint, can be used as an alternative to the Cookie header

Varsayılan değer: Basic

Url Parameter
Alan	Tip	Açıklama
id	Number	
ID for the booking you want to update

Request body
Alan	Tip	Açıklama
firstnameopsiyonel	String	
Firstname for the guest who made the booking

lastnameopsiyonel	String	
Lastname for the guest who made the booking

totalpriceopsiyonel	Number	
The total price for the booking

depositpaidopsiyonel	Boolean	
Whether the deposit has been paid or not

  checkinopsiyonel	Date	
Date the guest is checking in

  checkoutopsiyonel	Date	
Date the guest is checking out

additionalneedsopsiyonel	String	
Any other needs the guest has

Success 200
Alan	Tip	Açıklama
firstname	String	
Firstname for the guest who made the booking

lastname	String	
Lastname for the guest who made the booking

totalprice	Number	
The total price for the booking

depositpaid	Boolean	
Whether the deposit has been paid or not

bookingdates	Object	
Sub-object that contains the checkin and checkout dates

  checkin	Date	
Date the guest is checking in

  checkout	Date	
Date the guest is checking out

additionalneeds	String	
Any other needs the guest has

JSON Response:
XML Response:
URL Response:
HTTP/1.1 200 OK

{
    "firstname" : "James",
    "lastname" : "Brown",
    "totalprice" : 111,
    "depositpaid" : true,
    "bookingdates" : {
        "checkin" : "2018-01-01",
        "checkout" : "2019-01-01"
    },
    "additionalneeds" : "Breakfast"
}
Booking - DeleteBooking
Deletes a booking from the API. Requires an authorization token to be set in the header or a Basic auth header.

delete
https://restful-booker.herokuapp.com/booking/1
Example 1 (Cookie):
Example 2 (Basic auth):
curl -X DELETE \
  https://restful-booker.herokuapp.com/booking/1 \
  -H 'Content-Type: application/json' \
  -H 'Cookie: token=abc123'
Header
Alan	Tip	Açıklama
Cookieopsiyonel	string	
Sets an authorization token to access the DELETE endpoint, can be used as an alternative to the Authorization

Varsayılan değer: token=<token_value>

Authorizationopsiyonel	string	
YWRtaW46cGFzc3dvcmQxMjM=] Basic authorization header to access the DELETE endpoint, can be used as an alternative to the Cookie header

Varsayılan değer: Basic

Url Parameter
Alan	Tip	Açıklama
id	Number	
ID for the booking you want to update

Success 200
Alan	Tip	Açıklama
OK	String	
Default HTTP 201 response

Response:
HTTP/1.1 201 Created
Ping
Ping - HealthCheck
A simple health check endpoint to confirm whether the API is up and running.

get
https://restful-booker.herokuapp.com/ping
Ping server:
curl -i https://restful-booker.herokuapp.com/ping
Success 200
Alan	Tip	Açıklama
OK	String	
Default HTTP 201 response

Response:
HTTP/1.1 201 Created
Oluşturan apidoc 0.25.0 - 2025-06-11T20:24:26.733Z