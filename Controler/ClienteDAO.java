package Controler;

import Classes.Cliente;
import Banco.Banco;
import java.util.Scanner;

import java.math.BigDecimal;
import java.sql.*;

public class ClienteDAO {

    private Connection conn;

    public ClienteDAO(Connection conn) {
        this.conn = conn;  // Atribuindo a conexão recebida ao atributo da classe
    }

    public void listarClientes() throws SQLException {
        String query = "SELECT * FROM Cliente";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setNome(rs.getString("nome"));
                cliente.setSobrenome(rs.getString("sobrenome"));
                cliente.setCpf(rs.getString("cpf"));
                cliente.setSenha(rs.getString("senha"));
                cliente.setDataNascimento(rs.getDate("data_nascimento").toLocalDate());
                cliente.setTorcida(rs.getString("torcida"));
                cliente.setAssisteOp(rs.getBoolean("assiste_op"));
                cliente.setCidade(rs.getString("cidade"));

                System.out.println(cliente);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Retorna a lista de clientes
    }
    public boolean cadastrarCliente(Cliente cliente) throws SQLException {
        String query = "INSERT INTO Cliente (nome, sobrenome, cpf, senha, data_nascimento, torcida, assiste_op, cidade) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        // Usando o 'conn' passado no construtor
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getSobrenome());
            stmt.setString(3, cliente.getCpf());
            stmt.setString(4, cliente.getSenha()); // Certifique-se de que a senha está segura
            stmt.setDate(5, Date.valueOf(cliente.getDataNascimento()));
            stmt.setString(6, cliente.getTorcida());
            stmt.setBoolean(7, cliente.isAssisteOp());
            stmt.setString(8, cliente.getCidade());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Retorna true se a inserção foi bem-sucedida
        } catch (SQLException e) {
            e.printStackTrace(); // Exibe o erro para depuração
            return false; // Retorna false se houver um erro durante a inserção
        }
    }

    public int login(String emailOuNumero, String senha, long pedido_id) throws SQLException {
        // Consulta para verificar login do Cliente (usando email)
        String queryCliente = "SELECT * FROM Cliente WHERE email = ? and senha = ?";

        // Consulta para verificar login do Funcionario (usando numero_cadastro)
        String queryFuncionario = "SELECT * FROM Funcionario WHERE numero_cadastro = ? and senha = ?";

        if (emailOuNumero.contains("@")) {
            // Login como Cliente (email)
            PreparedStatement stmtCliente = conn.prepareStatement(queryCliente);
            stmtCliente.setString(1, emailOuNumero);
            stmtCliente.setString(2, senha);
            ResultSet rsCliente = stmtCliente.executeQuery();

            if (rsCliente.next()) {
                // Pega o cliente_id
                long clienteId = rsCliente.getLong("id_cliente");

                // Fecha a conexão atual e conecta com as credenciais do cliente
                Banco.closeConnection();
                conn = Banco.getConnection("cliente", "senha_cliente");

                System.out.println("Você está logado como Cliente!");

                // Atualiza a tabela Pedido, associando o cliente aos pedidos não finalizados
                String updatePedido = "UPDATE Pedido SET cliente_id = ? WHERE id_pedido = ?";
                PreparedStatement stmtUpdatePedido = conn.prepareStatement(updatePedido);
                stmtUpdatePedido.setLong(1, clienteId);
                stmtUpdatePedido.setLong(2,pedido_id);
                int rowsAffected = stmtUpdatePedido.executeUpdate();


                return 1; // Cliente logado com sucesso
            }
        } else {
            // Login como Funcionario (numero_cadastro)
            PreparedStatement stmtFuncionario = conn.prepareStatement(queryFuncionario);
            stmtFuncionario.setString(1, emailOuNumero);
            stmtFuncionario.setString(2, senha);
            ResultSet rsFuncionario = stmtFuncionario.executeQuery();

            if (rsFuncionario.next()) {
                Banco.closeConnection(); // Fecha a conexão atual
                conn = Banco.getConnection("funcionario", "senha_funcionario"); // Conecta com as credenciais do funcionário
                System.out.println("Você está logado como Funcionario!");
                return 2;
            }
        }

        // Caso não tenha encontrado cliente ou funcionário, login inválido
        System.out.println("Login ou senha inválidos.");
        return 0;
    }


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
                    this.visualizarPecas();
                    break;
                case 2:
                    System.out.print("Digite o nome da peça: ");
                    String nome = scanner.nextLine();
                    this.pesquisarPecaPorNome(nome);
                    break;
                case 3:
                    System.out.print("Digite o nome do fabricante: ");
                    String fabricante = scanner.nextLine();
                    this.pesquisarPecaPorFabricante(fabricante);
                    break;
                case 4:
                    System.out.println("Digite a categoria da peça");
                    String categoria = scanner.nextLine();
                    this.pesquisarPecaPorCategoria(categoria);
                    break;
                case 5:
                    continuar = false;
                    System.out.println("Saindo do menu.");
                    break;
                default:
                    System.out.println("Opção inválida, tente novamente.");
            }
        }
    }

    // Método para visualizar todas as peças
    public void visualizarPecas() throws SQLException {
        String query = "SELECT * FROM Produto";
        Statement stmt = conn.createStatement(); // Usando o 'conn'
        ResultSet rs = stmt.executeQuery(query);

        System.out.printf("%-30s %-20s %-15s %-50s%n", "Nome", "Categoria", "Preço (R$)", "Descrição");
        System.out.println("=".repeat(120)); // Linha de separação

        while (rs.next()) {
            String nome = rs.getString("produto_nome");
            String categoria = rs.getString("produto_categoria");
            BigDecimal preco = rs.getBigDecimal("produto_preco");
            String descricao = rs.getString("produto_descricao");

            System.out.printf("%-30s %-20s %-15.2f %-50s%n", nome, categoria, preco, descricao);
        }

        System.out.println("=".repeat(120)); // Linha de separação no final
    }


    // Método para pesquisar peças por nome
    public void pesquisarPecaPorNome(String nome) throws SQLException {
        String query = "SELECT * FROM Produto WHERE produto_nome LIKE ?";
        PreparedStatement stmt = conn.prepareStatement(query); // Usando o 'conn'
        stmt.setString(1, "%" + nome + "%");

        ResultSet rs = stmt.executeQuery();

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

    // Método para pesquisar peças por categoria
    public void pesquisarPecaPorCategoria(String categoria) throws SQLException {
        String query = "SELECT * FROM Produto WHERE produto_categoria = ?";
        PreparedStatement stmt = conn.prepareStatement(query); // Usando o 'conn'
        stmt.setString(1, categoria);

        ResultSet rs = stmt.executeQuery();

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

    // Método para pesquisar peças por fabricante
    public void pesquisarPecaPorFabricante(String fabricante) throws SQLException {
        String query = "SELECT * FROM Produto WHERE produto_fabricante = ?";
        PreparedStatement stmt = conn.prepareStatement(query); // Usando o 'conn'
        stmt.setString(1, fabricante);

        ResultSet rs = stmt.executeQuery();

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
