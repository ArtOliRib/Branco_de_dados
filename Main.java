import java.sql.*;
import java.util.*;

import Classes.CadastroLogin;
import Controler.*;

public class Main {

    private static final String URL = "jdbc:mysql://localhost:3306/roda_quebrada";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean logadoCliente = false;
        boolean logadoFuncionario = false;
        long pedido;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Conexão estabelecida com sucesso!");
            ClienteDAO clienteDAO = new ClienteDAO(conn);
            CarrinhoDAO carrinhoDAO = new CarrinhoDAO(conn);
            CadastroLogin cadastroLogin = new CadastroLogin(clienteDAO);
            pedido = carrinhoDAO.criarPedidoTemporario();

            boolean running = true;

            while (running) {
                if (!logadoCliente && !logadoFuncionario) {
                    exibirMenuNaoLogado();
                    int opcao = scanner.nextInt();
                    scanner.nextLine(); // Limpar o buffer

                    switch (opcao) {
                        case 1:
                            cadastroLogin.exibirMenuCadastro(); // Exibir cadastro
                            break;
                        case 2:
                            boolean[] fluxoMenu = cadastroLogin.exibirMenuLogin(logadoCliente,logadoFuncionario, pedido);

                            logadoCliente = fluxoMenu[0];
                            logadoFuncionario = fluxoMenu[1];
                            break;
                        case 3:
                            clienteDAO.menuVisualizarPecas();
                            break;

                        case 4:
                            try {
                                System.out.println("Digite o nome da Peça:");
                                String nome = scanner.nextLine();

                                System.out.println("Digite a marca da Peça:");
                                String marca = scanner.nextLine();

                                System.out.println("Digite a quantidade:");
                                int quantidade = scanner.nextInt();


                                boolean teste = carrinhoDAO.adicionarItemPedido(pedido, nome, marca, quantidade);
                                if (teste){
                                    System.out.println("O produto foi adicionado com sucesso!");
                                } else{
                                    System.out.println("Falha na adição do produto");
                                }
                                break;

                            } catch (InputMismatchException e) {

                                System.out.println("Erro: A quantidade deve ser um número inteiro.");
                                scanner.nextLine();
                                break;

                            } catch (Exception e) {
                                // Handles any other exceptions that might occur
                                System.out.println("Ocorreu um erro: " + e.getMessage());
                                break;
                            }
                        case 5:
                            System.out.println(pedido);
                            carrinhoDAO.verCarrinho(pedido);
                            System.out.println("Voce não pode finalizar a compra porque voce não está logado!");
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
                            try {
                                System.out.println("Digite o nome da Peça:");
                                String nome = scanner.nextLine();

                                System.out.println("Digite a marca da Peça:");
                                String marca = scanner.nextLine();

                                System.out.println("Digite a quantidade:");
                                int quantidade = scanner.nextInt();


                                boolean teste = carrinhoDAO.adicionarItemPedido(pedido, nome, marca, quantidade);
                                if (teste){
                                    System.out.println("O produto foi adicionado com sucesso!");
                                } else{
                                    System.out.println("Falha na adição do produto");
                                }
                                break;

                            } catch (InputMismatchException e) {

                                System.out.println("Erro: A quantidade deve ser um número inteiro.");
                                scanner.nextLine();
                                break;

                            } catch (Exception e) {
                                // Handles any other exceptions that might occur
                                System.out.println("Ocorreu um erro: " + e.getMessage());
                                break;
                            }

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
        System.out.println("2. Fazer Login");
        System.out.println("3. Visualizar Peças");
        System.out.println("4. Adicionar peças ao carrinho");
        System.out.println("5. Ver carrinho");
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
