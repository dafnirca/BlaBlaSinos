const form = document.getElementById("cadastro-form");

form.addEventListener("submit", function(event) {

    event.preventDefault();

    const nome = document.getElementById("nome").value;

    const email = document.getElementById("email").value;

    const senha = document.getElementById("senha").value;

    const confirmarSenha = document.getElementById("confirmarSenha").value;

    if (senha !== confirmarSenha) {

        alert("As senhas não coincidem.");

        return;
    }

    console.log("Cadastro enviado:");

    console.log({
        nome,
        email,
        senha
    });

    alert("Cadastro enviado!");
    // Redireciona para a tela de login após cadastro
    window.location.href = "login.html";
});