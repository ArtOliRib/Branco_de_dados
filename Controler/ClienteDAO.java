package Controler;

import Classes.Cliente;
import Banco.Banco;
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

    // Método para fazer login do cliente
    public void login(String cpf, String senha) throws SQLException {
        String queryCliente = "SELECT * FROM Cliente WHERE cpf = ? and senha = ?";
        /*
        String queryFuncionario = "SELECT * FROM Cliente WHERE cpf = ? and senha = ?";
         */
        PreparedStatement stmtCliente = conn.prepareStatement(queryCliente);
        /*
        PreparedStatement stmtFuncionario = conn.prepareStatement(queryFuncionario);
         */
        stmtCliente.setString(1, cpf);
        stmtCliente.setString(2, senha);

        /*
        stmtFuncionario.setString(1, cpf);
        stmtFuncionario.setString(2, senha);
        */
        ResultSet rsCliente = stmtCliente.executeQuery();
        /*
        ResultSet rsFuncionario = stmtFuncionario.executeQuery();
         */
        if (rsCliente.next()) {

            // Troca para o usuário do banco para 'cliente' do banco
            Banco.closeConnection(); // Fecha a conexão atual
            conn = Banco.getConnection("cliente", "senha_cliente"); // Conecta com as credenciais do cliente

            // Atualizando o status do cliente

            System.out.println(("Voce esta logado!"));

        }else{

        // Se não encontrou, retorna null ou uma mensagem indicando login ou senha inválidos
        System.out.println("Login ou senha inválidos.");
        }
    }


    // Método para visualizar todas as peças
    public void visualizarPecas() throws SQLException {
        String query = "SELECT * FROM Produto";
        Statement stmt = conn.createStatement(); // Usando o 'conn'
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            System.out.println("Nome: " + rs.getString("produto_nome") +
                    ", Categoria: " + rs.getString("produto_categoria") +
                    ", Preço: " + rs.getBigDecimal("produto_preco"));
        }
    }

    // Método para pesquisar peças por nome
    public void pesquisarPecaPorNome(String nome) throws SQLException {
        String query = "SELECT * FROM Produto WHERE produto_nome LIKE ?";
        PreparedStatement stmt = conn.prepareStatement(query); // Usando o 'conn'
        stmt.setString(1, "%" + nome + "%");

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            System.out.println("Nome: " + rs.getString("produto_nome") +
                    ", Categoria: " + rs.getString("produto_categoria") +
                    ", Preço: " + rs.getBigDecimal("produto_preco"));
        }
    }

    // Método para pesquisar peças por categoria
    public void pesquisarPecaPorCategoria(String categoria) throws SQLException {
        String query = "SELECT * FROM Produto WHERE produto_categoria = ?";
        PreparedStatement stmt = conn.prepareStatement(query); // Usando o 'conn'
        stmt.setString(1, categoria);

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            System.out.println("Nome: " + rs.getString("produto_nome") +
                    ", Categoria: " + rs.getString("produto_categoria") +
                    ", Preço: " + rs.getBigDecimal("produto_preco"));
        }
    }

    // Método para pesquisar peças por fabricante
    public void pesquisarPecaPorFabricante(String fabricante) throws SQLException {
        String query = "SELECT * FROM Produto WHERE produto_fabricante = ?";
        PreparedStatement stmt = conn.prepareStatement(query); // Usando o 'conn'
        stmt.setString(1, fabricante);

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            System.out.println("Nome: " + rs.getString("produto_nome") +
                    ", Categoria: " + rs.getString("produto_categoria") +
                    ", Preço: " + rs.getBigDecimal("produto_preco"));
        }
    }
}
