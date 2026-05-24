const API_PERFIL = '/api/perfil';

const nomeInput = document.getElementById('nome');
const emailInput = document.getElementById('email');
const cnhInput = document.getElementById('cnh');
const modeloInput = document.getElementById('modelo');
const corInput = document.getElementById('cor');
const placaInput = document.getElementById('placa');
const motoristaCheckbox = document.getElementById('sou-motorista-checkbox');
const motoristaFieldsDiv = document.getElementById('motorista-fields');
const minhasCaronasLink = document.getElementById('link-minhas-caronas');
const perfilForm = document.getElementById('perfil-form');

function toggleMotoristaFields() {
    motoristaFieldsDiv.classList.toggle('hidden', !motoristaCheckbox.checked);
}

async function carregarPerfil() {
    const userId = localStorage.getItem('userId');
    if (!userId) {
        alert("Erro: Usuário não está logado. Faça o login novamente.");
        window.location.href = '/login.html';
        return;
    }

    try {
        const response = await fetch(`${API_PERFIL}?id=${encodeURIComponent(userId)}`);
        if (!response.ok) throw new Error(`Não foi possível carregar o perfil: ${response.status}`);
        
        const usuario = await response.json();

        nomeInput.value = usuario.nome || '';
        emailInput.value = usuario.email || '';
        cnhInput.value = usuario.cnh || '';
        modeloInput.value = usuario.modeloVeiculo || '';
        corInput.value = usuario.corVeiculo || '';
        placaInput.value = usuario.placaVeiculo || '';

        const isMotoristaCompleto = usuario.cnh && usuario.placaVeiculo;
        if (isMotoristaCompleto) {
            motoristaCheckbox.checked = true;
            minhasCaronasLink.classList.remove('hidden');
        } else {
            minhasCaronasLink.classList.add('hidden');
        }
        toggleMotoristaFields();

    } catch (error) {
        console.error('Falha ao carregar dados do perfil:', error);
    }
}

async function salvarPerfil(event) {
    event.preventDefault();
    const userId = localStorage.getItem('userId');

    const dadosPerfil = {
        id: parseInt(userId),
        nome: nomeInput.value,
        cnh: cnhInput.value,
        modeloVeiculo: modeloInput.value,
        corVeiculo: corInput.value,
        placaVeiculo: placaInput.value
    };

    try {
        const response = await fetch(API_PERFIL, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dadosPerfil)
        });
        const result = await response.json();

        if (response.ok) {
            alert(result.message);
            carregarPerfil();
        } else {
            throw new Error(result.error);
        }
    } catch (error) {
        console.error('Falha ao salvar perfil:', error);
        alert(`Não foi possível salvar: ${error.message}`);
    }
}

window.addEventListener('load', carregarPerfil);
motoristaCheckbox.addEventListener('change', toggleMotoristaFields);
perfilForm.addEventListener('submit', salvarPerfil);