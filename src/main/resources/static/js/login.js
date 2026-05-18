const form = document.getElementById("login-form");

form.addEventListener("submit", async function(event) {

    event.preventDefault();

    const email = document.getElementById("email").value;

    const senha = document.getElementById("senha").value;

    try {
        const response = await fetch('/api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, senha })
        });

        const result = await response.json();

        if (response.ok) {
            localStorage.setItem('userId', result.id);
            localStorage.setItem('userNome', result.nome);
            localStorage.setItem('userEmail', result.email);
            localStorage.setItem('userTipo', result.tipo);
            alert(result.message);
            window.location.href = 'perfil.html';
        } else {
            alert('Erro ao autenticar: ' + result.error);
        }
    } catch (error) {
        console.error('Falha na comunicação com o servidor:', error);
        alert('Não foi possível conectar ao servidor. Tente novamente.');
    }
});