package com.mockito.learning.happyhotel.booking;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class Sample03MockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @SpyBean // can also use @MockBean
    private BookingService bookingService;

    @Test
    public void shouldReturnDefaultMessage() throws Exception {
        // given

        when(bookingService.getAvailablePlaceCount()).thenReturn(10);
        // when
        this.mockMvc.perform(get("/greeting"))
                .andDo(print())

                // then
                .andExpect(status().isOk())
                .andExpect(content().string("Greetings from The Happy Hotel. We've got enough beds for 10 guests!"));
    }
}