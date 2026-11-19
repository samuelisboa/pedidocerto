package br.edu.uniesp.pedidocerto.shared.exception;

/** Violação de uma regra do domínio (ex.: entregar pedido com saldo pendente). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
