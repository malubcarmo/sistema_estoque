// SistemaEstoque.java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class SistemaEstoque {

    // Produto imutável (Java record)
    public static record Product(int codigo, String nome, int quantidade, BigDecimal preco, BigDecimal precoOriginal) {}

    // ---------------------
    // Funções puras
    // ---------------------
    public static BigDecimal calcularDesconto(BigDecimal preco, BigDecimal percentual) {
        BigDecimal factor = BigDecimal.ONE.subtract(percentual.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
        return preco.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal removerDesconto(BigDecimal precoComDesconto, BigDecimal precoOriginal) {
        return precoOriginal.setScale(2, RoundingMode.HALF_UP);
    }

    public static boolean estoqueBaixo(int quantidade, int limite) {
        return quantidade < limite;
    }

    // ---------------------
    // Funções de ordem superior / operações imutáveis
    // ---------------------

    public static List<Product> applyToPrices(List<Product> produtos, UnaryOperator<BigDecimal> priceFn) {
        return produtos.stream()
                .map(p -> new Product(p.codigo(), p.nome(), p.quantidade(), priceFn.apply(p.preco()), p.precoOriginal()))
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<Product> aplicarDescontoTodos(List<Product> produtos, BigDecimal percentual) {
        return applyToPrices(produtos, preco -> calcularDesconto(preco, percentual));
    }

    public static List<Product> aplicarDescontoIndividual(List<Product> produtos, int codigo, BigDecimal percentual) {
        return produtos.stream()
                .map(p -> {
                    if (p.codigo() == codigo) {
                        BigDecimal novoPreco = calcularDesconto(p.preco(), percentual);
                        return new Product(p.codigo(), p.nome(), p.quantidade(), novoPreco, p.precoOriginal());
                    } else {
                        return p;
                    }
                })
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<Product> removerDescontos(List<Product> produtos) {
        return produtos.stream()
                .map(p -> new Product(p.codigo(), p.nome(), p.quantidade(), removerDesconto(p.preco(), p.precoOriginal()), p.precoOriginal()))
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<Product> filterProducts(List<Product> produtos, BiPredicate<Integer, Integer> pred, int limite) {
        return produtos.stream()
                .filter(p -> pred.test(p.quantidade(), limite))
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<Product> insertProduct(List<Product> produtos, Product novo) {
        List<Product> copy = new ArrayList<>(produtos);
        copy.add(novo);
        return Collections.unmodifiableList(copy);
    }
    public static List<Product> movimentarEstoque(List<Product> produtos, int codigo, int deltaQuantidade) {
        return produtos.stream()
                .map(p -> {
                    if (p.codigo() == codigo) {
                        int novaQtd = p.quantidade() + deltaQuantidade;
                        if (novaQtd < 0) novaQtd = p.quantidade(); 
                        return new Product(p.codigo(), p.nome(), novaQtd, p.preco(), p.precoOriginal());
                    } else {
                        return p;
                    }
                })
                .collect(Collectors.toUnmodifiableList());
    }

    public static int totalItens(List<Product> produtos) {
        return produtos.stream().mapToInt(Product::quantidade).sum();
    }

    public static BigDecimal totalValor(List<Product> produtos) {
        return produtos.stream()
                .map(p -> p.preco().multiply(BigDecimal.valueOf(p.quantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static Optional<Product> buscarPorCodigo(List<Product> produtos, int codigo) {
        return produtos.stream().filter(p -> p.codigo() == codigo).findFirst();
    }

    // ---------------------
    // Main / Interface
    // ---------------------
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<Product> produtos = Collections.unmodifiableList(new ArrayList<>()); // estoque inicial vazio (imutável)
        boolean running = true;

        while (running) {
            System.out.println("\n==== SISTEMA DE ESTOQUE ====");
            System.out.println("1. Cadastrar produtos");
            System.out.println("2. Entrada/Saída de estoque");
            System.out.println("3. Aplicar desconto em TODOS os produtos");
            System.out.println("4. Aplicar desconto em UM produto");
            System.out.println("5. Remover descontos (restaurar preços originais)");
            System.out.println("6. Relatório do estoque");
            System.out.println("7. Sair");
            System.out.print("Escolha uma opção: ");

            String opt = sc.nextLine().trim();

            switch (opt) {
                case "1" -> {
                    System.out.print("\nQuantos produtos deseja cadastrar? ");
                    int qtd = Integer.parseInt(sc.nextLine().trim());
                    List<Product> novos = new ArrayList<>();
                    for (int i = 0; i < qtd; i++) {
                        System.out.println("\nProduto " + (i + 1) + ":");
                        System.out.print("Código: ");
                        int codigo = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("Nome: ");
                        String nome = sc.nextLine().trim();
                        System.out.print("Quantidade inicial: ");
                        int quantidade = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("Preço unitário (ex: 12.50): R$ ");
                        BigDecimal preco = new BigDecimal(sc.nextLine().trim()).setScale(2, RoundingMode.HALF_UP);

                        Product p = new Product(codigo, nome, quantidade, preco, preco);
                        novos.add(p);
                    }
                    List<Product> merged = new ArrayList<>(produtos);
                    merged.addAll(novos);
                    produtos = Collections.unmodifiableList(merged);
                    System.out.println("\nCadastro concluído!");
                }

                case "2" -> {
                    System.out.print("\nDigite o código do produto: ");
                    int codigo = Integer.parseInt(sc.nextLine().trim());
                    Optional<Product> maybe = buscarPorCodigo(produtos, codigo);
                    if (maybe.isEmpty()) {
                        System.out.println("Produto não encontrado!");
                        break;
                    }
                    Product p = maybe.get();
                    System.out.println("Produto encontrado: " + p.nome());
                    System.out.print("1. Entrada\n2. Saída\nEscolha: ");
                    String escolha = sc.nextLine().trim();
                    if ("1".equals(escolha)) {
                        System.out.print("Quantidade de entrada: ");
                        int qtd = Integer.parseInt(sc.nextLine().trim());
                        produtos = movimentarEstoque(produtos, codigo, +qtd);
                        System.out.println("Entrada realizada!");
                    } else if ("2".equals(escolha)) {
                        System.out.print("Quantidade de saída: ");
                        int qtd = Integer.parseInt(sc.nextLine().trim());
                        if (qtd <= p.quantidade()) {
                            produtos = movimentarEstoque(produtos, codigo, -qtd);
                            System.out.println("Saída realizada!");
                        } else {
                            System.out.println("Quantidade insuficiente em estoque!");
                        }
                    } else {
                        System.out.println("Opção inválida.");
                    }
                }

                case "3" -> {
                    System.out.print("\nDigite o percentual de desconto (ex: 10): ");
                    BigDecimal desconto = new BigDecimal(sc.nextLine().trim());
                    produtos = aplicarDescontoTodos(produtos, desconto);
                    System.out.println("Desconto aplicado a todos os produtos.");
                }

                case "4" -> {
                    System.out.print("\nDigite o código do produto para aplicar desconto: ");
                    int codigo = Integer.parseInt(sc.nextLine().trim());
                    Optional<Product> maybe = buscarPorCodigo(produtos, codigo);
                    if (maybe.isEmpty()) {
                        System.out.println("Produto não encontrado!");
                        break;
                    }
                    System.out.print("Digite o percentual de desconto: ");
                    BigDecimal desconto = new BigDecimal(sc.nextLine().trim());
                    produtos = aplicarDescontoIndividual(produtos, codigo, desconto);
                    Product atualizado = buscarPorCodigo(produtos, codigo).get();
                    System.out.printf("Desconto aplicado ao produto %s! Novo preço: R$ %s%n", atualizado.nome(), atualizado.preco().toPlainString());
                }

                case "5" -> {
                    produtos = removerDescontos(produtos);
                    System.out.println("Todos os produtos voltaram ao preço original.");
                }

                case "6" -> {
                    if (produtos.isEmpty()) {
                        System.out.println("Nenhum produto cadastrado.");
                        break;
                    }
                    System.out.println("\n=== RELATÓRIO DE ESTOQUE ===");
                    produtos.forEach(prod -> System.out.printf("%s (Cód %d): %d unidades — R$ %s%n",
                            prod.nome(), prod.codigo(), prod.quantidade(), prod.preco().toPlainString()));

                    System.out.println("\nTotal de itens: " + totalItens(produtos));
                    System.out.println("Valor total do estoque: R$ " + totalValor(produtos).toPlainString());

                    System.out.print("\nDeseja ver produtos com baixo estoque? (digite limite mínimo ou ENTER para pular): ");
                    String line = sc.nextLine().trim();
                    if (!line.isEmpty()) {
                        int limite = Integer.parseInt(line);
                        List<Product> baixos = filterProducts(produtos, SistemaEstoque::estoqueBaixo, limite);
                        System.out.println("\n=== PRODUTOS COM BAIXO ESTOQUE ===");
                        baixos.forEach(prod -> System.out.printf("%s (Cód %d) - Quantidade: %d%n", prod.nome(), prod.codigo(), prod.quantidade()));
                    }
                }

                case "7" -> {
                    System.out.println("Saindo...");
                    running = false;
                }

                default -> System.out.println("Opção inválida!");
            }
        }

        sc.close();
    }
}
