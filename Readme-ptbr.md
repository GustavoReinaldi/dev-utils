# Dev-Utils

**Orquestrador central de requisições para ambientes de microsserviços.** Evite alterar o código da sua aplicação principal alternando instantaneamente entre serviços reais e respostas simuladas (Mocks).

## 🚀 O que ele faz?

O Dev-Utils atua como um **Proxy/Mock inteligente** que intercepta chamadas e decide o destino com base em prioridades:

1. **Mock (Ativo):** Se houver um Mock cadastrado para o endpoint, retorna o JSON estático imediatamente.
2. **Proxy (Ativo):** Se não houver Mock, redireciona para o `host` configurado na rota.
3. **Fallback (Global):** Se nada acima coincidir, envia a requisição para uma URL padrão global.

## 🛠️ Como usar

1. **Crie uma Collection:** Agrupe seus endpoints por projeto (ex: "Financeiro", "Vendas").
2. **Configure Mocks:** Cadastre o Path, Método HTTP e o JSON de resposta para testes rápidos.
3. **Defina Rotas:** Aponte prefixos de URL para serviços reais rodando localmente ou em staging.
4. **Alterne em segundos:** Ative ou inative qualquer regra via interface sem reiniciar nada.

## 📦 Importação e Exportação

* **Padronização:** Exporte suas configurações em JSON e compartilhe com o time para que todos testem o mesmo cenário de erro ou sucesso.

## 🏃 Execução Rápida

* **Requisitos:** Java 17 e Maven.
* **Build:** `mvn clean install`
* **Rodar:** `mvn spring-boot:run`
* **Acesso:** `http://localhost:8080`
