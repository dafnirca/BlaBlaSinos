const API_PERFIL = '/api/perfil';

window.onload = async function() {
    const userId = localStorage.getItem('userId');

    if (!userId) {
        alert("Erro: Usuário não está logado. Faça o login novamente.");
        window.location.href = '/login.html';
        return;
    }

    document.getElementById('nome').value = localStorage.getItem('userNome') || '';
    document.getElementById('email').value = localStorage.getItem('userEmail') || '';

    try {
        const response = await fetch(`${API_PERFIL}?id=${encodeURIComponent(userId)}`);
        if (!response.ok) {
            console.warn('Não foi possível carregar o perfil do usuário:', response.status);
            return;
        }

        const usuario = await response.json();

        if (usuario.cnh) {
            document.getElementById('cnh').value = usuario.cnh;
        }
        if (usuario.marcaVeiculo) {
            document.getElementById('marca').value = usuario.marcaVeiculo;
        }
        if (usuario.vagas !== undefined && usuario.vagas !== null) {
            document.getElementById('vagas').value = usuario.vagas;
        }
        if (usuario.marcaVeiculo) {
            document.getElementById('marca').value = usuario.marcaVeiculo;
        }
        if (usuario.modeloVeiculo) {
            document.getElementById('modelo').value = usuario.modeloVeiculo;
        }
        if (usuario.corVeiculo) {
            document.getElementById('cor').value = usuario.corVeiculo;
        }
        if (usuario.placaVeiculo) {
            document.getElementById('placa').value = usuario.placaVeiculo;
        }
    } catch (error) {
        console.error('Falha ao carregar dados do perfil:', error);
    }
};

document.getElementById('perfil-form').addEventListener('submit', async function(event) {
    event.preventDefault();

    const userId = localStorage.getItem('userId');
    if (!userId) {
        alert("Erro: Usuário não está logado. Faça o login novamente.");
        window.location.href = '/login.html';
        return;
    }

        const vagasInput = document.getElementById('vagas').value;
        const dadosPerfil = {
            id: parseInt(userId), // Envia o ID para o backend saber quem atualizar
            nome: document.getElementById('nome').value,
            cnh: document.getElementById('cnh').value,
            marcaVeiculo: document.getElementById('marca').value,
            modeloVeiculo: document.getElementById('modelo').value,
            corVeiculo: document.getElementById('cor').value,
            placaVeiculo: document.getElementById('placa').value,
            vagas: vagasInput ? parseInt(vagasInput) : null
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