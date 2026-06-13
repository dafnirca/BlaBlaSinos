document.addEventListener('DOMContentLoaded', async () => {
    const userId = localStorage.getItem('userId');
    if (!userId) { window.location.href = 'login.html'; return; }

    const viagensList = document.getElementById('viagens-list');
    const emptyState = document.getElementById('empty-state');
    const table = document.getElementById('viagens-table');

    async function carregarViagens() {
        viagensList.innerHTML = '';
        try {
            const resp = await fetch(`/api/solicitacoes?passageiroId=${encodeURIComponent(userId)}`);
            if (!resp.ok) throw new Error('Falha ao buscar reservas');
            const reservas = await resp.json();
            const confirmadas = reservas.filter(r => r.status === 'CONFIRMADA');
            if (!confirmadas || confirmadas.length === 0) {
                emptyState.classList.remove('hidden');
                table.classList.add('hidden');
                return;
            }
            emptyState.classList.add('hidden');
            table.classList.remove('hidden');

            for (const reserva of confirmadas) {
                const carResp = await fetch(`/api/caronas?id=${encodeURIComponent(reserva.caronaId)}`);
                if (!carResp.ok) continue;
                const carona = await carResp.json();
                const motoristaResp = await fetch(`/api/perfil?id=${encodeURIComponent(carona.motoristaId)}`);
                const motorista = motoristaResp.ok ? await motoristaResp.json() : { nome: 'Motorista' };

                const tr = document.createElement('tr');
                const data = carona.dataHora ? new Date(carona.dataHora).toLocaleString('pt-BR') : '';
                const status = carona.status || 'AGENDADA';

                const actionTd = document.createElement('td');

                if (status === 'CONCLUIDA') {
                    // verificar se passageiro já avaliou motorista
                    try {
                        const aResp = await fetch(`/api/avaliacoes?caronaId=${carona.id}&avaliadorId=${userId}`);
                        if (aResp.ok) {
                            const dataA = await aResp.json();
                            const avaliaveis = dataA.avaliaveis || [];
                            if (avaliaveis.includes(carona.motoristaId)) {
                                const btn = document.createElement('button');
                                btn.className = 'btn-principal';
                                btn.textContent = 'Avaliar Motorista';
                                btn.addEventListener('click', () => avaliarMotorista(carona.id, carona.motoristaId));
                                actionTd.appendChild(btn);
                            } else {
                                const span = document.createElement('span');
                                span.textContent = 'Avaliação enviada';
                                actionTd.appendChild(span);
                            }
                        }
                    } catch (err) {
                        console.error('Erro ao checar avaliacao', err);
                    }
                } else {
                    actionTd.textContent = '-';
                }

                tr.innerHTML = `
                    <td>${data}</td>
                    <td>${carona.origem || ''}</td>
                    <td>${carona.destino || ''}</td>
                    <td>${motorista.nome || 'Motorista'}</td>
                    <td>${status}</td>
                `;
                tr.appendChild(actionTd);
                viagensList.appendChild(tr);
            }
        } catch (err) {
            console.error('Erro ao carregar viagens', err);
            emptyState.classList.remove('hidden');
            table.classList.add('hidden');
        }
    }

    async function avaliarMotorista(caronaId, motoristaId) {
        const notaStr = prompt('Nota para o motorista (1-5):', '5');
        const nota = parseInt(notaStr);
        if (isNaN(nota) || nota < 1 || nota > 5) { alert('Nota inválida'); return; }
        const comentario = prompt('Comentário (opcional):', '');
        try {
            const post = await fetch('/api/avaliacoes', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ caronaId: parseInt(caronaId), avaliadorId: parseInt(userId), avaliadoId: parseInt(motoristaId), nota: nota, comentario: comentario })
            });
            const res = await post.json();
            if (!post.ok) throw new Error(res.error || 'Erro ao enviar avaliação');
            alert('Avaliação enviada com sucesso.');
            carregarViagens();
        } catch (err) {
            alert('Erro: ' + err.message);
        }
    }

    carregarViagens();
});
