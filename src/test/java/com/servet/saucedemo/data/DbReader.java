package com.servet.saucedemo.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Test kullanıcılarını bir veritabanından okur. Burada H2 in-memory veritabanı kullanılıyor;
 * gerçek bir projede sadece JDBC URL'si değişir (ör. QA ortamının PostgreSQL'i).
 */
public final class DbReader {

    public record DbUser(String username, String password, String userType, boolean canLogin, String expectedError) {

        /** Raporda şifre görünmesin diye toString'i sadeleştiriyoruz. */
        @Override
        public String toString() {
            return username + " (" + userType + ")";
        }
    }

    private DbReader() {
    }

    public static List<DbUser> activeUsers() {
        String sql = """
                SELECT username, password, user_type, can_login, expected_error
                FROM users
                WHERE active = TRUE
                ORDER BY username
                """;
        // Her okuma kendi boş veritabanını kurar (UUID) → paralel çalışmada çakışma olmaz.
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID());
             Statement statement = connection.createStatement()) {
            statement.execute("RUNSCRIPT FROM 'classpath:testdata/users.sql' CHARSET 'UTF-8'");
            List<DbUser> users = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery(sql)) {
                while (rs.next()) {
                    users.add(new DbUser(rs.getString("username"), rs.getString("password"),
                            rs.getString("user_type"), rs.getBoolean("can_login"), rs.getString("expected_error")));
                }
            }
            return users;
        } catch (SQLException e) {
            throw new IllegalStateException("Test verisi veritabanından okunamadı", e);
        }
    }
}
