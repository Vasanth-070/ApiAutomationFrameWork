package com.automation.framework.services.payment.endpoints;

public class FlightEndpoints {
    
    // Ixigo Flight Booking API Endpoints
    public static final String IXIGO_FLIGHT_TRIP_DETAILS = "/flight-booking-read/flight-trip/details/{tripId}";
    public static final String IXIGO_FLIGHT_SEARCH = "/flight-booking/search";
    public static final String IXIGO_FLIGHT_BOOKING = "/flight-booking/book";
    public static final String IXIGO_FLIGHT_CANCEL = "/flight-booking/cancel/{bookingId}";
    public static final String Payment_init = "/payments/v4/transaction/{transactionId}?campaigns=PENDING_LOADER";
}