package com.springboot.application.testing.io;

import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
// this @DataJpaTest will disable auto configuration
// and it will create application context only for JPA related components because we are only
// dealing with JPA components
//Also this annotation will make our test methods transactional and will rollback when completes
// By default, it will use embedded in-memory database for our tests
class UserEntityIntegrationTest {

    @Autowired
    private TestEntityManager testEntityManager;

    private UserEntity userEntity;

    @BeforeEach
    public void setupMethod() {
        userEntity = new UserEntity();
        userEntity.setFirstName("John");
        userEntity.setUserId(UUID.randomUUID().toString());
        userEntity.setLastName("Doe");
        userEntity.setEmail("john@doe.com");
        userEntity.setEncryptedPassword("password");
    }

    @Test
    void testUserEntity_WhenValidInput_retunsStoredUser() {
        // act
        UserEntity responseEntity = testEntityManager.persistAndFlush(userEntity);

        // assert
        assertTrue(responseEntity.getId() > 0);
        assertEquals(userEntity.getUserId(), responseEntity.getUserId());
    }


    @Test
    void testUserEntity_WhenInValidInput_throwsException() {
        //input
        userEntity.setFirstName("4123412341234123709782341908372571asdfasdjjklsdjfshd");
        // act
        assertThrows(PersistenceException.class,
                () -> testEntityManager.persistAndFlush(userEntity),
                "Should throw persistence exception"
        );

    }
}