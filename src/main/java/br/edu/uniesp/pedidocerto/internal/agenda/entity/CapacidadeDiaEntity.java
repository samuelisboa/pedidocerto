package br.edu.uniesp.pedidocerto.internal.agenda.entity;

import br.edu.uniesp.pedidocerto.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/** Capacidade de produção de um dia, usada para só prometer datas que a equipe consegue cumprir. */
@Entity
@Getter
@Table(name = "capacidade_dia")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CapacidadeDiaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate data;

    @Column(name = "capacidade_horas", nullable = false, precision = 8, scale = 2)
    private BigDecimal capacidadeHoras;

    @Column(name = "horas_comprometidas", nullable = false, precision = 8, scale = 2)
    private BigDecimal horasComprometidas = BigDecimal.ZERO;

    public CapacidadeDiaEntity(LocalDate data, BigDecimal capacidadeHoras) {
        if (data == null) {
            throw new RegraNegocioException("A data é obrigatória.");
        }
        exigirPositivo(capacidadeHoras, "A capacidade do dia");
        this.data = data;
        this.capacidadeHoras = capacidadeHoras;
        this.horasComprometidas = BigDecimal.ZERO;
    }

    public void reservar(BigDecimal horas) {
        exigirPositivo(horas, "As horas reservadas");
        if (horas.compareTo(horasDisponiveis()) > 0) {
            throw new RegraNegocioException("O dia " + data + " não comporta mais " + horas
                    + " h: restam " + horasDisponiveis() + " h.");
        }
        this.horasComprometidas = this.horasComprometidas.add(horas);
    }

    public void liberar(BigDecimal horas) {
        exigirPositivo(horas, "As horas liberadas");
        if (horas.compareTo(horasComprometidas) > 0) {
            throw new RegraNegocioException("Não é possível liberar mais horas do que as reservadas.");
        }
        this.horasComprometidas = this.horasComprometidas.subtract(horas);
    }

    public void redefinirCapacidade(BigDecimal novaCapacidade) {
        exigirPositivo(novaCapacidade, "A capacidade do dia");
        if (novaCapacidade.compareTo(horasComprometidas) < 0) {
            throw new RegraNegocioException("A nova capacidade é menor que as horas já comprometidas.");
        }
        this.capacidadeHoras = novaCapacidade;
    }

    public BigDecimal horasDisponiveis() {
        return capacidadeHoras.subtract(horasComprometidas);
    }

    public boolean estaLotado() {
        return horasDisponiveis().signum() <= 0;
    }

    public int percentualOcupado() {
        return horasComprometidas.multiply(BigDecimal.valueOf(100))
                .divide(capacidadeHoras, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private static void exigirPositivo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() <= 0) {
            throw new RegraNegocioException(campo + " deve ser maior que zero.");
        }
    }
}
