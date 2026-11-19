package br.edu.uniesp.pedidocerto.internal.pedido.service;

import br.edu.uniesp.pedidocerto.internal.pedido.dto.PainelResponse;
import br.edu.uniesp.pedidocerto.internal.pedido.dto.PedidoResponse;
import br.edu.uniesp.pedidocerto.internal.pedido.dto.PedidoResumoResponse;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.PedidoEntity;
import br.edu.uniesp.pedidocerto.internal.pedido.entity.StatusPedido;
import br.edu.uniesp.pedidocerto.internal.pedido.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Casos de uso de leitura: consulta de pedidos e painel do proprietário. */
@Service
@Transactional(readOnly = true)
public class PedidoConsultaService {

    private static final Set<StatusPedido> A_RECEBER = Set.of(
            StatusPedido.APROVADO, StatusPedido.EM_PRODUCAO, StatusPedido.PRONTO);

    private final PedidoRepository pedidos;

    public PedidoConsultaService(PedidoRepository pedidos) {
        this.pedidos = pedidos;
    }

    public PedidoResponse buscar(Long id) {
        return PedidoResponse.de(pedidos.buscarOuFalhar(id));
    }

    /** Filtros opcionais e combináveis; os resultados saem ordenados pela data de entrega. */
    public List<PedidoResumoResponse> listar(StatusPedido status, Long clienteId, boolean somenteAtrasados) {
        LocalDateTime agora = LocalDateTime.now();
        List<PedidoEntity> base = status == null
                ? pedidos.findAllByOrderByDataEntregaPrevistaAsc()
                : pedidos.findByStatusOrderByDataEntregaPrevistaAsc(status);
        return base.stream()
                .filter(p -> clienteId == null || p.getCliente().getId().equals(clienteId))
                .filter(p -> !somenteAtrasados || p.estaAtrasado(agora))
                .map(p -> PedidoResumoResponse.de(p, agora))
                .toList();
    }

    public PainelResponse painel() {
        LocalDateTime agora = LocalDateTime.now();
        List<PedidoEntity> todos = pedidos.findAll();

        Map<StatusPedido, Long> porStatus = new EnumMap<>(StatusPedido.class);
        todos.stream().collect(Collectors.groupingBy(PedidoEntity::getStatus, Collectors.counting()))
                .forEach(porStatus::put);

        BigDecimal aReceber = todos.stream()
                .filter(p -> A_RECEBER.contains(p.getStatus()))
                .map(PedidoEntity::saldoPendente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PainelResponse(
                porStatus.getOrDefault(StatusPedido.EM_PRODUCAO, 0L),
                todos.stream().filter(p -> p.estaAtrasado(agora)).count(),
                aReceber,
                todos.stream().flatMap(p -> p.getAlteracoes().stream())
                        .filter(a -> a.aguardaLeituraDaProducao()).count(),
                porStatus);
    }
}
