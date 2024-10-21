-- Criar o banco de dados
CREATE DATABASE IF NOT EXISTS Roda_Quebrada;

-- Selecionar o banco de dados que foi criado
USE Roda_Quebrada;

-- Tabelas
CREATE TABLE Produto (
    id_produto BIGINT AUTO_INCREMENT PRIMARY KEY,
    produto_nome VARCHAR(50) NOT NULL,
    produto_descricao VARCHAR(200),
    produto_preco DECIMAL(8,2) NOT NULL,
    produto_categoria VARCHAR(50),
    produto_fabricante VARCHAR(50),
    quantidade_estoque INT NOT NULL DEFAULT 0
);

CREATE TABLE Cliente (
    id_cliente BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    sobrenome VARCHAR(50) NOT NULL,
    cpf VARCHAR(11) UNIQUE NOT NULL,
    email VARCHAR(50) UNIQUE NOT NULL,
    senha VARCHAR(50) NOT NULL,
    data_nascimento DATE NOT NULL,
    torcida VARCHAR(50),
    assiste_op BOOLEAN NOT NULL,
    cidade VARCHAR(50)
);

CREATE TABLE Funcionario (
    id_funcionario BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    numero_cadastro VARCHAR(10) UNIQUE NOT NULL,
    senha VARCHAR(50) NOT NULL,
    sobrenome VARCHAR(50) NOT NULL
);


CREATE TABLE FormaPagamento (
    id_pagamento BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    descricao VARCHAR(50) NOT NULL,
    status_id VARCHAR(50) NOT NULL
);

CREATE TABLE Pedido (
    id_pedido BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT,
    data_pedido TIMESTAMP NOT NULL,
    funcionario_id BIGINT NOT NULL,
    forma_pagamento_id BIGINT,
    finalizado BOOLEAN DEFAULT FALSE
);

CREATE TABLE ItemPedido (
    id_item BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INT NOT NULL DEFAULT 0,
    valor_unitario DECIMAL(8,2) NOT NULL
);


-- Adicionando as chaves estrangeiras
ALTER TABLE Pedido
ADD CONSTRAINT fk_cliente
FOREIGN KEY (cliente_id) REFERENCES Cliente(id_cliente) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE Pedido
ADD CONSTRAINT fk_vendedor
FOREIGN KEY (funcionario_id) REFERENCES Funcionario(id_funcionario) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE Pedido
ADD CONSTRAINT fk_formaPagamento
FOREIGN KEY (forma_pagamento_id) REFERENCES FormaPagamento(id_pagamento) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ItemPedido
ADD CONSTRAINT fk_itemPedido
FOREIGN KEY (pedido_id) REFERENCES Pedido(id_pedido) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ItemPedido
ADD CONSTRAINT fk_itemProduto
FOREIGN KEY (produto_id) REFERENCES Produto(id_produto) ON UPDATE CASCADE ON DELETE CASCADE;


-- Criar usuário cliente
CREATE USER IF NOT EXISTS 'cliente'@'localhost' IDENTIFIED BY 'senha_cliente';

-- Permissões para o usuário cliente (sem acesso as tabelas de pagamento)
GRANT SELECT ON Cliente TO 'cliente'@'localhost';
GRANT SELECT ON Produto TO 'cliente'@'localhost';
GRANT SELECT ON Pedido TO 'cliente'@'localhost';

GRANT UPDATE ON Pedido TO 'cliente'@'localhost';

GRANT INSERT ON Pedido TO 'cliente'@'localhost';
GRANT INSERT ON ItemPedido TO 'cliente'@'localhost';

-- Criar usuário usuario_logado (com acesso as tabelas de pagamento)
CREATE USER IF NOT EXISTS 'usuario_logado'@'localhost' IDENTIFIED BY 'senha_usuario_logado';

-- Permissões para o usuário usuario_logado 
GRANT SELECT ON Cliente TO 'usuario_logado'@'localhost';
GRANT SELECT ON Produto TO 'usuario_logado'@'localhost';
GRANT SELECT ON Pedido TO 'usuario_logado'@'localhost';
GRANT SELECT ON FormaPagamento TO 'usuario_logado'@'localhost';

GRANT INSERT ON Pedido TO 'usuario_logado'@'localhost';
GRANT INSERT ON ItemPedido TO 'usuario_logado'@'localhost';

GRANT UPDATE ON Cliente TO 'usuario_logado'@'localhost';



-- Criar usuário funcionario
CREATE USER IF NOT EXISTS 'funcionario'@'localhost' IDENTIFIED BY 'senha_funcionario';

-- Permissões para o usuário funcionario
GRANT SELECT ON Cliente TO 'funcionario'@'localhost';
GRANT SELECT ON Produto TO 'funcionario'@'localhost';
GRANT SELECT ON Pedido TO 'funcionario'@'localhost';
GRANT SELECT ON Funcionario TO 'funcionario'@'localhost';
GRANT SELECT ON FormaPagamento TO 'funcionario'@'localhost';

GRANT INSERT ON Produto TO 'funcionario'@'localhost';
GRANT INSERT ON FormaPagamento TO 'funcionario'@'localhost';
GRANT INSERT ON Cliente TO 'funcionario'@'localhost';

GRANT UPDATE ON Produto TO 'funcionario'@'localhost';
GRANT UPDATE ON Funcionario TO 'funcionario'@'localhost';
GRANT UPDATE ON FormaPagamento TO 'funcionario'@'localhost';


-- View de detalhes
CREATE VIEW ItensCarrinho AS
SELECT 
    p.id_pedido AS id_pedido,
    IFNULL(c.nome, 'Cliente não logado') AS cliente_nome,
    IFNULL(c.sobrenome, '') AS cliente_sobrenome,
    ip.produto_id AS id_produto,
    prod.produto_nome AS produto_nome,
    prod.produto_fabricante AS marca,
    ip.quantidade AS quantidade,
    ip.valor_unitario AS valor_unitario,
    (ip.quantidade * ip.valor_unitario) AS total_item,
    p.finalizado AS status_pedido
FROM 
    Pedido p
LEFT JOIN 
    Cliente c ON p.cliente_id = c.id_cliente
JOIN 
    ItemPedido ip ON p.id_pedido = ip.pedido_id
JOIN 
    Produto prod ON ip.produto_id = prod.id_produto
WHERE 
    p.finalizado = FALSE;
    

DELIMITER //

CREATE PROCEDURE RelatorioVendasSimplificado()
BEGIN
    -- Total de Vendas
    SELECT 
        SUM(ip.quantidade * ip.valor_unitario) AS total_vendas,
        SUM(ip.quantidade) AS total_pecas_vendidas,
        COUNT(DISTINCT p.id_pedido) AS total_pedidos
    FROM 
        Pedido p
    JOIN 
        ItemPedido ip ON p.id_pedido = ip.pedido_id;

    -- Total de Vendas por Funcionário
    SELECT 
        f.id_funcionario,
        CONCAT(f.nome, ' ', f.sobrenome) AS funcionario_nome,
        SUM(ip.quantidade * ip.valor_unitario) AS total_vendas_funcionario
    FROM 
        Funcionario f
    JOIN 
        Pedido p ON f.id_funcionario = p.funcionario_id
    JOIN 
        ItemPedido ip ON p.id_pedido = ip.pedido_id
    GROUP BY 
        f.id_funcionario, f.nome, f.sobrenome;

    -- Total de Vendas por Categoria de Produto
    SELECT 
        pr.produto_categoria,
        SUM(ip.quantidade * ip.valor_unitario) AS total_vendas_categoria
    FROM 
        ItemPedido ip
    JOIN 
        Produto pr ON ip.produto_id = pr.id_produto
    JOIN 
        Pedido p ON ip.pedido_id = p.id_pedido
    GROUP BY 
        pr.produto_categoria;
END //

DELIMITER ;




 -- Mock itens and mock people.
INSERT INTO Funcionario (nome, numero_cadastro, senha, sobrenome) 
VALUES 
    ('João', '1234567890', 'senha123', 'Silva'),
    ('Maria', '0987654321', 'senha456', 'Oliveira'),
    ('Pedro', '1122334455', 'senha789', 'Santos'),
    ('Ana', '2233445566', 'senha321', 'Lima'),
    ('Lucas', '3344556677', 'senha654', 'Ferreira'),
    ('Mariana', '4455667788', 'senha987', 'Costa'),
    ('Ricardo', '5566778899', 'senha012', 'Pereira'),
    ('Juliana', '6677889900', 'senha345', 'Ribeiro');


INSERT INTO Cliente (nome, sobrenome, cpf, email, senha, data_nascimento, torcida, assiste_op, cidade) 
VALUES 
('Carlos', 'Santos', '12345678901', 'carlos.santos@example.com', 'senhaCarlos123', '1990-05-15', 'Flamengo', TRUE, 'Rio de Janeiro'),
('Ana', 'Souza', '23456789012', 'ana.souza@example.com', 'senhaAna123', '1985-10-25', 'Vasco', FALSE, 'São Paulo'),
('João', 'Oliveira', '34567890123', 'joao.oliveira@example.com', 'senhaJoao123', '1992-12-02', 'Botafogo', TRUE, 'Brasília'),
('Mariana', 'Lima', '45678901234', 'mariana.lima@example.com', 'senhaMariana123', '1995-07-18', 'Fluminense', TRUE, 'Belo Horizonte'),
('Pedro', 'Gomes', '56789012345', 'pedro.gomes@example.com', 'senhaPedro123', '1988-03-10', 'Corinthians', FALSE, 'Porto Alegre');


-- Agora, insira as formas de pagamento ativas
INSERT INTO FormaPagamento (nome, descricao, status_id) VALUES ('Cartão de Crédito','Pagamentos feitos pelo cartão bancario', 'ativo');
INSERT INTO FormaPagamento (nome, descricao, status_id) VALUES ('Boleto Bancário','Pagamentos feitos pelo cartão bancario' ,'ativo');
INSERT INTO FormaPagamento (nome, descricao, status_id) VALUES ('PIX','Pagamento atraves do sistema PIX' ,'ativo');
INSERT INTO FormaPagamento (nome, descricao, status_id) VALUES ('Beries','Pagamento atraves de Beries' ,'ativo');




INSERT INTO Produto (produto_nome, produto_descricao, produto_preco, produto_categoria, produto_fabricante, quantidade_estoque) VALUES
('Farois LED', 'Farois de alta luminosidade para carro', 199.99, 'Iluminação', 'Philips', 100),
('Pastilhas de Freio', 'Pastilhas de freio para veículos de passeio', 79.99, 'Freios', 'Bosch', 200),
('Óleo de Motor', 'Óleo sintético 5W30', 89.99, 'Lubrificação', 'Mobil', 150),
('Filtro de Ar', 'Filtro de ar para motor', 49.99, 'Filtros', 'Fram', 80),
('Bateria Automotiva', 'Bateria de 60Ah', 399.99, 'Eletrônica', 'Heliar', 60),
('Amortecedor Traseiro', 'Amortecedor para suspensão traseira', 159.99, 'Suspensão', 'Monroe', 90),
('Pneu 205/55R16', 'Pneu para veículos de passeio', 499.99, 'Pneus', 'Michelin', 40),
('Correia Dentada', 'Correia dentada de motor', 109.99, 'Acessórios', 'Continental', 30),
('Radiador', 'Radiador de alumínio', 299.99, 'Refrigeração', 'Valeo', 50),
('Escapamento', 'Escapamento esportivo', 599.99, 'Exaustão', 'MagnaFlow', 20),
('Paralama Dianteiro', 'Paralama dianteiro esquerdo', 249.99, 'Carroceria', 'Dorman', 10),
('Tampa do Motor', 'Tampa do motor de alumínio', 169.99, 'Carroceria', 'Spectre', 5),
('Vela de Ignição', 'Vela de ignição para motor a gasolina', 19.99, 'Ignição', 'NGK', 8),
('Cabo de Aceleração', 'Cabo de aceleração para veículos', 29.99, 'Transmissão', 'ACDelco', 6),
('Bico Injetor', 'Bico injetor para motores', 49.99, 'Injeção', 'Delphi', 4),
('Capa de Banco', 'Capa de banco em couro', 79.99, 'Acessórios', 'Sparco', 2),
('Painel de Instrumentos', 'Painel com display digital', 299.99, 'Eletrônica', 'VDO', 3),
('Filtro de Óleo', 'Filtro de óleo para motor', 29.99, 'Filtros', 'Mann-Filter', 1),
('Sensor de Estacionamento', 'Sensor para estacionamento', 199.99, 'Eletrônica', 'Pioneer', 2),
('Lâmpada Halógena', 'Lâmpada halógena para faróis', 39.99, 'Iluminação', 'Osram', 3),
('Cintos de Segurança', 'Cintos de segurança para automóveis', 69.99, 'Segurança', 'Takata', 5),
('Suporte para Smartphone', 'Suporte de celular para carro', 29.99, 'Acessórios', 'Baseus', 15),
('Extintor de Incêndio', 'Extintor de incêndio automotivo', 89.99, 'Segurança', 'Kidde', 20),
('Ar Condicionado', 'Sistema de ar condicionado completo', 899.99, 'Climatização', 'Denso', 10),
('Trocador de Óleo', 'Trocador de óleo automático', 399.99, 'Lubrificação', 'Liqui Moly', 8),
('Câmbio Automático', 'Câmbio automático para carros', 1499.99, 'Transmissão', 'ZF', 2),
('Volante Esportivo', 'Volante esportivo em couro', 249.99, 'Acessórios', 'Momo', 10),
('Capô', 'Capô para carro modelo XYZ', 399.99, 'Carroceria', 'OEM', 5),
('Lanterna Traseira', 'Lanterna traseira LED', 99.99, 'Iluminação', 'Valeo', 20),
('Grade Dianteira', 'Grade dianteira para carro', 149.99, 'Carroceria', 'TYC', 15),
('Braço do Limpador', 'Braço do limpador de para-brisa', 49.99, 'Acessórios', 'Valeo', 30),
('Conector OBD-II', 'Conector para diagnóstico OBD-II', 59.99, 'Eletrônica', 'Autel', 40),
('Cabo de Bateria', 'Cabo de bateria para veículos', 19.99, 'Eletrônica', 'Bosch', 50),
('Cilindro de Freio', 'Cilindro de freio traseiro', 79.99, 'Freios', 'TRW', 15),
('Capa de Volante', 'Capa de volante em couro', 49.99, 'Acessórios', 'Sparco', 20),
('Difusor de Ar', 'Difusor de ar para climatização', 39.99, 'Climatização', 'Denso', 25),
('Rodas de Liga Leve', 'Rodas de liga leve 17"', 1599.99, 'Rodas', 'BBS', 10),
('Engate para Reboque', 'Engate para reboque universal', 299.99, 'Acessórios', 'Thule', 8),
('Fusíveis', 'Conjunto de fusíveis automotivos', 19.99, 'Eletrônica', 'Bussmann', 50),
('Bicos de Água', 'Bicos de água para lavador de parabrisa', 29.99, 'Acessórios', 'Dorman', 45),
('Porta-copos', 'Porta-copos para carro', 14.99, 'Acessórios', 'WeatherTech', 60),
('Protetor de Soleira', 'Protetor de soleira em inox', 39.99, 'Acessórios', '3M', 30),
('Suporte de Bicicleta', 'Suporte para bicicleta no carro', 199.99, 'Acessórios', 'Thule', 10);

