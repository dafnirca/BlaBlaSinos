const form = document.getElementById("login-form");

form.addEventListener("submit", function(event) {

    event.preventDefault();

    const email = document.getElementById("email").value;

    const senha = document.getElementById("senha").value;

    console.log("Login enviado:");

    console.log({
        email,
        senha
    });

    alert("Login enviado!");
});