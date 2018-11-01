package notes.DAO;

import notes.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;


@Repository
public class UserDao {

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbc;

    private static final UserMapper userMapper = new UserMapper();

    @PostConstruct
    public void init() {
        System.out.println("JDBC is called. DataSource = " + dataSource);
        jdbc = new JdbcTemplate(dataSource);
    }

    public void createUser(User user) {
        final String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        jdbc.update(sql, user.getName(), user.getEmail(), user.getPassword());
    }

    public User getUserByNickname(String nickname) {
        final String sql = "SELECT * FROM users WHERE username::citext = ?::citext";
        return jdbc.queryForObject(sql, userMapper, nickname);
    }


    public User check(String email, String password) {
        final String sql = "SELECT * FROM users WHERE email = ?";
        return jdbc.queryForObject(sql, userMapper, email);

    }

    private static final class UserMapper implements RowMapper<User> {

        @Override
        public User mapRow(ResultSet resultSet, int i) throws SQLException {
            final User user = new User();
            user.setName(resultSet.getString("username"));
            user.setEmail(resultSet.getString("email"));
            user.setPassword(resultSet.getString("password"));
            return user;
        }
    }
}
