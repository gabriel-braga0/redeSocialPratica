package com.redesocial.service;

import com.redesocial.domain.Usuario;
import com.redesocial.dto.AuthResponse;
import com.redesocial.dto.UsuarioDTO;
import com.redesocial.dto.UsuarioInserirDTO;
import com.redesocial.repository.UsuarioRepository;
import com.redesocial.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private JwtUtil jwtUtil;

    public Usuario tokenUser(){
        String token = request.getHeader("Authorization");
        token = token.substring(7);
        Optional<Usuario> usuario = usuarioRepository.findByEmail(jwtUtil.getUserName(token));
        return usuario.orElse(null);
    }

    public AuthResponse usuarioLogin(){
        if(tokenUser()==null){
            return null;
        }
        Usuario user = tokenUser();
        return new AuthResponse(user.getId(),user.getNome(), user.getEmail());
    }

    public UsuarioDTO salvar(UsuarioInserirDTO usuarioInserirDTO) {
        Usuario usuario = new Usuario(usuarioInserirDTO);
        usuario.setSenha(encoder.encode(usuario.getSenha()));
        usuarioRepository.save(usuario);
        return new UsuarioDTO(usuario);
    }

    public UsuarioDTO buscarPorId(Long id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        return usuario.map(UsuarioDTO::new).orElse(null);
    }

    public List<UsuarioDTO> buscarTodos(){
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios.stream().map(UsuarioDTO::new).collect(Collectors.toList());
    }


    public UsuarioDTO atualizar(UsuarioInserirDTO usuarioDTO) {
        Usuario usuario = tokenUser();
        usuario.setNome(usuarioDTO.nome());
        usuario.setSobrenome(usuarioDTO.sobrenome());
        usuario.setEmail(usuarioDTO.email());
        usuario.setSenha(encoder.encode(usuarioDTO.senha()));
        usuario.setDataNascimento(usuarioDTO.dataNascimento());
        usuarioRepository.save(usuario);
        return new UsuarioDTO(usuario);
    }

    public void remover() {
        Usuario usuario = tokenUser();
        usuarioRepository.deleteById(usuario.getId());
    }

    public List<UsuarioDTO> seguir(Long id) {
        Usuario usuario = tokenUser();
        Optional<Usuario> usuarioExistente = usuarioRepository.findById(id);
        if(usuarioExistente.isEmpty()) {
            return null;
        }
        usuario.getSeguindo().add(usuarioExistente.get());
        usuarioRepository.save(usuario);
        return usuario.getSeguindo().stream().map(UsuarioDTO::new).collect(Collectors.toList());
    }

    public List<UsuarioDTO> seguidores(){
        Usuario usuario = tokenUser();
        return usuario.getSeguidores().stream().map(UsuarioDTO::new).collect(Collectors.toList());
    }

    public List<UsuarioDTO> seguindo(){
        Usuario usuario = tokenUser();
        return usuario.getSeguindo().stream().map(UsuarioDTO::new).collect(Collectors.toList());
    }
}
