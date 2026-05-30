document.addEventListener('DOMContentLoaded', async () => {
    const motoristaId = localStorage.getItem('userId');

    if (!motoristaId) {
        alert("Você precisa estar logado para acessar esta página.");
        window.location.href = 'login.html';
        return;
    }

    const solicitacoesList = document.getElementById('solicitacoes-list');
    const solicitacoesTable = document.getElementById('solicitacoes-table');
    const emptyState = document.getElementById('empty-state');
    const solicitacoesCount = document.getElementById('solicitacoes-count');
    const modalConfirmacao = document.getElementById('modal-confirmacao');
    const btnConfirmar = document.getElementById('btn-confirmar');
    const btnCancelarModal = document.getElementById('btn-cancelar-modal');
    const modalMensagem = document.getElementById('modal-mensagem');
    const btnLogout = document.getElementById('btn-logout');

    let decisioPendente = null;
    let pendingSolicitacoesCache = new Set();
    let firstLoad = true;

    // Logout
    btnLogout.addEventListener('click', (e) => {
        e.preventDefault();
        localStorage.removeItem('userId');
        localStorage.removeItem('userEmail');
        window.location.href = 'login.html';
    });

    // Carrega as solicitações pendentes
    async function carregarSolicitacoes() {
        try {
            const response = await fetch(`/api/solicitacoes?motoristaId=${motoristaId}`);
            if (!response.ok) throw new Error('Falha ao carregar solicitações.');

            const solicitacoes = await response.json();
            renderSolicitacoes(solicitacoes);
        } catch (error) {
            console.error('Erro ao carregar solicitações:', error);
            solicitacoesCount.textContent = 'Erro ao carregar solicitações';
        }
    }

    // Função auxiliar para buscar nome do passageiro
    async function buscarNomePassageiro(passageiroId) {
        try {
            const response = await fetch(`/api/perfil?id=${encodeURIComponent(passageiroId)}`);
            if (response.ok) {
                const usuario = await response.json();
                return usuario.nome || 'Passageiro';
            }
            return 'Passageiro';
        } catch (error) {
            console.error(`Erro ao buscar passageiro ${passageiroId}:`, error);
            return 'Passageiro';
        }
    }

    // Renderiza a tabela de solicitações
    async function renderSolicitacoes(solicitacoes) {
        solicitacoesList.innerHTML = '';
        const hasSolicitacoes = solicitacoes && solicitacoes.length > 0;

        emptyState.classList.toggle('hidden', hasSolicitacoes);
        solicitacoesTable.classList.toggle('hidden', !hasSolicitacoes);

        if (!hasSolicitacoes) {
            solicitacoesCount.textContent = 'Nenhuma solicitação pendente';
            return;
        }

        solicitacoesCount.textContent = `${solicitacoes.length} solicitação(ões) pendente(s)`;

        // Busca nomes de todos os passageiros em paralelo
        const nomeMap = {};
        await Promise.all(
            solicitacoes.map(async (sol) => {
                nomeMap[sol.passageiroId] = await buscarNomePassageiro(sol.passageiroId);
            })
        );

        solicitacoes.forEach(sol => {
            const tr = document.createElement('tr');
            const nomePassageiro = nomeMap[sol.passageiroId] || 'Passageiro';
            tr.innerHTML = `
                <td>${nomePassageiro}</td>
                <td>${sol.caronaId}</td>
                <td>${formatarData(sol.dataHora || 'N/A')}</td>
                <td>Origem → Destino</td>
                <td>
                    <div class="acoes-btn">
                        <button class="btn btn-aceitar" data-id="${sol.id}" data-acao="ACEITAR">
                            Aceitar
                        </button>
                        <button class="btn btn-recusar" data-id="${sol.id}" data-acao="RECUSAR">
                            Recusar
                        </button>
                    </div>
                </td>
            `;
            solicitacoesList.appendChild(tr);
        });

        // Event listeners para os botões
        document.querySelectorAll('.btn-aceitar, .btn-recusar').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const reservaId = btn.getAttribute('data-id');
                const acao = btn.getAttribute('data-acao');
                const acao_pt = acao === 'ACEITAR' ? 'aceitar' : 'recusar';

                decisioPendente = { reservaId, acao };
                modalMensagem.textContent = `Tem certeza que deseja ${acao_pt} esta solicitação?`;
                modalConfirmacao.classList.remove('hidden');
            });
        });
    }

    // Modal de confirmação
    btnConfirmar.addEventListener('click', async () => {
        if (!decisioPendente) return;

        try {
            const response = await fetch('/api/solicitacoes', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    motoristaId: parseInt(motoristaId),
                    reservaId: parseInt(decisioPendente.reservaId),
                    acao: decisioPendente.acao
                })
            });

            if (response.ok) {
                const acao_pt = decisioPendente.acao === 'ACEITAR' ? 'aceita' : 'recusada';
                alert(`Solicitação ${acao_pt} com sucesso!`);
                modalConfirmacao.classList.add('hidden');
                decisioPendente = null;
                carregarSolicitacoes();
            } else {
                const result = await response.json();
                throw new Error(result.error);
            }
        } catch (error) {
            alert('Erro ao processar a decisão: ' + error.message);
            modalConfirmacao.classList.add('hidden');
        }
    });

    btnCancelarModal.addEventListener('click', () => {
        modalConfirmacao.classList.add('hidden');
        decisioPendente = null;
    });

    // Função auxiliar para formatar data
    function formatarData(dataStr) {
        if (!dataStr || dataStr === 'N/A') return 'N/A';
        try {
            const data = new Date(dataStr);
            return data.toLocaleString('pt-BR');
        } catch {
            return dataStr;
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
    // Recarrega a cada 10 segundos
    carregarSolicitacoes();
    setInterval(carregarSolicitacoes, 10000);
});
