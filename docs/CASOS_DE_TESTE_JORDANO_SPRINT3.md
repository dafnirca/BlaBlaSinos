# Casos de Teste - Sprint 3 (Responsabilidade: Jordano)

Este documento detalha os casos de teste para as funcionalidades desenvolvidas nesta sprint.

## UC04 - Backend: Decisão de Solicitações (Aceite/Recusa)

| ID do Teste | Funcionalidade | Descrição do Teste | Passos para Execução | Resultado Esperado |
| :--- | :--- | :--- | :--- | :--- |
| **CT-JS301** | Aceitar solicitação de vaga | Verificar se o motorista consegue aceitar uma solicitação pendente. | 1. Criar carona com vagas disponíveis.<br>2. Passageiro solicita vaga (`POST /api/solicitacoes`).<br>3. Motorista aceita (`PUT /api/solicitacoes` com `acao=ACEITAR`). | A reserva passa para `CONFIRMADA`.<br>O backend retorna sucesso e persiste o novo status. |
| **CT-JS302** | Recusar solicitação de vaga | Verificar se o motorista consegue recusar uma solicitação pendente. | 1. Criar carona com solicitação pendente.<br>2. Motorista recusa (`PUT /api/solicitacoes` com `acao=RECUSAR`). | A reserva passa para `CANCELADA`.<br>O backend retorna sucesso e persiste o novo status. |
| **CT-JS303** | Atualização de vagas no aceite | Verificar se o número de vagas é decrementado ao aceitar. | 1. Registrar `vagas_disponiveis` da carona.<br>2. Aceitar uma solicitação pendente. | `vagas_disponiveis` é reduzido em 1 após o aceite. |
| **CT-JS304** | Vagas inalteradas na recusa | Verificar se a recusa não altera vagas disponíveis. | 1. Registrar `vagas_disponiveis` da carona.<br>2. Recusar uma solicitação pendente. | `vagas_disponiveis` permanece com o mesmo valor. |

## UC02 - Backend: Regras de Negócio e Persistência de Carona

| ID do Teste | Funcionalidade | Descrição do Teste | Passos para Execução | Resultado Esperado |
| :--- | :--- | :--- | :--- | :--- |
| **CT-JS305** | Obrigatoriedade de campus (origem/destino) | Verificar regra que exige campus válido em origem ou destino. | 1. Tentar cadastrar carona sem referência a São Leopoldo/Porto Alegre.<br>2. Tentar cadastrar apenas com "Unisinos" sem campus. | O backend rejeita as operações com erro da regra RN02.1. |
| **CT-JS306** | Motorista não solicita própria carona | Verificar bloqueio de auto-solicitação. | 1. Motorista cria uma carona.<br>2. Mesmo usuário tenta solicitar vaga na própria carona. | O backend retorna erro de regra de negócio.<br>Nenhuma reserva é criada. |
| **CT-JS307** | Persistência do valor da corrida | Verificar armazenamento e retorno do campo `valor`. | 1. Criar carona com valor informado.<br>2. Consultar a carona criada por ID (`GET /api/caronas?id=...`). | O valor é persistido e retornado corretamente na resposta. |
| **CT-JS308** | Edição de carona | Verificar se o motorista consegue editar carona futura respeitando regras. | 1. Criar carona futura.<br>2. Editar origem/destino/data/vagas (`PUT /api/caronas`). | Dados são atualizados com sucesso sem quebrar regras de negócio. |
| **CT-JS309** | Cancelamento de carona | Verificar se o motorista consegue cancelar carona futura. | 1. Criar carona futura.<br>2. Cancelar (`DELETE /api/caronas?id=...&motoristaId=...`). | Carona é removida/cancelada com resposta de sucesso. |

## UC03 - Frontend: Melhorias de Exibição

| ID do Teste | Funcionalidade | Descrição do Teste | Passos para Execução | Resultado Esperado |
| :--- | :--- | :--- | :--- | :--- |
| **CT-JS310** | Exibir valor em detalhes da carona | Verificar exibição do valor da corrida na tela de detalhes. | 1. Acessar `detalhe-carona.html?id=<id>` de carona com valor definido. | O valor aparece formatado corretamente no bloco de detalhes. |
| **CT-JS311** | Exibir nomes dos passageiros em solicitações | Verificar se solicitações mostram nomes em vez de apenas IDs. | 1. Criar solicitações pendentes de passageiros diferentes.<br>2. Abrir `solicitacoes.html`. | A coluna de passageiro exibe os nomes corretos dos usuários. |
| **CT-JS312** | Exibir data/hora e rota em solicitações pendentes | Verificar correção visual de data/hora e rota. | 1. Abrir `solicitacoes.html` com solicitações pendentes. | Cada linha mostra data/hora formatada e rota `Origem -> Destino`. |
| **CT-JS313** | Exibir nome do motorista na busca de caronas | Verificar exibição do nome do motorista em `buscar-caronas.html`. | 1. Acessar `buscar-caronas.html`.<br>2. Buscar/listar caronas disponíveis. | O nome do motorista aparece em cada item de carona exibido. |

## UC06 - Frontend e Backend: Sistema de Notificações (RN04.6)

| ID do Teste | Funcionalidade | Descrição do Teste | Passos para Execução | Resultado Esperado |
| :--- | :--- | :--- | :--- | :--- |
| **CT-JS314** | Notificar motorista sobre nova solicitação | Verificar notificação de nova solicitação para o motorista. | 1. Passageiro solicita vaga em carona.<br>2. Motorista abre o sino de notificações. | Notificação de `NOVA_SOLICITACAO` é exibida no dropdown com badge de não lidas. |
| **CT-JS315** | Notificar passageiro sobre aceite | Verificar notificação para passageiro quando solicitação é aceita. | 1. Passageiro solicita vaga.<br>2. Motorista aceita.<br>3. Passageiro abre notificações. | Notificação de aceite é exibida e pode ser marcada como lida. |
| **CT-JS316** | Notificar passageiro sobre recusa | Verificar notificação para passageiro quando solicitação é recusada. | 1. Passageiro solicita vaga.<br>2. Motorista recusa.<br>3. Passageiro abre notificações. | Notificação de recusa é exibida e pode ser marcada como lida. |
| **CT-JS317** | Redirecionar motorista ao clicar na notificação | Verificar navegação do motorista para tomada de decisão. | 1. Gerar notificação `NOVA_SOLICITACAO`.<br>2. Clicar nela no sino. | O sistema redireciona para `solicitacoes.html` para aprovar/recusar. |
| **CT-JS318** | Redirecionar passageiro ao clicar na notificação | Verificar navegação do passageiro para detalhes da carona. | 1. Gerar notificação de aceite ou recusa para passageiro.<br>2. Clicar na notificação no sino. | O sistema redireciona para `detalhe-carona.html?id=<caronaId>`. |
