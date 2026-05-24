// A variável caronasMock foi removida.

function formatarResultado(caronas) {
    // Esta função continua a mesma, mas agora recebe caronas reais do banco.
    return caronas.map(carona => {
        const item = document.createElement("div");
        item.className = "result-card";

        // Adicionamos um link para a página de detalhes
        const link = document.createElement("a");
        link.href = `/detalhe-carona.html?id=${carona.id}`;
        link.className = "card-link";

        item.innerHTML = `
            <strong>${carona.origem} → ${carona.destino}</strong>
            <div class="result-detail">
                <span><strong>Motorista:</strong> ${carona.motoristaNome || 'N/A'}</span>
                <span><strong>Data:</strong> ${new Date(carona.dataHora).toLocaleDateString('pt-BR')}</span>
                <span><strong>Hora:</strong> ${new Date(carona.dataHora).toLocaleTimeString('pt-BR', {hour: '2-digit', minute: '2-digit'})}</span>
            </div>
            <div class="result-detail">
                <span><strong>Vagas disponíveis:</strong> ${carona.vagasDisponiveis}</span>
                <span><strong>Valor:</strong> R$ ${carona.valor ? carona.valor.toFixed(2) : '0.00'}</span>
            </div>
        `;
        
        link.appendChild(item);
        return link;
    });
}

function mostrarResultados(resultados) {
    const resultsList = document.getElementById("results-list");
    const resultsCount = document.getElementById("results-count");

    resultsList.innerHTML = "";

    if (!resultados || resultados.length === 0) {
        resultsCount.textContent = "Nenhuma carona encontrada.";
        const vazio = document.createElement("p");
        vazio.className = "empty-state";
        vazio.textContent = "Tente ajustar os filtros ou buscar por outra data.";
        resultsList.appendChild(vazio);
        return;
    }

    resultsCount.textContent = `${resultados.length} carona(s) encontrada(s)`;
    const cards = formatarResultado(resultados);
    cards.forEach(card => resultsList.appendChild(card));
}

// --- LÓGICA DE BUSCA CORRIGIDA ---
async function buscarCaronasNoBackend(event) {
    event.preventDefault();

    const destino = document.getElementById("destino").value.trim();
    const data = document.getElementById("data").value;

    // Constrói a URL para a chamada da API
    const params = new URLSearchParams();
    if (destino) {
        params.append('destino', destino);
    }
    if (data) {
        params.append('data', data);
    }

    try {
        // Faz a chamada fetch para o CaronaHandler
        const response = await fetch(`/api/caronas/buscar?${params.toString()}`);
        const resultados = await response.json();

        if (!response.ok) {
            throw new Error(resultados.error || 'Falha ao buscar caronas.');
        }

        mostrarResultados(resultados);

    } catch (error) {
        console.error('Erro ao buscar caronas:', error);
        mostrarResultados([]); // Mostra a mensagem de "nenhuma carona encontrada"
    }
}

window.addEventListener("load", function () {
    // A busca agora só acontece quando o formulário é submetido
    document.getElementById("buscar-caronas-form").addEventListener("submit", buscarCaronasNoBackend);
});