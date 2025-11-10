# Sistema de Estoque Funcional (Python, Java e C)

Este projeto implementa um **sistema de controle de estoque** em três linguagens — **Python**, **Java** e **C** — seguindo os **princípios do paradigma funcional**, como **funções puras**, **imutabilidade** e **funções de ordem superior**.

O sistema permite:
- Inserir e remover produtos do estoque  
- Filtrar produtos com baixo estoque  
- Aplicar e remover descontos em produtos  
- Calcular o valor total e a quantidade total de itens
- Gerar relatório dos produtos em estoque

---

## Conceitos Funcionais Aplicados

### Funções Puras
Cada linguagem contém **pelo menos duas funções puras**, ou seja:
- **Não possuem efeitos colaterais**
- **Não modificam dados externos**
- **Com a mesma entrada, produzem a mesma saída**

**Exemplos:**
- `calcularDesconto(preco, desconto)`  
- `filtrarBaixoEstoque(produtos, limite)`  

Essas funções não alteram o estado global do programa e retornam sempre os mesmos resultados.

---

### Imutabilidade
As estruturas de dados são manipuladas **sem mutação direta**:
- **Python:** novas listas são criadas a partir de listas originais (usando `map`, `filter` e `copy`).
- **Java:** uso de `List.copyOf()`, `Stream` e `record` para representar dados imutáveis.
- **C:** simulação de imutabilidade com cópias de estruturas e retorno de novos arrays.

Isso garante que as coleções originais permaneçam intactas, mesmo após transformações.

---

### Funções de Ordem Superior
O código faz uso de:
- `map`, `filter` e `reduce` (ou equivalentes)  
- Funções que **recebem outras funções como parâmetro** (ex.: `aplicarFuncaoNosPrecos`)  
- **Closures** e funções retornadas (em Python e Java)

Essas técnicas permitem abstrair operações genéricas, tornando o código mais expressivo e modular.

---

## Funcionalidades do Sistema

| Função | Descrição |
|--------|------------|
| `inserirProduto()` | Adiciona novo item ao estoque |
| `retirarProduto()` | Remove item do estoque |
| `filtrarBaixoEstoque()` | Retorna produtos com quantidade abaixo de um limite |
| `calcularDesconto()` | Calcula o novo preço com desconto |
| `aplicarDescontoProduto()` | Aplica desconto em **apenas um produto** |
| `removerDescontoProduto()` | Remove o desconto aplicado em um produto |
| `calcularTotais()` | Retorna o total de itens e valor do estoque |

---

## Tecnologias Utilizadas

Python 3.10+

Java 17+

C99

Paradigma funcional aplicado em linguagens imperativas

---

## Licença

Este projeto é distribuído sob a licença MIT.
Sinta-se livre para usar, estudar e modificar para fins educacionais.