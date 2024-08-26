import java.sql.*;
import java.util.Scanner;
import controler.*;

public class Main {

    private static final String URL = "jdbc:mysql://localhost:3306/bd_solda";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Conexão estabelecida com sucesso!");

            CarPartDAO carPartDAO = new CarPartDAO(conn);

            while (true) {
                System.out.println("\n=== Loja de Peças de Carro ===");
                System.out.println("1. Inserir peça");
                System.out.println("2. Exibir todas as peças");
                System.out.println("3. Atualizar peça");
                System.out.println("4. Remover peça");
                System.out.println("5. Sair");
                System.out.print("Escolha uma opção: ");
                int option = scanner.nextInt();
                scanner.nextLine(); // Limpa o buffer
                System.out.println("");

                switch (option) {
                    case 1:
                        System.out.print("Nome da peça: ");
                        String nomePeca = scanner.nextLine();
                        System.out.print("Carro: ");
                        String carro = scanner.nextLine();
                        System.out.print("Valor (em reais): ");
                        double valor = scanner.nextDouble();
                        carPartDAO.insertPart(carro, nomePeca, valor);
                        break;
                    case 2:
                        carPartDAO.showAllParts();
                        System.out.println("");
                        break;
                    case 3:
                        System.out.print("Nome da peça a ser atualizada: ");
                        nomePeca = scanner.nextLine();
                        System.out.print("Nome do carro a que a peça pertence: ");
                        carro = scanner.nextLine();
                        System.out.print("Novo valor (em reais): ");
                        valor = scanner.nextDouble();
                        carPartDAO.updatePart(nomePeca, carro, valor);
                        break;
                    case 4:
                        System.out.print("Nome da peça a ser removida: ");
                        nomePeca = scanner.nextLine();
                        System.out.print("Nome do carro a que a peça pertence: ");
                        carro = scanner.nextLine();
                        carPartDAO.deletePart(nomePeca, carro);
                        break;
                    case 5:
                        System.out.println("Saindo...");
                        scanner.close();
                        return;
                    default:
                        System.out.println("Opção inválida, tente novamente.");
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}