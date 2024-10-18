package Controler;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class EstoqueDAO {

    private Connection conn;

    public EstoqueDAO(Connection conn) {
        this.conn = conn;
    }

    // Exibe o menu para visualizar as peças
    public void menuVisualizarPecasFuncionario() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        boolean continuar = true;

        while (continuar) {
            // Mostra as opções do menu
            System.out.println("\nMenu Visualizar Peças");
            System.out.println("1. Listar todas as peças");
            System.out.println("2. Pesquisar peça por nome");
            System.out.println("3. Pesquisar peça por fabricante");
            System.out.println("4. Pesquisar peça por categoria");
            System.out.println("5. Sair");

            // Captura a opção escolhida pelo usuário
            System.out.print("Escolha uma opção: ");
            int opcao = scanner.nextInt();
            scanner.nextLine(); // Consome a nova linha

            // Executa a ação correspondente à opção escolhida
            switch (opcao) {
                case 1:
                    this.visualizarPecasFuncionario(); // Listar todas as peças
                    break;
                case 2:
                    System.out.print("Digite o nome da peça: ");
                    String nome = scanner.nextLine();
                    this.pesquisarPecaPorNomeFuncionario(nome); // Pesquisar peça por nome
                    break;
                case 3:
                    System.out.print("Digite o nome do fabricante: ");
                    String fabricante = scanner.nextLine();
                    this.pesquisarPecaPorFabricanteFuncionario(fabricante); // Pesquisar peça por fabricante
                    break;
                case 4:
                    System.out.print("Digite a categoria da peça: ");
                    String categoria = scanner.nextLine();
                    this.pesquisarPecaPorCategoriaFuncionario(categoria); // Pesquisar peça por categoria
                    break;
                case 5:
                    continuar = false; // Sai do menu
                    System.out.println("Saindo do menu.");
                    break;
                default:
                    System.out.println("Opção inválida, tente novamente."); // Opção inválida
            }
        }
    }

    // Exibe todas as peças no estoque
    public void visualizarPecasFuncionario() throws SQLException {
        String query = "SELECT id_produto, produto_nome, produto_categoria, produto_preco, produto_descricao FROM Produto";
        Statement stmt = conn.createStatement();  // Cria uma declaração usando a conexão
        ResultSet rs = stmt.executeQuery(query);

        // Cabeçalho das colunas
        System.out.printf("%-10s %-30s %-20s %-15s %-50s%n", "ID", "Nome", "Categoria", "Preço (R$)", "Descrição");
        System.out.println("=".repeat(130)); // Linha de separação

        // Itera pelos resultados e exibe cada peça
        while (rs.next()) {
            long id = rs.getLong("id_produto");  // Obtém o ID do produto
            String nome = rs.getString("produto_nome");
            String categoria = rs.getString("produto_categoria");
            BigDecimal preco = rs.getBigDecimal("produto_preco");
            String descricao = rs.getString("produto_descricao");

            // Exibe o ID, nome, categoria, preço e descrição
            System.out.printf("%-10d %-30s %-20s %-15.2f %-50s%n", id, nome, categoria, preco, descricao);
        }

        System.out.println("=".repeat(130)); // Linha de separação no final
    }

    // Pesquisa uma peça no estoque pelo nome
    public void pesquisarPecaPorNomeFuncionario(String nome) throws SQLException {
        String query = "SELECT id_produto, produto_nome, produto_categoria, produto_preco, produto_descricao FROM Produto WHERE produto_nome LIKE ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, "%" + nome + "%"); // Define o parâmetro de busca

        ResultSet rs = stmt.executeQuery();

        // Cabeçalho das colunas
        System.out.printf("%-10s %-30s %-20s %-15s %-50s%n", "ID", "Nome", "Categoria", "Preço (R$)", "Descrição");
        System.out.println("=".repeat(130)); // Linha de separação

        // Itera pelos resultados e exibe cada peça encontrada
        while (rs.next()) {
            long id = rs.getLong("id_produto");
            System.out.printf("%-10d %-30s %-20s %-15.2f %-50s%n",
                    id,
                    rs.getString("produto_nome"),
                    rs.getString("produto_categoria"),
                    rs.getBigDecimal("produto_preco"),
                    rs.getString("produto_descricao"));
        }

        System.out.println("=".repeat(130)); // Linha de separação no final
    }

    // Pesquisa uma peça no estoque pela categoria
    public void pesquisarPecaPorCategoriaFuncionario(String categoria) throws SQLException {
        String query = "SELECT id_produto, produto_nome, produto_categoria, produto_preco, produto_descricao FROM Produto WHERE produto_categoria = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, categoria); // Define o parâmetro de busca

        ResultSet rs = stmt.executeQuery();

        // Cabeçalho das colunas
        System.out.printf("%-10s %-30s %-20s %-15s %-50s%n", "ID", "Nome", "Categoria", "Preço (R$)", "Descrição");
        System.out.println("=".repeat(130)); // Linha de separação

        // Itera pelos resultados e exibe cada peça encontrada
        while (rs.next()) {
            long id = rs.getLong("id_produto");
            System.out.printf("%-10d %-30s %-20s %-15.2f %-50s%n",
                    id,
                    rs.getString("produto_nome"),
                    rs.getString("produto_categoria"),
                    rs.getBigDecimal("produto_preco"),
                    rs.getString("produto_descricao"));
        }

        System.out.println("=".repeat(130)); // Linha de separação no final
    }

    // Pesquisa uma peça no estoque pelo fabricante
    public void pesquisarPecaPorFabricanteFuncionario(String fabricante) throws SQLException {
        String query = "SELECT id_produto, produto_nome, produto_categoria, produto_preco, produto_descricao FROM Produto WHERE produto_fabricante = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, fabricante); // Define o parâmetro de busca

        ResultSet rs = stmt.executeQuery();

        // Cabeçalho das colunas
        System.out.printf("%-10s %-30s %-20s %-15s %-50s%n", "ID", "Nome", "Categoria", "Preço (R$)", "Descrição");
        System.out.println("=".repeat(130)); // Linha de separação

        // Itera pelos resultados e exibe cada peça encontrada
        while (rs.next()) {
            long id = rs.getLong("id_produto");
            System.out.printf("%-10d %-30s %-20s %-15.2f %-50s%n",
                    id,
                    rs.getString("produto_nome"),
                    rs.getString("produto_categoria"),
                    rs.getBigDecimal("produto_preco"),
                    rs.getString("produto_descricao"));
        }

        System.out.println("=".repeat(130)); // Linha de separação no final
    }

    // Método para adicionar uma nova peça ao estoque
    public boolean adicionarPecaEstoque(String nomeProduto, String descricaoProduto, double precoProduto, String categoriaProduto, String fabricanteProduto, int quantidadeEstoque) throws SQLException {
        String sql = "INSERT INTO Produto (produto_nome, produto_descricao, produto_preco, produto_categoria, produto_fabricante, quantidade_estoque) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);

        // Preenchendo os valores da consulta
        stmt.setString(1, nomeProduto);
        stmt.setString(2, descricaoProduto);
        stmt.setDouble(3, precoProduto);
        stmt.setString(4, categoriaProduto);
        stmt.setString(5, fabricanteProduto);
        stmt.setInt(6, quantidadeEstoque);

        int rowsAffected = stmt.executeUpdate(); // Executa a inserção e obtém o número de linhas afetadas

        return rowsAffected > 0;  // Retorna true se a peça foi adicionada com sucesso
    }

    // Método para modificar as informações de uma peça no estoque
    public boolean modificarPecaEstoque(Long idProduto, String nomeProduto, String descricaoProduto, Double precoProduto, String categoriaProduto, String fabricanteProduto, Integer quantidadeEstoque) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE Produto SET ");
        ArrayList<Object> parametros = new ArrayList<>();

        // Verifica quais campos precisam ser atualizados
        if (nomeProduto != null && !nomeProduto.isEmpty()) {
            sql.append("produto_nome = ?, ");
            parametros.add(nomeProduto);
        }
        if (descricaoProduto != null && !descricaoProduto.isEmpty()) {
            sql.append("produto_descricao = ?, ");
            parametros.add(descricaoProduto);
        }
        if (precoProduto != null) {
            sql.append("produto_preco = ?, ");
            parametros.add(precoProduto);
        }
        if (categoriaProduto != null && !categoriaProduto.isEmpty()) {
            sql.append("produto_categoria = ?, ");
            parametros.add(categoriaProduto);
        }
        if (fabricanteProduto != null && !fabricanteProduto.isEmpty()) {
            sql.append("produto_fabricante = ?, ");
            parametros.add(fabricanteProduto);
        }
        if (quantidadeEstoque != null) {
            sql.append("quantidade_estoque = ?, ");
            parametros.add(quantidadeEstoque);
        }

        // Remover a última vírgula e espaço
        if (parametros.isEmpty()) {
            return false; // Nenhum campo foi alterado
        }

        // Remove a vírgula extra no final da string
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE id_produto = ?");
        parametros.add(idProduto);

        try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            // Preenche os valores dinâmicos
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }

            int rowsAffected = stmt.executeUpdate(); // Executa a atualização e obtém o número de linhas afetadas
            return rowsAffected > 0; // Retorna true se a modificação foi bem-sucedida
        }
    }

    // Método para gerar um relatório de vendas
    public void gerarRelatorioVendas() throws SQLException {
        // Preparar a chamada da stored procedure
        String sql = "{CALL RelatorioVendasSimplificado()}";
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            // Executar a stored procedure
            boolean hasResultSet = stmt.execute();

            // Processar o resultado da primeira consulta (Total de Vendas)
            if (hasResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    if (rs.next()) {
                        // Exibe total de vendas e peças vendidas
                        System.out.println("\nTotal de Vendas: R$ " + String.format("%.2f", rs.getBigDecimal("total_vendas")));
                        System.out.println("Total de Peças Vendidas: " + rs.getInt("total_pecas_vendidas"));
                        System.out.println("Total de Pedidos: " + rs.getInt("total_pedidos"));
                    }
                }
            }

            // Processar o resultado da segunda consulta (Total de Vendas por Funcionário)
            if (stmt.getMoreResults()) {
                try (ResultSet rs = stmt.getResultSet()) {
                    System.out.println("\nTotal de Vendas por Funcionário:");
                    System.out.printf("%-15s %-30s %-20s%n", "ID Funcionário", "Nome", "Total Vendas (R$)");
                    System.out.println("=".repeat(70)); // Linha de separação
                    while (rs.next()) {
                        // Exibe total de vendas por funcionário
                        System.out.printf("%-15d %-30s %-20.2f%n",
                                rs.getLong("id_funcionario"),
                                rs.getString("funcionario_nome"),
                                rs.getBigDecimal("total_vendas_funcionario"));
                    }
                }
            }

            // Processar o resultado da terceira consulta (Total de Vendas por Categoria)
            if (stmt.getMoreResults()) {
                try (ResultSet rs = stmt.getResultSet()) {
                    System.out.println("\nTotal de Vendas por Categoria de Produto:");
                    System.out.printf("%-30s %-20s%n", "Categoria", "Total Vendas (R$)");
                    System.out.println("=".repeat(70)); // Linha de separação
                    while (rs.next()) {
                        // Exibe total de vendas por categoria
                        System.out.printf("%-30s %-20.2f%n",
                                rs.getString("produto_categoria"),
                                rs.getBigDecimal("total_vendas_categoria"));
                    }
                }
            }
        }
    }
}
