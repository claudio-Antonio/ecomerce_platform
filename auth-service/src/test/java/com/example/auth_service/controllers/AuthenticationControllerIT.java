package com.example.auth_service.controllers;

import com.example.auth_service.IntegrationTestBase;
import com.example.auth_service.controllers.dtos.AuthenticationDTO;
import com.example.auth_service.controllers.dtos.RegisterDTO;
import com.example.auth_service.domain.enums.Role;
import com.example.auth_service.repositories.jpa.UserRepository;
import com.example.auth_service.repositories.redis.BlacklistRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationControllerIT extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private BlacklistRepository blacklistRepository;
    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        blacklistRepository.deleteAll();
    }

    // ---- REGISTER ----

    @Test
    void register_comDadosValidos_deveRetornar200ECriarUsuario() throws Exception {
        mockMvc.perform(buildRegisterRequest("João", "joao@email.com", "123456", Role.CUSTOMER))
                .andExpect(status().isOk());

        assertNotNull(userRepository.findByEmail("joao@email.com"));
    }

    @Test
    void register_comEmailDuplicado_deveRetornar400SemCriarDuplicata() throws Exception {
        mockMvc.perform(buildRegisterRequest("João", "joao@email.com", "123456", Role.CUSTOMER));
        mockMvc.perform(buildRegisterRequest("João", "joao@email.com", "123456", Role.CUSTOMER))
                .andExpect(status().isBadRequest());

        assertEquals(1, userRepository.count());
    }

    // ---- LOGIN ----

    @Test
    void login_comCredenciaisValidas_deveRetornarToken() throws Exception {
        mockMvc.perform(buildRegisterRequest("João", "joao@email.com", "123456", Role.CUSTOMER));

        mockMvc.perform(buildLoginRequest("joao@email.com", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_comSenhaErrada_deveRetornar403() throws Exception {
        mockMvc.perform(buildRegisterRequest("João", "joao@email.com", "123456", Role.CUSTOMER));

        mockMvc.perform(buildLoginRequest("joao@email.com", "senha-errada"))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_comEmailInexistente_deveRetornar403() throws Exception {
        mockMvc.perform(buildLoginRequest("naoexiste@email.com", "123456"))
                .andExpect(status().isForbidden());
    }

    // ---- LOGOUT ----

    @Test
    void logout_comTokenValido_deveRetornar204EAdicionarNaBlacklist() throws Exception {
        String token = registerAndLogin(mockMvc, objectMapper, "João", "joao@email.com", "123456", Role.CUSTOMER);

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isNoContent());

        assertEquals(1, blacklistRepository.count());
    }

    @Test
    void logout_comTokenRevogado_deveBloquearProximoRequest() throws Exception {
        String token = registerAndLogin(mockMvc, objectMapper, "João", "joao@email.com", "123456", Role.CUSTOMER);

        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", bearerHeader(token)));

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", bearerHeader(token)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_semHeader_deveRetornar400() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isBadRequest());
    }

    // ---- UTILITARIOS ----

    private MockHttpServletRequestBuilder buildRegisterRequest(String name,
                                                               String email,
                                                               String password,
                                                               Role role) throws Exception {
        return post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new RegisterDTO(name, email, password, role)
                ));
    }

    private MockHttpServletRequestBuilder buildLoginRequest(String email,
                                                            String password) throws Exception {
        return post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new AuthenticationDTO(email, password)
                ));
    }
}