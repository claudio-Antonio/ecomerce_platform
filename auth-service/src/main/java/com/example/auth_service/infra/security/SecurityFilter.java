package com.example.auth_service.infra.security;

import com.example.auth_service.domain.User;
import com.example.auth_service.infra.redis.TokenBlackListService;
import com.example.auth_service.infra.redis.UserCacheService;
import com.example.auth_service.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final TokenBlackListService blackListService;
    private final UserCacheService userCacheService;

    /* Esse metodo vai rodar a cada request -> primeiro voce pega da request o token e comeca a trabalhar encima disso -> Se token == null, o filterChain.doFilter
    * chama o proximo filtro. -> Senao, pega o subject retornado por validateToken(token) e busca com o userRepository o usuario com base no email ->
    * Depois chama o UsernamePasswordAuthenticationToken e passa como paramentro o  user, credenciais e role para fazer a parte de autorizacao nas urls ->
    * Por fim salva esse contexto na parte de autenticacao do spring security */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = this.recovertoken(request);

        if(token != null) {
            var subject = tokenService.validateToken(token);

            if(subject != null) {
                // Primeiro checa blacklist
                String jti = tokenService.extractJti(token);
                if(jti != null && blackListService.isRevoked(jti)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                // Segundo tenta cache antes de ir ao banco
                UserDetails user = userCacheService.getFromCache(subject).orElseGet(() -> {
                            User dbUser = (User) userRepository.findByEmail(subject);
                            if(dbUser != null) userCacheService.cacheUser(dbUser);
                            return dbUser;
                        });

                if(user != null) {
                    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    /* metodo que verifica se na request tem um token -> primeiro verifica se tem o header "Authorization" -> se tiver, pega a string "Bearer " que tem em
    * todo token JWT e substitui por vazio para assim poder chamar o metodo validateToken(token_formatado)*/
    private String recovertoken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if(authHeader == null) return null;
        String token = authHeader.replace("Bearer ", "");
        return token;
    }
}
