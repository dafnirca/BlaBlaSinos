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

    // Renderiza a tabela de solicitações
    function renderSolicitacoes(solicitacoes) {
        solicitacoesList.innerHTML = '';
        const hasSolicitacoes = solicitacoes && solicitacoes.length > 0;

        emptyState.classList.toggle('hidden', hasSolicitacoes);
        solicitacoesTable.classList.toggle('hidden', !hasSolicitacoes);

        if (!hasSolicitacoes) {
            solicitacoesCount.textContent = 'Nenhuma solicitação pendente';
            return;
        }

        solicitacoesCount.textContent = `${solicitacoes.length} solicitação(ões) pendente(s)`;

        solicitacoes.forEach(sol => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${sol.passageiroId || 'N/A'}</td>
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

    // Recarrega a cada 10 segundos
    carregarSolicitacoes();
    setInterval(carregarSolicitacoes, 10000);
});
