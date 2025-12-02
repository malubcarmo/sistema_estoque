import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class SistemaEstoque {

    //Classe abstrata
    public static abstract class Produto {

        private final int codigo;
        private final String nome;
        private final int quantidade;
        private final BigDecimal preco;
        private final BigDecimal precoOriginal;

        public Produto(int codigo, String nome, int quantidade,
                       BigDecimal preco, BigDecimal precoOriginal) {

            this.codigo = codigo;
            this.nome = nome;
            this.quantidade = quantidade;
            this.preco = preco;
            this.precoOriginal = precoOriginal;
        }

        //Getters
        public int getCodigo() { return codigo; }
        public String getNome() { return nome; }
        public int getQuantidade() { return quantidade; }
        public BigDecimal getPreco() { return preco; }
        public BigDecimal getPrecoOriginal() { return precoOriginal; }

        //Método polimórfico onde cada produto pode exibir informação diferente
        public abstract String getTipo();
    }

    //Classe deriavada — ProdutoSimples (sem desconto)
    public static class ProdutoSimples extends Produto {

        public ProdutoSimples(int codigo, String nome, int quantidade,
                              BigDecimal precoOriginal) {

            super(codigo, nome, quantidade, precoOriginal, precoOriginal);
        }

        @Override
        public String getTipo() {
            return "Produto Simples";
        }
    }

    //Classe derivada — ProdutoComDesconto
    public static class ProdutoComDesconto extends Produto {

        private final BigDecimal descontoPercentual;

        public ProdutoComDesconto(int codigo, String nome, int quantidade,
                                  BigDecimal precoOriginal, BigDecimal descontoPercentual) {

            super(codigo, nome, quantidade,
                    aplicar(precoOriginal, descontoPercentual),
                    precoOriginal);

            this.descontoPercentual = descontoPercentual;
        }

        public BigDecimal getDescontoPercentual() { return descontoPercentual; }

        private static BigDecimal aplicar(BigDecimal preco, BigDecimal percentual) {
            BigDecimal factor = BigDecimal.ONE.subtract(
                    percentual.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
            );
            return preco.multiply(factor).setScale(2, RoundingMode.HALF_UP);
        }

        @Override
        public String getTipo() {
            return "Produto com Desconto";
        }
    }

    //Funções puras
    public static BigDecimal calcularDesconto(BigDecimal preco, BigDecimal percentual) {
        BigDecimal factor = BigDecimal.ONE.subtract(
                percentual.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
        );
        return preco.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    public static boolean estoqueBaixo(int quantidade, int limite) {
        return quantidade < limite;
    }

    //Operações Funcionais
    public static List<Produto> aplicarDescontoTodos(List<Produto> produtos, BigDecimal percentual) {
        return produtos.stream()
                .map(p -> new ProdutoComDesconto(
                        p.getCodigo(),
                        p.getNome(),
                        p.getQuantidade(),
                        p.getPrecoOriginal(),
                        percentual
                ))
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<Produto> aplicarDescontoIndividual(List<Produto> produtos, int codigo, BigDecimal percentual) {
        return produtos.stream()
                .map(p -> {
                    if (p.getCodigo() == codigo) {
                        return new ProdutoComDesconto(
                                p.getCodigo(),
                                p.getNome(),
                                p.getQuantidade(),
                                p.getPrecoOriginal(),
                                percentual
                        );
                    }
                    return p;
                })
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<Produto> removerDescontos(List<Produto> produtos) {
        return produtos.stream()
                .map(p -> new ProdutoSimples(
                        p.getCodigo(),
                        p.getNome(),
                        p.getQuantidade(),
                        p.getPrecoOriginal()
                ))
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<Produto> insertProduct(List<Produto> produtos, Produto novo) {
        List<Produto> copy = new ArrayList<>(produtos);
        copy.add(novo);
        return Collections.unmodifiableList(copy);
    }

    public static List<Produto> movimentarEstoque(List<Produto> produtos, int codigo, int deltaQuantidade) {
        return produtos.stream()
                .map(p -> {
                    if (p.getCodigo() == codigo) {
                        int novaQtd = p.getQuantidade() + deltaQuantidade;
                        if (novaQtd < 0) novaQtd = p.getQuantidade();

                        // mantém o tipo original do produto ✔ polimorfismo
                        if (p instanceof ProdutoComDesconto pDesc) {
                            return new ProdutoComDesconto(
                                    pDesc.getCodigo(),
                                    pDesc.getNome(),
                                    novaQtd,
                                    pDesc.getPrecoOriginal(),
                                    pDesc.getDescontoPercentual()
                            );
                        } else {
                            return new ProdutoSimples(
                                    p.getCodigo(),
                                    p.getNome(),
                                    novaQtd,
                                    p.getPrecoOriginal()
                            );
                        }
                    }
                    return p;
                })
                .collect(Collectors.toUnmodifiableList());
    }

    public static int totalItens(List<Produto> produtos) {
        return produtos.stream().mapToInt(Produto::getQuantidade).sum();
    }

    public static BigDecimal totalValor(List<Produto> produtos) {
        return produtos.stream()
                .map(p -> p.getPreco().multiply(BigDecimal.valueOf(p.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static Optional<Produto> buscarPorCodigo(List<Produto> produtos, int codigo) {
        return produtos.stream()
                .filter(p -> p.getCodigo() == codigo)
                .findFirst();
    }

    public static List<Produto> filtrarBaixoEstoque(List<Produto> produtos, int limite) {
        return produtos.stream()
                .filter(p -> estoqueBaixo(p.getQuantidade(), limite))
                .collect(Collectors.toUnmodifiableList());
    }

    //Interface de Console
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        List<Produto> produtos = Collections.unmodifiableList(new ArrayList<>());
        boolean running = true;

        while (running) {

            System.out.println("\n==== SISTEMA DE ESTOQUE ====");
            System.out.println("1. Cadastrar produtos");
            System.out.println("2. Entrada/Saída de estoque");
            System.out.println("3. Aplicar desconto em TODOS os produtos");
            System.out.println("4. Aplicar desconto em UM produto");
            System.out.println("5. Remover descontos");
            System.out.println("6. Relatório");
            System.out.println("7. Sair");
            System.out.print("Escolha uma opção: ");

            String opt = sc.nextLine().trim();

            switch (opt) {

                case "1" -> {
                    System.out.print("Quantos produtos deseja cadastrar? ");
                    int qtd = Integer.parseInt(sc.nextLine().trim());

                    List<Produto> novos = new ArrayList<>();

                    for (int i = 0; i < qtd; i++) {
                        System.out.println("\nProduto " + (i + 1));
                        System.out.print("Código: ");
                        int codigo = Integer.parseInt(sc.nextLine().trim());

                        System.out.print("Nome: ");
                        String nome = sc.nextLine().trim();

                        System.out.print("Quantidade inicial: ");
                        int quantidade = Integer.parseInt(sc.nextLine().trim());

                        System.out.print("Preço (ex: 12.50): R$ ");
                        BigDecimal preco = new BigDecimal(sc.nextLine().trim())
                                .setScale(2, RoundingMode.HALF_UP);

                        novos.add(new ProdutoSimples(codigo, nome, quantidade, preco));
                    }

                    List<Produto> merged = new ArrayList<>(produtos);
                    merged.addAll(novos);
                    produtos = Collections.unmodifiableList(merged);

                    System.out.println("Cadastro concluído!");
                }

                case "2" -> {
                    System.out.print("Código do produto: ");
                    int codigo = Integer.parseInt(sc.nextLine().trim());

                    Optional<Produto> maybe = buscarPorCodigo(produtos, codigo);
                    if (maybe.isEmpty()) {
                        System.out.println("Produto não encontrado!");
                        break;
                    }

                    Produto p = maybe.get();
                    System.out.println("Produto encontrado: " + p.getNome());
                    System.out.print("1. Entrada | 2. Saída: ");
                    String op = sc.nextLine().trim();

                    if ("1".equals(op)) {
                        System.out.print("Quantidade de entrada: ");
                        int qtd = Integer.parseInt(sc.nextLine().trim());
                        produtos = movimentarEstoque(produtos, codigo, qtd);
                        System.out.println("Entrada realizada!");
                    } else if ("2".equals(op)) {
                        System.out.print("Quantidade de saída: ");
                        int qtd = Integer.parseInt(sc.nextLine().trim());

                        if (qtd <= p.getQuantidade()) {
                            produtos = movimentarEstoque(produtos, codigo, -qtd);
                            System.out.println("Saída realizada!");
                        } else {
                            System.out.println("Quantidade insuficiente!");
                        }
                    }
                }

                case "3" -> {
                    System.out.print("Percentual de desconto: ");
                    BigDecimal d = new BigDecimal(sc.nextLine().trim());
                    produtos = aplicarDescontoTodos(produtos, d);
                    System.out.println("Desconto aplicado a todos os produtos!");
                }

                case "4" -> {
                    System.out.print("Código do produto: ");
                    int codigo = Integer.parseInt(sc.nextLine().trim());

                    Optional<Produto> maybe = buscarPorCodigo(produtos, codigo);
                    if (maybe.isEmpty()) {
                        System.out.println("Produto não encontrado!");
                        break;
                    }

                    System.out.print("Percentual de desconto: ");
                    BigDecimal d = new BigDecimal(sc.nextLine().trim());

                    produtos = aplicarDescontoIndividual(produtos, codigo, d);

                    Produto atualizado = buscarPorCodigo(produtos, codigo).get();
                    System.out.println("Desconto aplicado! Novo preço: R$ " +
                            atualizado.getPreco());
                }

                case "5" -> {
                    produtos = removerDescontos(produtos);
                    System.out.println("Descontos removidos!");
                }

                case "6" -> {
                    if (produtos.isEmpty()) {
                        System.out.println("Nenhum produto cadastrado!");
                        break;
                    }

                    System.out.println("\n=== RELATÓRIO ===");
                    produtos.forEach(p ->
                            System.out.printf("%s (Cód %d): %d unidades - R$ %s - [%s]%n",
                                    p.getNome(), p.getCodigo(), p.getQuantidade(),
                                    p.getPreco().toPlainString(),
                                    p.getTipo())
                    );

                    System.out.println("\nTotal de itens: " + totalItens(produtos));
                    System.out.println("Valor total: R$ " + totalValor(produtos));

                    System.out.print("\nLimite para baixo estoque (ENTER para ignorar): ");
                    String line = sc.nextLine().trim();

                    if (!line.isEmpty()) {
                        int limite = Integer.parseInt(line);
                        List<Produto> baixos = filtrarBaixoEstoque(produtos, limite);

                        System.out.println("\n=== BAIXO ESTOQUE ===");
                        baixos.forEach(p ->
                                System.out.printf("%s (Cód %d) — %d unidades%n",
                                        p.getNome(), p.getCodigo(), p.getQuantidade()));
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
