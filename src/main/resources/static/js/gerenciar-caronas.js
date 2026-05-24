// --- Elementos do DOM ---
const caronasList = document.getElementById('caronas-list');
const emptyState = document.getElementById('empty-state');
const modal = document.getElementById('carona-modal');
const modalTitle = document.getElementById('modal-title');
const caronaForm = document.getElementById('carona-form');
const btnAdicionar = document.getElementById('btn-adicionar-carona');
const btnCancelarModal = document.getElementById('btn-cancelar-modal');

// --- Funções ---
function renderCaronas(caronas) {
    caronasList.innerHTML = '';
    const hasCaronas = caronas && caronas.length > 0;
    
    // Mostra/esconde a tabela ou a mensagem de estado vazio
    emptyState.classList.toggle('hidden', hasCaronas);
    caronasList.closest('table').classList.toggle('hidden', !hasCaronas);

    if (!hasCaronas) return;

    caronas.forEach(carona => {
        const dataHora = new Date(carona.dataHora);
        const dataFormatada = dataHora.toLocaleDateString('pt-BR');
        const horaFormatada = dataHora.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${carona.destino}</td>
            <td>${dataFormatada} às ${horaFormatada}</td>
            <td>${carona.vagasDisponiveis}/${carona.vagasTotais}</td>
            <td>${carona.valor || '0.00'}</td>
            <td class="actions">
                <button class="btn-edit" data-id="${carona.id}" disabled>Editar</button>
                <button class="btn-delete" data-id="${carona.id}" disabled>Cancelar</button>
            </td>
        `;
        caronasList.appendChild(tr);
    });
}

function openModal() {
    caronaForm.reset();
    modalTitle.textContent = 'Oferecer Nova Carona';
    document.getElementById('carona-id').value = '';
    modal.classList.remove('hidden'); // CORREÇÃO: Mostra o modal
}

function closeModal() {
    modal.classList.add('hidden'); // CORREÇÃO: Esconde o modal
}

async function carregarMinhasCaronas() {
    const motoristaId = localStorage.getItem('userId');
    if (!motoristaId) {
        alert("Usuário não logado. Redirecionando para login.");
        window.location.href = '/login.html';
        return;
    }

    try {
        const response = await fetch(`/api/caronas?motoristaId=${motoristaId}`);
        if (!response.ok) throw new Error('Falha ao buscar caronas.');
        const caronas = await response.json();
        renderCaronas(caronas);
    } catch (error) {
        console.error(error);
        renderCaronas([]); // Mostra a lista vazia em caso de erro
    }
}

async function salvarCarona(event) {
    event.preventDefault();
    const motoristaId = localStorage.getItem('userId');
    const data = document.getElementById('data').value;
    const hora = document.getElementById('hora').value;

    const caronaData = {
        motoristaId: parseInt(motoristaId),
        origem: document.getElementById('origem').value,
        destino: document.getElementById('destino').value,
        dataHora: `${data}T${hora}`,
        vagasTotais: parseInt(document.getElementById('vagas').value),
        valor: parseFloat(document.getElementById('valor').value) || 0
    };

    try {
        const response = await fetch('/api/caronas', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(caronaData)
        });
        const result = await response.json();

        if (response.ok) {
            alert("Carona oferecida com sucesso!");
            closeModal(); // CORREÇÃO: Fecha o modal após o sucesso
            carregarMinhasCaronas(); // Recarrega a lista para mostrar a nova carona
        } else {
            throw new Error(result.error);
        }
    } catch (error) {
        alert(`Erro ao salvar carona: ${error.message}`);
    }
}

// --- Inicialização e Event Listeners ---
window.addEventListener('load', carregarMinhasCaronas);
btnAdicionar.addEventListener('click', openModal);
btnCancelarModal.addEventListener('click', closeModal); // CORREÇÃO: Garante que o botão de cancelar feche o modal
caronaForm.addEventListener('submit', salvarCarona);