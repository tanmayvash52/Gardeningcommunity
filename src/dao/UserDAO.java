
package dao;

import model.User;
import util.DatabaseConnection;
import util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User login(String email, String password) {

        String sql = "SELECT user_id, name, email, password, role "
                   + "FROM users WHERE email = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                String storedPassword = rs.getString("password");
                boolean authenticated;

                if (PasswordUtil.isHashed(storedPassword)) {
                    authenticated = PasswordUtil.verifyPassword(
                            password, storedPassword);
                } else {
                    authenticated = storedPassword != null
                            && MessageDigestHelper.equals(
                                    password, storedPassword);
                }

                if (!authenticated) {
                    return null;
                }

                if (!PasswordUtil.isHashed(storedPassword)) {
                    String newHash = PasswordUtil.hashPassword(password);

                    String updateSql =
                            "UPDATE users SET password = ? "
                          + "WHERE user_id = ? AND password = ?";

                    try (PreparedStatement update =
                                 con.prepareStatement(updateSql)) {

                        update.setString(1, newHash);
                        update.setInt(2, rs.getInt("user_id"));
                        update.setString(3, storedPassword);
                        update.executeUpdate();
                    }
                }

                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));

                return user;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        String sql = "SELECT user_id, name, email, role FROM users";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                users.add(user);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    public boolean addUser(
            String name, String email, String password, String role) {

        String sql = "INSERT INTO users "
                   + "(name, email, password, role) VALUES (?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, PasswordUtil.hashPassword(password));
            ps.setString(4, role);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(
            int userId, String name, String email, String role) {

        String sql = "UPDATE users SET name = ?, email = ?, role = ? "
                   + "WHERE user_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, role);
            ps.setInt(4, userId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(int userId) {

        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static class MessageDigestHelper {

        private static boolean equals(String first, String second) {
            return java.security.MessageDigest.isEqual(
                    first.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    second.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}