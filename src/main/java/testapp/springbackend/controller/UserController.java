package testapp.springbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import testapp.springbackend.entity.User;
import testapp.springbackend.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/user")
public class UserController {
    private UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    @GetMapping(value = "/findbyid")
    public Optional<User> findUserById(@RequestParam("id") Integer id){
        return userRepository.findById(id);
    }
}
