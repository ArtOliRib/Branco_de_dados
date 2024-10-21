package Controler;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class CarrinhoDAO {

    private final Connection conn;

    public CarrinhoDAO(Connection conn) {
        this.conn = conn;
    }

    // Cria um pedido temporário sem cliente (cliente_id = null ou 0)
    public long criarPedidoTemporario() throws SQLException {
        long funcionarioId = obterFuncionarioAleatorio();

        // Insere um pedido temporário com o id_funcionario selecionado aleatoriamente
        String sql = "INSERT INTO Pedido (cliente_id, data_pedido, funcionario_id, forma_pagamento_id) VALUES (NULL, ?, ?, NULL)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(2, funcionarioId);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);  // Retorna o id_pedido gerado
                } else {
                    throw new SQLException("Erro ao criar o pedido temporário.");
                }
            }
        }
    }

    // Obtém um funcionário aleatório
    private long obterFuncionarioAleatorio() throws SQLException {
        String sqlFuncionario = "SELECT id_funcionario FROM Funcionario ORDER BY RAND() LIMIT 1";
        try (PreparedStatement stmtFuncionario = conn.prepareStatement(sqlFuncionario);
             ResultSet rsFuncionario = stmtFuncionario.executeQuery()) {
            if (rsFuncionario.next()) {
                return rsFuncionario.getLong("id_funcionario");
            } else {
                throw new SQLException("Nenhum funcionário encontrado.");
            }
        }
    }

    // Adiciona um item ao pedido
    public boolean adicionarItemPedido(long idPedido, String nomeProduto, String marcaProduto, int quantidade) throws SQLException {
        long idProduto = encontrarProduto(nomeProduto, marcaProduto);

        if (idProduto == -1) {
            System.out.println("Produto não encontrado.");
            return false; // Retorna false se o produto não for encontrado
        }

        // Verifica se há quantidade suficiente em estoque
        if (!verificarEstoque(idProduto, quantidade)) {
            System.out.println("Estoque insuficiente.");
            return false; // Retorna false se o estoque for insuficiente
        }

        // Insere o item ao pedido (carrinho)
        if (inserirItemPedido(idPedido, idProduto, quantidade)) {
            atualizarEstoque(idProduto, quantidade); // Atualiza a quantidade em estoque
            System.out.println("Quantidade em estoque atualizada.");
            return true; // Retorna true se o item foi adicionado com sucesso
        }
        return false; // Retorna false se a inserção do item falhar
    }

    // Encontra o id do produto com base no nome e marca
    private long encontrarProduto(String nomeProduto, String marcaProduto) throws SQLException {
        String sqlProduto = "SELECT id_produto FROM Produto WHERE produto_nome = ? AND produto_fabricante = ?";
        try (PreparedStatement stmtProduto = conn.prepareStatement(sqlProduto)) {
            stmtProduto.setString(1, nomeProduto);
            stmtProduto.setString(2, marcaProduto);
            try (ResultSet rsProduto = stmtProduto.executeQuery()) {
                return rsProduto.next() ? rsProduto.getLong("id_produto") : -1; // Retorna -1 se não encontrado
            }
        }
    }

    // Verifica se há estoque suficiente para o produto
    private boolean verificarEstoque(long idProduto, int quantidade) throws SQLException {
        String sqlEstoque = "SELECT quantidade_estoque FROM Produto WHERE id_produto = ?";
        try (PreparedStatement stmtEstoque = conn.prepareStatement(sqlEstoque)) {
            stmtEstoque.setLong(1, idProduto);
            try (ResultSet rsEstoque = stmtEstoque.executeQuery()) {
                return rsEstoque.next() && rsEstoque.getInt("quantidade_estoque") >= quantidade; // Retorna true se há estoque suficiente
            }
        }
    }

    // Insere um item no pedido
    private boolean inserirItemPedido(long idPedido, long idProduto, int quantidade) throws SQLException {
        String sqlAdicionarItem = "INSERT INTO ItemPedido (pedido_id, produto_id, quantidade, valor_unitario) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmtAdicionarItem = conn.prepareStatement(sqlAdicionarItem)) {
            stmtAdicionarItem.setLong(1, idPedido);
            stmtAdicionarItem.setLong(2, idProduto);
            stmtAdicionarItem.setInt(3, quantidade);
            stmtAdicionarItem.setDouble(4, obterValorUnitario(idProduto)); // Obtém o valor unitário do produto
            return stmtAdicionarItem.executeUpdate() > 0; // Retorna true se a inserção foi bem-sucedida
        }
    }

    // Obtém o valor unitário de um produto
    private double obterValorUnitario(long idProduto) throws SQLException {
        String sqlPreco = "SELECT produto_preco FROM Produto WHERE id_produto = ?";
        try (PreparedStatement stmtPreco = conn.prepareStatement(sqlPreco)) {
            stmtPreco.setLong(1, idProduto);
            try (ResultSet rsPreco = stmtPreco.executeQuery()) {
                return rsPreco.next() ? rsPreco.getDouble("produto_preco") : 0.0; // Retorna o preço ou 0 se não encontrado
            }
        }
    }

    // Atualiza a quantidade em estoque de um produto
    private void atualizarEstoque(long idProduto, int quantidade) throws SQLException {
        String sqlAtualizarEstoque = "UPDATE Produto SET quantidade_estoque = quantidade_estoque - ? WHERE id_produto = ?";
        try (PreparedStatement stmtAtualizarEstoque = conn.prepareStatement(sqlAtualizarEstoque)) {
            stmtAtualizarEstoque.setInt(1, quantidade);
            stmtAtualizarEstoque.setLong(2, idProduto);
            stmtAtualizarEstoque.executeUpdate(); // Executa a atualização do estoque
        }
    }

    // Finaliza a compra de um pedido, associando um cliente
    public boolean finalizarCompra(long idPedido, long idCliente) throws SQLException {
        // Atualiza o pedido com o id do cliente e finaliza a compra
        String sqlFinalizarPedido = "UPDATE Pedido SET cliente_id = ?, finalizado = true WHERE id_pedido = ?";
        try (PreparedStatement stmtFinalizarPedido = conn.prepareStatement(sqlFinalizarPedido)) {
            stmtFinalizarPedido.setLong(1, idCliente);
            stmtFinalizarPedido.setLong(2, idPedido);

            if (stmtFinalizarPedido.executeUpdate() > 0) {
                return finalizarCompraComPagamento(idPedido, idCliente) != -1; // Retorna true se a finalização da compra for bem-sucedida
            }
        }
        return false; // Retorna false se não foi possível atualizar o pedido
    }

    // Finaliza a compra com pagamento, retornando o status da operação
    public long finalizarCompraComPagamento(long idPedido, long idCliente) throws SQLException {
        double desconto = calcularDescontoCliente(idCliente);
        double valorTotal = calcularValorTotalPedido(idPedido);
        double valorComDesconto = aplicarDesconto(valorTotal, desconto); // Aplica desconto ao valor total

        // Exibe as formas de pagamento disponíveis
        long formaPagamentoId = escolherFormaPagamento();
        if (formaPagamentoId == -1) {
            return -1;  // Retorna se não houver forma de pagamento selecionada
        }

        Scanner scanner = new Scanner(System.in);

        String detalhesPagamento = null;

        switch ((int) formaPagamentoId) {
            case 1: // Cartão de Crédito
                // Coleta os dados do cartão de crédito diretamente no case

                System.out.println("Digite o número do cartão de crédito:");
                String numeroCartao = scanner.nextLine();

                System.out.println("Digite a validade do cartão (MM/AA):");
                String validadeCartao = scanner.nextLine();

                System.out.println("Digite o código de segurança (CVV):");
                String codigoSeguranca = scanner.nextLine();

                // Formata e armazena os detalhes do pagamento
                detalhesPagamento = String.format("Cartão: %s, Validade: %s, CVV: %s", numeroCartao, validadeCartao, codigoSeguranca);
                break;

            case 2: // Boleto
                detalhesPagamento = gerarBoleto();
                System.out.println("Boleto gerado. Pague para confirmar o pedido.");
                detalhesPagamento = "Boleto";
                break;

            case 3: // PIX
                String chavePix = gerarChavePix(); // Gera a chave Pix
                System.out.println("A chave PIX gerada é: " + chavePix);

                // Confirmação de pagamento via PIX integrada diretamente no case
                System.out.println("Confirme o pagamento via PIX (s/n):");
                String confirmacaoPix = scanner.nextLine();

                if (!confirmacaoPix.equalsIgnoreCase("s")) {
                    System.out.println("Pagamento via PIX não confirmado.");
                    return -1;
                }

                detalhesPagamento = "Chave PIX: " + chavePix;
                break;


            case 4: // Berries (alguma moeda específica do sistema)
                if (!confirmarPagamentoBerries(valorComDesconto)) {
                    System.out.println("Saldo de berries insuficiente ou pagamento cancelado.");
                    return -1;
                }
                break;

            default:
                System.out.println("Forma de pagamento inválida.");
                return -1;
        }

        if (detalhesPagamento == null) {
            return -1;  // Retorna se não foi possível coletar os detalhes do pagamento
        }

        // Atualiza o pedido com os dados do pagamento
        return atualizarPedidoComPagamento(idPedido, formaPagamentoId) ? 0 : -1;
    }


    private String gerarChavePix() {
        // Define os caracteres permitidos (alfanuméricos)
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder chavePix = new StringBuilder(32);
        Random random = new Random();

        // Gera a chave PIX de 32 caracteres aleatórios
        for (int i = 0; i < 32; i++) {
            int index = random.nextInt(caracteres.length());
            chavePix.append(caracteres.charAt(index));
        }

        return chavePix.toString();
    }



    private String gerarBoleto() {
        Random random = new Random();

        // Gera a primeira parte do boleto com 5 dígitos
        int parte1 = random.nextInt(90000) + 10000; // Garante que será um número entre 10000 e 99999

        // Gera a segunda parte do boleto com 5 dígitos
        int parte2 = random.nextInt(90000) + 10000; // Garante que será um número entre 10000 e 99999

        // Retorna o código de boleto formatado
        return String.format("Código de Boleto: %05d-%05d", parte1, parte2);
    }


    private boolean confirmarPagamentoBerries(double valor) {
        // Verifica se o cliente tem berries suficientes e confirma o pagamento
        // Aqui poderia haver uma consulta ao saldo de berries e a lógica para confirmação
        Scanner scanner = new Scanner(System.in);
        System.out.println("Confirme o pagamento de " + valor + " Berries (s/n):");
        String confirmacao = scanner.nextLine();
        return confirmacao.equalsIgnoreCase("s");
    }


    // Calcula o desconto do cliente com base em seus dados
    private double calcularDescontoCliente(long idCliente) throws SQLException {
        String sqlCliente = "SELECT assiste_op, torcida, cidade FROM Cliente WHERE id_cliente = ?";
        try (PreparedStatement stmtCliente = conn.prepareStatement(sqlCliente)) {
            stmtCliente.setLong(1, idCliente);
            try (ResultSet rsCliente = stmtCliente.executeQuery()) {
                if (rsCliente.next()) {
                    boolean assisteOp = rsCliente.getBoolean("assiste_op");
                    String torcida = rsCliente.getString("torcida");
                    String cidade = rsCliente.getString("cidade");
                    return calcularDesconto(assisteOp, torcida, cidade); // Retorna o desconto calculado
                }
            }
        }
        return 0.0; // Retorna 0 se não houver desconto aplicável
    }

    // Calcula o desconto com base em condições específicas
    private double calcularDesconto(boolean assisteOp, String torcida, String cidade) {
        double desconto = 0.0;
        if (assisteOp) {
            desconto += 0.10;  // 10% de desconto se assiste a OP
        }
        if ("Flamengo".equalsIgnoreCase(torcida)) {
            desconto += 0.10;  // Mais 10% de desconto se for torcedor do Flamengo
        }
        if ("Sousa".equalsIgnoreCase(cidade)) {
            desconto += 0.10;  // Mais 10% de desconto se for de Sousa
        }
        return desconto; // Retorna o total de desconto calculado
    }

    // Aplica o desconto ao valor total
    private double aplicarDesconto(double valorTotal, double desconto) {
        return valorTotal * (1 - desconto); // Retorna o valor total com desconto aplicado
    }

    // Escolhe uma forma de pagamento disponível
    private long escolherFormaPagamento() throws SQLException {
        String sqlFormasPagamento = "SELECT id_pagamento, nome FROM FormaPagamento WHERE status_id = 'ativo'";
        HashMap<Integer, Long> formasPagamentoMap = new HashMap<>();
        int count = 1;

        System.out.println("Escolha uma forma de pagamento:");
        try (PreparedStatement stmtFormasPagamento = conn.prepareStatement(sqlFormasPagamento);
             ResultSet rsFormasPagamento = stmtFormasPagamento.executeQuery()) {

            while (rsFormasPagamento.next()) {
                long idPagamento = rsFormasPagamento.getLong("id_pagamento");
                String nomePagamento = rsFormasPagamento.getString("nome");
                formasPagamentoMap.put(count++, idPagamento); // Mapeia a forma de pagamento
                System.out.println(count - 1 + ". " + nomePagamento); // Exibe as opções de pagamento
            }
        }

        Scanner scanner = new Scanner(System.in);
        int opcao = scanner.nextInt(); // Captura a opção escolhida pelo usuário
        return formasPagamentoMap.getOrDefault(opcao, -1L); // Retorna o id da forma de pagamento ou -1 se inválido
    }


    // Atualiza o pedido com os dados de pagamento
    private boolean atualizarPedidoComPagamento(long idPedido, long formaPagamentoId) throws SQLException {
        String sqlAtualizarPedido = "UPDATE Pedido SET forma_pagamento_id = ? WHERE id_pedido = ?";
        try (PreparedStatement stmtAtualizar = conn.prepareStatement(sqlAtualizarPedido)) {
            stmtAtualizar.setLong(1, formaPagamentoId);
            stmtAtualizar.setLong(2, idPedido);
            return stmtAtualizar.executeUpdate() > 0; // Retorna true se a atualização for bem-sucedida
        }
    }

    // Calcula o valor total do pedido
    private double calcularValorTotalPedido(long idPedido) throws SQLException {
        String sqlValorTotal = "SELECT SUM(valor_unitario * quantidade) AS total FROM ItemPedido WHERE pedido_id = ?";
        try (PreparedStatement stmtValorTotal = conn.prepareStatement(sqlValorTotal)) {
            stmtValorTotal.setLong(1, idPedido);
            try (ResultSet rsValorTotal = stmtValorTotal.executeQuery()) {
                return rsValorTotal.next() ? rsValorTotal.getDouble("total") : 0.0; // Retorna o valor total ou 0 se não encontrado
            }
        }
    }

    public void verCarrinho(long idPedido) {
        String sql = "SELECT * FROM ItensCarrinho WHERE id_pedido = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idPedido);
            ResultSet rs = stmt.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println("Carrinho vazio ou pedido não encontrado.");
                return;
            }

            System.out.println("=== Detalhes do Carrinho ===");
            double valorTotal = 0;

            while (rs.next()) {
                String nomeProduto = rs.getString("produto_nome");
                int quantidade = rs.getInt("quantidade");
                double valorUnitario = rs.getDouble("valor_unitario");
                double totalItem = rs.getDouble("total_item");

                System.out.println("Produto: " + nomeProduto);
                System.out.println("Quantidade: " + quantidade);
                System.out.println("Valor Unitário: R$ " + valorUnitario);
                System.out.println("Total Item: R$ " + totalItem);
                System.out.println("----------------------------");

                valorTotal += totalItem;
            }

            System.out.println("Valor Total do Carrinho: R$ " + valorTotal);

        } catch (SQLException e) {
            System.out.println("Erro ao visualizar o carrinho: " + e.getMessage());
        }
    }
}

