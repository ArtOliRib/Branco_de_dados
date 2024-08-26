package controler;

import java.sql.*;

public class CarPartDAO {

    private Connection conn;

    public CarPartDAO(Connection conn) {
        this.conn = conn;
        checkAndCreateTables();
    }

    private void checkAndCreateTables() {
        try (Statement stmt = conn.createStatement()) {
            // Obtém metadata do banco de dados
            DatabaseMetaData dbm = conn.getMetaData();

            // Verifica se a tabela valor_peca existe
            ResultSet tables = dbm.getTables(null, null, "valor_peca", null);
            if (!tables.next()) { // A tabela não existe
                System.out.println("Tabela 'valor_peca' não encontrada. Criando tabela...");

                // SQL para criar a tabela valor_peca
                String sql = "CREATE TABLE valor_peca (" +
                        "id INT NOT NULL AUTO_INCREMENT, " +
                        "nome_peca VARCHAR(100) COLLATE utf8_general_ci, " +
                        "valor DECIMAL(10, 2), " +
                        "PRIMARY KEY (id)," +
                        "UNIQUE KEY (nome_peca))";

                stmt.executeUpdate(sql);
                System.out.println("Tabela 'valor_peca' criada com sucesso.");
            } else {
                System.out.println("Tabela 'valor_peca' já existe.");
            }

            // Verifica se a tabela pecas existe
            ResultSet tables2 = dbm.getTables(null, null, "pecas", null);
            if (!tables2.next()) { // A tabela não existe
                System.out.println("Tabela 'pecas' não encontrada. Criando tabela...");

                // SQL para criar a tabela pecas
                String sql = "CREATE TABLE pecas (" +
                        "carro VARCHAR(100) COLLATE utf8_general_ci, " +
                        "nome_peca VARCHAR(100) COLLATE utf8_general_ci, " +
                        "PRIMARY KEY (carro, nome_peca), " +
                        "FOREIGN KEY (nome_peca) REFERENCES valor_peca(nome_peca))";

                stmt.executeUpdate(sql);
                System.out.println("Tabela 'pecas' criada com sucesso.");
            } else {
                System.out.println("Tabela 'pecas' já existe.");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar/criar as tabelas:");
            e.printStackTrace();
        }
    }

    public void insertPart(String carro, String nomePeca, double valor) throws SQLException {
        // Primeiro, insere ou atualiza a tabela valor_peca
        String insertOrUpdateSQL = "INSERT INTO valor_peca (nome_peca, valor) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE valor = VALUES(valor)";
        try (PreparedStatement pr = conn.prepareStatement(insertOrUpdateSQL)) {
            pr.setString(1, nomePeca);
            pr.setDouble(2, valor);
            pr.executeUpdate();
        }

        // Depois, insere na tabela pecas
        String insertSQL = "INSERT INTO pecas (carro, nome_peca) VALUES (?, ?)";
        try (PreparedStatement pr = conn.prepareStatement(insertSQL)) {
            pr.setString(1, carro);
            pr.setString(2, nomePeca);
            pr.executeUpdate();
            System.out.println("Peça inserida com sucesso!");
        }
    }

    public void showAllParts() throws SQLException {
        // JOIN as duas tabelas para retirar as informações
        String selectSQL = "SELECT p.carro, p.nome_peca, v.valor " +
                "FROM pecas p JOIN valor_peca v ON p.nome_peca = v.nome_peca";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(selectSQL)) {
            while (rs.next()) {
                System.out.println("Carro: " + rs.getString("carro"));
                System.out.println("Nome da Peça: " + rs.getString("nome_peca"));
                System.out.println("Valor: R$" + rs.getDouble("valor"));
                System.out.println("------------------------");
            }
        }
    }

    public void updatePart(String nomePeca, String carro, double valor) throws SQLException {
        // Atualiza o valor da peça na tabela valor_peca
        String updateValorSQL = "UPDATE valor_peca SET valor = ? WHERE nome_peca = ?";
        try (PreparedStatement pr = conn.prepareStatement(updateValorSQL)) {
            pr.setDouble(1, valor);
            pr.setString(2, nomePeca);
            int colunasAlteradas = pr.executeUpdate();
            if (colunasAlteradas > 0) {
                System.out.println("Valor da peça atualizado com sucesso na tabela 'valor_peca'!");
            } else {
                System.out.println("Peça não encontrada na tabela 'valor_peca'.");
            }
        }

        // Atualiza o valor da peça na tabela pecas
        String updatePecasSQL = "UPDATE pecas SET nome_peca = ? WHERE carro = ? AND nome_peca = ?";
        try (PreparedStatement pr = conn.prepareStatement(updatePecasSQL)) {
            pr.setString(1, nomePeca);
            pr.setString(2, carro);
            pr.setString(3, nomePeca);
            int colunasAlteradas = pr.executeUpdate();
            if (colunasAlteradas > 0) {
                System.out.println("Peça atualizada com sucesso na tabela 'pecas'!");
            } else {
                System.out.println("Peça não encontrada na tabela 'pecas'.");
            }
        }
    }

    public void deletePart(String nomePeca, String carro) throws SQLException {
        // Primeiro, remove da tabela pecas
        String deleteFromPecasSQL = "DELETE FROM pecas WHERE nome_peca = ? AND carro = ?";
        try (PreparedStatement pr = conn.prepareStatement(deleteFromPecasSQL)) {
            pr.setString(1, nomePeca);
            pr.setString(2, carro);
            int colunasAlteradas = pr.executeUpdate();
            if (colunasAlteradas > 0) {
                System.out.println("Peça removida com sucesso da tabela 'pecas'!");
            } else {
                System.out.println("Peça não encontrada na tabela 'pecas'.");
            }
        }

        // Verifica se a peça ainda existe na tabela pecas
        String checkPecasSQL = "SELECT COUNT(*) FROM pecas WHERE nome_peca = ?";
        try (PreparedStatement pr = conn.prepareStatement(checkPecasSQL)) {
            pr.setString(1, nomePeca);
            try (ResultSet rs = pr.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // Se não houver mais referências para a peça na tabela pecas, remove da tabela
                    // valor_peca
                    String deleteFromValorPecaSQL = "DELETE FROM valor_peca WHERE nome_peca = ?";
                    try (PreparedStatement pr2 = conn.prepareStatement(deleteFromValorPecaSQL)) {
                        pr2.setString(1, nomePeca);
                        int colunasAlteradas = pr2.executeUpdate();
                        if (colunasAlteradas > 0) {
                            System.out.println("Peça removida com sucesso da tabela 'valor_peca'!");
                        } else {
                            System.out.println("Peça não encontrada na tabela 'valor_peca'.");
                        }
                    }
                }
            }
        }
    }

}
