package com.automation.framework.services.payment.endpoints;

public class FlightEndpoints {
    
    // Ixigo Flight Booking API Endpoints
    public static final String IXIGO_FLIGHT_TRIP_DETAILS = "/flight-booking-read/flight-trip/details/{tripId}";
    public static final String IXIGO_FLIGHT_SEARCH = "/flight-booking/search";
    public static final String IXIGO_FLIGHT_BOOKING = "/flight-booking/book";
    public static final String IXIGO_FLIGHT_CANCEL = "/flight-booking/cancel/{bookingId}";
}