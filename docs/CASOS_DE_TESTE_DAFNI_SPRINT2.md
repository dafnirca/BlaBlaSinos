# Casos de Teste - Sprint 2 (Responsabilidade: Dafni Rosa)

Este documento detalha os casos de teste para as funcionalidades desenvolvidas nesta sprint.

## UC09 - Gerenciar Perfil de Usuário (Backend)

| ID do Teste | Funcionalidade | Descrição do Teste | Passos para Execução | Resultado Esperado |
| :--- | :--- | :--- | :--- | :--- |
| **CT-D01** | Atualização de Perfil | Verificar se o backend salva corretamente os dados do perfil do motorista no banco de dados. | 1. Enviar uma requisição `PUT` para o endpoint `/api/perfil`.<br>2. O corpo da requisição deve conter um JSON com `id`, `nome`, `cnh`, `modeloVeiculo`, `corVeiculo` e `placaVeiculo`. | A requisição retorna status `200 OK`.<br>Ao inspecionar o banco de dados, a linha do usuário correspondente ao `id` enviado está com as colunas `cnh`, `modelo_veiculo`, etc., atualizadas. |

## UC02/UC07 - Gerenciar Caronas (Frontend com Mock)

| ID do Teste | Funcionalidade | Descrição do Teste | Passos para Execução | Resultado Esperado |
| :--- | :--- | :--- | :--- | :--- |
| **CT-D02** | Visualização da Tela | Verificar se a tela de gerenciamento de caronas carrega e exibe os dados mockados corretamente. | 1. Acessar a página `gerenciar-caronas.html`. | A página é renderizada sem erros.<br>A tabela exibe a lista de caronas definida no array `caronasMock` do arquivo JS. |
| **CT-D03** | Adicionar Nova Carona (UI) | Verificar se o modal para adicionar uma nova carona é aberto corretamente. | 1. Na tela "Minhas Caronas", clicar no botão "Oferecer Nova Carona". | O modal (pop-up) é exibido com o formulário para cadastro de carona com os campos vazios. |
| **CT-D04** | Editar Carona (UI) | Verificar se o modal de edição é aberto com os dados da carona selecionada. | 1. Na tela "Minhas Caronas", clicar no botão "Editar" de uma carona existente. | O modal é exibido com os campos do formulário preenchidos com as informações da carona selecionada. |
| **CT-D05** | Cancelar Carona (UI) | Verificar se a confirmação de cancelamento é acionada. | 1. Na tela "Minhas Caronas", clicar no botão "Cancelar" de uma carona. | Uma caixa de diálogo de confirmação do navegador (`confirm()`) é exibida com a mensagem de confirmação. |