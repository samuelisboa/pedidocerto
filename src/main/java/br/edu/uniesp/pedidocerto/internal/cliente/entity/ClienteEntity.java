package br.edu.uniesp.pedidocerto.internal.cliente.entity;

import br.edu.uniesp.pedidocerto.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "clientes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 20)
    private String telefone;

    @Column(length = 120)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_preferencial", nullable = false, length = 20)
    private CanalContato canalPreferencial;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    public ClienteEntity(String nome, String telefone, String email, CanalContato canalPreferencial) {
        exigirNome(nome);
        exigirContato(telefone, email);
        if (canalPreferencial == null) {
            throw new RegraNegocioException("O canal preferencial do cliente é obrigatório.");
        }
        this.nome = nome.trim();
        this.telefone = telefone;
        this.email = email;
        this.canalPreferencial = canalPreferencial;
        this.ativo = true;
        this.criadoEm = LocalDateTime.now();
    }

    public void atualizarContato(String telefone, String email) {
        exigirContato(telefone, email);
        this.telefone = telefone;
        this.email = email;
    }

    public void alterarCanalPreferencial(CanalContato canal) {
        if (canal == null) {
            throw new RegraNegocioException("O canal preferencial do cliente é obrigatório.");
        }
        this.canalPreferencial = canal;
    }

    public void inativar() {
        this.ativo = false;
    }

    public void reativar() {
        this.ativo = true;
    }

    private static void exigirNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome do cliente é obrigatório.");
        }
    }

    private static void exigirContato(String telefone, String email) {
        boolean semTelefone = telefone == null || telefone.isBlank();
        boolean semEmail = email == null || email.isBlank();
        if (semTelefone && semEmail) {
            throw new RegraNegocioException("Informe ao menos um telefone ou e-mail do cliente.");
        }
    }
}
