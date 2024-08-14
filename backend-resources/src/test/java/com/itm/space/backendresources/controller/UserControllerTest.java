package com.itm.space.backendresources.controller;

import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(username = "admin", roles = "MODERATOR")
class UserControllerTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private Keycloak keycloak;

    @AfterEach
    void clear() {
        List<UserRepresentation> list =
                keycloak.realm("ITM").users().search("IvanIv");
        if (!list.isEmpty()) {
            UserRepresentation user = list.get(0);
            keycloak.realm("ITM").users().get(user.getId()).remove();
        }
    }

    @Test
    void createUserWithValidUserRequest() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                        "username": "IvanIv",
                        "email": "ivan@gmail.com",
                        "password": "pass",
                        "firstName": "Ivan",
                        "lastName": "Ivanov"
                        }
                        """);

        mvc.perform(mockHttpServletRequestBuilder).andExpect(status().isOk());
    }

    @Test
    void createUserWithInvalidUserRequest() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                        "username": "IvanIv",
                        "email": "ivan",
                        "password": "pass",
                        "firstName": "Ivan",
                        "lastName": "Ivanov"
                        }
                        """);

        mvc.perform(mockHttpServletRequestBuilder).andExpect(status().isBadRequest());
    }

    @Test
    void getUserByIdReturnValidUserResponse() throws Exception {
        UserRequest userRequest = new UserRequest(
                "username2",
                "email2@mail.ru",
                "password",
                "name",
                "lastname"
                );
        userService.createUser(userRequest);
        String testUserUUID = keycloak.realm("ITM").users().search("username2").get(0).getId();

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                MockMvcRequestBuilders.get("/api/users/" + testUserUUID);

        mvc.perform(mockHttpServletRequestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                        {
                        "firstName": "name",
                        "lastName": "lastname",
                        "email": "email2@mail.ru",
                        "roles": ["default-roles-itm"],
                        "groups": []
                        }
                        """)
                        );

        keycloak.realm("ITM").users().get(testUserUUID).remove();
    }

    @Test
    void getUserByInvalidID() throws Exception {
        String testUserUUID = String.valueOf(UUID.randomUUID());

        MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                MockMvcRequestBuilders.get("/api/users/" + testUserUUID);

        mvc.perform(mockHttpServletRequestBuilder)
                .andExpectAll(
                        status().isInternalServerError(),
                        content().contentType("text/plain;charset=UTF-8")
                );
    }

    @Test
    void hello() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                get("/api/users/hello");

        mvc.perform(mockHttpServletRequestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().string("admin")
                );
    }

    @Test
    @WithMockUser(username = "user")
    void helloWithoutAuthorization() throws Exception {
        MockHttpServletRequestBuilder mockHttpServletRequestBuilder =
                get("/api/users/hello");

        mvc.perform(mockHttpServletRequestBuilder)
                .andExpect(status().isForbidden());
    }
}
