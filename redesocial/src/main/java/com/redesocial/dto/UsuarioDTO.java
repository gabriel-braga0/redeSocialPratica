package com.redesocial.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.redesocial.domain.Post;
import com.redesocial.domain.Usuario;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record UsuarioDTO(Long id,
                         String nome,
                         String sobrenome,
                         String email,
                         @JsonFormat(pattern = "dd/MM/yyyy") LocalDate dataNascimento) {

    public UsuarioDTO(Usuario usuario) {
       this(usuario.getId(), usuario.getNome(), usuario.getSobrenome(), usuario.getEmail(), usuario.getDataNascimento());
    }
}
