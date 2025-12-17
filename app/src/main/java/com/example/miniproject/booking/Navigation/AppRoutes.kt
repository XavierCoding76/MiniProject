package com.example.miniproject.booking.Navigation

sealed class AppRoute(val route: String) {
    object BookingHistory : AppRoute("booking_history")
    object FacilityList : AppRoute("facility_list")
    object FacilityDetails : AppRoute("facility_details/{facilityId}") {
        fun createRoute(id: String) = "facility_details/$id"
    }
    object BookingForm : AppRoute("booking_form/{facilityId}") {
        fun createRoute(id: String) = "booking_form/$id"
    }
}