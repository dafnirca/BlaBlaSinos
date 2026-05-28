document.addEventListener('DOMContentLoaded', async () => {
    const params = new URLSearchParams(window.location.search);
    const caronaId = params.get('id');
    let reservaId = null; // Guardará o ID da reserva após ser criada

    if (!caronaId) {
        document.body.innerHTML = '<h1>Erro: ID da carona não fornecido.</h1>';
        return;
    }

    // --- Elementos do DOM ---
    const btnSolicitar = document.getElementById('btn-solicitar');
    const statusResultado = document.getElementById('status-resultado');
    const statusText = document.getElementById('status-text');
    const btnCancelar = document.getElementById('btn-cancelar');
    let previousStatus = null;
    let firstStatusLoad = true;

    // --- Busca e preenche os dados reais da carona e do motorista ---
    async function carregarDetalhesCarona() {
        try {
            const resp = await fetch(`/api/caronas?id=${encodeURIComponent(caronaId)}`);
            if (!resp.ok) throw new Error('Falha ao carregar detalhes da carona');
            const carona = await resp.json();

            document.getElementById('carona-origem').textContent = carona.origem || 'N/A';
            document.getElementById('carona-destino').textContent = carona.destino || 'N/A';
            document.getElementById('carona-datahora').textContent = carona.dataHora ? new Date(carona.dataHora).toLocaleString('pt-BR') : 'N/A';
            document.getElementById('carona-vagas').textContent = `${carona.vagasDisponiveis}/${carona.vagasTotais}`;

            // Busca dados do motorista
            try {
                const respUser = await fetch(`/api/perfil?id=${encodeURIComponent(carona.motoristaId)}`);
                if (respUser.ok) {
                    const usuario = await respUser.json();
                    document.getElementById('motorista-nome').textContent = usuario.nome || 'Motorista';
                } else {
                    document.getElementById('motorista-nome').textContent = 'Motorista';
                }
            } catch (err) {
                console.error('Erro ao carregar motorista:', err);
                document.getElementById('motorista-nome').textContent = 'Motorista';
            }
        } catch (error) {
            console.error('Erro ao carregar detalhes da carona:', error);
        }
    }

    async function carregarStatusReserva() {
        const passageiroId = localStorage.getItem('userId');
        if (!passageiroId) return;

        try {
            const response = await fetch(`/api/solicitacoes?passageiroId=${encodeURIComponent(passageiroId)}`);
            if (!response.ok) return;
            const reservas = await response.json();
            const reservaExistente = reservas.find(r => String(r.caronaId) === String(caronaId));

            if (reservaExistente) {
                reservaId = reservaExistente.id;
                if (!firstStatusLoad && previousStatus && previousStatus !== reservaExistente.status) {
                    if (!window.__BlaBlaSinosNotificationSystem) {
                        if (reservaExistente.status === 'CONFIRMADA') {
                            showNotification('Solicitação aceita', 'O motorista aceitou sua solicitação.');
                        } else if (reservaExistente.status === 'CANCELADA') {
                            showNotification('Solicitação recusada', 'O motorista recusou sua solicitação.');
                        }
                    }
                }
                previousStatus = reservaExistente.status;
                atualizarStatus(reservaExistente.status);
            }
        } catch (error) {
            console.error('Falha ao carregar status da reserva:', error);
        } finally {
            firstStatusLoad = false;
        }
    }

    function atualizarStatus(status) {
        statusResultado.style.display = 'block';
        statusText.textContent = {
            'PENDENTE': 'Sua solicitação está pendente. Aguardando resposta do motorista.',
            'CONFIRMADA': 'Solicitação aceita! Parabéns, sua vaga foi confirmada.',
            'CANCELADA': 'Solicitação recusada pelo motorista.'
        }[status] || `Status da solicitação: ${status}`;

        if (status === 'PENDENTE') {
            btnCancelar.style.display = 'inline-block';
            btnSolicitar.style.display = 'none';
        } else {
            btnCancelar.style.display = 'none';
            btnSolicitar.style.display = 'none';
        }
    }

    function showNotification(title, message) {
        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            container.className = 'toast-container';
            document.body.appendChild(container);
        }

        const toast = document.createElement('div');
        toast.className = 'toast';
        toast.innerHTML = `<strong>${title}</strong><p>${message}</p>`;
        container.appendChild(toast);

        requestAnimationFrame(() => toast.classList.add('visible'));
        setTimeout(() => toast.classList.remove('visible'), 5000);
        setTimeout(() => toast.remove(), 5600);
    }

    const solicitarVaga = async () => {
        const passageiroId = localStorage.getItem('userId');
        if (!passageiroId) {
            alert("Você precisa estar logado para solicitar uma vaga.");
            return;
        }

        try {
            const response = await fetch('/api/solicitacoes', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ caronaId: parseInt(caronaId), passageiroId: parseInt(passageiroId) })
            });
            const result = await response.json();

            if (response.ok) {
                alert("Solicitação enviada com sucesso!");
                reservaId = result.id;
                atualizarStatus(result.status);
            } else {
                throw new Error(result.error);
            }
        } catch (error) {
            alert('Erro ao solicitar vaga: ' + error.message);
        }
    };

    const cancelarSolicitacao = async () => {
        const passageiroId = localStorage.getItem('userId');
        if (!reservaId) {
            alert("Não foi possível encontrar o ID da sua reserva para cancelar.");
            return;
        }
        if (!passageiroId) {
            alert("Erro de autenticação. Por favor, faça o login novamente.");
            return;
        }

        try {
            // ==================================================================
            // === ESTA É A CORREÇÃO FINAL ===
            // ==================================================================
            // A URL agora envia 'reservaId' e 'passageiroId', como o backend espera.
            const response = await fetch(`/api/solicitacoes?reservaId=${reservaId}&passageiroId=${passageiroId}`, { 
                method: 'DELETE' 
            });
            
            if (response.ok) {
                const result = await response.json();
                alert(result.message); // "Sua solicitação foi cancelada."
                btnSolicitar.style.display = 'block';
                statusResultado.style.display = 'none';
                reservaId = null;
            } else {
                const result = await response.json();
                throw new Error(result.error);
            }
        } catch (error) {
            alert('Erro ao cancelar: ' + error.message);
        }
    };

    // --- Event Listeners ---
    btnSolicitar.addEventListener('click', solicitarVaga);
    btnCancelar.addEventListener('click', cancelarSolicitacao);

    // Carrega dados da carona e do motorista, depois o status da reserva
    await carregarDetalhesCarona();
    await carregarStatusReserva();
    setInterval(carregarStatusReserva, 5000);
});