package com.mockito.learning.happyhotel.booking;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Sample02ControllerSampleTest {

    @LocalServerPort
    private int port;

    private URL base;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private TestRestTemplate template;

    @BeforeEach
    public void setUp() throws Exception {
        this.base = new URL("http://localhost:" + port + "/greeting");
    }

    @Test
    public void getHello() throws Exception {
        // given
        String expected = "Greetings from The Happy Hotel. We've got enough beds for 10 guests!";

        when(bookingService.getAvailablePlaceCount()).thenReturn(10);
        // when
        ResponseEntity<String> response = template.getForEntity(base.toString(), String.class);
        String actual = response.getBody();

        // then
        assertEquals(expected, actual);
    }

}
