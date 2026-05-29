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
                <button class="btn-edit" data-id="${carona.id}">Editar</button>
                <button class="btn-delete" data-id="${carona.id}">Cancelar</button>
            </td>
        `;
        caronasList.appendChild(tr);
        const btnEdit = tr.querySelector('.btn-edit');
        const btnDelete = tr.querySelector('.btn-delete');
        if (btnEdit) btnEdit.addEventListener('click', () => abrirModalParaEdicao(carona));
        if (btnDelete) btnDelete.addEventListener('click', () => cancelarCarona(carona.id));
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
    const editingId = document.getElementById('carona-id').value;

    try {
        let response;
        if (editingId && editingId.trim() !== '') {
            // Edit existing
            response = await fetch(`/api/caronas?id=${editingId}&motoristaId=${motoristaId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ ...caronaData, id: parseInt(editingId) })
            });
        } else {
            // Create new
            response = await fetch('/api/caronas', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(caronaData)
            });
        }

        const result = await response.json();
        if (response.ok) {
            alert(editingId ? "Carona atualizada com sucesso!" : "Carona oferecida com sucesso!");
            closeModal();
            carregarMinhasCaronas();
        } else {
            throw new Error(result.error || 'Erro desconhecido');
        }
    } catch (error) {
        alert(`Erro ao salvar carona: ${error.message}`);
    }
}

// Preenche o modal para editar uma carona
function abrirModalParaEdicao(carona) {
    modalTitle.textContent = 'Editar Carona';
    document.getElementById('carona-id').value = carona.id || '';
    document.getElementById('origem').value = carona.origem || '';
    document.getElementById('destino').value = carona.destino || '';
    const dt = new Date(carona.dataHora);
    document.getElementById('data').value = dt.toISOString().slice(0,10);
    document.getElementById('hora').value = dt.toTimeString().slice(0,5);
    document.getElementById('vagas').value = carona.vagasTotais || '';
    document.getElementById('valor').value = carona.valor || '';
    modal.classList.remove('hidden');
}

async function cancelarCarona(caronaId) {
    const motoristaId = localStorage.getItem('userId');
    if (!confirm('Tem certeza que deseja cancelar esta carona?')) return;
    try {
        const response = await fetch(`/api/caronas?id=${caronaId}&motoristaId=${motoristaId}`, { method: 'DELETE' });
        const result = await response.json();
        if (response.ok) {
            alert(result.message || 'Carona cancelada com sucesso');
            carregarMinhasCaronas();
        } else {
            throw new Error(result.error || 'Erro desconhecido');
        }
    } catch (error) {
        alert(`Erro ao cancelar carona: ${error.message}`);
    }
}

// --- Inicialização e Event Listeners ---
window.addEventListener('load', carregarMinhasCaronas);
btnAdicionar.addEventListener('click', openModal);
btnCancelarModal.addEventListener('click', closeModal); // CORREÇÃO: Garante que o botão de cancelar feche o modal
caronaForm.addEventListener('submit', salvarCarona);