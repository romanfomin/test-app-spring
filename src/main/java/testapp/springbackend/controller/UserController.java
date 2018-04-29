package testapp.springbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import testapp.springbackend.entity.User;
import testapp.springbackend.exception.IllegalUserIdException;
import testapp.springbackend.exception.IncompleteUserInfoException;
import testapp.springbackend.exception.UserNotFoundException;
import testapp.springbackend.repository.UserRepository;

import java.util.Optional;

@RestController
@RequestMapping(value = "/user")
public class UserController {
    private UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping(
            params = "id",
            produces = "application/json"
    )
    @ResponseBody
    public User findUserById(@RequestParam("id") Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return user.get();
        }
        if (id <= 0) {
            throw new IllegalUserIdException();
        }
        throw new UserNotFoundException();

    }


    @PostMapping(
            consumes = "application/json",
            produces = "application/json"
    )
    public Long addUser(@RequestBody User user) {
        if (user.getFirstName().isEmpty()
                || user.getLastName().isEmpty()
                || user.getEmail().isEmpty()
                || user.getPhoneNumber().isEmpty()) {
            throw new IncompleteUserInfoException();
        }
        return userRepository.save(user).getId();

    }

}
