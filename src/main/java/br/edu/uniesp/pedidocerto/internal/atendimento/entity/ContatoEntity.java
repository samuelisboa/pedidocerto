package br.edu.uniesp.pedidocerto.internal.atendimento.entity;

import br.edu.uniesp.pedidocerto.internal.cliente.entity.CanalContato;
import br.edu.uniesp.pedidocerto.internal.cliente.entity.ClienteEntity;
import br.edu.uniesp.pedidocerto.internal.funcionario.entity.FuncionarioEntity;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.PedidoEntity;
import br.edu.uniesp.pedidocerto.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Mensagem recebida de um cliente, inclusive fora do horário de atendimento. */
@Entity
@Getter
@Table(name = "contatos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContatoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteEntity cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private PedidoEntity pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalContato canal;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Column(name = "recebido_em", nullable = false)
    private LocalDateTime recebidoEm;

    @Column(name = "fora_do_horario", nullable = false)
    private boolean foraDoHorario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tratado_por_id")
    private FuncionarioEntity tratadoPor;

    @Column(name = "tratado_em")
    private LocalDateTime tratadoEm;

    public ContatoEntity(ClienteEntity cliente, CanalContato canal, String mensagem,
                         LocalDateTime recebidoEm, boolean foraDoHorario) {
        if (cliente == null || canal == null) {
            throw new RegraNegocioException("Cliente e canal são obrigatórios.");
        }
        if (mensagem == null || mensagem.isBlank()) {
            throw new RegraNegocioException("A mensagem do contato é obrigatória.");
        }
        this.cliente = cliente;
        this.canal = canal;
        this.mensagem = mensagem;
        this.recebidoEm = recebidoEm;
        this.foraDoHorario = foraDoHorario;
    }

    public void vincularAoPedido(PedidoEntity pedido) {
        if (pedido == null) {
            throw new RegraNegocioException("O pedido é obrigatório.");
        }
        if (!pedido.getCliente().equals(cliente)) {
            throw new RegraNegocioException("O pedido pertence a outro cliente.");
        }
        this.pedido = pedido;
    }

    public void marcarComoTratado(FuncionarioEntity funcionario, LocalDateTime quando) {
        if (tratadoEm != null) {
            throw new RegraNegocioException("Este contato já foi tratado.");
        }
        if (funcionario == null || !funcionario.isAtivo()) {
            throw new RegraNegocioException("O contato deve ser tratado por um funcionário ativo.");
        }
        this.tratadoPor = funcionario;
        this.tratadoEm = quando;
    }

    public boolean estaPendente() {
        return tratadoEm == null;
    }
}
