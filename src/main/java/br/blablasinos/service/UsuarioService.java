package br.blablasinos.service;

import java.util.Optional;

import br.blablasinos.exception.UsuarioValidationException;
import br.blablasinos.model.TipoUsuario; // << IMPORT ADICIONADO
import br.blablasinos.model.Usuario;
import br.blablasinos.repository.SqliteUsuarioRepository;
import br.blablasinos.repository.UsuarioRepository;
import br.blablasinos.validation.UsuarioValidator;

public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService() {
        this(new SqliteUsuarioRepository());
    }

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public Usuario cadastrar(Usuario usuario) {
        UsuarioValidator.validar(usuario);

        String emailNormalizado = usuario.getEmail().trim().toLowerCase();
        usuario.setEmail(emailNormalizado);
        usuario.setNome(usuario.getNome().trim());

        if (repository.existsByEmail(emailNormalizado)) {
            throw new UsuarioValidationException("O e-mail já está cadastrado.");
        }

        return repository.salvar(usuario);
    }

    public void atualizarPerfil(Usuario usuarioComDadosNovos) throws UsuarioValidationException {
        if (usuarioComDadosNovos.getId() == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo para uma atualização.");
        }

        // 1. Busca o usuário completo que já existe no banco de dados.
        Usuario usuarioExistente = repository.buscarPorId(usuarioComDadosNovos.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado para atualização."));

        // 2. Atualiza os dados do objeto existente com as informações que vieram do formulário.
        usuarioExistente.setNome(usuarioComDadosNovos.getNome());
        usuarioExistente.setCnh(usuarioComDadosNovos.getCnh());
        usuarioExistente.setModeloVeiculo(usuarioComDadosNovos.getModeloVeiculo());
        usuarioExistente.setCorVeiculo(usuarioComDadosNovos.getCorVeiculo());
        usuarioExistente.setPlacaVeiculo(usuarioComDadosNovos.getPlacaVeiculo());

        // 3. APLICA A REGRA DE NEGÓCIO: Se o usuário preencheu os dados mínimos, ele vira motorista.
        boolean seTornouMotorista = usuarioComDadosNovos.getCnh() != null && !usuarioComDadosNovos.getCnh().isBlank() &&
                                     usuarioComDadosNovos.getPlacaVeiculo() != null && !usuarioComDadosNovos.getPlacaVeiculo().isBlank();

        if (seTornouMotorista) {
            usuarioExistente.setTipo(TipoUsuario.MOTORISTA);
        }
        
        // 4. Salva o objeto completo e atualizado de volta no banco.
        repository.update(usuarioExistente);
    }

    public Optional<Usuario> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo.");
        }
        return repository.buscarPorId(id);
    }
}