package br.edu.uniesp.pedidocerto.shared.web;

import java.time.LocalDateTime;
import java.util.List;

public record ErroResponse(int status, String mensagem, List<String> detalhes, LocalDateTime em) {

    public static ErroResponse de(int status, String mensagem) {
        return new ErroResponse(status, mensagem, List.of(), LocalDateTime.now());
    }

    public static ErroResponse de(int status, String mensagem, List<String> detalhes) {
        return new ErroResponse(status, mensagem, detalhes, LocalDateTime.now());
    }
}
