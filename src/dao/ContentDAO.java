package dao;

import model.Content;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ContentDAO {

    public List<Content> getPendingContent() {

        List<Content> contents = new ArrayList<>();

        String sql = "SELECT content_id, user_id, title, description, " +
                     "content_type, status FROM content WHERE status = 'PENDING'";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Content content = new Content();

                content.setContentId(rs.getInt("content_id"));
                content.setUserId(rs.getInt("user_id"));
                content.setTitle(rs.getString("title"));
                content.setDescription(rs.getString("description"));
                content.setContentType(rs.getString("content_type"));
                content.setStatus(rs.getString("status"));

                contents.add(content);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return contents;
    }

    public boolean updateStatus(int contentId, String status) {

        String sql = "UPDATE content SET status = ? WHERE content_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, contentId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}