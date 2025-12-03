from abc import ABC, abstractmethod
from functools import reduce

#Classe abstrata (não instanciada)
class ItemEstoque(ABC):
    @abstractmethod
    def calcular_valor(self):
        pass

    @abstractmethod
    def descricao(self):
        pass

#Classe base (herda da classe abstrata)

class Produto(ItemEstoque):
    def __init__(self, codigo, nome, quantidade, preco):
        self._codigo = codigo
        self._nome = nome
        self._quantidade = quantidade
        self._preco = preco
        self._preco_original = preco

#Encapsulamento (getters e setters)
    def get_codigo(self):
        return self._codigo

    def get_nome(self):
        return self._nome

    def set_nome(self, nome):
        if len(nome) < 2:
            raise ValueError("O nome deve ter pelo menos 2 caracteres.")
        self._nome = nome

    def get_quantidade(self):
        return self._quantidade

    def set_quantidade(self, quantidade):
        if quantidade < 0:
            raise ValueError("A quantidade não pode ser negativa.")
        self._quantidade = quantidade

    def get_preco(self):
        return self._preco

    def set_preco(self, preco):
        if preco < 0:
            raise ValueError("O preço não pode ser negativo.")
        self._preco = preco

    def get_preco_original(self):
        return self._preco_original

#Método obrigatório vindo da classe abstrata
    def calcular_valor(self):
        return self._quantidade * self._preco

#Método obrigatório da classe abstrata (polimórfico)
    def descricao(self):
        return f"Produto comum: {self._nome} — R$ {self._preco:.2f}"

#Classe derivada (herença = polimofismo)
class ProdutoComDesconto(Produto):
    def __init__(self, codigo, nome, quantidade, preco, percentual):
        super().__init__(codigo, nome, quantidade, preco)
        self._percentual = percentual
        self._preco = self.calcular_preco_descontado()

    def calcular_preco_descontado(self):
        return self._preco_original - (self._preco_original * (self._percentual / 100))

    def get_percentual(self):
        return self._percentual

    # Polimorfismo — sobrescreve método da classe pai
    def calcular_valor(self):
        return self._quantidade * self._preco

    # Polimorfismo — sobrescreve método da classe pai
    def descricao(self):
        return (f"Produto com desconto: {self._nome} — "
                f"{self._percentual}% OFF — R$ {self._preco:.2f}")

#Lista de produtos

produtos = []

#Funções puras
def calcular_desconto(preco, percentual):
    return preco - (preco * (percentual / 100.0))


def remover_desconto(preco, preco_original):
    return preco_original


def estoque_baixo(quantidade, limite):
    return quantidade < limite


#Funções de ordem superior
def aplicar_funcao_nos_precos(lista_produtos, fn):
    return list(map(fn, lista_produtos))


def filtrar_produtos(lista_produtos, predicado):
    return list(filter(predicado, lista_produtos))


def reduzir_total(lista, funcao, inicial):
    return reduce(funcao, lista, inicial)


#Funções auxiliares
def buscar_produto_por_codigo(lista, codigo):
    for p in lista:
        if p.get_codigo() == codigo:
            return p
    return None

#Funções principais
def cadastrar_produtos():
    global produtos
    num = int(input("\nQuantos produtos deseja cadastrar? "))
    novos = []

    for i in range(num):
        print(f"\nProduto {i + 1}:")
        codigo = int(input("Código: "))
        nome = input("Nome: ")
        quantidade = int(input("Quantidade inicial: "))
        preco = float(input("Preço unitário: R$ "))

        novos.append(Produto(codigo, nome, quantidade, preco))

    produtos = produtos + novos
    print("\nCadastro concluído!\n")


def movimentar_estoque():
    global produtos
    codigo = int(input("\nDigite o código do produto: "))
    produto = buscar_produto_por_codigo(produtos, codigo)

    if not produto:
        print("Produto não encontrado!")
        return

    print(f"Produto encontrado: {produto.get_nome()}")
    opcao = int(input("1. Entrada\n2. Saída\nEscolha: "))

    if opcao == 1:
        qtd = int(input("Quantidade de entrada: "))
        produto.set_quantidade(produto.get_quantidade() + qtd)
        print("Entrada realizada!")

    elif opcao == 2:
        qtd = int(input("Quantidade de saída: "))
        if qtd <= produto.get_quantidade():
            produto.set_quantidade(produto.get_quantidade() - qtd)
            print("Saída realizada!")
        else:
            print("Quantidade insuficiente em estoque!")
    else:
        print("Opção inválida.")


def calcular_total_itens():
    return reduzir_total(
        [p.get_quantidade() for p in produtos],
        lambda acc, x: acc + x,
        0
    )


def calcular_valor_total():
    return reduzir_total(
        [p.calcular_valor() for p in produtos],
        lambda acc, x: acc + x,
        0.0
    )

#Recursos exigidos
def aplicar_desconto_todos():
    global produtos
    desconto = float(input("\nDigite o percentual de desconto: "))

    def aplicar(p):
        return ProdutoComDesconto(
            p.get_codigo(),
            p.get_nome(),
            p.get_quantidade(),
            p.get_preco(),
            desconto
        )

    produtos = aplicar_funcao_nos_precos(produtos, aplicar)
    print("\nDesconto aplicado a todos os produtos!\n")


def aplicar_desconto_individual():
    global produtos
    codigo = int(input("\nDigite o código do produto para aplicar o desconto: "))
    produto = buscar_produto_por_codigo(produtos, codigo)

    if not produto:
        print("Produto não encontrado!")
        return

    desconto = float(input("Digite o percentual de desconto: "))

    novo = ProdutoComDesconto(
        produto.get_codigo(),
        produto.get_nome(),
        produto.get_quantidade(),
        produto.get_preco(),
        desconto
    )

    produtos = [novo if p.get_codigo() == codigo else p for p in produtos]

    print(f"\nDesconto aplicado ao produto {produto.get_nome()}!\n")


def remover_descontos():
    global produtos
    produtos = [
        Produto(
            p.get_codigo(),
            p.get_nome(),
            p.get_quantidade(),
            p.get_preco_original()
        )
        for p in produtos
    ]
    print("\nTodos os produtos voltaram ao preço original!\n")


def relatorio_estoque():
    if not produtos:
        print("Nenhum produto cadastrado.")
        return

    print("\n=== RELATÓRIO DE ESTOQUE ===")
    for p in produtos:
        print(f"{p.descricao()} — Quantidade: {p.get_quantidade()}")

    print(f"\nTotal de itens: {calcular_total_itens()}")
    print(f"Valor total do estoque: R$ {calcular_valor_total():.2f}")

    limite_str = input("\nDeseja ver produtos com baixo estoque? (digite limite mínimo ou ENTER para pular): ")

    if limite_str.strip() == "":
        return

    limite = int(limite_str)
    baixos = filtrar_produtos(produtos, lambda p: estoque_baixo(p.get_quantidade(), limite))

    print("\n=== PRODUTOS COM BAIXO ESTOQUE ===")
    for p in baixos:
        print(f"{p.get_nome()} - {p.get_quantidade()} unidades")

#Função principal (interface de console)
def main():
    while True:
        print("\n==== SISTEMA DE ESTOQUE ====")
        print("1. Cadastrar produtos")
        print("2. Entrada/Saída de estoque")
        print("3. Aplicar desconto em TODOS os produtos")
        print("4. Aplicar desconto em UM produto")
        print("5. Remover descontos")
        print("6. Relatório do estoque")
        print("7. Sair")

        opcao = input("Escolha uma opção: ")

        if opcao == "1":
            cadastrar_produtos()
        elif opcao == "2":
            movimentar_estoque()
        elif opcao == "3":
            aplicar_desconto_todos()
        elif opcao == "4":
            aplicar_desconto_individual()
        elif opcao == "5":
            remover_descontos()
        elif opcao == "6":
            relatorio_estoque()
        elif opcao == "7":
            print("Saindo...")
            break
        else:
            print("Opção inválida!")


if __name__ == "__main__":
    main()
