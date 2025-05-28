package com.redesocial.service;

import com.redesocial.domain.Post;
import com.redesocial.domain.Usuario;
import com.redesocial.dto.PostDTO;
import com.redesocial.dto.PostInserirDTO;
import com.redesocial.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private NotificationProducer producer;

    public List<PostDTO> findAll() {
        Usuario user = usuarioService.tokenUser();
        return postRepository.findAll().stream().map(p -> new PostDTO(p, user)).toList();
        // postRepository.findAll().stream().map(PostDTO::new).collect(Collectors.toList());
    }

    public PostDTO findById(Long id) {
        Optional<Post> post = postRepository.findById(id);

        Usuario user = usuarioService.tokenUser();
        return post.map(p -> new PostDTO(post.get(), user)).orElse(null);

        // return post.map(PostDTO::new).orElse(null);
    }

    public Post save(PostInserirDTO postDto) {
        Usuario user = usuarioService.tokenUser();
        return postRepository.save(new Post(postDto, user));
    }

    public PostDTO like(Long id) {
        Usuario user = usuarioService.tokenUser();
        Optional<Post> post = postRepository.findById(id);
        if (post.isPresent()) {
            if (post.get().getCurtidas().contains(user)) {
                post.get().getCurtidas().remove(user);
            } else {
                post.get().getCurtidas().add(user);
                try {
                    producer.enviarNotificacao("like",
                            post.get().getId().toString());
                } catch (Exception e) {
                    System.err.println("Erro ao enviar mensagem KAFKA");
                }

            }
            postRepository.save(post.get());
        }
        return post.map(p -> new PostDTO(post.get(), user)).orElse(null);
    }
}
