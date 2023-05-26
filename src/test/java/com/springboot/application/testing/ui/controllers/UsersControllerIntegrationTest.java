package com.springboot.application.testing.ui.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.springboot.application.testing.security.SecurityConstants;
import com.springboot.application.testing.ui.response.UserRest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestPropertySource(locations = "/application-test.properties",
//        properties = "server.port=8081")
//@TestPropertySource(locations = "/application-test.properties")
//  properties = {"server.port=8081", "hostname=192.168.0.2"} to override properties with @SpringBootTest and @TestPropertySource annotation
// SpringBootTest.WebEnvironment.MOCK env is the default webEnvironment,
// and in that it doesn't run the real embedded server container
// so you use MockMvc to test you app

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) //by default Junit will create new instance of test class per test method
        // this @TestInstance is to override that behaviour by providing the Lifecycle as PER_CLASS
class UsersControllerIntegrationTest {

    @Value("${server.port}")
    private int serverPort;

    @LocalServerPort
    private int localServerPort;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private String authorizationToken;

    @Test
    @DisplayName("User can be created")
    @Order(1)
    void testCreateUser_whenValidInput_returnsUserDetails() throws JsonProcessingException {
        // creating body
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonObject = mapper.createObjectNode();
        jsonObject.put("firstName", "John");
        jsonObject.put("lastName", "Doe");
        jsonObject.put("email", "john@doe.com");
        jsonObject.put("password", "password");
        jsonObject.put("repeatPassword", "password");

        String jsonObjStr = mapper.writeValueAsString(jsonObject);
        System.out.println(jsonObjStr);
        // headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        // request
        HttpEntity<String> request = new HttpEntity<>(jsonObjStr, headers);

        // send post request
        ResponseEntity<UserRest> createdUserDetailsEntity = testRestTemplate
                .postForEntity("/users",
                        request,
                        UserRest.class);
        UserRest createdUserDetails = createdUserDetailsEntity.getBody();

        assertEquals(HttpStatus.OK, createdUserDetailsEntity.getStatusCode());
        assertEquals(
                jsonObject.get("firstName").asText(),
                createdUserDetails.getFirstName(),
                "Returned user's firstname doesn't match"
        );
        assertEquals(
                jsonObject.get("lastName").asText(),
                createdUserDetails.getLastName(),
                "Returned user's lastName doesn't match"
        );
        assertEquals(
                jsonObject.get("email").asText(),
                createdUserDetails.getEmail(),
                "Returned user's email doesn't match"
        );
        assertFalse(
                createdUserDetails.getUserId().trim().isEmpty(),
                "User id should not match"
        );


    }

    @Test
    @DisplayName("GET /users requires JWT")
    @Order(2)
    void testGetUsers_WhenMissingJWT_returns403() {
        //headers
        HttpHeaders headers = new HttpHeaders();
        // another way to set headers
        headers.set("Accept", "application/json");

        // request
        HttpEntity requestEntity = new HttpEntity(null, headers);

        // send get request
        ResponseEntity<List<UserRest>> response = testRestTemplate.exchange("/users",
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<List<UserRest>>() {
                } // to tell which type of object/datatype get endpoint are we expecting
        );

        assertEquals(HttpStatus.FORBIDDEN,
                response.getStatusCode(),
                "It should return 403."
        );

    }

    @Test
    @DisplayName("/login works")
    @Order(3)
    void testUserLogin_whenValidCredsProvided_returnsJWTinAuthHeader() throws JsonProcessingException {
        // body
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode loginObjectNode = mapper.createObjectNode().put("email", "john@doe.com")
                .put("password", "password");
        // request
        HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(loginObjectNode));

        // send post request

        ResponseEntity response = testRestTemplate.postForEntity("/users/login", entity, null);
        authorizationToken = response.getHeaders().getValuesAsList(SecurityConstants.HEADER_STRING).get(0);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP Status code should be 200");
        assertNotNull(authorizationToken,
                "Response should contain Authorization header with JWT");
        assertNotNull(response.getHeaders().getValuesAsList("UserID").get(0),
                "Response should contain UserID in response header");
    }

    @Test
    @DisplayName("GET /users with JWT")
    @Order(4)
    void testGetUsers_WhenCorrectJWTProvided_returnsUsers() {
        // headers
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        System.out.println("header: " + authorizationToken);
        headers.setBearerAuth(authorizationToken.split(" ")[1]);

        // entity/request
        HttpEntity entity = new HttpEntity(headers);

        // get
        ResponseEntity<List<UserRest>> response = testRestTemplate.exchange("/users",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<UserRest>>() {
                }
        );

        assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP Status code should be 200");
        List<UserRest> actualUsers = response.getBody();
        assertTrue(actualUsers.size() == 1,
                "There should be one user");


    }

}


