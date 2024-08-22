package controler;

import java.sql.*;

public class CarPartDAO {

    private Connection conn;

    public CarPartDAO(Connection conn) {
        this.conn = conn;
        checkAndCreateTable();
    }

    // Método para verificar e criar a tabela "pecas" se não existir
    private void checkAndCreateTable() {
        try (Statement stmt = conn.createStatement()) {
            // Verifica se a tabela existe
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "pecas", null);
            if (!tables.next()) { // A tabela não existe
                System.out.println("Tabela 'pecas' não encontrada. Criando tabela...");

                // SQL para criar a tabela
                String sql = "CREATE TABLE pecas (" +
                        "id INT NOT NULL AUTO_INCREMENT, " +
                        "nome_peca VARCHAR(100), " +
                        "carro VARCHAR(100), " +
                        "valor DECIMAL(10, 2), " +
                        "PRIMARY KEY (id))";

                stmt.executeUpdate(sql);
                System.out.println("Tabela 'pecas' criada com sucesso.");
            } else {
                System.out.println("Tabela 'pecas' já existe.");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar/criar a tabela:");
            e.printStackTrace();
        }
    }

    public void insertPart(String nomePeca, String carro, double valor) throws SQLException {
        String insertSQL = "INSERT INTO pecas (nome_peca, carro, valor) VALUES (?, ?, ?)";
        try (PreparedStatement pr = conn.prepareStatement(insertSQL)) {
            pr.setString(1, nomePeca);
            pr.setString(2, carro);
            pr.setDouble(3, valor);
            pr.executeUpdate();
            System.out.println("Peça inserida com sucesso!");
        }
    }

    public void showAllParts() throws SQLException {
        String selectSQL = "SELECT * FROM pecas";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(selectSQL)) {
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Nome: " + rs.getString("nome_peca"));
                System.out.println("Carro: " + rs.getString("carro"));
                System.out.println("Valor: R$" + rs.getDouble("valor"));
                System.out.println("------------------------");
            }
        }
    }

    public void updatePart(String nomePeca, double valor) throws SQLException {
        String updateSQL = "UPDATE pecas SET valor = ? WHERE nome_peca = ?";
        try (PreparedStatement pr = conn.prepareStatement(updateSQL)) {
            pr.setDouble(1, valor);
            pr.setString(2, nomePeca);
            int rowsAffected = pr.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Peça atualizada com sucesso!");
            } else {
                System.out.println("Peça não encontrada.");
            }
        }
    }

    public void deletePart(String nomePeca) throws SQLException {
        String deleteSQL = "DELETE FROM pecas WHERE nome_peca = ?";
        try (PreparedStatement pr = conn.prepareStatement(deleteSQL)) {
            pr.setString(1, nomePeca);
            int rowsAffected = pr.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Peça removida com sucesso!");
            } else {
                System.out.println("Peça não encontrada.");
            }
        }
    }
}