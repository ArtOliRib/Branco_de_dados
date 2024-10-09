import java.sql.*;
import java.util.Scanner;

import Classes.Cadastro;
import Controler.*;

public class Main {

    private static final String URL = "jdbc:mysql://localhost:3306/roda_quebrada";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean logadoCliente = false;
        boolean logadoFuncionario = false;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Conexão estabelecida com sucesso!");
            ClienteDAO clienteDAO = new ClienteDAO(conn);
            Cadastro cadastro = new Cadastro(clienteDAO);

            boolean running = true;

            while (running) {
                if (!logadoCliente && !logadoFuncionario) {
                    exibirMenuNaoLogado();
                    int opcao = scanner.nextInt();
                    scanner.nextLine(); // Limpar o buffer

                    switch (opcao) {
                        case 1:
                            cadastro.exibirMenu(); // Exibir cadastro
                            break;
                        case 2:
                            // Adicione aqui a lógica para adicionar peças ao carrinho
                            System.out.println("Adicionar peças ao carrinho - em implementação.");
                            break;
                        case 3:
                            System.out.print("Digite o CPF: ");
                            String cpf = scanner.nextLine();
                            System.out.print("Digite a senha: ");
                            String senha = scanner.nextLine();
                            if (clienteDAO.login(cpf, senha) == 1) {
                                logadoCliente = true; // Marcar como logado se o login for bem-sucedido
                                System.out.println("Login cliente bem-sucedido!");
                            } else if (clienteDAO.login(cpf, senha) == 2) {
                                logadoFuncionario = true; // Marcar como logado se o login for bem-sucedido
                                System.out.println("Login Funcionario bem-sucedido!");
                            } else {
                                System.out.println("Login falhou. Verifique CPF e senha.");
                            }
                            break;
                        case 4:
                            clienteDAO.menuVisualizarPecas();
                            break;
                        case 0:
                            running = false;
                            System.out.println("Saindo do programa...");
                            break;
                        default:
                            System.out.println("Opção inválida! Tente novamente.");
                    }
                    System.out.println(); // Linha em branco para espaçamento

                } else if (logadoCliente) {
                    exibirMenuClienteLogado();
                    int opcao = scanner.nextInt();
                    scanner.nextLine(); // Limpar o buffer

                    switch (opcao) {
                        case 1:
                            clienteDAO.menuVisualizarPecas();
                            break;
                        case 2:
                            // Adicione aqui a lógica para adicionar peças ao carrinho
                            System.out.println("Adicionar peças ao carrinho - em implementação.");
                            break;
                        case 3:
                            // Adicione aqui a lógica para finalizar compra
                            System.out.println("Finalizar compra - em implementação.");
                            break;
                        case 0:
                            logadoCliente = false;
                            break;
                        default:
                            System.out.println("Opção inválida! Tente novamente.");
                    }
                    System.out.println(); // Linha em branco para espaçamento
                } else if (logadoFuncionario) {  // Corrigindo a condição para funcionários
                exibirMenuFuncionarioLogado();
                int opcao = scanner.nextInt();
                scanner.nextLine(); // Limpar o buffer

                switch (opcao) {
                    case 1:
                        // Visualizar peças disponíveis no sistema
                        clienteDAO.menuVisualizarPecas();
                        break;
                    case 2:
                        // Adicionar peças ao sistema (em implementação)
                        System.out.println("Adicionar novas peças ao sistema - em implementação.");
                        // Futuramente, implementar lógica para adicionar peças ao estoque
                        break;
                    case 3:
                        // Gerar relatório mensal (em implementação)
                        System.out.println("Gerar relatório mensal de vendas/estoque - em implementação.");
                        // Futuramente, implementar lógica para geração de relatórios
                        break;
                    case 0:
                        // Sair do sistema
                        logadoFuncionario = false; // Deslogar funcionário
                        System.out.println("Saindo da sessão...");
                        break;
                    default:
                        System.out.println("Opção inválida! Tente novamente.");
                }
                System.out.println(); // Linha em branco para espaçamento

                }
            }

        } catch (SQLException error) {
            System.out.println("Erro ao conectar: " + error.getMessage());
        } finally {
            scanner.close(); // Fechar o scanner ao final
        }
    }

    private static void exibirMenuNaoLogado() {
        System.out.println("=== Menu Principal ===");
        System.out.println("1. Fazer cadastro");
        System.out.println("2. Adicionar peças ao carrinho");
        System.out.println("3. Fazer Login");
        System.out.println("4. Visualizar Peças");
        System.out.println("0. Sair");
        System.out.print("Escolha uma opção: ");
    }

    private static void exibirMenuClienteLogado() {
        System.out.println("=== Menu Principal Cliente Logado ===");
        System.out.println("1. Visualizar Peças");
        System.out.println("2. Adicionar peças ao carrinho");
        System.out.println("3. Finalizar compra");
        System.out.println("0. Sair");
        System.out.print("Escolha uma opção: ");
    }

    private static void exibirMenuFuncionarioLogado() {
        System.out.println("=== Menu Principal Logado Funcionario ===");
        System.out.println("1. Visualizar Peças");
        System.out.println("2. Adicionar peças ao sistema");
        System.out.println("3. Gerar relatorio mensal");
        System.out.println("0. Sair");
        System.out.print("Escolha uma opção: ");
    }
}
