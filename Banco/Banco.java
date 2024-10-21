package Banco;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Banco {
    private static Connection connection;


    // Método para conectar ao banco de dados com um usuário específico
    public static Connection getConnection(String user, String password) throws SQLException {
        String url = "jdbc:mysql://localhost:3306/roda_quebrada"; // URL do seu banco de dados
        connection = DriverManager.getConnection(url, user, password);
        return connection;
    }

    // Método para fechar a conexão
    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
