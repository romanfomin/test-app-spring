package testapp.springbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import testapp.springbackend.entity.User;
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
        if(user.isPresent()){
            return user.get();
        }else{
            //error!!!
            return null;
        }
    }


    @PostMapping(
            consumes = "application/json",
            produces = "application/json"
    )
    public void addUser(@RequestBody User user){
        userRepository.save(user);
    }

}
