package testapp.springbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import testapp.springbackend.entity.Status;
import testapp.springbackend.entity.User;
import testapp.springbackend.entity.UserStatusResp;
import testapp.springbackend.exception.IllegalUserIdException;
import testapp.springbackend.exception.IllegalUserStatusException;
import testapp.springbackend.exception.IncompleteUserInfoException;
import testapp.springbackend.exception.UserNotFoundException;
import testapp.springbackend.repository.UserRepository;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/user")
public class UserController {
    private UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(
                userRepository::setAwayStatus,0,2,TimeUnit.MINUTES);
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
        if (user == null
                || user.getFirstName() == null || user.getFirstName().isEmpty()
                || user.getLastName() == null || user.getLastName().isEmpty()
                || user.getEmail() == null || user.getEmail().isEmpty()
                || user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty()) {
            throw new IncompleteUserInfoException();
        }
        user.setDate(new Timestamp(System.currentTimeMillis()));
        user.setStatus(Status.ONLINE);
        return userRepository.save(user).getId();

    }


    @PatchMapping(
            consumes = "application/json",
            produces = "application/json"
    )
    public UserStatusResp setStatus(@RequestBody User user) {
        if (user == null || user.getStatus() == null) {
            throw new IllegalUserStatusException();
        }
        if (user.getId() <= 0) {
            throw new IllegalUserIdException();
        }

        Optional<User> userRecOpt = userRepository.findById(user.getId());
        if(!userRecOpt.isPresent()){
            throw new UserNotFoundException();
        }

        User userRec=userRecOpt.get();
        userRec.setDate(new Timestamp(System.currentTimeMillis()));
        UserStatusResp statusResp = new UserStatusResp(
                userRec.getId(),
                user.getStatus(),
                userRec.getStatus(),
                userRec.getDate()
        );
        userRec.setStatus(user.getStatus());

        userRepository.save(userRec);
        return statusResp;
    }
}
