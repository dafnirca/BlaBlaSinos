package br.blablasinos.validation;

import br.blablasinos.exception.UsuarioValidationException;
import br.blablasinos.model.Usuario;

public final class UsuarioValidator {

    private static final String EMAIL_PATTERN = "^[^@\\s]+@(?:edu\\.)?unisinos\\.br$";

    private UsuarioValidator() {
    }

    public static void validar(Usuario usuario) {
        if (usuario == null) {
            throw new UsuarioValidationException("Usuário não pode ser nulo.");
        }

        if (usuario.getNome() == null || usuario.getNome().isBlank()) {
            throw new UsuarioValidationException("Nome é obrigatório.");
        }

        validarEmail(usuario.getEmail());
        validarSenha(usuario.getSenha());

        if (usuario.getTipo() == null) {
            throw new UsuarioValidationException("Tipo de usuário é obrigatório.");
        }
    }

    private static void validarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new UsuarioValidationException("E-mail é obrigatório.");
        }

        String emailNormalizado = email.trim().toLowerCase();

        if (!emailNormalizado.matches(EMAIL_PATTERN)) {
            throw new UsuarioValidationException(
                "E-mail deve ser institucional com domínio @unisinos.br ou @edu.unisinos.br.");
        }
    }

    private static void validarSenha(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new UsuarioValidationException("Senha é obrigatória.");
        }

        if (senha.length() < 8) {
            throw new UsuarioValidationException(
                "Senha deve conter no mínimo 8 caracteres.");
        }

        if (!senha.matches(".*[A-Za-z].*")) {
            throw new UsuarioValidationException(
                "Senha deve conter pelo menos uma letra.");
        }

        if (!senha.matches(".*\\d.*")) {
            throw new UsuarioValidationException(
                "Senha deve conter pelo menos um número.");
        }
    }
}
