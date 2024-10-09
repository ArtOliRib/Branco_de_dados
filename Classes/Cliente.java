package Classes;

import java.time.LocalDate;

public class Cliente {
    private long idCliente; // ID do cliente
    private String nome; // Nome do cliente
    private String sobrenome; // Sobrenome do cliente
    private String cpf; // CPF do cliente
    private String senha; // Senha do cliente
    private String email;
    private LocalDate dataNascimento; // Data de nascimento do cliente
    private String torcida; // Time do cliente
    private boolean assisteOp; // Se o cliente assiste One Piece
    private String cidade; // Cidade do cliente


    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSobrenome() {
        return sobrenome;
    }

    public void setSobrenome(String sobrenome) {
        this.sobrenome = sobrenome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getTorcida() {
        return torcida;
    }

    public void setTorcida(String torcida) {
        this.torcida = torcida;
    }

    public boolean isAssisteOp() {
        return assisteOp;
    }

    public void setAssisteOp(boolean assisteOp) {
        this.assisteOp = assisteOp;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    // Método para exibir informações do cliente
    @Override
    public String toString() {
        return "Cliente:\n" +
                "\n Nome = '" + nome + '\'' +
                ",\n Sobrenome = '" + sobrenome + '\'' +
                ",\n CPF = '" + cpf + '\'' +
                ",\n Senha = '" + senha + '\'' +
                ",\n Email = '" + email + '\'' +
                ",\n Data de Nascimento = " + dataNascimento +
                ",\n Torcida = '" + torcida + '\'' +
                ",\n Assiste Op = " + assisteOp +
                ",\n Cidade = '" + cidade + '\'';
    }
}
