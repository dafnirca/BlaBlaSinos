# 🚗 BlaBlaSinos

> Sistema de caronas compartilhadas para a comunidade universitária do campus São Leopoldo da Universidade do Vale do Rio dos Sinos (Unisinos).

Conecta motoristas e passageiros da comunidade Unisinos — alunos, professores e funcionários — para o compartilhamento de deslocamentos até o campus, promovendo mobilidade sustentável, redução de custos e integração entre os membros da universidade.

**Disciplina:** Implementação de Software — Unisinos  
**Equipe:** Dafni Rosa · Gabriel Kaiper · Jordano Rodrigues  
**Status:** 🟡 Em desenvolvimento (Sprint 1)

---

## Funcionalidades do MVP

- Autenticação restrita a e-mails institucionais (`@edu.unisinos.br`)
- Cadastro de perfil como **motorista** (com dados de veículo) ou **passageiro**
- Oferta de caronas com origem, destino, data, horário e número de vagas
- Busca de caronas disponíveis com filtros por destino e data
- Solicitação de vaga com reserva temporária de 30 minutos
- Aceite ou recusa de solicitações pelo motorista
- Cancelamento com regra de 1h de antecedência após confirmação
- Notificações internas no sistema

---

## Tecnologias utilizadas

| Categoria | Tecnologia |
|-----------|-----------|
| Linguagem | Java 17+ |
| Interface gráfica | Java Swing |
| Banco de dados | SQLite (via `sqlite-jdbc`) |
| Testes | JUnit 5 |
| Build | Maven |
| Controle de versão | Git + GitHub |
| Arquitetura | MVC + Service Layer |

---

## Pré-requisitos

Antes de configurar o projeto, certifique-se de ter instalado:

- **JDK 17 ou superior**  
  Verifique com: `java -version`  
  Download: https://adoptium.net

- **Maven 3.8 ou superior**  
  Verifique com: `mvn -version`  
  Download: https://maven.apache.org/download.cgi

- **Git**  
  Verifique com: `git --version`  
  Download: https://git-scm.com

> **IDEs recomendadas:** IntelliJ IDEA Community ou Eclipse IDE for Java Developers.  
> O banco de dados SQLite é embutido — não requer instalação separada.

---

## Configuração do ambiente de desenvolvimento

### 1. Clonar o repositório

```bash
git clone https://github.com/seu-usuario/caronas-unisinos.git
cd caronas-unisinos
```

### 2. Fazer checkout na branch de desenvolvimento

```bash
git checkout develop
```

### 3. Instalar as dependências e compilar

```bash
mvn clean install
```

Isso irá baixar as dependências declaradas no `pom.xml` (incluindo `sqlite-jdbc` e `junit-jupiter`) e compilar o projeto.

### 4. Executar a aplicação

```bash
mvn exec:java -Dexec.mainClass="br.unisinos.caronas.Main"
```

Ou, se preferir gerar o JAR executável primeiro:

```bash
mvn package
java -jar target/caronas-unisinos-1.0-SNAPSHOT.jar
```

Na primeira execução, o sistema criará automaticamente o arquivo de banco de dados `caronas.db` na raiz do projeto e aplicará o schema inicial (`src/main/resources/schema.sql`).

### 5. Executar os testes

```bash
mvn test
```

Os testes unitários ficam em `src/test/java/` e cobrem as camadas `service/` e `repository/`.

---

## Estrutura do projeto

```
caronas-unisinos/
├── src/
│   ├── main/
│   │   ├── java/br/unisinos/caronas/
│   │   │   ├── config/        # DatabaseConfig, AppConfig
│   │   │   ├── model/         # Entidades: Usuario, Carona, Reserva
│   │   │   ├── repository/    # Acesso ao SQLite via JDBC
│   │   │   ├── service/       # Regras de negócio (casos de uso)
│   │   │   ├── handler/       # Controladores MVC
│   │   │   └── view/          # Interfaces gráficas (Swing)
│   │   └── resources/
│   │       ├── schema.sql     # DDL do banco de dados
│   │       └── app.properties
│   └── test/
│       └── java/br/unisinos/caronas/
│           ├── service/       # Testes unitários
│           └── repository/    # Testes de integração
└── docs/                      # Diagramas e documentação técnica
```

Consulte o arquivo [`docs/arquitetura.png`](docs/arquitetura.png) para o diagrama completo das camadas.

---

## Fluxo de contribuição

Este projeto usa revisão cruzada obrigatória entre pares. Nenhum integrante faz merge do próprio PR.

```
feature/branch  →  Pull Request  →  revisão de outro integrante  →  merge na develop
```

Consulte a seção **Fluxo de Pull Requests** no README para o passo a passo completo de branches, commits e critérios de revisão.

---

## Sprints

| Sprint | Foco | Status |
|--------|------|--------|
| Sprint 1 | Planejamento — escopo, ambiente, modelagem e documentação base | 🟡 Em andamento |
| Sprint 2 | Núcleo funcional — autenticação, cadastro de caronas e persistência | ⬜ Aguardando |
| Sprint 3 | Funcionalidades completas — fluxo do passageiro, notificações e testes | ⬜ Aguardando |
| Sprint 4 | Entrega final — polimento, documentação técnica e apresentação | ⬜ Aguardando |

---

## Documentação

| Documento | Descrição |
|-----------|-----------|
| [`docs/modelo-er.png`](docs/modelo-er.png) | Diagrama Entidade-Relacionamento |
| [`docs/diagrama-casos-de-uso.png`](docs/diagrama-casos-de-uso.png) | Diagrama UML de casos de uso |
| [`docs/arquitetura.png`](docs/arquitetura.png) | Diagrama de arquitetura MVC + Service Layer |
| [`docs/manual-usuario.md`](docs/manual-usuario.md) | Manual do usuário *(entregue no Sprint 4)* |

---

## Licença

Projeto acadêmico desenvolvido para a disciplina de Implementação de Software — Unisinos.  
Uso restrito à avaliação acadêmica.
