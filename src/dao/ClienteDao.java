package dao;

import factory.ConnectionFactory;
import java.sql.*;
import modelo.Cliente;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ClienteDao {

    // Tornando a connection final, pois não há motivo para mudar sua referência após a inicialização
    private final Connection connection;

    // Construtor para inicializar a conexão com o banco
    public ClienteDao() {
        this.connection = new ConnectionFactory().getConnection();

    }

    // Método para validar CPF
    private boolean validarCPF(String cpf) {
        // Remove caracteres não numéricos
        cpf = cpf.replaceAll("[^0-9]", "");

        // Verifica se tem 11 dígitos
        if (cpf.length() != 11) {
            return false;
        }

        // Verifica se todos os dígitos são iguais (inválido)
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        // Cálculo do primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int resto = soma % 11;
        int digito1 = (resto < 2) ? 0 : 11 - resto;

        // Cálculo do segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        resto = soma % 11;
        int digito2 = (resto < 2) ? 0 : 11 - resto;

        // Verifica se os dígitos calculados conferem com os informados
        return (Character.getNumericValue(cpf.charAt(9)) == digito1)
                && (Character.getNumericValue(cpf.charAt(10)) == digito2);
    }

    // Método para validar e-mail
    private boolean validarEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.compile(regex).matcher(email).matches();
    }

    // Método para validar telefone
    private boolean validarTelefone(String telefone) {
        // Remove caracteres não numéricos
        telefone = telefone.replaceAll("[^0-9]", "");
        // Verifica se tem entre 10 e 11 dígitos (incluindo DDD)
        return telefone.length() >= 10 && telefone.length() <= 11;
    }

    // Método para verificar se CPF já existe
    private boolean cpfExiste(String cpf, Integer idExclusao) throws SQLException {
        String sql = "SELECT COUNT(*) FROM cliente WHERE cpf = ?";
        if (idExclusao != null) {
            sql += " AND id != ?";
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            if (idExclusao != null) {
                stmt.setInt(2, idExclusao);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Método para adicionar cliente ao banco de dados
    public void adiciona(Cliente cliente) {
        // Validação dos campos obrigatórios
        if (cliente.getNome() == null || cliente.getNome().trim().isEmpty()) {
            throw new RuntimeException("O nome do cliente é obrigatório.");
        }

        if (cliente.getCpf() == null || cliente.getCpf().trim().isEmpty()) {
            throw new RuntimeException("O CPF do cliente é obrigatório.");
        }

        // Validação do CPF
        if (!validarCPF(cliente.getCpf())) {
            throw new RuntimeException("CPF inválido.");
        }

        // Validação do e-mail
        if (cliente.getEmail() != null && !cliente.getEmail().trim().isEmpty() && !validarEmail(cliente.getEmail())) {
            throw new RuntimeException("E-mail inválido.");
        }

        // Validação do telefone
        if (cliente.getTelefone() != null && !cliente.getTelefone().trim().isEmpty() && !validarTelefone(cliente.getTelefone())) {
            throw new RuntimeException("Telefone inválido. Formato esperado: (DDD) número com 8 ou 9 dígitos.");
        }

        // Verifica se CPF já existe
        try {
            if (cpfExiste(cliente.getCpf(), null)) {
                throw new RuntimeException("Já existe um cliente cadastrado com este CPF.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar CPF: " + e.getMessage());
        }

        String sql = "INSERT INTO cliente(nome,cpf,email,telefone,endereço,data_nascimento) VALUES(?,?,?,?,?,?)";

        // Usando try-with-resources para garantir o fechamento automático tanto do PreparedStatement quanto da Connection
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getCpf());
            stmt.setString(3, cliente.getEmail());
            stmt.setString(4, cliente.getTelefone());
            stmt.setString(5, cliente.getEndereço());
            stmt.setDate(6, java.sql.Date.valueOf(cliente.getData()));
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

    //metodo Buscar
    public List<Cliente> buscarPorCpf(String cpf) throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM cliente WHERE cpf = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clientes.add(mapearCliente(rs));
                }
            }
        }
        return clientes;
    }

    public List<Cliente> buscarPorNome(String nome) throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM clientes WHERE nome LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + nome + "%"); // Busca por nome parcial
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clientes.add(mapearCliente(rs));
                }
            }
        }
        return clientes;
    }

// Método auxiliar para mapear os resultados do ResultSet para o objeto Cliente
    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setCodigo(rs.getInt("id"));
        cliente.setNome(rs.getString("nome"));
        cliente.setCpf(rs.getString("cpf"));
        cliente.setEmail(rs.getString("email"));
        cliente.setTelefone(rs.getString("telefone"));
        cliente.setEndereço(rs.getString("endereço"));
        cliente.setData(rs.getDate("data_nascimento").toLocalDate());
        return cliente;
    }

    //metodo listar clientes
    public List<Cliente> listarTodos() {
        String sql = "SELECT id, nome, cpf, email, telefone, endereço, data_nascimento FROM cliente";
        List<Cliente> clientes = new ArrayList<>();

        try (Connection conn = new ConnectionFactory().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setCodigo(rs.getInt("id"));
                cliente.setNome(rs.getString("nome"));
                cliente.setCpf(rs.getString("cpf"));
                cliente.setEmail(rs.getString("email"));
                cliente.setTelefone(rs.getString("telefone"));
                cliente.setEndereço(rs.getString("endereço"));

                // Conversão correta para LocalDate
                java.sql.Date sqlDate = rs.getDate("data_nascimento");
                if (sqlDate != null) {
                    cliente.setData(sqlDate.toLocalDate());
                }

                clientes.add(cliente);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar clientes", e);
        }

        return clientes;
    }
    // metodo de atualização da tabela

    public void atualizar(Cliente cliente) {
        String sql = "UPDATE cliente SET nome=?, cpf=?, email=?, telefone=?, endereço=?, data_nascimento=? WHERE id=?";

        try (Connection conn = new ConnectionFactory().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getCpf());
            stmt.setString(3, cliente.getEmail());
            stmt.setString(4, cliente.getTelefone());
            stmt.setString(5, cliente.getEndereço());
            stmt.setDate(6, java.sql.Date.valueOf(cliente.getData()));
            stmt.setInt(7, cliente.getCodigo());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar cliente", e);
        }
    }

    //metodo excluir id
    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM cliente WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
