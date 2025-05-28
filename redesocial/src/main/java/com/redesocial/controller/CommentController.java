package com.redesocial.controller;

import com.redesocial.dto.CommentDTO;
import com.redesocial.dto.CommentInserirDTO;
import com.redesocial.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/{id}")
    public ResponseEntity<CommentDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.findById(id));
    }

    @PostMapping("/{id}")
    public ResponseEntity<CommentDTO> addComment(@PathVariable Long id, @RequestBody CommentInserirDTO comment) {
        CommentDTO commentDto = commentService.save(comment, id);
        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .replacePath("/comment/{id}")
                .buildAndExpand(commentDto.id())
                .toUri();
        return ResponseEntity.created(uri).body(commentDto);
    }

}
