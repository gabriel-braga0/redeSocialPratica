package com.redesocial.service;

import com.redesocial.domain.Comment;
import com.redesocial.domain.Usuario;
import com.redesocial.dto.CommentDTO;
import com.redesocial.dto.CommentInserirDTO;
import com.redesocial.repository.CommentRepository;
import com.redesocial.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private NotificationProducer producer;

    @Autowired
    private PostRepository postRepository;

    public CommentDTO findById(Long id) {
        Optional<Comment> comment = commentRepository.findById(id);
        return comment.map(CommentDTO::new).orElse(null);
    }

    public CommentDTO save(CommentInserirDTO commentDTO, Long idPost) {
        Usuario user = usuarioService.tokenUser();
        Comment comment = new Comment(commentDTO, user);
        comment.setPost(postRepository.findById(idPost).get());
        comment = commentRepository.save(comment);
        try {
            producer.enviarNotificacao("comment", comment.getPost().getId().toString());
        } catch (RuntimeException e) {
            System.err.println("Kafka indisponível.");
        }
        return new CommentDTO(comment);
    }
}
