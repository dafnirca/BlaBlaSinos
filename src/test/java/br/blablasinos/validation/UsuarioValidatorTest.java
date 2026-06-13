package br.blablasinos.validation;

import br.blablasinos.exception.UsuarioValidationException;
import br.blablasinos.model.TipoUsuario;
import br.blablasinos.model.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UsuarioValidatorTest {

    @Test
    public void deveValidarUsuarioValido() {
        Usuario usuario = new Usuario(
            1L,
            "Ana Silva",
            "ana.silva@edu.unisinos.br",
            "Senha123",
            TipoUsuario.PASSAGEIRO
        );

        assertDoesNotThrow(() -> UsuarioValidator.validar(usuario));
    }

    @Test
    public void deveReprovarEmailForaDoDominioInstitucional() {
        Usuario usuario = new Usuario(
            null,
            "Pedro",
            "pedro@gmail.com",
            "Senha123",
            TipoUsuario.MOTORISTA
        );

        UsuarioValidationException exception = assertThrows(
            UsuarioValidationException.class,
            () -> UsuarioValidator.validar(usuario)
        );

        assertEquals("E-mail deve ser institucional com domínio @unisinos.br ou @edu.unisinos.br.", exception.getMessage());
    }

    @Test
    public void deveReprovarSenhaCurta() {
        Usuario usuario = new Usuario(
            null,
            "Carla",
            "carla@edu.unisinos.br",
            "S1mpla",
            TipoUsuario.PASSAGEIRO
        );

        UsuarioValidationException exception = assertThrows(
            UsuarioValidationException.class,
            () -> UsuarioValidator.validar(usuario)
        );

        assertEquals("Senha deve conter no mínimo 8 caracteres.", exception.getMessage());
    }

    @Test
    public void deveReprovarSenhaSemNumero() {
        Usuario usuario = new Usuario(
            null,
            "André",
            "andre@edu.unisinos.br",
            "SenhaSemNumero",
            TipoUsuario.MOTORISTA
        );

        UsuarioValidationException exception = assertThrows(
            UsuarioValidationException.class,
            () -> UsuarioValidator.validar(usuario)
        );

        assertEquals("Senha deve conter pelo menos um número.", exception.getMessage());
    }

    @Test
    public void deveReprovarSenhaSemLetra() {
        Usuario usuario = new Usuario(
            null,
            "Mariana",
            "mariana@edu.unisinos.br",
            "12345678",
            TipoUsuario.PASSAGEIRO
        );

        UsuarioValidationException exception = assertThrows(
            UsuarioValidationException.class,
            () -> UsuarioValidator.validar(usuario)
        );

        assertEquals("Senha deve conter pelo menos uma letra.", exception.getMessage());
    }
}
