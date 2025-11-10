#include <stdio.h>
#include <string.h>

#define MAX_PRODUTOS 50

// ==========================
// ESTRUTURAS E VARIÁVEIS
// ==========================

typedef struct {
    int codigo;
    char nome[50];
    int quantidade;
    float preco;         
    float precoOriginal; 
} Produto;

Produto produtos[MAX_PRODUTOS];
int numProdutos = 0;

// ==========================
// FUNÇÕES FUNCIONAIS (PUXAS)
// ==========================

float calcularDesconto(float preco, float percentual) {
    return preco - (preco * (percentual / 100.0f));
}

float removerDesconto(float precoComDesconto, float precoOriginal) {
    return precoOriginal; 
}

int estoqueBaixo(int quantidade, int limite) {
    return quantidade < limite;
}

// ==========================
// FUNÇÕES DE ORDEM SUPERIOR
// ==========================

void aplicarFuncaoNosPrecos(float (*fn)(float, void*), void *ctx) {
    for (int i = 0; i < numProdutos; i++) {
        float novoPreco = fn(produtos[i].preco, ctx);
        produtos[i].preco = novoPreco;
    }
    printf("\nFunção aplicada a todos os preços com sucesso!\n");
}

float desconto_fn(float preco, void *ctx) {
    float desconto = *(float*)ctx;
    return calcularDesconto(preco, desconto);
}

void filtrarProdutos(int (*pred)(int, int), int limite) {
    printf("\n=== PRODUTOS COM BAIXO ESTOQUE (<= %d) ===\n", limite);
    for (int i = 0; i < numProdutos; i++) {
        if (pred(produtos[i].quantidade, limite)) {
            printf("%s (Código %d) - Quantidade: %d\n",
                   produtos[i].nome, produtos[i].codigo, produtos[i].quantidade);
        }
    }
}

// ==========================
// FUNÇÕES AUXILIARES
// ==========================

void limparBuffer() {
    int c;
    while ((c = getchar()) != '\n' && c != EOF);
}

int buscarProdutoPorCodigo(int codigo) {
    for (int i = 0; i < numProdutos; i++) {
        if (produtos[i].codigo == codigo)
            return i;
    }
    return -1;
}

// ==========================
// FUNÇÕES PRINCIPAIS DO SISTEMA
// ==========================

void cadastrarProdutos() {
    printf("\nQuantos produtos deseja cadastrar? ");
    scanf("%d", &numProdutos);
    limparBuffer();

    for (int i = 0; i < numProdutos; i++) {
        printf("\nProduto %d:\n", i + 1);
        printf("Código: ");
        scanf("%d", &produtos[i].codigo);
        limparBuffer();

        printf("Nome (sem espaços, ex: arroz_branco): ");
        scanf("%s", produtos[i].nome);

        printf("Quantidade inicial: ");
        scanf("%d", &produtos[i].quantidade);

        printf("Preço unitário: R$ ");
        scanf("%f", &produtos[i].preco);

        produtos[i].precoOriginal = produtos[i].preco;
    }

    printf("\nCadastro concluído!\n");
}

void movimentarEstoque() {
    int codigo, opcao, qtd;
    printf("\nDigite o código do produto: ");
    scanf("%d", &codigo);

    int idx = buscarProdutoPorCodigo(codigo);
    if (idx == -1) {
        printf("Produto não encontrado!\n");
        return;
    }

    printf("Produto encontrado: %s\n", produtos[idx].nome);
    printf("1. Entrada\n2. Saída\nEscolha: ");
    scanf("%d", &opcao);

    if (opcao == 1) {
        printf("Quantidade de entrada: ");
        scanf("%d", &qtd);
        produtos[idx].quantidade += qtd;
        printf("Entrada realizada!\n");
    } else if (opcao == 2) {
        printf("Quantidade de saída: ");
        scanf("%d", &qtd);
        if (qtd <= produtos[idx].quantidade) {
            produtos[idx].quantidade -= qtd;
            printf("Saída realizada!\n");
        } else {
            printf("Quantidade insuficiente em estoque!\n");
        }
    } else {
        printf("Opção inválida.\n");
    }
}

int calcularTotalItens() {
    int soma = 0;
    for (int i = 0; i < numProdutos; i++)
        soma += produtos[i].quantidade;
    return soma;
}

float calcularValorTotal() {
    float total = 0;
    for (int i = 0; i < numProdutos; i++)
        total += produtos[i].quantidade * produtos[i].preco;
    return total;
}

// ==========================
// NOVAS FUNÇÕES (REQUERIDAS)
// ==========================

void aplicarDescontoIndividual() {
    int codigo;
    float desconto;
    printf("\nDigite o código do produto para aplicar o desconto: ");
    scanf("%d", &codigo);

    int idx = buscarProdutoPorCodigo(codigo);
    if (idx == -1) {
        printf("Produto não encontrado!\n");
        return;
    }

    printf("Digite o percentual de desconto: ");
    scanf("%f", &desconto);

    float novoPreco = calcularDesconto(produtos[idx].preco, desconto);
    produtos[idx].preco = novoPreco;

    printf("\nDesconto aplicado ao produto %s! Novo preço: R$ %.2f\n",
           produtos[idx].nome, produtos[idx].preco);
}

void removerDescontos() {
    for (int i = 0; i < numProdutos; i++) {
        produtos[i].preco = produtos[i].precoOriginal;
    }
    printf("\nTodos os produtos voltaram ao preço original!\n");
}

// ==========================
// RELATÓRIO E MENU
// ==========================

void relatorioEstoque() {
    if (numProdutos == 0) {
        printf("Nenhum produto cadastrado.\n");
        return;
    }

    printf("\n=== RELATÓRIO DE ESTOQUE ===\n");
    for (int i = 0; i < numProdutos; i++) {
        printf("%s (Cód %d): %d unidades — R$ %.2f\n",
               produtos[i].nome, produtos[i].codigo,
               produtos[i].quantidade, produtos[i].preco);
    }

    printf("\nTotal de itens: %d\n", calcularTotalItens());
    printf("Valor total do estoque: R$ %.2f\n", calcularValorTotal());

    char entrada[10];
    printf("\nDeseja ver produtos com baixo estoque? (digite limite mínimo ou pressione ENTER para pular): ");
    limparBuffer();
    fgets(entrada, sizeof(entrada), stdin);

    if (entrada[0] != '\n') {
        int limite = atoi(entrada);
        filtrarProdutos(estoqueBaixo, limite);
    } else {
        printf("Filtro de baixo estoque ignorado.\n");
    }
}

// ==========================
// FUNÇÃO PRINCIPAL
// ==========================

int main() {
    int opcao;

    do {
        printf("\n==== SISTEMA DE ESTOQUE ====\n");
        printf("1. Cadastrar produtos\n");
        printf("2. Entrada/Saída de estoque\n");
        printf("3. Aplicar desconto em TODOS os produtos\n");
        printf("4. Aplicar desconto em UM produto\n");
        printf("5. Remover descontos (restaurar preços originais)\n");
        printf("6. Relatório do estoque\n");
        printf("7. Sair\n");
        printf("Escolha uma opção: ");
        scanf("%d", &opcao);

        switch(opcao) {
            case 1:
                cadastrarProdutos();
                break;
            case 2:
                movimentarEstoque();
                break;
            case 3: {
                float desconto;
                printf("\nDigite o percentual de desconto: ");
                scanf("%f", &desconto);
                aplicarFuncaoNosPrecos(desconto_fn, &desconto);
                break;
            }
            case 4:
                aplicarDescontoIndividual();
                break;
            case 5:
                removerDescontos();
                break;
            case 6:
                relatorioEstoque();
                break;
            case 7:
                printf("Saindo...\n");
                break;
            default:
                printf("Opção inválida!\n");
        }
    } while(opcao != 7);

    return 0;
}
