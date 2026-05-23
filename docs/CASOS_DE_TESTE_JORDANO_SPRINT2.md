# Casos de Teste - Sprint 2 (Responsabilidade: Jordano)

Este documento detalha os casos de teste para as funcionalidades desenvolvidas nesta sprint.

## UC01 - Frontend: Telas de Login e Cadastro

| ID do Teste | Funcionalidade | Descrição do Teste | Passos para Execução | Resultado Esperado |
| :--- | :--- | :--- | :--- | :--- |
| **CT-D01** | Carregar tela de Login | Verificar se a página de login é exibida corretamente. | 1. Acessar `login.html`. | A página é carregada sem erros.<br>Os campos `email` e `senha` são exibidos e o botão de login está habilitado. |
| **CT-D02** | Navegar para cadastro | Verificar se o link/botão de cadastro funciona na tela de login. | 1. Acessar `login.html`.<br>2. Clicar em `Criar conta` ou `Cadastrar`. | O navegador abre a página `cadastro.html` ou o formulário de cadastro é exibido. |
| **CT-D03** | Formulário de cadastro | Verificar se o formulário de cadastro valida os campos obrigatórios. | 1. Acessar `cadastro.html`.<br>2. Deixar campos obrigatórios vazios.<br>3. Tentar enviar o formulário. | O sistema indica erros nos campos obrigatórios e não envia a requisição. |
| **CT-D04** | Envio de cadastro (UI) | Verificar se o formulário de cadastro monta corretamente os dados para envio. | 1. Acessar `cadastro.html`.<br>2. Preencher nome, email, senha e confirmar senha.<br>3. Enviar o formulário. | O front-end envia uma requisição para `/api/cadastro` com o JSON correto.<br>Uma mensagem de sucesso ou redirecionamento é exibido. |

## UC09 - Frontend: Tela de Gerenciamento de Perfil (CNH, Veículo)

| ID do Teste | Funcionalidade | Descrição do Teste | Passos para Execução | Resultado Esperado |
| :--- | :--- | :--- | :--- | :--- |
| **CT-D05** | Carregar tela de perfil | Verificar se a tela de perfil exibe os campos de CNH e veículo. | 1. Acessar `perfil.html`. | A página é exibida com os campos `CNH`, `Modelo do veículo`, `Cor do veículo` e `Placa do veículo`. |
| **CT-D06** | Exibir dados existentes | Verificar se os dados atuais do perfil são carregados no formulário. | 1. Acessar `perfil.html` com usuário autenticado.<br>2. Observar o formulário. | Os campos de CNH e veículo aparecem preenchidos com os dados do usuário. |
| **CT-D07** | Atualizar perfil | Verificar se a ação de salvar perfil dispara a requisição correta. | 1. Alterar os dados de CNH e veículo.<br>2. Clicar em `Salvar` ou `Atualizar`. | O front-end envia uma requisição `PUT` para `/api/perfil` com o JSON atualizado.<br>Uma confirmação de sucesso é exibida. |

## UC03 - Frontend: Tela de Busca de Caronas com Filtros (Passageiro)

| ID do Teste | Funcionalidade | Descrição do Teste | Passos para Execução | Resultado Esperado |
| :--- | :--- | :--- | :--- | :--- |
| **CT-D08** | Carregar tela de busca | Verificar se a tela de busca de caronas é exibida corretamente. | 1. Acessar `buscar-caronas.html`. | A página é exibida sem erros.<br>Os campos de filtro `origem`, `destino` e `data` estão visíveis. |
| **CT-D09** | Listagem inicial de caronas | Verificar se a tela exibe caronas disponíveis ao abrir. | 1. Acessar `buscar-caronas.html`. | A lista de caronas aparece na página.<br>Cada carona mostra origem, destino, data e vagas disponíveis. |
| **CT-D10** | Filtro de caronas | Verificar se os filtros atualizam a lista de caronas. | 1. Acessar `buscar-caronas.html`.<br>2. Preencher filtro de origem/destino.<br>3. Aplicar filtro. | A lista é atualizada apenas com as caronas que correspondem aos filtros selecionados. |

## UC01 - Backend: Cadastro de Usuário (RN01.1, RN01.2)

| ID do Teste | Funcionalidade | Descrição do Teste | Passos para Execução | Resultado Esperado |
| :--- | :--- | :--- | :--- | :--- |
| **CT-D11** | Cadastro de usuário válido | Verificar se o backend cadastra um novo usuário corretamente. | 1. Enviar uma requisição `POST` para `/api/cadastro` com JSON contendo `nome`, `email`, `senha` e outros campos obrigatórios.<br>2. Confirmar que o status retornado é `201 Created` ou `200 OK`. | A resposta retorna sucesso.<br>O usuário é salvo no banco de dados e pode ser encontrado pela chave `email`. |
| **CT-D12** | Cadastro de usuário duplicado | Verificar se o backend rejeita cadastro com email já existente. | 1. Enviar `POST /api/cadastro` com email já cadastrado. | A resposta retorna erro `409 Conflict` ou mensagem de usuário existente.<br>Nenhum novo usuário é criado no banco de dados. |

## UC01 - Backend: Autenticação de Usuário (RN01.3)

| ID do Teste | Funcionalidade | Descrição do Teste | Passos para Execução | Resultado Esperado |
| :--- | :--- | :--- | :--- | :--- |
| **CT-D13** | Login com credenciais válidas | Verificar se o backend autentica usuário com email e senha corretos. | 1. Enviar `POST /api/login` com JSON contendo `email` e `senha` válidos. | A resposta retorna status `200 OK`.<br>O corpo da resposta indica autenticação bem-sucedida (por exemplo, token, sessão ou mensagem de sucesso). |
| **CT-D14** | Login com credenciais inválidas | Verificar se o backend rejeita credenciais incorretas. | 1. Enviar `POST /api/login` com email ou senha incorretos. | A resposta retorna `401 Unauthorized` ou mensagem de falha de autenticação.<br>O usuário não é autenticado. |