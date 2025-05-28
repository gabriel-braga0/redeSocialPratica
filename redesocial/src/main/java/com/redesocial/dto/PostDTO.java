package com.redesocial.dto;

import com.redesocial.domain.Post;
import com.redesocial.domain.Usuario;

import java.time.LocalDateTime;
import java.util.List;

public record PostDTO(Long id,
        String conteudo,
        LocalDateTime dataCriacao,
        UsuarioDTO usuario,
        List<CommentDTO> comentarios,
        List<UsuarioDTO> curtidas,
        Boolean isLikedByUser) {

    public PostDTO(Post post, Usuario user) {
        this(post.getId(), post.getConteudo(), post.getDataCriacao(), new UsuarioDTO(post.getUsuario()),
                post.getComments().stream().map(CommentDTO::new).toList(),
                post.getCurtidas().stream().map(UsuarioDTO::new).toList(), post.getCurtidas().contains(user));
    }

}
