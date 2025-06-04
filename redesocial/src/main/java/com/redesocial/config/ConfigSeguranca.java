package com.redesocial.config;

import com.redesocial.domain.Usuario;
import com.redesocial.repository.UsuarioRepository;
import com.redesocial.security.JwtAuthenticationFilter;
import com.redesocial.security.JwtAuthorizationFilter;
import com.redesocial.security.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Optional;

@Configuration
@EnableWebSecurity
public class ConfigSeguranca {

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http,
			AuthenticationManager authenticationManager) throws Exception {

		JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtUtil,
				usuarioRepository);
		jwtAuthenticationFilter.setFilterProcessesUrl("/api/auth/login");

		JwtAuthorizationFilter jwtAuthorizationFilter = new JwtAuthorizationFilter(authenticationManager, jwtUtil,
				userDetailsService);

		http.csrf(csrf -> csrf.disable())
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/api/auth/login").permitAll()
						.requestMatchers(HttpMethod.POST, "/user").permitAll()
						.requestMatchers("/login/oauth2/code/**", "/oauth2/**").permitAll()
						.requestMatchers("/api/auth/google/exchange-code").permitAll()
						.requestMatchers("/user/**").authenticated()
						.requestMatchers("/post/**").authenticated()
						.requestMatchers("/comment/**").authenticated()
						.anyRequest().permitAll())
				.oauth2Login(oauth2 -> oauth2
						.userInfoEndpoint(userInfo -> userInfo
								.userService(oauth2UserService()))
						.successHandler(oauth2AuthenticationSuccessHandler())

				)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //
				.exceptionHandling(exceptionHandling -> exceptionHandling
						.authenticationEntryPoint((request, response, authException) -> {
							response.setContentType("application/json;charset=UTF-8");
							response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
							response.getWriter()
									.write("{\"status\": 401, \"error\": \"Não autorizado\", \"message\": \""
											+ authException.getMessage() + "\", \"path\": \"" + request.getRequestURI()
											+ "\"}");
						}))
				.addFilter(jwtAuthenticationFilter) //
				.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class); //

		return http.build();
	}

	@Bean
	public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
		DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
		return request -> {
			OAuth2User oauth2User = delegate.loadUser(request);
			String email = oauth2User.getAttribute("email");
			if (email != null) {
				Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
				if (usuarioOpt.isEmpty()) {
					Usuario novoUsuario = new Usuario();
					novoUsuario.setEmail(email);
					novoUsuario.setNome(oauth2User.getAttribute("given_name"));
					novoUsuario.setSobrenome(oauth2User.getAttribute("family_name"));
					novoUsuario.setSenha(bCryptPasswordEncoder().encode(java.util.UUID.randomUUID().toString()));
					usuarioRepository.save(novoUsuario);
				} else {
					Usuario usuarioExistente = usuarioOpt.get();
					usuarioExistente.setNome(oauth2User.getAttribute("given_name"));
					usuarioExistente.setSobrenome(oauth2User.getAttribute("family_name"));
					if (usuarioExistente.getSenha() == null || usuarioExistente.getSenha().isEmpty()) {
						usuarioExistente
								.setSenha(bCryptPasswordEncoder().encode(java.util.UUID.randomUUID().toString()));
					}
					usuarioRepository.save(usuarioExistente);
				}
			}
			return oauth2User;
		};
	}

	@Bean
	public AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler() {
		return (request, response, authentication) -> {
			OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
			String email = oauth2User.getAttribute("email");

			Usuario usuario = usuarioRepository.findByEmail(email)
					.orElseThrow(() -> new IllegalStateException(
							"Usuário OAuth2 não encontrado no banco após processamento: " + email));

			String token = jwtUtil.generateToken(usuario.getEmail());
			response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
			response.addHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.AUTHORIZATION);

			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter()
					.write(String.format("{\"token\": \"%s\", \"id\": %d, \"nome\": \"%s\", \"email\": \"%s\"}",
							token, usuario.getId(), usuario.getNome(), usuario.getEmail()));
			response.getWriter().flush();
		};
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration corsConfiguration = new CorsConfiguration();
		corsConfiguration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
		corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "PUT", "OPTIONS"));
		corsConfiguration.setAllowedHeaders(
				Arrays.asList(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT));
		corsConfiguration
				.setExposedHeaders(Arrays.asList(HttpHeaders.AUTHORIZATION, HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS));
		corsConfiguration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", corsConfiguration);
		return source;
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}