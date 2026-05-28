# Testes de Integração para o Fluxo Principal do Projeto

Este documento detalha os testes de integração que confirmam o comportamento do sistema no fluxo principal de caronas.

## Objetivo

Validar a integração entre as camadas de serviço e repositório para os principais cenários de uso: cadastro de carona, solicitação de vaga, decisão de reserva e atualização de vagas.

## Escopo

O documento cobre os fluxos de ponta a ponta para:

- cadastro de carona por motorista válido;
- solicitação de vaga por passageiro;
- listagem de solicitações pendentes pelo motorista;
- aceitação de solicitação com decremento de vagas disponíveis;
- visualização de reservas confirmadas pelo passageiro.

## Ambiente de Teste

Os testes de integração usam implementações em memória ou de banco leves de repositório para simular operações reais sem depender de interfaces de usuário.

### Classe principal de testes

- `src/test/java/br/blablasinos/service/CaronaIntegrationTest.java`

### Repositórios envolvidos

- `CaronaRepository` / `FakeCaronaRepository`
- `UsuarioRepository` / `InMemoryUsuarioRepository`
- `ReservaRepository` / `InMemoryReservaRepository`

## Cenário principal

### 1. Cadastro de carona

- Preparação: cadastrar motorista com dados completos (`CNH`, `modelo`, `cor`, `placa`).
- Ação: cadastrar carona com origem e destino válidos e horário futuro.
- Resultado esperado: carona criada com ID não nulo e vagas disponíveis iguais a vagas totais.

### 2. Solicitação de vaga

- Preparação: cadastrar passageiro válido.
- Ação: passageiro solicita vaga na carona criada.
- Resultado esperado: reserva criada com status `PENDENTE`.

### 3. Listagem de solicitações pendentes

- Ação: motorista consulta solicitações pendentes.
- Resultado esperado: retorno de ao menos uma reserva com status `PENDENTE`.

### 4. Aceitação da solicitação

- Ação: motorista aceita a solicitação pendente.
- Resultado esperado:
  - reserva atualizada para status `CONFIRMADA`;
  - a carona tem `vagasDisponiveis` decrementada em 1.

### 5. Verificação final pelo passageiro

- Ação: passageiro lista suas reservas.
- Resultado esperado: presença de reserva com status `CONFIRMADA`.

## Observações

- Esse teste valida regras de negócio integradas com armazenamento e atualização de estado.
- O foco está em garantir que o ciclo completo de oferta e aceitação de carona funcione do início ao fim.
