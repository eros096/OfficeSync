package Database;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import models.Supply;
import models.SupplyRequest;
import models.User;

public final class OfficeSyncDatabase {
    // Change these values if your local MySQL port, username, or password is different.
    private static final String URL = "jdbc:mysql://localhost:3306/officesync?useSSL=false&serverTimezone=Asia/Manila";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private OfficeSyncDatabase() {
    }

    private static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("MySQL Connector/J is missing. Build with Maven so the dependency is included.", ex);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static User authenticate(String email, String password) throws SQLException {
        String sql = """
                SELECT u.user_id, u.full_name, u.email, u.role, u.department_id, d.department_name
                FROM users u
                INNER JOIN departments d ON d.department_id = u.department_id
                WHERE u.email = ? AND u.password_hash = ?
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setString(2, sha256(password));

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                return new User(
                        result.getInt("user_id"),
                        result.getString("full_name"),
                        result.getString("email"),
                        User.roleFromDisplayName(result.getString("role")),
                        result.getInt("department_id"),
                        result.getString("department_name")
                );
            }
        }
    }

    public static List<Supply> findAllSupplies() throws SQLException {
        String sql = """
                SELECT supply_id, supply_name, category, quantity_in_stock, reorder_level, is_available
                FROM supplies
                ORDER BY supply_name
                """;
        List<Supply> supplies = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                supplies.add(mapSupply(result));
            }
        }
        return supplies;
    }

    public static List<Supply> findLowStockSupplies() throws SQLException {
        String sql = """
                SELECT supply_id, supply_name, category, quantity_in_stock, reorder_level, is_available
                FROM supplies
                WHERE is_available = TRUE AND quantity_in_stock <= reorder_level
                ORDER BY supply_name
                """;
        List<Supply> supplies = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                supplies.add(mapSupply(result));
            }
        }
        return supplies;
    }

    public static void addSupply(String name, String category, int stock, int reorderLevel) throws SQLException {
        String sql = """
                INSERT INTO supplies (supply_name, category, quantity_in_stock, reorder_level, is_available)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, category);
            statement.setInt(3, stock);
            statement.setInt(4, reorderLevel);
            statement.setBoolean(5, stock > 0);
            statement.executeUpdate();
        }
    }

    public static void updateSupply(int id, String name, String category, int stock, int reorderLevel) throws SQLException {
        String sql = """
                UPDATE supplies
                SET supply_name = ?, category = ?, quantity_in_stock = ?, reorder_level = ?, is_available = ?
                WHERE supply_id = ?
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, category);
            statement.setInt(3, stock);
            statement.setInt(4, reorderLevel);
            statement.setBoolean(5, stock > 0);
            statement.setInt(6, id);
            statement.executeUpdate();
        }
    }

    public static void deleteSupply(int id) throws SQLException {
        String sql = "DELETE FROM supplies WHERE supply_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public static int countSupplies() throws SQLException {
        return count("SELECT COUNT(*) FROM supplies");
    }

    public static int countLowStockSupplies() throws SQLException {
        return count("SELECT COUNT(*) FROM supplies WHERE is_available = TRUE AND quantity_in_stock <= reorder_level");
    }

    public static List<SupplyRequest> findVisibleRequests(User user) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT r.request_id, u.full_name, u.department_id, d.department_name,
                       s.supply_name, rd.quantity_requested, r.request_date, r.status
                FROM requests r
                INNER JOIN users u ON u.user_id = r.user_id
                INNER JOIN departments d ON d.department_id = u.department_id
                INNER JOIN request_details rd ON rd.request_id = r.request_id
                INNER JOIN supplies s ON s.supply_id = rd.supply_id
                """);

        if (user.getRole() == User.Role.EMPLOYEE) {
            sql.append(" WHERE u.user_id = ?");
        } else if (user.getRole() == User.Role.DEPARTMENT_HEAD) {
            sql.append(" WHERE u.department_id = ?");
        }
        sql.append(" ORDER BY r.request_date DESC, r.request_id DESC");

        List<SupplyRequest> requests = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            if (user.getRole() == User.Role.EMPLOYEE) {
                statement.setInt(1, user.getId());
            } else if (user.getRole() == User.Role.DEPARTMENT_HEAD) {
                statement.setInt(1, user.getDepartmentId());
            }

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    requests.add(mapRequest(result));
                }
            }
        }
        return requests;
    }

    public static void submitRequest(int userId, int supplyId, int quantity) throws SQLException {
        String requestSql = "INSERT INTO requests (user_id, request_date, status) VALUES (?, ?, 'Pending')";
        String detailSql = "INSERT INTO request_details (request_id, supply_id, quantity_requested) VALUES (?, ?, ?)";

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement requestStatement = connection.prepareStatement(requestSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement detailStatement = connection.prepareStatement(detailSql)) {
                requestStatement.setInt(1, userId);
                requestStatement.setDate(2, Date.valueOf(LocalDate.now()));
                requestStatement.executeUpdate();

                int requestId;
                try (ResultSet keys = requestStatement.getGeneratedKeys()) {
                    keys.next();
                    requestId = keys.getInt(1);
                }

                detailStatement.setInt(1, requestId);
                detailStatement.setInt(2, supplyId);
                detailStatement.setInt(3, quantity);
                detailStatement.executeUpdate();
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public static void updateRequestStatus(int requestId, String status) throws SQLException {
        String sql = "UPDATE requests SET status = ? WHERE request_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setInt(2, requestId);
            statement.executeUpdate();
        }
    }

    public static void deleteRequest(int requestId) throws SQLException {
        String detailSql = "DELETE FROM request_details WHERE request_id = ?";
        String requestSql = "DELETE FROM requests WHERE request_id = ?";

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement detailStatement = connection.prepareStatement(detailSql);
                 PreparedStatement requestStatement = connection.prepareStatement(requestSql)) {
                detailStatement.setInt(1, requestId);
                detailStatement.executeUpdate();

                requestStatement.setInt(1, requestId);
                requestStatement.executeUpdate();
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public static int countPendingRequestsFor(User user) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM requests r
                INNER JOIN users u ON u.user_id = r.user_id
                WHERE r.status = 'Pending'
                """);
        if (user.getRole() == User.Role.EMPLOYEE) {
            sql.append(" AND u.user_id = ?");
        } else if (user.getRole() == User.Role.DEPARTMENT_HEAD) {
            sql.append(" AND u.department_id = ?");
        }

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            if (user.getRole() == User.Role.EMPLOYEE) {
                statement.setInt(1, user.getId());
            } else if (user.getRole() == User.Role.DEPARTMENT_HEAD) {
                statement.setInt(1, user.getDepartmentId());
            }

            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1);
            }
        }
    }

    private static int count(String sql) throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            result.next();
            return result.getInt(1);
        }
    }

    private static Supply mapSupply(ResultSet result) throws SQLException {
        return new Supply(
                result.getInt("supply_id"),
                result.getString("supply_name"),
                result.getString("category"),
                result.getInt("quantity_in_stock"),
                result.getInt("reorder_level"),
                result.getBoolean("is_available")
        );
    }

    private static SupplyRequest mapRequest(ResultSet result) throws SQLException {
        return new SupplyRequest(
                result.getInt("request_id"),
                result.getString("full_name"),
                result.getInt("department_id"),
                result.getString("department_name"),
                result.getString("supply_name"),
                result.getInt("quantity_requested"),
                result.getDate("request_date").toLocalDate(),
                result.getString("status")
        );
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte item : hash) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available.", ex);
        }
    }
}
