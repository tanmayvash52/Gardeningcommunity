package ui;

import dao.UserDAO;
import model.User;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;

    public LoginFrame() {

        setTitle("Online Gardening Community - Login");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("Gardening Community Login");
        JLabel emailLabel = new JLabel("Email:");
        JLabel passwordLabel = new JLabel("Password:");

        emailField = new JTextField();
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JButton clearButton = new JButton("Clear");

        panel.add(titleLabel);
        panel.add(new JLabel(""));

        panel.add(emailLabel);
        panel.add(emailField);

        panel.add(passwordLabel);
        panel.add(passwordField);

        panel.add(loginButton);
        panel.add(clearButton);

        add(panel);

        loginButton.addActionListener(e -> login());

        clearButton.addActionListener(e -> {
            emailField.setText("");
            passwordField.setText("");
        });
    }

    private void login() {

        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter email and password."
            );

            return;
        }

        UserDAO userDAO = new UserDAO();
        User user = userDAO.login(email, password);

        if (user != null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Login successful!\nWelcome " + user.getName()
            );

            if (user.getRole().equalsIgnoreCase("ADMIN")) {

                JOptionPane.showMessageDialog(
                        this,
                        "Opening Admin Dashboard"
                );

                AdminDashboard dashboard = new AdminDashboard();
                dashboard.setVisible(true);

                this.dispose();
              }  else if (user.getRole().equalsIgnoreCase("GARDENER")) {

     GardenerDashboard dashboard =
        new GardenerDashboard(user.getUserId());

    dashboard.setVisible(true);

    this.dispose();

}
             else {

                JOptionPane.showMessageDialog(
                        this,
                        "Unknown user role: " + user.getRole()
                );
            }

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Invalid email or password."
            );
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);

        });
    }
}