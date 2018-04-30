package testapp.springbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import testapp.springbackend.entity.Status;
import testapp.springbackend.entity.User;
import testapp.springbackend.entity.UserStatus;
import testapp.springbackend.entity.UserStatusResp;
import testapp.springbackend.exception.IllegalUserIdException;
import testapp.springbackend.exception.IllegalUserStatusException;
import testapp.springbackend.exception.IncompleteUserInfoException;
import testapp.springbackend.exception.UserNotFoundException;
import testapp.springbackend.repository.UserRepository;
import testapp.springbackend.repository.UserStatusRepository;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/user")
public class UserStatusController {
    private UserStatusRepository userStatusRepository;
    private UserRepository userRepository;
    private ScheduledExecutorService scheduler=Executors.newScheduledThreadPool(1);

    @Autowired
    public UserStatusController(UserStatusRepository userStatusRepository, UserRepository userRepository) {
        this.userStatusRepository = userStatusRepository;
        this.userRepository = userRepository;
    }


    @PatchMapping(
            consumes = "application/json",
            produces = "application/json"
    )
    public UserStatusResp setStatus(@RequestBody UserStatus userStatus) {
        if (userStatus == null || userStatus.getStatus() == null) {
            throw new IllegalUserStatusException();
        }
        if (userStatus.getId() <= 0) {
            throw new IllegalUserIdException();
        }

        UserStatusResp statusResp = new UserStatusResp();
        Optional<UserStatus> prevStatus = userStatusRepository.findById(userStatus.getId());
        if (!prevStatus.isPresent()) {
            Optional<User> user=userRepository.findById(userStatus.getId());
            if(!user.isPresent()){
                throw new UserNotFoundException();
            }
            statusResp.setPrevStatus(null);
        }else{
            statusResp.setPrevStatus(prevStatus.get().getStatus());
        }
        userStatus.setDate(new Date());
        userStatusRepository.save(userStatus);

// for each userstatus. if now - date>=5 min => away
//        if(userStatus.getStatus()==Status.ONLINE){
//            scheduler.schedule(()->userStatusRepository.save(new UserStatus(userStatus.getId(),Status.AWAY,new Date())),
//                    5,TimeUnit.SECONDS);
//            scheduler.shutdown();
//        }

        statusResp.setStatus(userStatus.getStatus());
        statusResp.setId(userStatus.getId());
        statusResp.setDate(userStatus.getDate());
        return statusResp;
    }

}
