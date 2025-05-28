package com.redesocial.controller;

import com.redesocial.domain.Post;
import com.redesocial.dto.CommentDTO;
import com.redesocial.dto.PostDTO;
import com.redesocial.dto.PostInserirDTO;
import com.redesocial.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping
    public ResponseEntity<List<PostDTO>> getPosts() {
        return ResponseEntity.ok(postService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPost(@PathVariable Long id) {
        if (postService.findById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(postService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PostInserirDTO> createPost(@RequestBody PostInserirDTO post) {
        Post newPost = postService.save(post);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newPost.getId())
                .toUri();
        return ResponseEntity.created(uri).body(post);
    }

    @PostMapping("/{id}")
    public ResponseEntity<PostDTO> likePost(@PathVariable Long id) {
        if (postService.like(id) == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(postService.findById(id));
    }

}
