package dao;

import factory.ConnectionFactory;
import java.sql.*;
import modelo.Cliente;

public class ClienteDao {
    // Tornando a connection final, pois não há motivo para mudar sua referência após a inicialização
    private final Connection connection;

    // Construtor para inicializar a conexão com o banco
    public ClienteDao() {
        this.connection = new ConnectionFactory().getConnection();
    }

    // Método para adicionar cliente ao banco de dados
    public void adiciona(Cliente cliente) {
        String sql = "INSERT INTO cliente(nome,cpf,email,telefone,endereço,data_nascimento) VALUES(?)";
 

        // Usando try-with-resources para garantir o fechamento automático tanto do PreparedStatement quanto da Connection
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cliente.getNome(), cliente.getCpf());
            stmt.execute();
        } catch (SQLException e) {
            // Captura de erro mais detalhada
            System.out.println("Erro ao adicionar cliente: " + e.getMessage());
            throw new RuntimeException("Erro ao adicionar cliente", e);
        } finally {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close(); // Fechar a conexão manualmente após uso
                }
            } catch (SQLException e) {
                // Erro ao fechar a conexão
                System.out.println("Erro ao fechar a conexão: " + e.getMessage());
            }
        }
    }
}
