Esta documentação foi elaborada para o arquivo `README.md` do seu repositório, focando na clareza para desenvolvedores Junior e destacando o funcionamento lógico da solução **Dev-Utils**.

---

# Dev-Utils

O **Dev-Utils** é uma ferramenta desenvolvida para facilitar a orquestração local de sistemas distribuídos em microsserviços. Ele atua como um ponto central (Proxy/Mock) que decide como as requisições devem ser tratadas, permitindo que você alterne entre serviços reais e respostas simuladas sem precisar alterar o código da sua aplicação principal.

## 🚀 Como o sistema funciona

O núcleo do projeto baseia-se em uma hierarquia de decisão para cada requisição recebida. Quando uma chamada chega ao Dev-Utils, ele segue esta ordem de prioridade:

1. **Mock Configs (Ativos):** O sistema verifica se existe uma resposta estática cadastrada para o caminho (path) e método HTTP solicitado. Se houver um Mock **ativo**, ele retorna o JSON configurado imediatamente.
2. **Proxy Routes (Ativos):** Caso não exista um mock ativo, o sistema busca uma rota de Proxy. Se o início da URL bater com um prefixo cadastrado e a rota estiver **ativa**, a requisição é redirecionada para o servidor (host) específico daquele serviço.
3. **Fallback (Última Instância):** Se nenhum mock ou rota for encontrado, o sistema utiliza a **Fallback URL**. Ela é o último destino consultado para garantir que a requisição não "se perca".

### ⚙️ A Natureza da Fallback URL

Diferente das rotas específicas, a **Fallback URL é Global**. Embora ela possa ser visualizada dentro da interface de uma Collection, ela não pertence exclusivamente a nenhuma delas. Alterar a Fallback URL afeta todo o ecossistema do Dev-Utils globalmente, ou seja, para todas as Collections.

## 🛠️ Recursos de Gerenciamento

* **Ativar/Inativar Mocks e Proxies:** Você pode "desligar" um mock ou uma rota sem precisar apagá-los. Isso permite que você teste o comportamento de um serviço real e mude para um mock em segundos apenas alterando o status.
* **Exclusão Completa:** O sistema permite a remoção de mocks, proxies e coleções inteiras. Ao excluir uma Collection, todos os itens vinculados a ela são removidos automaticamente.
* **Separação por Collections:** As Collections funcionam como pastas organizadoras. Você pode criar uma coleção para o "Projeto Financeiro" e outra para o "Projeto de Vendas", mantendo os endpoints de cada contexto isolados e organizados.

### 📦 Importação e Exportação

O Dev-Utils permite exportar suas configurações para um arquivo JSON e importá-las em outra instância.

* **Caso de Uso:** Um desenvolvedor pode configurar todo o cenário de erro de uma API complexa e exportar essa Collection para que o resto do time de front-end possa simular o mesmo erro em suas máquinas locais apenas importando o arquivo, garantindo padronização nos testes.

## 🛠️ Requisitos e Tecnologias

* **Java:** 17
* **Framework:** Spring Boot 3.3.6
* **Gerenciador de Dependências:** Maven
* **Banco de Dados:** SQLite (Armazenamento local simplificado)

## 🏃 Como Buildar e Rodar

Para executar o projeto em sua máquina, utilize os comandos Maven:

1. **Buildar o projeto:**
```bash
mvn clean install

```


2. **Executar a aplicação:**
```bash
mvn spring-boot:run

```



A aplicação estará disponível em `http://localhost:8080`.
