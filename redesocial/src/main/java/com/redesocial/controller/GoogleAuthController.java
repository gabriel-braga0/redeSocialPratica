package com.redesocial.controller; // Ou o teu pacote de controllers

import com.redesocial.domain.Usuario;
import com.redesocial.repository.UsuarioRepository;
import com.redesocial.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth/google") // Define o caminho base para este controller
public class GoogleAuthController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    // Este redirect-uri deve ser o mesmo que o teu frontend (Next.js) usa
    // ao iniciar o fluxo com o Google e o que está configurado no Google Console.
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    // DTO para receber o código do frontend
    static class CodeRequestBody {
        private String code;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    // DTO para a resposta de autenticação (consistente com o teu login tradicional)
    static class AuthResponse {
        public String token;
        public Long id; // Ou o tipo do teu Usuario.id
        public String nome;
        public String email;
        // Adiciona outros campos se o teu UserProfile/API de login os tiver (ex:
        // sobrenome)

        public AuthResponse(String token, Long id, String nome, String email) {
            this.token = token;
            this.id = id;
            this.nome = nome;
            this.email = email;
        }
    }

    @PostMapping("/exchange-code") // Endpoint: /api/auth/google/exchange-code
    public ResponseEntity<?> exchangeCodeForToken(@RequestBody CodeRequestBody codeRequestBody) {
        String authorizationCode = codeRequestBody.getCode();
        if (authorizationCode == null || authorizationCode.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Authorization code is missing"));
        }

        try {
            // 1. Trocar o código de autorização por tokens do Google
            String tokenUri = "https://oauth2.googleapis.com/token";
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", authorizationCode);
            params.add("client_id", googleClientId);
            params.add("client_secret", googleClientSecret);
            params.add("redirect_uri", googleRedirectUri); // Crucial: deve ser o mesmo URI de redirecionamento
            params.add("grant_type", "authorization_code");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

            ResponseEntity<Map> googleTokenResponse = restTemplate.postForEntity(tokenUri, requestEntity, Map.class);

            if (!googleTokenResponse.getStatusCode().is2xxSuccessful() || googleTokenResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Erro ao trocar código com Google", "details",
                                googleTokenResponse.getBody()));
            }

            Map<String, Object> googleTokens = googleTokenResponse.getBody();
            String accessTokenGoogle = (String) googleTokens.get("access_token");
            // String idTokenString = (String) googleTokens.get("id_token"); // Podes querer
            // validar este também

            if (accessTokenGoogle == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Access token do Google não recebido"));
            }

            // 2. Usar o access_token para obter informações do perfil do utilizador do
            // Google
            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.setBearerAuth(accessTokenGoogle);
            HttpEntity<Void> userInfoRequestEntity = new HttpEntity<>(userInfoHeaders);
            ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v3/userinfo",
                    HttpMethod.GET,
                    userInfoRequestEntity,
                    Map.class);

            if (!userInfoResponse.getStatusCode().is2xxSuccessful() || userInfoResponse.getBody() == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Erro ao buscar informações do usuário no Google"));
            }

            Map<String, Object> userInfo = userInfoResponse.getBody();
            String email = (String) userInfo.get("email");
            String nomeGoogle = (String) userInfo.get("given_name");
            String sobrenomeGoogle = (String) userInfo.get("family_name");
            // String pictureUrl = (String) userInfo.get("picture"); // Se quiseres usar

            if (email == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Email não retornado pelo Google"));
            }

            // 3. Lógica para encontrar/criar utilizador no teu DB (baseado no teu
            // oauth2UserService)
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            Usuario usuario;
            if (usuarioOpt.isEmpty()) {
                usuario = new Usuario();
                usuario.setEmail(email);
                usuario.setNome(nomeGoogle != null ? nomeGoogle : email.split("@")[0]); // Fallback para nome
                if (sobrenomeGoogle != null)
                    usuario.setSobrenome(sobrenomeGoogle);
                // Define uma senha aleatória e forte, pois o utilizador não usará senha para
                // este login
                usuario.setSenha(bCryptPasswordEncoder.encode(UUID.randomUUID().toString()));
                // Define outros campos padrão se necessário
            } else {
                usuario = usuarioOpt.get();
                // Atualiza informações se desejar (ex: nome pode mudar no Google)
                usuario.setNome(nomeGoogle != null ? nomeGoogle : usuario.getNome());
                if (sobrenomeGoogle != null)
                    usuario.setSobrenome(sobrenomeGoogle);
                // Garante que o utilizador tem uma senha, mesmo que logue com Google
                if (usuario.getSenha() == null || usuario.getSenha().isEmpty()) {
                    usuario.setSenha(bCryptPasswordEncoder.encode(UUID.randomUUID().toString()));
                }
            }
            usuario = usuarioRepository.save(usuario);

            // 4. Gera o teu token JWT da aplicação
            String appToken = jwtUtil.generateToken(usuario.getEmail());

            // 5. Retorna a resposta para o frontend
            AuthResponse authResponse = new AuthResponse(appToken, usuario.getId(), usuario.getNome(),
                    usuario.getEmail());
            return ResponseEntity.ok(authResponse);

        } catch (HttpClientErrorException e) {
            // Erro específico do cliente HTTP (ex: 4xx do Google)
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", "Erro na comunicação com o Google", "details", e.getResponseBodyAsString()));
        } catch (Exception e) {
            // Outros erros
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno no servidor", "message", e.getMessage()));
        }
    }
}