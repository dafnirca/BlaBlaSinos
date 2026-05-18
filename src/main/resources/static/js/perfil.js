const mockUsuario = {

    nome: "Jordano Rodrigues",
    email: "jordano@edu.unisinos.br",

    cnh: "12345678900",
    categoria: "B",

    marca: "Volkswagen",
    modelo: "Gol",
    cor: "Branco",
    placa: "ABC1234",
    vagas: 4
};

window.onload = function() {

    document.getElementById("nome").value =
        mockUsuario.nome;

    document.getElementById("email").value =
        mockUsuario.email;

    document.getElementById("cnh").value =
        mockUsuario.cnh;

    document.getElementById("categoria").value =
        mockUsuario.categoria;

    document.getElementById("marca").value =
        mockUsuario.marca;

    document.getElementById("modelo").value =
        mockUsuario.modelo;

    document.getElementById("cor").value =
        mockUsuario.cor;

    document.getElementById("placa").value =
        mockUsuario.placa;

    document.getElementById("vagas").value =
        mockUsuario.vagas;
};

document.getElementById('form-perfil').addEventListener('submit', async function(event) {
    event.preventDefault();

    const userId = localStorage.getItem('userId');
    if (!userId) {
        alert("Erro: Usuário não está logado. Faça o login novamente.");
        window.location.href = '/login.html';
        return;
    }

    const dadosPerfil = {
        id: parseInt(userId), // Envia o ID para o backend saber quem atualizar
        nome: document.getElementById('nome').value,
        cnh: document.getElementById('cnh').value,
        modeloVeiculo: document.getElementById('modeloVeiculo').value,
        corVeiculo: document.getElementById('corVeiculo').value,
        placaVeiculo: document.getElementById('placaVeiculo').value
        // Adicione outros campos se necessário
    };

    try {
        const response = await fetch('/api/perfil', {
            method: 'PUT', // O método que seu PerfilHandler espera
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(dadosPerfil)
        });

        const result = await response.json();

        if (response.ok) {
            alert(result.message); // "Perfil atualizado com sucesso!"
        } else {
            alert('Erro: ' + result.error);
        }
    } catch (error) {
        console.error('Falha na comunicação com o servidor:', error);
        alert('Não foi possível conectar ao servidor. Tente novamente.');
    }
});