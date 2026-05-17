const mockUsuario = {

    nome: "Jordano Rodrigues",
    email: "jordano@edu.unisinos.br",

    cnh: "12345678900",
    categoria: "B",

    marca: "Volkswagen",
    modelo: "Gol",
    cor: "Branco",
    placa: "ABC1234",
    vagas: 4
};

window.onload = function() {

    document.getElementById("nome").value =
        mockUsuario.nome;

    document.getElementById("email").value =
        mockUsuario.email;

    document.getElementById("cnh").value =
        mockUsuario.cnh;

    document.getElementById("categoria").value =
        mockUsuario.categoria;

    document.getElementById("marca").value =
        mockUsuario.marca;

    document.getElementById("modelo").value =
        mockUsuario.modelo;

    document.getElementById("cor").value =
        mockUsuario.cor;

    document.getElementById("placa").value =
        mockUsuario.placa;

    document.getElementById("vagas").value =
        mockUsuario.vagas;
};

document
    .getElementById("perfil-form")
    .addEventListener(
        "submit",
        function(event) {

            event.preventDefault();

            alert(
                "Perfil atualizado com sucesso!"
            );
        }
    );