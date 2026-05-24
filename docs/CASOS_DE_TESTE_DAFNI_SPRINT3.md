# Casos de Teste - Sprint 3 (Responsabilidade: Dafni Rosa)

Este documento detalha os casos de teste para as funcionalidades do fluxo de solicitação de vagas, implementadas na Sprint 3.

## UC04 - Solicitar Vaga em Carona

| ID do Teste | Funcionalidade | Descrição do Teste | Passos para Execução | Resultado Esperado |
| :--- | :--- | :--- | :--- | :--- |
| **CT-D06** | Solicitação com Sucesso | Verificar se um passageiro logado consegue solicitar uma vaga em uma carona disponível. | 1. Fazer login como **Passageiro**.<br>2. Buscar uma carona com vagas disponíveis.<br>3. Clicar na carona para ver os detalhes.<br>4. Clicar no botão "Criar Solicitação". | **Frontend:** Um alerta de sucesso é exibido. A UI muda para "Aguardando confirmação..." e o botão "Cancelar Solicitação" aparece.<br>**Backend:** Uma nova linha é criada na tabela `reservas` com o status `PENDENTE`. O número de `vagas_disponiveis` na tabela `caronas` é decrementado em 1. |
| **CT-D07** | Solicitação em Carona Lotada | Verificar se o sistema impede a solicitação de vaga em uma carona que não tem mais vagas disponíveis. | 1. Repetir os passos do teste CT-D06 até que todas as vagas de uma carona sejam preenchidas.<br>2. Com outro usuário **Passageiro**, tentar solicitar uma vaga na mesma carona (agora lotada). | **Frontend:** Um alerta de erro é exibido com a mensagem "Não há vagas disponíveis nesta carona."<br>**Backend:** Nenhuma nova reserva é criada. O número de vagas não é alterado. |

## UC05 - Cancelar Solicitação de Vaga

| ID do Teste | Funcionalidade | Descrição do Teste | Passos para Execução | Resultado Esperado |
| :--- | :--- | :--- | :--- | :--- |
| **CT-D08** | Cancelamento com Sucesso | Verificar se um passageiro consegue cancelar uma solicitação com status "Pendente". | 1. Executar os passos do teste CT-D06 para criar uma solicitação.<br>2. Na tela de detalhes da carona, clicar no botão "Cancelar Solicitação". | **Frontend:** Um alerta de sucesso é exibido. A UI retorna ao estado inicial, mostrando o botão "Criar Solicitação".<br>**Backend:** A linha correspondente na tabela `reservas` é deletada. O número de `vagas_disponiveis` na tabela `caronas` é incrementado em 1 (a vaga é devolvida). |