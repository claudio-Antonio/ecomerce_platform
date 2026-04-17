package com.example.auth_service.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.auth_service.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {
    @Value("${api.security.token.secret}")
    private String secret;

    /* generateToken() recebe um user para ver se tem a role necessaria -> a sessao de autenticacao eh STATELESS, logo, as informacoes de roles ficam no token ->
    * Primeiro no try-catch criamos um Algorithm de hash para criar o token, esse obj recebe por parametro uma secret que nos criamos para o token ser unico ->
    * Para criar o token precisa da classe JWT.create() que vem com uma serie de mecanismos para produzi-lo.*/
    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(user.getEmail())
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);

            return token;
        }
        catch (JWTCreationException e) {
            throw new RuntimeException("Error while generating token. " + e);
        }
    }
    /* validateToken(token) recebe o token e caso seja valido retorna um usuario -> Primeiro eh necessario denovo criar um Algorithm com o mesmo tipo de hash
    * que o da criacao e que receba a mesma secret -> na validacao se recebe o algorithm, depois o issuer, depois faz o build do verify que recebe o token e
    * caso esteja tudo autenticado e nao expirado retorna o user com .getSubject().*/
    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token)
                    .getSubject();
        }
        catch (JWTVerificationException e) {
            throw new RuntimeException("Error while validating token. " + e);
        }
    }

    private Instant genExpirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}
