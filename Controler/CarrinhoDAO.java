package Controler;

import java.sql.*;
import java.time.LocalDateTime;

public class CarrinhoDAO {

    private Connection conn;

    public CarrinhoDAO(Connection conn) {
        this.conn = conn;
    }

    // Criar um pedido temporário sem cliente (cliente_id = null ou 0)
    public long criarPedidoTemporario() throws SQLException {
        // 1. Selecionar um id_funcionario aleatório
        String sqlFuncionario = "SELECT id_funcionario FROM Funcionario ORDER BY RAND() LIMIT 1";
        PreparedStatement stmtFuncionario = conn.prepareStatement(sqlFuncionario);
        ResultSet rsFuncionario = stmtFuncionario.executeQuery();

        long funcionarioId;

        if (rsFuncionario.next()) {
            funcionarioId = rsFuncionario.getLong("id_funcionario");
        } else {
            throw new SQLException("Nenhum funcionário encontrado.");
        }

        // 2. Criar o pedido temporário usando o id_funcionario selecionado aleatoriamente
        String sql = "INSERT INTO Pedido (cliente_id, data_pedido, funcionario_id, forma_pagamento_id) VALUES (NULL, ?, ?, NULL)";
        PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
        stmt.setTimestamp(1, java.sql.Timestamp.valueOf(LocalDateTime.now()));
        stmt.setLong(2, funcionarioId); // Usando o id_funcionario aleatório
        stmt.executeUpdate();
        ResultSet rs = stmt.getGeneratedKeys();

        if (rs.next()) {
            return rs.getLong(1);  // Retorna o id_pedido gerado
        } else {
            throw new SQLException("Erro ao criar o pedido temporário.");
        }
    }


    // Método para adicionar item ao pedido
    public boolean adicionarItemPedido(long idPedido, String nomeProduto, String marcaProduto, int quantidade) throws SQLException {
        // Encontrar o produto pelo nome e pela marca
        String sqlProduto = "SELECT id_produto, produto_preco, quantidade_estoque FROM Produto WHERE produto_nome = ? AND produto_fabricante = ?";
        PreparedStatement stmtProduto = conn.prepareStatement(sqlProduto);
        stmtProduto.setString(1, nomeProduto);
        stmtProduto.setString(2, marcaProduto);
        ResultSet rsProduto = stmtProduto.executeQuery();

        if (!rsProduto.next()) {
            System.out.println("Produto não encontrado.");
            return false;
        }

        long idProduto = rsProduto.getLong("id_produto");
        double valorUnitario = rsProduto.getDouble("produto_preco");
        int estoqueAtual = rsProduto.getInt("quantidade_estoque");


        // Verificar se há quantidade suficiente em estoque
        if (estoqueAtual < quantidade) {
            System.out.println("Estoque insuficiente. Quantidade disponível: " + estoqueAtual);
            return false;
        }

        // Adicionar item ao pedido (carrinho)
        String sqlAdicionarItem = "INSERT INTO ItemPedido (pedido_id, produto_id, quantidade, valor_unitario) VALUES (?, ?, ?, ?)";
        PreparedStatement stmtAdicionarItem = conn.prepareStatement(sqlAdicionarItem);
        stmtAdicionarItem.setLong(1, idPedido);
        stmtAdicionarItem.setLong(2, idProduto);
        stmtAdicionarItem.setInt(3, quantidade);
        stmtAdicionarItem.setDouble(4, valorUnitario);

        int rowsAffected = stmtAdicionarItem.executeUpdate();

        if (rowsAffected > 0) {
            // Subtrair a quantidade do estoque
            String sqlAtualizarEstoque = "UPDATE Produto SET quantidade_estoque = quantidade_estoque - ? WHERE id_produto = ?";
            PreparedStatement stmtAtualizarEstoque = conn.prepareStatement(sqlAtualizarEstoque);
            stmtAtualizarEstoque.setInt(1, quantidade);
            stmtAtualizarEstoque.setLong(2, idProduto);
            stmtAtualizarEstoque.executeUpdate();
            System.out.println("Quantidade em estoque atualizada.");
            return true;
        }
        return false;
    }

    public boolean finalizarCompra(long idPedido) throws SQLException {

        // Atualizar o pedido com o id do cliente e finalizar a compra
        String sqlFinalizarPedido = "UPDATE Pedido SET cliente_id = ?, finalizado = true WHERE id_pedido = ?";
        PreparedStatement stmtFinalizarPedido = conn.prepareStatement(sqlFinalizarPedido);
        //stmtFinalizarPedido.setLong(1, idCliente);
        stmtFinalizarPedido.setLong(2, idPedido);

        int rowsAffected = stmtFinalizarPedido.executeUpdate();
        return rowsAffected > 0;
    }
    public boolean finalizarPagamento(long idPedido, String nomeFormaPagamento, Long idCliente) throws SQLException {
        if (idCliente == null) {
            System.out.println("Você não está logado. Por favor, faça login para finalizar a compra.");
            return false;
        }

        // 1. Verificar se o pedido existe e se pertence ao cliente
        String sqlVerificarPedido = "SELECT * FROM Pedido WHERE id_pedido = ? AND cliente_id = ?";
        PreparedStatement stmtVerificarPedido = conn.prepareStatement(sqlVerificarPedido);
        stmtVerificarPedido.setLong(1, idPedido);
        stmtVerificarPedido.setLong(2, idCliente);
        ResultSet rsVerificarPedido = stmtVerificarPedido.executeQuery();

        if (!rsVerificarPedido.next()) {
            System.out.println("Pedido não encontrado ou não pertence ao cliente.");
            return false;
        }

        // 2. Verificar a forma de pagamento pelo nome e status ativo
        String sqlVerificarFormaPagamento = "SELECT id_pagamento, status_id FROM FormaPagamento WHERE nome = ? AND status_id = (SELECT id_status FROM StatusPagamento WHERE descricao = 'ativo')";
        PreparedStatement stmtVerificarFormaPagamento = conn.prepareStatement(sqlVerificarFormaPagamento);
        stmtVerificarFormaPagamento.setString(1, nomeFormaPagamento);
        ResultSet rsFormaPagamento = stmtVerificarFormaPagamento.executeQuery();

        if (!rsFormaPagamento.next()) {
            System.out.println("Forma de pagamento inválida ou inativa.");
            return false;
        }

        long formaPagamentoId = rsFormaPagamento.getLong("id_pagamento");

        // 3. Atualizar o pedido com a forma de pagamento e marcar como finalizado
        String sqlFinalizarPedido = "UPDATE Pedido SET forma_pagamento_id = ?, finalizado = true WHERE id_pedido = ?";
        PreparedStatement stmtFinalizarPedido = conn.prepareStatement(sqlFinalizarPedido);
        stmtFinalizarPedido.setLong(1, formaPagamentoId);
        stmtFinalizarPedido.setLong(2, idPedido);

        int rowsAffected = stmtFinalizarPedido.executeUpdate();

        if (rowsAffected > 0) {
            System.out.println("Pagamento finalizado com sucesso. Pedido ID: " + idPedido);
            return true;
        } else {
            System.out.println("Erro ao finalizar o pagamento.");
            return false;
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
