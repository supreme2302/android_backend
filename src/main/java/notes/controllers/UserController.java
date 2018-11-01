package notes.controllers;

import notes.models.Message;
import notes.models.User;
import notes.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;


@Controller
@RequestMapping("/users")
public class UserController {

    private enum UserStatus {
        SUCCESSFULLY_REGISTERED,
        SUCCESSFULLY_AUTHED,
        SUCCESSFULLY_LOGGED_OUT,
        ACCESS_ERROR,
        WRONG_CREDENTIALS,
        NOT_UNIQUE_USERNAME,
        ALREADY_AUTHENTICATED,
        NOT_FOUND
    }

    @Autowired
    private UserService userService;

    @RequestMapping(method = RequestMethod.PUT, path = "/create")
    public ResponseEntity create(HttpSession session, @RequestBody User user) {
        if (session.getAttribute("user") != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new Message(UserStatus.ALREADY_AUTHENTICATED, user.getName())
            );
        }

        if (user.getPassword() == null || user.getEmail() == null || user.getName() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new Message(UserStatus.WRONG_CREDENTIALS)
            );
        }
        try {
            userService.createUser(user);
        } catch (DuplicateKeyException except) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new Message(UserStatus.NOT_UNIQUE_USERNAME)
            );
        }

        sessionAuth(session, user);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new Message(UserStatus.SUCCESSFULLY_REGISTERED, user.getName())
        );
    }

    @RequestMapping(method = RequestMethod.POST, path = "/auth")
    public ResponseEntity auth(HttpSession session, @RequestBody User user) {
        if (session.getAttribute("user") != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new Message(UserStatus.ALREADY_AUTHENTICATED, user.getName())
            );
        }

        try {
            user = userService.check(user.getEmail(), user.getPassword());
        } catch (DataAccessException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new Message(UserStatus.WRONG_CREDENTIALS)
            );
        }

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new Message(UserStatus.WRONG_CREDENTIALS)
            );
        }
        sessionAuth(session, user);

        return ResponseEntity.ok(new Message(UserStatus.SUCCESSFULLY_AUTHED));
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/logout")
    public ResponseEntity logout(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new Message(UserStatus.ACCESS_ERROR)
            );
        }
        session.invalidate();
        return ResponseEntity.ok(new Message(UserStatus.SUCCESSFULLY_LOGGED_OUT));
    }


    @GetMapping(path = "/profile/{nickname}")
    public ResponseEntity getProfileUser(@PathVariable("nickname") String nickname) {
        User user = userService.getUserByNickname(nickname);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(UserStatus.NOT_FOUND));
        }
        return ResponseEntity.ok(user);
    }


    private static void sessionAuth(HttpSession session, User user) {
        session.setAttribute("user", user.getName());
        session.setMaxInactiveInterval(30 * 60);
    }
}
