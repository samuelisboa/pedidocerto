package br.edu.uniesp.pedidocerto.internal.pedido.entity;

public enum ZonaAlteracao {
    /** Produção ainda não começou: mudança livre e sem custo. */
    VERDE,
    /** Produção em andamento: mudança possível com custo e novo prazo. */
    AMARELA,
    /** Ponto sem volta: só como novo pedido. */
    VERMELHA
}
