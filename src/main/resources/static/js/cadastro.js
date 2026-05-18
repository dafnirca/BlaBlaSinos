const form = document.getElementById("cadastro-form");

form.addEventListener("submit", async function(event) {

    event.preventDefault();

    const nome = document.getElementById("nome").value;

    const email = document.getElementById("email").value;

    const senha = document.getElementById("senha").value;

    const confirmarSenha = document.getElementById("confirmarSenha").value;

    if (senha !== confirmarSenha) {

        alert("As senhas não coincidem.");

        return;
    }

    try {
        const response = await fetch('/api/cadastro', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ nome, email, senha })
        });

        const result = await response.json();

        if (response.ok) {
            alert(result.message);
            window.location.href = "login.html";
        } else {
            alert('Erro ao cadastrar: ' + result.error);
        }
    } catch (error) {
        console.error('Falha na comunicação com o servidor:', error);
        alert('Não foi possível conectar ao servidor. Tente novamente.');
    }
});