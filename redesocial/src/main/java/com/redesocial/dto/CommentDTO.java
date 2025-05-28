package com.redesocial.dto;

import com.redesocial.domain.Comment;

import java.time.LocalDateTime;

public record CommentDTO(Long id,
                         String texto,
                         LocalDateTime dataCriacao,
                         UsuarioDTO usuario) {

    public CommentDTO(Comment comment) {
        this(comment.getId(), comment.getTexto(), comment.getData(), new UsuarioDTO(comment.getUsuario()));
    }
}
