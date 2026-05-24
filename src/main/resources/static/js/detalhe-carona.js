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
    const statusAguardando = document.getElementById('status-aguardando');
    const btnCancelar = document.getElementById('btn-cancelar');

    // --- Lógica para buscar e preencher os dados da carona ---
    // (Esta parte depende de um endpoint que retorne os detalhes de uma carona)
    // Por agora, usaremos dados mockados como exemplo:
    const caronaMock = { id: caronaId, motorista: "Camila Souza", origem: "Campus Unisinos", destino: "Centro Histórico", dataHora: "2026-05-20T08:15:00", vagas: "2/3" };
    document.getElementById('motorista-nome').textContent = caronaMock.motorista;
    document.getElementById('carona-origem').textContent = caronaMock.origem;
    document.getElementById('carona-destino').textContent = caronaMock.destino;
    document.getElementById('carona-datahora').textContent = new Date(caronaMock.dataHora).toLocaleString('pt-BR');
    document.getElementById('carona-vagas').textContent = caronaMock.vagas;

    // --- Funções de Ação ---
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
                body: JSON.stringify({ caronaId: parseInt(caronaId), passageiroId: parseInt(passageiroId), status: 'PENDENTE' })
            });
            const result = await response.json();

            if (response.ok) {
                alert("Solicitação enviada com sucesso!");
                reservaId = result.id; // Salva o ID da reserva criada
                btnSolicitar.style.display = 'none';
                statusAguardando.style.display = 'block';
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
                statusAguardando.style.display = 'none';
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
});