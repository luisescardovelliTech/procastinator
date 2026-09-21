# Procrastinator

App web em Java para organizar tarefas e brigar com a procrastinação: lista de tarefas,
timer de foco 25/5, recompensas, equipes e relatórios em PDF.

Projeto da faculdade (ADS, UniSALESIANO), feito em dupla com o
[Guilherme Delfino](https://github.com/Guilhermeghd), com branches e pull requests.

## Stack
- Java 17, Maven (empacotado como `.war`)
- Jakarta Servlet 6.1 + JSP/JSTL
- Hibernate 7 (JPA) + MySQL
- JasperReports para os relatórios em PDF
- jBCrypt para hash de senha, Lombok, Gson, jQuery

## O que tem
- Cadastro, login e controle de sessão, com filtro de autenticação nas rotas
- Tarefas com categoria, status e histórico
- Timer de foco no ciclo 25/5, com resumo ligado às tarefas
- Recompensas e mensagens de incentivo
- Equipes, com membros e papéis
- Estatísticas com KPIs e gráficos
- Relatórios em PDF (ranking e tarefas por equipe) gerados com JasperReports

## Como rodar
1. Crie o banco no MySQL: `CREATE DATABASE procastinator;` (as tabelas são criadas pelo Hibernate)
2. Copie `src/main/resources/hibernate.cfg.xml.example` para `hibernate.cfg.xml` e coloque o seu usuário e a sua senha
3. Gere o pacote: `./mvnw clean package`
4. Faça o deploy do `.war` em um servidor compatível com Jakarta Servlet 6.1 (usamos o Tomcat 11)

## Estrutura
```
src/main/java/com/example/procastinator/
├── model/     entidades JPA
├── dao/       acesso a dados (Hibernate)
├── servlet/   controladores (um por tela)
├── filter/    autenticação
├── report/    serviços de PDF (JasperReports)
├── web/       sessão, mensagens flash, estatísticas
└── util/      HibernateUtil, hash de senha
src/main/resources/reports/   templates .jrxml
src/main/webapp/              JSPs, CSS e JS
```

## Quem fez o quê
- **Luís:** criação do projeto e do banco, cadastro/login/sessão, timer 25/5, página de
  estatísticas com KPIs e gráficos, relatórios com JasperReports
- **Guilherme:** módulo de equipes e tarefas

## O que eu faria diferente hoje
- Não versionar `target/`, `.idea/` nem o `hibernate.cfg.xml` desde o começo (já estão fora, no `.gitignore`)
- Ler usuário e senha do banco de variáveis de ambiente em vez de arquivo
- Escrever testes para os DAOs (o JUnit já está no `pom.xml`, mas os testes não)
