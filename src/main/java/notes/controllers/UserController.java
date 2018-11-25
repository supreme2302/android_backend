package notes.controllers;

import notes.models.Message;
import notes.models.Task;
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
import java.util.List;


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
        NOT_FOUND,
        SUCCESSFULLY_ADDED
    }

    @Autowired
    private UserService userService;

    @RequestMapping(method = RequestMethod.PUT, path = "/create")
    public ResponseEntity create(HttpSession session, @RequestBody User user) {
        System.out.println("session: " + session.getId());
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
        System.out.println("session: " + session.getId());
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


    @GetMapping(path = "/notes/{username}")
    public ResponseEntity getProfileUser(@PathVariable("username") String username, HttpSession session) {
        System.out.println("session: " + session.getId());
        if (session.getAttribute("user") == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new Message(UserStatus.ACCESS_ERROR)
            );
        }
//        User user = userService.getUserByNickname(username);
//        if (user == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(UserStatus.NOT_FOUND));
//        }

        List<Task> taskList = userService.getTaskList(username);
//        if (taskList == null) {}
        return ResponseEntity.ok(taskList);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/add")
    public ResponseEntity addNote(HttpSession session, @RequestBody Task task) {
        if (session.getAttribute("user") == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new Message(UserStatus.ACCESS_ERROR)
            );
        }
        userService.addNote(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(new Message(UserStatus.SUCCESSFULLY_ADDED));
    }

    @RequestMapping(method = RequestMethod.GET, path = "/info")
    public ResponseEntity getInfo(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    new Message(UserStatus.ACCESS_ERROR)
            );
        }
        User user = userService.getUserByNickname((String) session.getAttribute("user"));
        user.setPassword("kaka");

        return ResponseEntity.status(HttpStatus.OK).body(new Message(user));
    }


    private static void sessionAuth(HttpSession session, User user) {
        session.setAttribute("user", user.getName());
        session.setMaxInactiveInterval(30 * 60);
    }
}
