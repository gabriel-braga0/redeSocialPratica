package com.redesocial.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.redesocial.dto.UsuarioInserirDTO;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.*;

@Entity
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String sobrenome;

    private String email;

    private String senha;

    private LocalDate dataNascimento;

    @JsonBackReference
    @OneToMany(mappedBy = "usuario")
    private List<Post> postagens = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "usuario_seguidores", // Nome da tabela de junção
            joinColumns = @JoinColumn(name = "usuario_id"), // Coluna que referencia o usuário que segue
            inverseJoinColumns = @JoinColumn(name = "seguidor_id") // Coluna que referencia o seguidor
    )
    private Set<Usuario> seguindo = new HashSet<>(); // Usuários que este usuário segue

    @ManyToMany(mappedBy = "seguindo")
    private Set<Usuario> seguidores = new HashSet<>();

    public Usuario() {}

    public Usuario(UsuarioInserirDTO usuario) {
        this.nome = usuario.nome();
        this.sobrenome = usuario.sobrenome();
        this.email = usuario.email();
        this.senha = usuario.senha();
        this.dataNascimento = usuario.dataNascimento();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(getId(), usuario.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public Long getId() {
        return id;
    }

    public Set<Usuario> getSeguindo() {
        return seguindo;
    }

    public void setSeguindo(Set<Usuario> seguindo) {
        this.seguindo = seguindo;
    }

    public Set<Usuario> getSeguidores() {
        return seguidores;
    }

    public void setSeguidores(Set<Usuario> seguidores) {
        this.seguidores = seguidores;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Post> getPostagens() {
        return postagens;
    }

    public void setPostagens(List<Post> postagens) {
        this.postagens = postagens;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSobrenome() {
        return sobrenome;
    }

    public void setSobrenome(String sobrenome) {
        this.sobrenome = sobrenome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
