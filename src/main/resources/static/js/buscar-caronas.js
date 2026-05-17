const caronasMock = [
    {
        motorista: "Camila Souza",
        origem: "Campus Unisinos",
        destino: "Centro Histórico",
        data: "2026-05-20",
        hora: "08:15",
        vagas: 2,
        veiculo: "Carro",
        placa: "ABC1D23"
    },
    {
        motorista: "Rafael Lima",
        origem: "São Leopoldo",
        destino: "Porto Alegre",
        data: "2026-05-20",
        hora: "10:00",
        vagas: 3,
        veiculo: "Van",
        placa: "CDE4F56"
    },
    {
        motorista: "Laura Mendes",
        origem: "Itaqui",
        destino: "Campus Unisinos",
        data: "2026-05-21",
        hora: "07:30",
        vagas: 1,
        veiculo: "Carro",
        placa: "GHI7J89"
    }
];

function formatarResultado(caronas) {
    return caronas.map(caronas => {
        const item = document.createElement("div");
        item.className = "result-card";

        item.innerHTML = `
            <strong>${caronas.origem} → ${caronas.destino}</strong>
            <div class="result-detail">
                <span><strong>Motorista:</strong> ${caronas.motorista}</span>
                <span><strong>Data:</strong> ${caronas.data}</span>
                <span><strong>Hora:</strong> ${caronas.hora}</span>
            </div>
            <div class="result-detail">
                <span><strong>Vagas disponíveis:</strong> ${caronas.vagas}</span>
                <span><strong>Veículo:</strong> ${caronas.veiculo}</span>
                <span><strong>Placa:</strong> ${caronas.placa}</span>
            </div>
        `;

        return item;
    });
}

function filtrarCaronas(filtros) {
    return caronasMock.filter(caronas => {
        const origemMatch = filtros.origem === "" || caronas.origem.toLowerCase().includes(filtros.origem.toLowerCase());
        const destinoMatch = filtros.destino === "" || caronas.destino.toLowerCase().includes(filtros.destino.toLowerCase());
        const dataMatch = filtros.data === "" || caronas.data === filtros.data;
        const horaMatch = filtros.hora === "" || caronas.hora === filtros.hora;
        const vagasMatch = filtros.vagas === "" || caronas.vagas >= Number(filtros.vagas);
        const veiculoMatch = filtros.veiculo === "" || caronas.veiculo === filtros.veiculo;

        return origemMatch && destinoMatch && dataMatch && horaMatch && vagasMatch && veiculoMatch;
    });
}

function mostrarResultados(resultados) {
    const resultsList = document.getElementById("results-list");
    const resultsCount = document.getElementById("results-count");

    resultsList.innerHTML = "";

    if (resultados.length === 0) {
        resultsCount.textContent = "Nenhuma carona encontrada.";
        const vazio = document.createElement("p");
        vazio.className = "empty-state";
        vazio.textContent = "Tente ajustar os filtros ou limpar alguns campos.";
        resultsList.appendChild(vazio);
        return;
    }

    resultsCount.textContent = `${resultados.length} carona(s) encontrada(s)`;
    const cards = formatarResultado(resultados);

    cards.forEach(card => resultsList.appendChild(card));
}

window.addEventListener("load", function () {
    document.getElementById("buscar-caronas-form").addEventListener("submit", function (event) {
        event.preventDefault();

        const filtros = {
            origem: document.getElementById("origem").value.trim(),
            destino: document.getElementById("destino").value.trim(),
            data: document.getElementById("data").value,
            hora: document.getElementById("hora").value,
            vagas: document.getElementById("vagasBusca").value,
            veiculo: document.getElementById("veiculo").value
        };

        const resultados = filtrarCaronas(filtros);
        mostrarResultados(resultados);
    });
});
