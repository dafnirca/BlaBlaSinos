# Testes Unitários para Regras de Negócio

Este documento descreve os casos de teste unitário que validam as regras de negócio centrais do serviço de caronas do projeto.

## Objetivo

Garantir que a lógica de negócio implementada em `CaronaService` respeite as regras definidas para cadastro, edição, solicitação e gerenciamento de caronas.

## Escopo

Cobrir as principais validações de negócio, incluindo:

- validação de vagas mínimas e máximas
- validação de origem/destino válido (campus Unisinos)
- validação de data/hora futura
- validação de perfil de motorista e dados de veículo
- validação de conflito de horário de carona com o mesmo motorista
- validação de operações sobre caronas existentes

## Ambiente de Teste

Os testes unitários usam repositórios falsos em memória para isolar a lógica de serviço:

- `FakeCaronaRepository`
- `InMemoryUsuarioRepository`
- `FakeReservaRepository`

As classes de teste relevantes são:

- `src/test/java/br/blablasinos/service/CaronaServiceRulesTest.java`
- `src/test/java/br/blablasinos/service/CaronaServiceTest.java`

## Casos de Teste

### 1. Reprovar vagas fora do intervalo

- Cenário: motorista válido tenta cadastrar uma carona com 0 vagas ou mais de 4 vagas.
- Entrada: `vagasTotais = 0` e `vagasTotais = 5`.
- Resultado esperado: exceção `CaronaService.CaronaException` com mensagem indicando que o número de vagas deve ser entre 1 e 4.

### 2. Reprovar origem/destino sem campus Unisinos

- Cenário: motorista válido tenta cadastrar carona cuja origem e destino não contêm nenhum campus Unisinos.
- Entrada: origem = "Centro", destino = "Praia".
- Resultado esperado: exceção `CaronaService.CaronaException` com mensagem indicando que a origem ou destino deve ser um campus Unisinos.

### 3. Reprovar data/hora de saída não futura

- Cenário: motorista válido tenta cadastrar carona com horário passado.
- Entrada: data/hora com `LocalDateTime.now().minusHours(1)`.
- Resultado esperado: exceção `CaronaService.CaronaException` com mensagem informando que o horário deve ser posterior ao momento atual.

### 4. Reprovar perfil de usuário que não é motorista

- Cenário: usuário do tipo `PASSAGEIRO` tenta cadastrar carona.
- Resultado esperado: exceção `CaronaService.CaronaException` com mensagem de permissão apenas para motoristas.

### 5. Reprovar motorista com perfil incompleto

- Cenário: usuário do tipo `MOTORISTA` sem CNH, modelo, cor ou placa tenta cadastrar carona.
- Resultado esperado: exceção `CaronaService.CaronaException` com mensagem indicando que o perfil de motorista está incompleto.

### 6. Reprovar conflito de horário de carona

- Cenário: motorista com carona ativa tenta cadastrar outra carona com horário conflitando em menos de 60 minutos.
- Entrada: segunda carona com hora de saída a 10 minutos da primeira carona.
- Resultado esperado: exceção `CaronaService.CaronaException` com mensagem de conflito de horário.

## Pontos de Validação Adicionais

### Editar carona

- Validar que a edição não permite horários conflitantes com outras caronas do mesmo motorista.
- Validar que a edição exige origem/destino válidos e horário futuro.
- Validar que a redução de vagas não pode ser menor do que as vagas já reservadas.

### Cancelar carona

- Validar que carona pode ser cancelada apenas se ainda não tiver ocorrido.

### Solicitar vaga

- Validar que não é possível solicitar vaga em carona sem vagas disponíveis.

### Cancelar solicitação

- Validar que apenas o passageiro dono da solicitação pode cancelar.
- Validar que, ao cancelar solicitação confirmada, a vaga volta para a carona.

## Observações

- Os testes unitários focam na camada de serviço, não no acesso real ao banco de dados.
- A integração entre serviço e repositórios é verificada em testes de integração separados (`CaronaIntegrationTest`).
para regras de negocio

#