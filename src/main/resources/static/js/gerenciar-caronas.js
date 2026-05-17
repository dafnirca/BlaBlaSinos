const caronasMock = [
    {
        id: 1,
        destino: "Centro Histórico",
        dataHora: "2026-05-20T08:15:00",
        vagasDisponiveis: 2,
        vagasTotais: 3,
        origem: "Campus Unisinos",
    },
    {
        id: 2,
        destino: "Campus Unisinos",
        dataHora: "2026-05-21T18:00:00",
        vagasDisponiveis: 1,
        vagasTotais: 1,
        origem: "Itaqui",
    }
];

// Elementos do DOM
const caronasList = document.getElementById('caronas-list');
const emptyState = document.getElementById('empty-state');
const modal = document.getElementById('carona-modal');
const modalTitle = document.getElementById('modal-title');
const caronaForm = document.getElementById('carona-form');

function renderCaronas(caronas) {
    caronasList.innerHTML = '';

    if (caronas.length === 0) {
        emptyState.style.display = 'block';
        caronasList.style.display = 'none';
    } else {
        emptyState.style.display = 'none';
        caronasList.style.display = '';
        
        caronas.forEach(carona => {
            const dataHora = new Date(carona.dataHora);
            const dataFormatada = dataHora.toLocaleDateString('pt-BR');
            const horaFormatada = dataHora.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${carona.destino}</td>
                <td>${dataFormatada} às ${horaFormatada}</td>
                <td>${carona.vagasDisponiveis}/${carona.vagasTotais}</td>
                <td class="actions">
                    <button class="btn-edit" data-id="${carona.id}">Editar</button>
                    <button class="btn-delete" data-id="${carona.id}">Cancelar</button>
                </td>
            `;
            caronasList.appendChild(tr);
        });
    }
}

function openModal(carona = null) {
    caronaForm.reset();
    if (carona) {
        modalTitle.textContent = 'Editar Carona';
        document.getElementById('carona-id').value = carona.id;
        document.getElementById('origem').value = carona.origem;
        document.getElementById('destino').value = carona.destino;
        const dataHora = new Date(carona.dataHora);
        document.getElementById('data').value = dataHora.toISOString().split('T')[0];
        document.getElementById('hora').value = dataHora.toTimeString().substring(0, 5);
        document.getElementById('vagas').value = carona.vagasTotais;
    } else {
        modalTitle.textContent = 'Oferecer Nova Carona';
        document.getElementById('carona-id').value = '';
    }
    modal.style.display = 'flex';
}

function closeModal() {
    modal.style.display = 'none';
}

// Event Listeners
document.getElementById('btn-adicionar-carona').addEventListener('click', () => openModal());
document.getElementById('btn-cancelar-modal').addEventListener('click', closeModal);

caronaForm.addEventListener('submit', (event) => {
    event.preventDefault();
    // Aqui virá a lógica para salvar (criar/editar) a carona,
    // fazendo um fetch para o backend.
    console.log('Salvando carona...');
    closeModal();
});

caronasList.addEventListener('click', (event) => {
    if (event.target.classList.contains('btn-edit')) {
        const caronaId = event.target.dataset.id;
        const carona = caronasMock.find(c => c.id == caronaId);
        openModal(carona);
    }
    if (event.target.classList.contains('btn-delete')) {
        if (confirm('Tem certeza que deseja cancelar esta carona?')) {
            // Aqui virá a lógica para deletar a carona via fetch.
            console.log('Cancelando carona...');
        }
    }
});

// Carregamento inicial
window.addEventListener('load', () => {
    // No futuro, substituir caronasMock por uma chamada fetch ao backend
    // Ex: fetch('/api/minhas-caronas').then(...)
    renderCaronas(caronasMock);
});