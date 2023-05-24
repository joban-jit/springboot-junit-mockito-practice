package com.springboot.application.testing.ui.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.application.testing.service.UsersService;
import com.springboot.application.testing.service.UsersServiceImpl;
import com.springboot.application.testing.shared.UserDto;
import com.springboot.application.testing.ui.request.UserDetailsRequestModel;
import com.springboot.application.testing.ui.response.UserRest;
import org.junit.jupiter.api.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebMvcTest(controllers = UsersController.class
        , excludeAutoConfiguration = {SecurityAutoConfiguration.class})// second option here is to
// disable spring security config
// another way to disable
//@AutoConfigureMockMvc(addFilters = false) // to disable spring security config
@MockBean({UsersServiceImpl.class})
class UsersControllerWebLayerTest {

    @Autowired
    private MockMvc mockMvc;


    @Autowired // as I'm using MockBean on top of class level , here I simply
    // autowire here or simply use @MockBean here
    // @MockBean annotation will create the mock and put it into spring context
    // while @Mock will not do that.
    private UsersService usersService;

    @Test
    @DisplayName("User can be created")
    void testCreateUser_WhenCorrectInputProvided_retunsCreatedUser() throws Exception {
        UserDetailsRequestModel userDetailsRequestModel = new UserDetailsRequestModel();
        userDetailsRequestModel.setFirstName("John");
        userDetailsRequestModel.setLastName("Doe");
        userDetailsRequestModel.setEmail("doe@gmail.com");
        userDetailsRequestModel.setPassword("123456789");
        userDetailsRequestModel.setRepeatPassword("123456789");

        UserDto userDto = new ModelMapper().map(userDetailsRequestModel, UserDto.class);
        userDto.setUserId(UUID.randomUUID().toString());
        when(usersService.createUser(any(UserDto.class))).thenReturn(userDto);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userDetailsRequestModel));
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        String response = mvcResult.getResponse().getContentAsString();

        UserRest createdUser = new ObjectMapper().readValue(response, UserRest.class);
//        usersService.createUser()
        assertEquals(userDetailsRequestModel.getFirstName(),
                createdUser.getFirstName(),
                "The returned user first name is incorrect.");
        assertEquals(userDetailsRequestModel.getLastName(),
                createdUser.getLastName(),
                "The returned user last name is incorrect.");
        assertEquals(userDetailsRequestModel.getEmail(),
                createdUser.getEmail(),
                "The returned user email is incorrect.");
        assertFalse(createdUser.getUserId().isEmpty(),
                "The returned user user's id is empty.");
        assertEquals(userDetailsRequestModel.getFirstName(),
                createdUser.getFirstName(),
                "The returned user first name is incorrect.");

        verify(usersService, times(1)).createUser(any());
    }

    @Test
    @DisplayName("First name is not empty")
    void testCreateUser_WhenInCorrectInputProvided_retuns400Error() throws Exception {
        UserDetailsRequestModel invalidUserDetailsRequestModel = new UserDetailsRequestModel();
        invalidUserDetailsRequestModel.setFirstName("");
        invalidUserDetailsRequestModel.setLastName("Doe");
        invalidUserDetailsRequestModel.setEmail("doe@gmail.com");
        invalidUserDetailsRequestModel.setPassword("123456789");
        invalidUserDetailsRequestModel.setRepeatPassword("123456789");

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(invalidUserDetailsRequestModel));


        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
//        assertEquals(HttpStatus.BAD_REQUEST.value(),
//                mvcResult.getResponse().getStatus(),
//                "Expected response is 400"
//        );

//         another way to assert that
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

    }

}