import java.sql.*;
import java.util.Scanner;

import Classes.Cadastro;
import Controler.*;
import com.mysql.cj.xdevapi.Client;

public class Main {

    private static final String URL = "jdbc:mysql://localhost:3306/roda_quebrada";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private static int i = 1;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Conexão estabelecida com sucesso!");
            ClienteDAO ClienteDAO = new ClienteDAO(conn);

            while(i == 1){
                System.out.println("Teste de login e cadastro\n:");
                Cadastro cadastro = new Cadastro(ClienteDAO);

                cadastro.exibirMenu();
                ClienteDAO.listarClientes();
                ClienteDAO.login("12345678912","1234");

                i = 0;

            }


        } catch (SQLException error) {
            System.out.println(error);
        }
    }
}