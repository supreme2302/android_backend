package notes.services;


import notes.DAO.UserDao;
import notes.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    public void createUser(User user) {
        user.saltHash();
        userDao.createUser(user);
    }

    public User getUserByNickname(String nickname) {
        try {
            return userDao.getUserByNickname(nickname);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }

    }

    public User check(String email, String password) {
        final User user = userDao.check(email, password);
        if (user.checkPassword(password)) {
            return user;
        } else {
            return null;
        }
    }
}
