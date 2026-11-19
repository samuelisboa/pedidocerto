package br.edu.uniesp.pedidocerto.internal.funcionario.service;

import br.edu.uniesp.pedidocerto.internal.funcionario.dto.FuncionarioRequest;
import br.edu.uniesp.pedidocerto.internal.funcionario.dto.FuncionarioResponse;
import br.edu.uniesp.pedidocerto.internal.funcionario.entity.FuncionarioEntity;
import br.edu.uniesp.pedidocerto.internal.funcionario.repository.FuncionarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FuncionarioService {

    private final FuncionarioRepository funcionarios;

    public FuncionarioService(FuncionarioRepository funcionarios) {
        this.funcionarios = funcionarios;
    }

    public FuncionarioResponse criar(FuncionarioRequest request) {
        return FuncionarioResponse.de(funcionarios.save(new FuncionarioEntity(request.nome(), request.setor())));
    }

    @Transactional(readOnly = true)
    public FuncionarioResponse buscar(Long id) {
        return FuncionarioResponse.de(funcionarios.buscarOuFalhar(id));
    }

    @Transactional(readOnly = true)
    public List<FuncionarioResponse> listar() {
        return funcionarios.findAll().stream().map(FuncionarioResponse::de).toList();
    }

    public FuncionarioResponse inativar(Long id) {
        FuncionarioEntity funcionario = funcionarios.buscarOuFalhar(id);
        funcionario.inativar();
        return FuncionarioResponse.de(funcionario);
    }
}
