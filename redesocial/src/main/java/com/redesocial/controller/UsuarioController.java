package com.redesocial.controller;

import com.redesocial.domain.Usuario;
import com.redesocial.dto.AuthResponse;
import com.redesocial.dto.UsuarioDTO;
import com.redesocial.dto.UsuarioInserirDTO;
import com.redesocial.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> findAll() {
        return ResponseEntity.ok(usuarioService.buscarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @GetMapping("/token")
    public ResponseEntity<AuthResponse> userToken() {
        return ResponseEntity.ok(usuarioService.usuarioLogin());
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> insert(@RequestBody UsuarioInserirDTO usuarioInserirDTO) {

        UsuarioDTO user = usuarioService.salvar(usuarioInserirDTO);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.id())
                .toUri();
        return ResponseEntity.created(uri).body(user);
    }

    @PutMapping
    public ResponseEntity<UsuarioDTO> update(@RequestBody UsuarioInserirDTO usuarioInserirDTO) {
        return ResponseEntity.ok(usuarioService.atualizar(usuarioInserirDTO));
    }

    @PostMapping("/{id}")
    public ResponseEntity<List<UsuarioDTO>> seguir(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.seguir(id));
    }

    @GetMapping("/seguindo")
    public ResponseEntity<List<UsuarioDTO>> seguindo() {
        return ResponseEntity.ok(usuarioService.seguindo());
    }

    @GetMapping("/seguidores")
    public ResponseEntity<List<UsuarioDTO>> seguidores() {
        return ResponseEntity.ok(usuarioService.seguidores());
    }

}
