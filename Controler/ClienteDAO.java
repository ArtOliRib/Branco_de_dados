package Controler;

import Classes.Cliente;
import Banco.Banco;
import java.util.Scanner;
import java.math.BigDecimal;
import java.sql.*;

public class ClienteDAO {

    private Connection conn;

    public ClienteDAO(Connection conn) {
        this.conn = conn;  // Atribui a conexão recebida ao atributo da classe
    }

    // Cadastra um novo cliente no banco de dados
    public boolean cadastrarCliente(Cliente cliente) throws SQLException {
        String query = "INSERT INTO Cliente (nome, sobrenome, cpf, email, senha, data_nascimento, torcida, assiste_op, cidade) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Usa a conexão passada no construtor
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getSobrenome());
            stmt.setString(3, cliente.getCpf());
            stmt.setString(4, cliente.getEmail());
            stmt.setString(5, cliente.getSenha()); // Certifique-se de que a senha está segura
            stmt.setDate(6, Date.valueOf(cliente.getDataNascimento()));
            stmt.setString(7, cliente.getTorcida());
            stmt.setBoolean(8, cliente.isAssisteOp());
            stmt.setString(9, cliente.getCidade());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Retorna true se a inserção foi bem-sucedida
        } catch (SQLException e) {
            e.printStackTrace(); // Exibe o erro para depuração
            return false; // Retorna false se houver um erro durante a inserção
        }
    }

    // Realiza o login do cliente ou funcionário com base nas credenciais fornecidas
    public long login(String emailOuNumero, String senha, long pedido_id, Cliente cliente) throws SQLException {
        // Consulta para verificar login do Cliente (usando email)
        String queryCliente = "SELECT * FROM Cliente WHERE email = ? and senha = ?";

        // Consulta para verificar login do Funcionário (usando numero_cadastro)
        String queryFuncionario = "SELECT * FROM Funcionario WHERE numero_cadastro = ? and senha = ?";

        if (emailOuNumero.contains("@")) {
            // Tenta o login como Cliente (email)
            PreparedStatement stmtCliente = conn.prepareStatement(queryCliente);
            stmtCliente.setString(1, emailOuNumero);
            stmtCliente.setString(2, senha);
            ResultSet rsCliente = stmtCliente.executeQuery();

            if (rsCliente.next()) {
                // Obtém o cliente_id
                long clienteId = rsCliente.getLong("id_cliente");
                cliente.setIdCliente(clienteId);

                // Fecha a conexão atual e conecta com as credenciais do cliente
                Banco.closeConnection();
                conn = Banco.getConnection("cliente", "senha_cliente");

                System.out.println("Você está logado como Cliente!");

                // Atualiza a tabela Pedido, associando o cliente aos pedidos não finalizados
                String updatePedido = "UPDATE Pedido SET cliente_id = ? WHERE id_pedido = ?";
                PreparedStatement stmtUpdatePedido = conn.prepareStatement(updatePedido);
                stmtUpdatePedido.setLong(1, clienteId);
                stmtUpdatePedido.setLong(2, pedido_id);
                stmtUpdatePedido.executeUpdate(); // Executa a atualização

                return 1; // Cliente logado com sucesso
            }
        } else {
            // Tenta o login como Funcionário (numero_cadastro)
            PreparedStatement stmtFuncionario = conn.prepareStatement(queryFuncionario);
            stmtFuncionario.setString(1, emailOuNumero);
            stmtFuncionario.setString(2, senha);
            ResultSet rsFuncionario = stmtFuncionario.executeQuery();

            if (rsFuncionario.next()) {
                Banco.closeConnection(); // Fecha a conexão atual
                conn = Banco.getConnection("funcionario", "senha_funcionario"); // Conecta com as credenciais do funcionário
                System.out.println("Você está logado como Funcionário!");
                return 2; // Funcionário logado com sucesso
            }
        }

        // Caso não tenha encontrado cliente ou funcionário, login inválido
        System.out.println("Login ou senha inválidos.");
        return 0; // Login falhou
    }

    // Exibe o menu para visualizar peças
    public void menuVisualizarPecas() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        boolean continuar = true;

        while (continuar) {
            System.out.println("\nMenu Visualizar Peças");
            System.out.println("1. Listar todas as peças");
            System.out.println("2. Pesquisar peça por nome");
            System.out.println("3. Pesquisar peça por fabricante");
            System.out.println("4. Pesquisar peça por categoria");
            System.out.println("5. Sair");

            System.out.print("Escolha uma opção: ");
            int opcao = scanner.nextInt();
            scanner.nextLine(); // Consumir nova linha

            switch (opcao) {
                case 1:
                    this.visualizarPecas(); // Chama o método para listar todas as peças
                    break;
                case 2:
                    System.out.print("Digite o nome da peça: ");
                    String nome = scanner.nextLine();
                    this.pesquisarPecaPorNome(nome); // Chama o método para pesquisar peça por nome
                    break;
                case 3:
                    System.out.print("Digite o nome do fabricante: ");
                    String fabricante = scanner.nextLine();
                    this.pesquisarPecaPorFabricante(fabricante); // Chama o método para pesquisar peça por fabricante
                    break;
                case 4:
                    System.out.print("Digite a categoria da peça: ");
                    String categoria = scanner.nextLine();
                    this.pesquisarPecaPorCategoria(categoria); // Chama o método para pesquisar peça por categoria
                    break;
                case 5:
                    continuar = false; // Encerra o loop se a opção for sair
                    System.out.println("Saindo do menu.");
                    break;
                default:
                    System.out.println("Opção inválida, tente novamente."); // Mensagem de erro para opções inválidas
            }
        }
    }

    // Visualiza todas as peças no banco de dados
    public void visualizarPecas() throws SQLException {
        String query = "SELECT * FROM Produto";
        Statement stmt = conn.createStatement(); // Usa a conexão atual
        ResultSet rs = stmt.executeQuery(query);

        // Cabeçalho das colunas
        System.out.printf("%-30s %-20s %-15s %-50s%n", "Nome", "Categoria", "Preço (R$)", "Descrição");
        System.out.println("=".repeat(120)); // Linha de separação

        while (rs.next()) {
            String nome = rs.getString("produto_nome");
            String categoria = rs.getString("produto_categoria");
            BigDecimal preco = rs.getBigDecimal("produto_preco");
            String descricao = rs.getString("produto_descricao");

            // Exibe os dados das peças
            System.out.printf("%-30s %-20s %-15.2f %-50s%n", nome, categoria, preco, descricao);
        }

        System.out.println("=".repeat(120)); // Linha de separação no final
    }

    // Pesquisa peças por nome no banco de dados
    public void pesquisarPecaPorNome(String nome) throws SQLException {
        String query = "SELECT * FROM Produto WHERE produto_nome LIKE ?";
        PreparedStatement stmt = conn.prepareStatement(query); // Usa a conexão atual
        stmt.setString(1, "%" + nome + "%");

        ResultSet rs = stmt.executeQuery();

        // Cabeçalho das colunas
        System.out.printf("%-30s %-20s %-15s %-50s%n", "Nome", "Categoria", "Preço (R$)", "Descrição");
        System.out.println("=".repeat(120)); // Linha de separação

        while (rs.next()) {
            System.out.printf("%-30s %-20s %-15.2f %-50s%n",
                    rs.getString("produto_nome"),
                    rs.getString("produto_categoria"),
                    rs.getBigDecimal("produto_preco"),
                    rs.getString("produto_descricao"));
        }

        System.out.println("=".repeat(120)); // Linha de separação no final
    }

    // Pesquisa peças por categoria no banco de dados
    public void pesquisarPecaPorCategoria(String categoria) throws SQLException {
        String query = "SELECT * FROM Produto WHERE produto_categoria = ?";
        PreparedStatement stmt = conn.prepareStatement(query); // Usa a conexão atual
        stmt.setString(1, categoria);

        ResultSet rs = stmt.executeQuery();

        // Cabeçalho das colunas
        System.out.printf("%-30s %-20s %-15s %-50s%n", "Nome", "Categoria", "Preço (R$)", "Descrição");
        System.out.println("=".repeat(120)); // Linha de separação

        while (rs.next()) {
            System.out.printf("%-30s %-20s %-15.2f %-50s%n",
                    rs.getString("produto_nome"),
                    rs.getString("produto_categoria"),
                    rs.getBigDecimal("produto_preco"),
                    rs.getString("produto_descricao"));
        }

        System.out.println("=".repeat(120)); // Linha de separação no final
    }

    // Pesquisa peças por fabricante no banco de dados
    public void pesquisarPecaPorFabricante(String fabricante) throws SQLException {
        String query = "SELECT * FROM Produto WHERE produto_fabricante = ?";
        PreparedStatement stmt = conn.prepareStatement(query); // Usa a conexão atual
        stmt.setString(1, fabricante);

        ResultSet rs = stmt.executeQuery();

        // Cabeçalho das colunas
        System.out.printf("%-30s %-20s %-15s %-50s%n", "Nome", "Categoria", "Preço (R$)", "Descrição");
        System.out.println("=".repeat(120)); // Linha de separação

        while (rs.next()) {
            System.out.printf("%-30s %-20s %-15.2f %-50s%n",
                    rs.getString("produto_nome"),
                    rs.getString("produto_categoria"),
                    rs.getBigDecimal("produto_preco"),
                    rs.getString("produto_descricao"));
        }

        System.out.println("=".repeat(120)); // Linha de separação no final
    }
}
