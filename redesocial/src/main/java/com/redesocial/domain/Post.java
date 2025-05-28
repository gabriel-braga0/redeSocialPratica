package com.redesocial.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.redesocial.dto.PostInserirDTO;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String conteudo;

    private LocalDateTime dataCriacao;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "post_curtidas", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "usuario_id"))
    private Set<Usuario> curtidas = new HashSet<>();

    @JsonManagedReference
    @ManyToOne
    @JoinColumn
    private Usuario usuario;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    public Post() {
    }

    public Post(PostInserirDTO dto, Usuario usuario) {
        this.conteudo = dto.content();
        this.dataCriacao = LocalDateTime.now();
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Set<Usuario> getCurtidas() {
        return curtidas;
    }

    public void setCurtidas(Set<Usuario> curtidas) {
        this.curtidas = curtidas;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        Post post = (Post) o;
        return Objects.equals(getId(), post.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
