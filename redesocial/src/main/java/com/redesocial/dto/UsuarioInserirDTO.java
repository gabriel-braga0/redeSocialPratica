package com.redesocial.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record UsuarioInserirDTO(String nome,
                                String sobrenome,
                                String email,
                                String senha,
                                @JsonFormat(pattern = "dd/MM/yyyy") LocalDate dataNascimento) {
}