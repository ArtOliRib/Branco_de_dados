import java.sql.*;
import java.util.*;

import Classes.CadastroLogin;
import Classes.Cliente;
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

        // Estabelece a conexão com o banco de dados
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Conexão estabelecida com sucesso!");
            ClienteDAO clienteDAO = new ClienteDAO(conn);
            CarrinhoDAO carrinhoDAO = new CarrinhoDAO(conn);
            CadastroLogin cadastroLogin = new CadastroLogin(clienteDAO);
            EstoqueDAO estoqueDAO = new EstoqueDAO(conn);
            pedido = carrinhoDAO.criarPedidoTemporario(); // Cria um pedido temporário
            Cliente clienteAtual = new Cliente(); // Instancia um cliente atual

            boolean running = true; // Flag para controlar o loop principal

            // Loop principal do programa
            while (running) {
                if (!logadoCliente && !logadoFuncionario) {
                    exibirMenuNaoLogado(); // Exibe menu para usuários não logados
                    int opcao = scanner.nextInt();
                    scanner.nextLine(); // Limpa o buffer

                    // Trata as opções do menu para usuários não logados
                    switch (opcao) {
                        case 1:
                            cadastroLogin.exibirMenuCadastro(); // Exibir menu de cadastro
                            break;
                        case 2:
                            // Exibe menu de login e atualiza status de login
                            boolean[] fluxoMenu = cadastroLogin.exibirMenuLogin(logadoCliente, logadoFuncionario, pedido, clienteAtual);
                            logadoCliente = fluxoMenu[0];
                            logadoFuncionario = fluxoMenu[1];
                            break;
                        case 3:
                            clienteDAO.menuVisualizarPecas(); // Visualiza peças disponíveis
                            break;
                        case 4:
                            try {
                                // Solicita informações sobre o item a ser adicionado ao carrinho
                                System.out.println("Digite o nome da Peça:");
                                String nome = scanner.nextLine();

                                System.out.println("Digite a marca da Peça:");
                                String marca = scanner.nextLine();

                                System.out.println("Digite a quantidade:");
                                int quantidade = scanner.nextInt();

                                // Adiciona o item ao carrinho
                                boolean teste = carrinhoDAO.adicionarItemPedido(pedido, nome, marca, quantidade);
                                if (teste) {
                                    System.out.println("O produto foi adicionado com sucesso!");
                                } else {
                                    System.out.println("Falha na adição do produto");
                                }
                                break;

                            } catch (InputMismatchException e) {
                                System.out.println("Erro: A quantidade deve ser um número inteiro."); // Mensagem de erro para input inválido
                                scanner.nextLine(); // Limpa o buffer
                                break;

                            } catch (Exception e) {
                                // Captura qualquer outra exceção que possa ocorrer
                                System.out.println("Ocorreu um erro: " + e.getMessage());
                                break;
                            }
                        case 5:
                            carrinhoDAO.verCarrinho(pedido); // Exibe o carrinho
                            System.out.println("Você não pode finalizar a compra porque não está logado!");
                            break;
                        case 0:
                            running = false; // Encerra o programa
                            System.out.println("Saindo do programa...");
                            break;
                        default:
                            System.out.println("Opção inválida! Tente novamente."); // Mensagem de erro para opção inválida
                    }
                    System.out.println(); // Linha em branco para espaçamento

                } else if (logadoCliente) {
                    exibirMenuClienteLogado(); // Exibe menu para cliente logado
                    int opcao = scanner.nextInt();
                    scanner.nextLine(); // Limpa o buffer

                    // Trata as opções do menu para clientes logados
                    switch (opcao) {
                        case 1:
                            clienteDAO.menuVisualizarPecas(); // Visualiza peças disponíveis
                            break;
                        case 2:
                            try {
                                // Solicita informações sobre o item a ser adicionado ao carrinho
                                System.out.println("Digite o nome da Peça:");
                                String nome = scanner.nextLine();

                                System.out.println("Digite a marca da Peça:");
                                String marca = scanner.nextLine();

                                System.out.println("Digite a quantidade:");
                                int quantidade = scanner.nextInt();

                                // Adiciona o item ao carrinho
                                boolean teste = carrinhoDAO.adicionarItemPedido(pedido, nome, marca, quantidade);
                                if (teste) {
                                    System.out.println("O produto foi adicionado com sucesso!");
                                } else {
                                    System.out.println("Falha na adição do produto");
                                }
                                break;

                            } catch (InputMismatchException e) {
                                System.out.println("Erro: A quantidade deve ser um número inteiro."); // Mensagem de erro para input inválido
                                scanner.nextLine(); // Limpa o buffer
                                break;

                            } catch (Exception e) {
                                // Captura qualquer outra exceção que possa ocorrer
                                System.out.println("Ocorreu um erro: " + e.getMessage());
                                break;
                            }
                        case 3:
                            carrinhoDAO.verCarrinho(pedido); // Exibe o carrinho
                            break;
                        case 4:
                            // Finaliza a compra
                            carrinhoDAO.finalizarCompra(pedido, clienteAtual.getIdCliente());
                            break;
                        case 0:
                            logadoCliente = false; // Desloga o cliente
                            break;
                        default:
                            System.out.println("Opção inválida! Tente novamente."); // Mensagem de erro para opção inválida
                    }
                    System.out.println(); // Linha em branco para espaçamento

                } else if (logadoFuncionario) { // Tratamento para funcionários logados
                    exibirMenuFuncionarioLogado(); // Exibe menu para funcionário logado
                    int opcao = scanner.nextInt();
                    scanner.nextLine(); // Limpa o buffer

                    // Trata as opções do menu para funcionários logados
                    switch (opcao) {
                        case 1:
                            estoqueDAO.menuVisualizarPecasFuncionario(); // Visualiza peças disponíveis no sistema
                            break;
                        case 2:
                            // Adiciona uma nova peça ao estoque
                            System.out.println("=== Adicionar Peça ao Estoque ===");

                            System.out.print("Nome da peça: ");
                            String nome = scanner.nextLine();

                            System.out.print("Descrição da peça: ");
                            String descricao = scanner.nextLine();

                            System.out.print("Preço da peça: ");
                            double preco = scanner.nextDouble();

                            System.out.print("Categoria da peça: ");
                            scanner.nextLine();  // Consumir a nova linha
                            String categoria = scanner.nextLine();

                            System.out.print("Fabricante da peça: ");
                            String fabricante = scanner.nextLine();

                            System.out.print("Quantidade em estoque: ");
                            int quantidadeEstoque = scanner.nextInt();

                            try {
                                boolean sucesso = estoqueDAO.adicionarPecaEstoque(nome, descricao, preco, categoria, fabricante, quantidadeEstoque);
                                if (sucesso) {
                                    System.out.println("Peça adicionada ao estoque com sucesso!");
                                } else {
                                    System.out.println("Falha ao adicionar a peça ao estoque.");
                                }
                            } catch (SQLException e) {
                                System.out.println("Erro ao adicionar peça ao estoque: " + e.getMessage());
                            }
                            break;
                        case 3:
                            // Modifica uma peça existente no estoque
                            System.out.println("=== Modificar Peça no Estoque ===");

                            System.out.print("ID da peça a ser modificada: ");
                            long idPeca = scanner.nextLong();
                            scanner.nextLine();  // Consumir a nova linha

                            // Variáveis para armazenar os novos valores
                            String novoNome = null;
                            String novaDescricao = null;
                            Double novoPreco = null;
                            String novaCategoria = null;
                            String novoFabricante = null;
                            Integer novaQuantidade = null;

                            boolean continuarModificando = true; // Flag para controlar o loop de modificações

                            // Loop para modificar as propriedades da peça
                            while (continuarModificando) {
                                System.out.println("O que você deseja modificar?");
                                System.out.println("1. Nome");
                                System.out.println("2. Descrição");
                                System.out.println("3. Preço");
                                System.out.println("4. Categoria");
                                System.out.println("5. Fabricante");
                                System.out.println("6. Quantidade em estoque");
                                System.out.println("0. Finalizar modificações");

                                int opcaoModificacao = scanner.nextInt();
                                scanner.nextLine(); // Consumir nova linha

                                switch (opcaoModificacao) {
                                    case 1:
                                        System.out.print("Novo nome da peça: ");
                                        novoNome = scanner.nextLine();  // Armazena o novo nome
                                        break;
                                    case 2:
                                        System.out.print("Nova descrição da peça: ");
                                        novaDescricao = scanner.nextLine();  // Armazena a nova descrição
                                        break;
                                    case 3:
                                        System.out.print("Novo preço da peça: ");
                                        novoPreco = scanner.nextDouble();  // Armazena o novo preço
                                        scanner.nextLine();  // Consumir nova linha
                                        break;
                                    case 4:
                                        System.out.print("Nova categoria da peça: ");
                                        novaCategoria = scanner.nextLine();  // Armazena a nova categoria
                                        break;
                                    case 5:
                                        System.out.print("Novo fabricante da peça: ");
                                        novoFabricante = scanner.nextLine();  // Armazena o novo fabricante
                                        break;
                                    case 6:
                                        System.out.print("Nova quantidade em estoque: ");
                                        novaQuantidade = scanner.nextInt();  // Armazena a nova quantidade
                                        scanner.nextLine();  // Consumir nova linha
                                        break;
                                    case 0:
                                        continuarModificando = false;  // Finaliza as modificações
                                        System.out.println("Modificações finalizadas.");

                                        // Chama o método para modificar a peça no estoque
                                        try {
                                            boolean sucesso = estoqueDAO.modificarPecaEstoque(
                                                    idPeca, novoNome, novaDescricao, novoPreco, novaCategoria, novoFabricante, novaQuantidade
                                            );
                                            if (sucesso) {
                                                System.out.println("Peça modificada com sucesso!");
                                            } else {
                                                System.out.println("Falha ao modificar a peça.");
                                            }
                                        } catch (SQLException e) {
                                            System.out.println("Erro ao modificar peça no estoque: " + e.getMessage());
                                        }
                                        break;
                                    default:
                                        System.out.println("Opção inválida! Tente novamente."); // Mensagem de erro para opção inválida
                                        break;
                                }
                            }
                            break;

                        case 4:
                            // Aqui deve haver lógica para deletar uma peça do sistema
                            // estoqueDAO.deletarPeca(); // Exemplo de chamada, precisa ser implementado
                            break;

                        case 5:
                            estoqueDAO.gerarRelatorioVendas(); // Gera um relatório de vendas
                            break;

                        case 0:
                            logadoFuncionario = false; // Desloga o funcionário
                            System.out.println("Saindo da sessão...");
                            break;
                        default:
                            System.out.println("Opção inválida! Tente novamente."); // Mensagem de erro para opção inválida
                    }
                    System.out.println(); // Linha em branco para espaçamento
                }
            }

        } catch (SQLException error) {
            System.out.println("Erro ao conectar: " + error.getMessage()); // Mensagem de erro ao conectar ao banco
        } finally {
            scanner.close(); // Fechar o scanner ao final
        }
    }

    // Método para exibir o menu principal para usuários não logados
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

    // Método para exibir o menu principal para clientes logados
    private static void exibirMenuClienteLogado() {
        System.out.println("=== Menu Principal Cliente Logado ===");
        System.out.println("1. Visualizar Peças");
        System.out.println("2. Adicionar peças ao carrinho");
        System.out.println("3. Ver Carrinho");
        System.out.println("4. Finalizar compra");
        System.out.println("0. Sair");
        System.out.print("Escolha uma opção: ");
    }

    // Método para exibir o menu principal para funcionários logados
    private static void exibirMenuFuncionarioLogado() {
        System.out.println("=== Menu Principal Logado Funcionario ===");
        System.out.println("1. Visualizar Peças");
        System.out.println("2. Adicionar peças ao sistema");
        System.out.println("3. Modificar peça no sistema");
        System.out.println("4. Deletar uma peça do sistema"); // Implementar lógica para deletar peça
        System.out.println("5. Gerar relatorio mensal");
        System.out.println("0. Sair");
        System.out.print("Escolha uma opção: ");
    }
}
