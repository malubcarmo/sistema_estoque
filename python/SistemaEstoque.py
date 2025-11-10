from functools import reduce

# ==========================
# ESTRUTURA DE DADOS
# ==========================

class Produto:
    def __init__(self, codigo, nome, quantidade, preco):
        self.codigo = codigo
        self.nome = nome
        self.quantidade = quantidade
        self.preco = preco
        self.preco_original = preco

produtos = []


# ==========================
# FUNÇÕES PURAS
# ==========================

def calcular_desconto(preco, percentual):
    return preco - (preco * (percentual / 100.0))

def remover_desconto(preco_com_desconto, preco_original):
    return preco_original

def estoque_baixo(quantidade, limite):
    return quantidade < limite


# ==========================
# FUNÇÕES DE ORDEM SUPERIOR
# ==========================

def aplicar_funcao_nos_precos(lista_produtos, fn):
    return list(map(fn, lista_produtos))


def filtrar_produtos(lista_produtos, predicado):
    return list(filter(predicado, lista_produtos))


def reduzir_total(lista, funcao, inicial):
    return reduce(funcao, lista, inicial)


# ==========================
# FUNÇÕES AUXILIARES
# ==========================

def buscar_produto_por_codigo(lista, codigo):
    for p in lista:
        if p.codigo == codigo:
            return p
    return None

# ==========================
# FUNÇÕES PRINCIPAIS
# ==========================

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

    # Imutabilidade — cria nova lista
    produtos = produtos + novos
    print("\nCadastro concluído!\n")


def movimentar_estoque():
    global produtos
    codigo = int(input("\nDigite o código do produto: "))
    produto = buscar_produto_por_codigo(produtos, codigo)

    if not produto:
        print("Produto não encontrado!")
        return

    print(f"Produto encontrado: {produto.nome}")
    opcao = int(input("1. Entrada\n2. Saída\nEscolha: "))

    if opcao == 1:
        qtd = int(input("Quantidade de entrada: "))
        produto.quantidade += qtd
        print("Entrada realizada!")
    elif opcao == 2:
        qtd = int(input("Quantidade de saída: "))
        if qtd <= produto.quantidade:
            produto.quantidade -= qtd
            print("Saída realizada!")
        else:
            print("Quantidade insuficiente em estoque!")
    else:
        print("Opção inválida.")


def calcular_total_itens():
    return reduzir_total([p.quantidade for p in produtos], lambda acc, x: acc + x, 0)


def calcular_valor_total():
    return reduzir_total([p.quantidade * p.preco for p in produtos], lambda acc, x: acc + x, 0.0)


# ==========================
# NOVAS FUNÇÕES (REQUERIDAS)
# ==========================

def aplicar_desconto_todos():
    global produtos
    desconto = float(input("\nDigite o percentual de desconto: "))

    def aplicar_desconto(p):
        novo = Produto(p.codigo, p.nome, p.quantidade, calcular_desconto(p.preco, desconto))
        novo.preco_original = p.preco_original
        return novo

    produtos = aplicar_funcao_nos_precos(produtos, aplicar_desconto)
    print("\nDesconto aplicado a todos os produtos!\n")


def aplicar_desconto_individual():
    global produtos
    codigo = int(input("\nDigite o código do produto para aplicar o desconto: "))
    produto = buscar_produto_por_codigo(produtos, codigo)

    if not produto:
        print("Produto não encontrado!")
        return

    desconto = float(input("Digite o percentual de desconto: "))
    produto.preco = calcular_desconto(produto.preco, desconto)
    print(f"\nDesconto aplicado ao produto {produto.nome}! Novo preço: R$ {produto.preco:.2f}")


def remover_descontos():
    global produtos
    produtos = [
        Produto(p.codigo, p.nome, p.quantidade, remover_desconto(p.preco, p.preco_original))
        for p in produtos
    ]
    print("\nTodos os produtos voltaram ao preço original!\n")

def relatorio_estoque():
    if not produtos:
        print("Nenhum produto cadastrado.")
        return

    print("\n=== RELATÓRIO DE ESTOQUE ===")
    for p in produtos:
        print(f"{p.nome} (Cód {p.codigo}): {p.quantidade} unidades — R$ {p.preco:.2f}")

    total_itens = calcular_total_itens()
    total_valor = calcular_valor_total()

    print(f"\nTotal de itens: {total_itens}")
    print(f"Valor total do estoque: R$ {total_valor:.2f}")

    limite_str = input("\nDeseja ver produtos com baixo estoque? (digite limite mínimo ou ENTER para pular): ")

    if limite_str.strip() == "":
        print("Filtro de baixo estoque ignorado.")
        return

    try:
        limite = int(limite_str)
        baixos = filtrar_produtos(produtos, lambda p: estoque_baixo(p.quantidade, limite))
        if baixos:
            print("\n=== PRODUTOS COM BAIXO ESTOQUE ===")
            for p in baixos:
                print(f"{p.nome} (Cód {p.codigo}) - Quantidade: {p.quantidade}")
        else:
            print("\nNenhum produto com estoque abaixo do limite informado.")
    except ValueError:
        print("Valor inválido. Por favor, digite um número inteiro ou apenas pressione ENTER para pular.")


# ==========================
# FUNÇÃO PRINCIPAL
# ==========================

def main():
    while True:
        print("\n==== SISTEMA DE ESTOQUE ====")
        print("1. Cadastrar produtos")
        print("2. Entrada/Saída de estoque")
        print("3. Aplicar desconto em TODOS os produtos")
        print("4. Aplicar desconto em UM produto")
        print("5. Remover descontos (restaurar preços originais)")
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
