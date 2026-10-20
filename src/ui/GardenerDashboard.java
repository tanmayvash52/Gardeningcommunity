
package ui;

import util.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GardenerDashboard extends JFrame {

    private final int userId;

    public GardenerDashboard(int userId) {

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "A valid logged-in user ID is required."
            );
        }

        this.userId = userId;

        setTitle("Online Gardening Community - Gardener Dashboard");
        setSize(650, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );

        JLabel titleLabel = new JLabel(
                "Online Gardening Community - Gardener Dashboard",
                SwingConstants.CENTER
        );
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel welcomeLabel = new JLabel(
                "Welcome Gardener",
                SwingConstants.CENTER
        );
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.add(titleLabel);
        topPanel.add(welcomeLabel);

        JButton submitButton = new JButton("Submit Gardening Content");
        JButton viewButton = new JButton("View My Content");
        JButton projectButton = new JButton("My Gardening Projects");
        JButton discussionsButton =
                new JButton("Discussions and Comments");
        JButton logoutButton = new JButton("Logout");

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        buttonPanel.add(submitButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(projectButton);
        buttonPanel.add(discussionsButton);
        buttonPanel.add(logoutButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        add(mainPanel);

        submitButton.addActionListener(e -> openSubmitContent());
        viewButton.addActionListener(e -> openMyContent());
        projectButton.addActionListener(e -> openGardeningProjects());
        discussionsButton.addActionListener(e -> openDiscussions());

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

    private void openSubmitContent() {

        JTextField titleField = new JTextField();
        JTextArea descriptionArea = new JTextArea(8, 30);

        JComboBox<String> typeBox = new JComboBox<>(
                new String[]{"TIP", "DISCUSSION", "PROJECT"}
        );

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(descriptionArea));
        panel.add(new JLabel("Content Type:"));
        panel.add(typeBox);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Submit Gardening Content",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String contentType = (String) typeBox.getSelectedItem();

        if (title.isEmpty() || description.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter title and description."
            );
            return;
        }

        String sql = "INSERT INTO content "
                + "(user_id, title, description, content_type, status) "
                + "VALUES (?, ?, ?, ?, 'PENDING')";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, title);
            ps.setString(3, description);
            ps.setString(4, contentType);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                    this,
                    "Content submitted successfully!\n\n"
                            + "Status: PENDING\n\n"
                            + "Admin will review your content."
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error submitting content:\n" + ex.getMessage()
            );
        }
    }

    private void openMyContent() {

        String[] columns = {
                "Content ID", "Title", "Description", "Type", "Status"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String sql = "SELECT content_id, title, description, "
                + "content_type, status FROM content "
                + "WHERE user_id = ? ORDER BY content_id DESC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("content_id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("content_type"),
                            rs.getString("status")
                    });
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading your content:\n" + ex.getMessage()
            );
            return;
        }

        JTable table = new JTable(model);
        JFrame contentFrame = new JFrame("My Gardening Content");
        contentFrame.setSize(900, 450);
        contentFrame.setLocationRelativeTo(this);
        contentFrame.add(new JScrollPane(table));
        contentFrame.setVisible(true);
    }

    private void openGardeningProjects() {

        String[] columns = {
                "Project ID", "Project Name", "Description", "Status"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        loadGardeningProjects(model);

        JTable table = new JTable(model);
        JButton addButton = new JButton("Add New Project");

        addButton.addActionListener(e -> addNewGardeningProject(model));

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(addButton);

        JFrame projectFrame = new JFrame("My Gardening Projects");
        projectFrame.setSize(850, 500);
        projectFrame.setLocationRelativeTo(this);
        projectFrame.setLayout(new BorderLayout());
        projectFrame.add(new JScrollPane(table), BorderLayout.CENTER);
        projectFrame.add(bottomPanel, BorderLayout.SOUTH);
        projectFrame.setVisible(true);
    }

    private void loadGardeningProjects(DefaultTableModel model) {

        model.setRowCount(0);

        String sql = "SELECT project_id, project_name, description, status "
                + "FROM gardening_projects WHERE user_id = ? "
                + "ORDER BY project_id DESC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("project_id"),
                            rs.getString("project_name"),
                            rs.getString("description"),
                            rs.getString("status")
                    });
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading gardening projects:\n" + ex.getMessage()
            );
        }
    }

    private void addNewGardeningProject(DefaultTableModel model) {

        JTextField nameField = new JTextField();
        JTextArea descriptionArea = new JTextArea(6, 30);

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Project Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(descriptionArea));

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Add New Gardening Project",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String projectName = nameField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (projectName.isEmpty() || description.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter project name and description."
            );
            return;
        }

        String sql = "INSERT INTO gardening_projects "
                + "(user_id, project_name, description, status) "
                + "VALUES (?, ?, ?, 'ACTIVE')";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, projectName);
            ps.setString(3, description);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                    this,
                    "Gardening project added successfully!"
            );

            loadGardeningProjects(model);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error adding project:\n" + ex.getMessage()
            );
        }
    }

    private void openDiscussions() {

        String[] columns = {
                "Discussion ID", "User ID", "Title", "Description", "Status"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String sql = "SELECT discussion_id, user_id, title, description, "
                + "status FROM discussions WHERE status = 'APPROVED' "
                + "ORDER BY discussion_id DESC";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("discussion_id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("status")
                });
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading discussions:\n" + ex.getMessage()
            );
            return;
        }

        JTable table = new JTable(model);

        JButton createButton = new JButton("Create Discussion");
        JButton commentsButton = new JButton("View Comments");
        JButton addCommentButton = new JButton("Add Comment");

        createButton.addActionListener(e -> createDiscussion());

        commentsButton.addActionListener(e -> {
            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(
                        this, "Please select a discussion first."
                );
                return;
            }

            int discussionId = (int) model.getValueAt(row, 0);
            viewComments(discussionId);
        });

        addCommentButton.addActionListener(e -> {
            int row = table.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(
                        this, "Please select a discussion first."
                );
                return;
            }

            int discussionId = (int) model.getValueAt(row, 0);
            addComment(discussionId);
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(createButton);
        bottomPanel.add(commentsButton);
        bottomPanel.add(addCommentButton);

        JFrame frame = new JFrame("Gardening Discussions");
        frame.setSize(950, 450);
        frame.setLocationRelativeTo(this);
        frame.setLayout(new BorderLayout(5, 5));
        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void createDiscussion() {

        JTextField titleField = new JTextField();
        JTextArea descriptionArea = new JTextArea(6, 30);

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("Discussion Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(descriptionArea));

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Create Discussion",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (title.isEmpty() || description.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Please enter both title and description."
            );
            return;
        }

        String sql = "INSERT INTO discussions "
                + "(user_id, title, description, status) "
                + "VALUES (?, ?, ?, 'PENDING')";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, title);
            ps.setString(3, description);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                    this,
                    "Discussion submitted successfully!\n"
                            + "Status: PENDING\n"
                            + "Admin approval is required before it appears publicly."
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error creating discussion:\n" + ex.getMessage()
            );
        }
    }

    private void viewComments(int discussionId) {

        String[] columns = {
                "Comment ID", "User ID", "Comment", "Created At"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String sql = "SELECT comment_id, user_id, comment, created_at "
                + "FROM comments WHERE discussion_id = ? "
                + "ORDER BY created_at";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, discussionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("comment_id"),
                            rs.getInt("user_id"),
                            rs.getString("comment"),
                            rs.getTimestamp("created_at")
                    });
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading comments:\n" + ex.getMessage()
            );
            return;
        }

        JFrame frame = new JFrame("Discussion Comments");
        frame.setSize(750, 350);
        frame.setLocationRelativeTo(this);
        frame.add(new JScrollPane(new JTable(model)));
        frame.setVisible(true);
    }

    private void addComment(int discussionId) {

        JTextArea commentArea = new JTextArea(6, 30);

        int result = JOptionPane.showConfirmDialog(
                this,
                new JScrollPane(commentArea),
                "Write Your Comment",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String comment = commentArea.getText().trim();

        if (comment.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Please enter a comment."
            );
            return;
        }

        String sql = "INSERT INTO comments "
                + "(discussion_id, user_id, comment) VALUES (?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, discussionId);
            ps.setInt(2, userId);
            ps.setString(3, comment);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                    this, "Comment added successfully!"
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error adding comment:\n" + ex.getMessage()
            );
        }
    }
}