package com.springboot.application.testing.ui.controllers;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestPropertySource(locations = "/application-test.properties",
//        properties = "server.port=8081")
//@TestPropertySource(locations = "/application-test.properties")
//  properties = {"server.port=8081", "hostname=192.168.0.2"} to override properties with @SpringBootTest and @TestPropertySource annotation
// SpringBootTest.WebEnvironment.MOCK env is the default webEnvironment,
// and in that it doesn't run the real embedded server container
// so you use MockMvc to test you app

class UsersControllerIntegrationTest {

    @Value("${server.port}")
    private int serverPort;

    @LocalServerPort
    private int localServerPort;

    @Test
    void contextLoad() {
        System.out.println("server port: " + serverPort); // will always be 0 in case of random port config
        System.out.println("local server port: " + localServerPort);
    }

}