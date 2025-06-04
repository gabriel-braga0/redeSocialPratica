package com.redesocial.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redesocial.domain.Usuario;
import com.redesocial.dto.AuthResponse;
import com.redesocial.repository.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.ArrayList;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private AuthenticationManager authenticationManager;
	private JwtUtil jwtUtil;
	private UsuarioRepository repository;

	public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
			UsuarioRepository repository) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.repository = repository;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		try {
			LoginDTO login = new ObjectMapper().readValue(request.getInputStream(), LoginDTO.class);
			UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(login.getEmail(),
					login.getPassword(), new ArrayList<>());
			Authentication auth = authenticationManager.authenticate(authToken);
			return auth;
		} catch (IOException e) {
			throw new RuntimeException("Falha ao autenticar usuario", e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		String email = ((UserDetails) authResult.getPrincipal()).getUsername();
		String token = jwtUtil.generateToken(email);
		Usuario usuario = repository.findByEmail(email)
				.orElseThrow(() -> new IllegalStateException(
						"Usuário OAuth2 não encontrado no banco após processamento: " + email));
		response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
		response.addHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.AUTHORIZATION);
		response.getWriter()
				.write(String.format("{\"token\": \"%s\", \"id\": %d, \"nome\": \"%s\", \"email\": \"%s\"}",
						token, usuario.getId(), usuario.getNome(), usuario.getEmail()));
		response.getWriter().flush();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

	}

}
