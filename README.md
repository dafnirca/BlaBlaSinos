# 🚗 BlaBlaSinos — Sistema de Caronas Compartilhadas

Projeto finalizado: versão final do sistema acadêmico de caronas para a comunidade do campus São Leopoldo (Unisinos).

Resumo: conecta motoristas e passageiros da comunidade universitária para oferta e reserva de caronas, com regras de negócio para reservas temporárias, cancelamentos e notificações internas.

Status do repositório: Versão final entregue (Sprint 4 concluída).

Principais funcionalidades

- Autenticação com restrição a e-mails institucionais (`@edu.unisinos.br`).
- Cadastro de perfil como motorista (com dados do veículo) ou passageiro.
- Oferta de caronas com origem, destino, data, horário e número de vagas.
- Busca de caronas por destino e data, com filtros úteis.
- Solicitação de vaga com reserva temporária (30 minutos) e aceite/recusa pelo motorista.
- Política de cancelamento com regra de 1 hora de antecedência após confirmação.
- Notificações internas e gerenciamento de solicitações.

Tecnologias

- Linguagem: Java 17+
- Interface: Java Swing (GUI)
- Banco de dados: SQLite (sqlite-jdbc)
- Testes: JUnit 5
- Build: Maven
- Arquitetura: MVC + Service Layer

Pré-requisitos

- JDK 17+ (verifique com `java -version`)
- Maven 3.8+ (verifique com `mvn -version`)
- Git (verifique com `git --version`)

Como compilar e executar

1) Clonar o repositório e entrar na pasta:

```
git clone https://github.com/dafnirca/BlaBlaSinos.git
cd BlaBlaSinos
```

2) Compilar o projeto:

```
mvn clean install
```

3) Executar diretamente com Maven:

```
mvn exec:java -Dexec.mainClass="br.blablasinos.Main"
```

Ou gerar e executar o JAR:

```
mvn package
java -jar target/blablasinos-1.0-SNAPSHOT.jar
```

Observação: na primeira execução o arquivo de banco `caronas.db` é criado automaticamente usando o DDL em `src/main/resources/schema.sql`.

Testes

```
mvn test
```

Estrutura do projeto

```
blablasinos/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/blablasinos/
│   │   │       ├── config/
│   │   │       ├── exception/
│   │   │       ├── handler/
│   │   │       ├── model/
│   │   │       ├── repository/
│   │   │       ├── service/
│   │   │       ├── validation/
│   │   │       └── view/
│   │   └── resources/
│   │       ├── app.properties
│   │       ├── schema.sql
│   │       └── static/
│   │           ├── css/
│   │           ├── js/
│   │           └── *.html
│   └── test/
│       └── java/
│           └── br/blablasinos/
│               ├── repository/
│               ├── service/
│               └── validation/
├── target/ (artefatos gerados)
└── docs/ (diagramas e documentação técnica)
```

Contribuição

Fluxo esperado para contribuições:

```
feature/branch → Pull Request → revisão por outro integrante → merge na `develop`
```

Observação: nenhum integrante deve aprovar/mergear o próprio PR.

Equipe e créditos

- Dafni Rosa
- Gabriel Kaiper
- Jordano Rodrigues

Licença

Projeto acadêmico desenvolvido para avaliação na disciplina de Implementação de Software — Unisinos. Uso restrito à avaliação acadêmica.

Contato

Para dúvidas ou solicitações, abra uma issue no repositório ou contate os mantenedores via e-mail listado nos commits.

