package Classes;

import Controler.ClienteDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;

public class CadastroLogin {

    private Connection conn;
    private ClienteDAO clienteDAO;

    public CadastroLogin(ClienteDAO cliente) {
        this.conn = conn;
        this.clienteDAO = cliente;
    }

    public void exibirMenuCadastro() {
        Scanner scanner = new Scanner(System.in);
        boolean continuar = true;

        while (continuar) {
            System.out.println("=== Menu de Cadastro ===");
            System.out.println("1. Cadastrar Cliente");
            System.out.println("2. Sair");
            System.out.print("Escolha uma opção: ");

            int opcao = scanner.nextInt();
            scanner.nextLine(); // Limpa o buffer

            switch (opcao) {
                case 1:
                    Cliente cliente = new Cliente();
                    System.out.print("Nome: ");
                    cliente.setNome(scanner.nextLine());

                    System.out.print("Sobrenome: ");
                    cliente.setSobrenome(scanner.nextLine());

                    System.out.print("Email: ");
                    cliente.setEmail(scanner.nextLine());

                    System.out.print("CPF (somente números): ");
                    cliente.setCpf(scanner.nextLine());

                    System.out.print("Senha: ");
                    cliente.setSenha(scanner.nextLine()); // Considere usar um método seguro para armazenar senhas

                    System.out.print("Data de Nascimento (YYYY-MM-DD): ");
                    cliente.setDataNascimento(LocalDate.parse(scanner.nextLine()));

                    System.out.print("Torcida: ");
                    cliente.setTorcida(scanner.nextLine());

                    System.out.print("Assiste OP (true/false): ");
                    cliente.setAssisteOp(scanner.nextBoolean());
                    scanner.nextLine(); // Limpa o buffer

                    System.out.print("Cidade: ");
                    cliente.setCidade(scanner.nextLine());

                    try {
                        boolean sucesso = clienteDAO.cadastrarCliente(cliente);
                        if (sucesso) {
                            System.out.println("Cliente cadastrado com sucesso!");
                        } else {
                            System.out.println("Erro ao cadastrar o cliente.");
                        }
                    } catch (SQLException e) {
                        System.out.println("Erro ao cadastrar o cliente: " + e.getMessage());
                    }
                    break;
                case 2:
                    continuar = false;
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }

        //scanner.close();
        System.out.println("Saindo do menu.");
    }

    public boolean[] exibirMenuLogin(boolean logadoCliente, boolean logadoFuncionario,long pedido, Cliente cliente) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        boolean continuar = true;


        while (continuar) {
            System.out.println("=== Menu de Login  ===");
            System.out.println("1. Fazer Login");
            System.out.println("2. Sair");
            System.out.print("Escolha uma opção: ");

            int opcao = scanner.nextInt();
            scanner.nextLine(); // Limpa o buffer

            switch (opcao) {
                case 1:
                    System.out.print("Digite o seu Email ou sua Credencial de funcionario: ");
                    String cpf = scanner.nextLine();
                    System.out.print("Digite a senha: ");
                    String senha = scanner.nextLine();
                    if (clienteDAO.login(cpf, senha,pedido,cliente) == 1) {
                        logadoCliente = true; // Marcar como logado se o login for bem-sucedido
                        System.out.println("Login cliente bem-sucedido!");
                        return new boolean[]{logadoCliente, logadoFuncionario};

                    } else if (clienteDAO.login(cpf, senha,pedido,cliente) == 2) {
                        logadoFuncionario = true; // Marcar como logado se o login for bem-sucedido
                        System.out.println("Login Funcionario bem-sucedido!");
                        return new boolean[]{logadoCliente, logadoFuncionario};

                    } else {
                        System.out.println("Login falhou. Verifique CPF e senha.");
                    }
                    break;
                case 2:
                    continuar = false;
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }

        System.out.println("Saindo do menu.");
        return  new boolean[]{logadoCliente, logadoFuncionario};
    }
}