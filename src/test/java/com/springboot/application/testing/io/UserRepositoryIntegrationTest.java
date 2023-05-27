package com.springboot.application.testing.io;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
// this @DataJpaTest will disable auto configuration
// and it will create application context only for JPA related components because we are only
// dealing with JPA components
//Also this annotation will make our test methods transactional and will rollback when completes
// By default, it will use embedded in-memory database for our tests
class UserRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private UsersRepository usersRepository;

    private UserEntity storedEntity;

    @BeforeEach
    public void setupMethod() {
        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName("John");
        userEntity.setUserId(UUID.randomUUID().toString());
        userEntity.setLastName("Doe");
        userEntity.setEmail("john@doe.com");
        userEntity.setEncryptedPassword("password");
        storedEntity = testEntityManager.persistAndFlush(userEntity);
    }


    @Test
    void testFindByEmail_WhenValidInput_returnsCorrectUserDetails() {

        UserEntity responseEntity = usersRepository.findByEmail("john@doe.com");

        assertEquals(storedEntity.getEmail(), responseEntity.getEmail());
        assertEquals(storedEntity.getUserId(), responseEntity.getUserId());
        assertEquals(storedEntity.getId(), responseEntity.getId());

    }

    @Test
    void testJpqlQueryMethod_WhenValidInput_retunsListOfUserDetails() {

        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName("Jane");
        userEntity.setUserId(UUID.randomUUID().toString());
        userEntity.setLastName("Doe");
        userEntity.setEmail("jane@doe.com");
        userEntity.setEncryptedPassword("password");
        testEntityManager.persistAndFlush(userEntity);
        List<UserEntity> responseList = usersRepository.findUsersWithEmailEndingWith("doe.com");
        assertTrue(responseList.size() > 1);
        responseList.forEach(responseEntity -> {
            assertNotNull(responseEntity);
            assertTrue(responseEntity.getEmail().endsWith("doe.com"));
        });
    }
}