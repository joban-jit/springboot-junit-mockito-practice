package com.mockito.learning.happyhotel.booking;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HappyController {

    private BookingService bookingService;

    @RequestMapping("/greeting")
    public String index() {
        return "Greetings from The Happy Hotel. We've got enough beds for " + bookingService.getAvailablePlaceCount() + " guests!";
    }

    public HappyController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

}
