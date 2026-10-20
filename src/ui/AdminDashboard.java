
package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminDashboard extends JFrame {

    public AdminDashboard() {
        setTitle("Online Gardening Community - Admin Dashboard");
        setSize(650, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(
                "Online Gardening Community - Admin Dashboard",
                SwingConstants.CENTER
        );
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel welcomeLabel = new JLabel(
                "Welcome Admin",
                SwingConstants.CENTER
        );
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.add(titleLabel);
        topPanel.add(welcomeLabel);

        JButton userManagementButton = new JButton("User Management");
        JButton contentModerationButton = new JButton("Content Moderation");
        JButton discussionModerationButton = new JButton("Discussion Moderation");
        JButton systemSettingsButton = new JButton("System Settings");
        JButton logoutButton = new JButton("Logout");

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        buttonPanel.add(userManagementButton);
        buttonPanel.add(contentModerationButton);
        buttonPanel.add(discussionModerationButton);
        buttonPanel.add(systemSettingsButton);
        buttonPanel.add(logoutButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        add(mainPanel);

        userManagementButton.addActionListener(e -> openUserManagement());
        contentModerationButton.addActionListener(e -> openContentModeration());
        discussionModerationButton.addActionListener(e -> openDiscussionModeration());

        systemSettingsButton.addActionListener(e -> openSystemSettings());
                
        logoutButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Do you want to logout?",
                    "Logout",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdminDashboard dashboard = new AdminDashboard();
            dashboard.setVisible(true);
        });
    }

    
private void openUserManagement() {
    String[] columns = {"User ID", "Name", "Email", "Role"};

    DefaultTableModel model = new DefaultTableModel(columns, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    if (!loadUsers(model)) {
        return;
    }

    JTable table = new JTable(model);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    JButton addButton = new JButton("Add User");
    JButton editButton = new JButton("Edit User");
    JButton deleteButton = new JButton("Delete User");
    JButton refreshButton = new JButton("Refresh");

    JPanel buttonPanel = new JPanel();
    buttonPanel.add(addButton);
    buttonPanel.add(editButton);
    buttonPanel.add(deleteButton);
    buttonPanel.add(refreshButton);

    JFrame userFrame = new JFrame("User Management");
    userFrame.setSize(750, 450);
    userFrame.setLocationRelativeTo(this);
    userFrame.setLayout(new BorderLayout(5, 5));
    userFrame.add(new JScrollPane(table), BorderLayout.CENTER);
    userFrame.add(buttonPanel, BorderLayout.SOUTH);

    addButton.addActionListener(e -> {
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleBox =
                new JComboBox<>(new String[]{"GARDENER", "ADMIN"});

        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Role:"));
        panel.add(roleBox);

        int result = JOptionPane.showConfirmDialog(
                userFrame, panel, "Add User",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleBox.getSelectedItem();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    userFrame, "All fields are required.");
            return;
        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            JOptionPane.showMessageDialog(
                    userFrame, "Please enter a valid email address.");
            return;
        }

        boolean added = new dao.UserDAO().addUser(
                name, email, password, role);

        if (added) {
            JOptionPane.showMessageDialog(
                    userFrame, "User added successfully.");
            loadUsers(model);
        } else {
            JOptionPane.showMessageDialog(
                    userFrame,
                    "Could not add user. Check whether the email already exists "
                    + "or whether a database constraint prevented the operation.");
        }
    });

    editButton.addActionListener(e -> {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    userFrame, "Please select a user first.");
            return;
        }

        int row = table.convertRowIndexToModel(selectedRow);
        int userId = (int) model.getValueAt(row, 0);
        String oldName = String.valueOf(model.getValueAt(row, 1));
        String oldEmail = String.valueOf(model.getValueAt(row, 2));
        String oldRole = String.valueOf(model.getValueAt(row, 3));

        JTextField nameField = new JTextField(oldName);
        JTextField emailField = new JTextField(oldEmail);
        JComboBox<String> roleBox =
                new JComboBox<>(new String[]{"GARDENER", "ADMIN"});
        roleBox.setSelectedItem(oldRole);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Role:"));
        panel.add(roleBox);

        int result = JOptionPane.showConfirmDialog(
                userFrame, panel, "Edit User ID " + userId,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String role = (String) roleBox.getSelectedItem();

        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(
                    userFrame, "Name and email are required.");
            return;
        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            JOptionPane.showMessageDialog(
                    userFrame, "Please enter a valid email address.");
            return;
        }

        boolean updated = new dao.UserDAO().updateUser(
                userId, name, email, role);

        if (updated) {
            JOptionPane.showMessageDialog(
                    userFrame, "User updated successfully.");
            loadUsers(model);
        } else {
            JOptionPane.showMessageDialog(
                    userFrame,
                    "Could not update user. The email may already exist "
                    + "or a database constraint may have prevented the change.");
        }
    });

    deleteButton.addActionListener(e -> {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    userFrame, "Please select a user first.");
            return;
        }

        int row = table.convertRowIndexToModel(selectedRow);
        int userId = (int) model.getValueAt(row, 0);
        String name = String.valueOf(model.getValueAt(row, 1));
        String role = String.valueOf(model.getValueAt(row, 3));

        if ("ADMIN".equalsIgnoreCase(role)) {
            JOptionPane.showMessageDialog(
                    userFrame,
                    "Deleting an administrator is disabled for safety.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                userFrame,
                "Delete user '" + name + "' (ID " + userId + ")?\n"
                + "Related records or database constraints may prevent deletion.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean deleted = new dao.UserDAO().deleteUser(userId);

        if (deleted) {
            JOptionPane.showMessageDialog(
                    userFrame, "User deleted successfully.");
            loadUsers(model);
        } else {
            JOptionPane.showMessageDialog(
                    userFrame,
                    "Could not delete user. Related database records or "
                    + "foreign-key constraints may prevent deletion.");
        }
    });

    refreshButton.addActionListener(e -> loadUsers(model));

    userFrame.setVisible(true);
}

private boolean loadUsers(DefaultTableModel model) {
    model.setRowCount(0);

    String sql = "SELECT user_id, name, email, role FROM users ORDER BY user_id";

    try (Connection con = util.DatabaseConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            model.addRow(new Object[]{
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("role")
            });
        }

        return true;

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
                this, "Error loading users:\n" + ex.getMessage());
        return false;
    }
}


private void openSystemSettings() {
    String settingName = "site_name";
    String currentValue = "";
    int updatedBy = 1;

    try (Connection con = util.DatabaseConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(
                 "SELECT setting_value FROM system_settings " +
                 "WHERE setting_name = ?")) {

        ps.setString(1, settingName);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                currentValue = rs.getString("setting_value");
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "The site_name setting was not found.");
                return;
            }
        }

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
                this,
                "Error loading system settings:\n" + ex.getMessage());
        return;
    }

    JTextField siteNameField = new JTextField(currentValue, 25);

    JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
    panel.add(new JLabel("Community / Site Name:"));
    panel.add(siteNameField);

    int result = JOptionPane.showConfirmDialog(
            this,
            panel,
            "System Settings",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);

    if (result != JOptionPane.OK_OPTION) {
        return;
    }

    String newValue = siteNameField.getText().trim();

    if (newValue.isEmpty()) {
        JOptionPane.showMessageDialog(
                this,
                "The site name cannot be empty.");
        return;
    }

    if (newValue.length() > 255) {
        JOptionPane.showMessageDialog(
                this,
                "The site name must be 255 characters or fewer.");
        return;
    }

    try (Connection con = util.DatabaseConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(
                 "UPDATE system_settings " +
                 "SET setting_value = ?, updated_by = ? " +
                 "WHERE setting_name = ?")) {

        ps.setString(1, newValue);
        ps.setInt(2, updatedBy);
        ps.setString(3, settingName);

        int updated = ps.executeUpdate();

        if (updated > 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "System settings updated successfully.");
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "No settings were updated. Check the setting_name.");
        }

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
                this,
                "Error saving system settings:\n" + ex.getMessage());
    }
}

    private void openContentModeration() {
        String[] columns = {
                "Content ID", "User ID", "Title", "Description",
                "Type", "Status", "Created At"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0);

        try (Connection con = util.DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT content_id, user_id, title, description, " +
                     "content_type, status, created_at FROM content");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("content_id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("content_type"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                });
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this, "Error loading content:\n" + ex.getMessage());
            return;
        }

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JButton approveButton = new JButton("Approve");
        JButton rejectButton = new JButton("Reject");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);

        approveButton.addActionListener(e ->
                changeSelectedContentStatus(table, model, "APPROVED"));

        rejectButton.addActionListener(e ->
                changeSelectedContentStatus(table, model, "REJECTED"));

        JFrame contentFrame = new JFrame("Content Moderation");
        contentFrame.setSize(1000, 550);
        contentFrame.setLocationRelativeTo(this);
        contentFrame.setLayout(new BorderLayout());
        contentFrame.add(scrollPane, BorderLayout.CENTER);
        contentFrame.add(buttonPanel, BorderLayout.SOUTH);
        contentFrame.setVisible(true);
    }

    private void changeSelectedContentStatus(
            JTable table, DefaultTableModel model, String status) {

        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this, "Please select a content row first.");
            return;
        }

        int contentId = (int) table.getValueAt(
                table.convertRowIndexToModel(selectedRow), 0);

        try (Connection con = util.DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE content SET status = ? WHERE content_id = ?")) {

            ps.setString(1, status);
            ps.setInt(2, contentId);

            int updated = ps.executeUpdate();

            if (updated > 0) {
                JOptionPane.showMessageDialog(
                        this, "Content " + status.toLowerCase() +
                        " successfully.");
                refreshContentTable(model);
            } else {
                JOptionPane.showMessageDialog(this, "Content was not found.");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this, "Error updating content:\n" + ex.getMessage());
        }
    }

    private void refreshContentTable(DefaultTableModel model) {
        model.setRowCount(0);

        try (Connection con = util.DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT content_id, user_id, title, description, " +
                     "content_type, status, created_at FROM content");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("content_id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("content_type"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                });
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this, "Error refreshing content:\n" + ex.getMessage());
        }
    }

    private void openDiscussionModeration() {
        String[] columns = {
                "Discussion ID", "User ID", "Title",
                "Description", "Status", "Created At"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        if (!loadPendingDiscussions(model)) {
            return;
        }

        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(3).setPreferredWidth(300);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);

        JButton approveButton = new JButton("Approve Discussion");
        JButton rejectButton = new JButton("Reject Discussion");
        JButton refreshButton = new JButton("Refresh");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        buttonPanel.add(refreshButton);

        JFrame discussionFrame = new JFrame("Discussion Moderation");
        discussionFrame.setSize(1000, 500);
        discussionFrame.setLocationRelativeTo(this);
        discussionFrame.setLayout(new BorderLayout());

        discussionFrame.add(
                new JScrollPane(table), BorderLayout.CENTER);
        discussionFrame.add(buttonPanel, BorderLayout.SOUTH);

        approveButton.addActionListener(e ->
                changeSelectedDiscussionStatus(
                        table, model, "APPROVED"));

        rejectButton.addActionListener(e ->
                changeSelectedDiscussionStatus(
                        table, model, "REJECTED"));

        refreshButton.addActionListener(e ->
                loadPendingDiscussions(model));

        discussionFrame.setVisible(true);
    }

    private boolean loadPendingDiscussions(DefaultTableModel model) {
        model.setRowCount(0);

        try (Connection con = util.DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT discussion_id, user_id, title, description, " +
                     "status, created_at FROM discussions " +
                     "WHERE status = 'PENDING' ORDER BY created_at DESC");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("discussion_id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                });
            }

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(
                        this, "There are no pending discussions.");
            }

            return true;

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading discussions:\n" + ex.getMessage());
            return false;
        }
    }

    private void changeSelectedDiscussionStatus(
            JTable table,
            DefaultTableModel model,
            String status) {

        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this, "Please select a discussion first.");
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int discussionId = (int) model.getValueAt(modelRow, 0);

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to " + status.toLowerCase() +
                " this discussion?",
                "Confirm Discussion Moderation",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String sql =
                "UPDATE discussions SET status = ? " +
                "WHERE discussion_id = ? AND status = 'PENDING'";

        try (Connection con = util.DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, discussionId);

            int updated = ps.executeUpdate();

            if (updated > 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Discussion " + status.toLowerCase() +
                        " successfully."
                );
                loadPendingDiscussions(model);
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "This discussion is no longer pending. Refresh the list.");
                loadPendingDiscussions(model);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error updating discussion:\n" + ex.getMessage());
        }
    }
}