package testapp.springbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import testapp.springbackend.controller.UserController;
import testapp.springbackend.entity.Status;
import testapp.springbackend.entity.User;
import testapp.springbackend.entity.UserStatusResp;
import testapp.springbackend.repository.UserRepository;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
public class ControllerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private static UserController userController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<Long, User> users = new HashMap<>();
    private AtomicLong userId = new AtomicLong(0);

    @Before
    public void setUp() {
        int delay = 1000;
        when(userRepository.save(any(User.class)))
                .then(inv -> {
                    Thread.sleep(delay);
                    User user = inv.getArgument(0);
                    if(users.get(user.getId())==null) {
                        user.setId(userId.incrementAndGet());
                    }
                    users.put(user.getId(), user);
                    return user;
                });
        when(userRepository.findById(anyLong()))
                .then(inv -> {
                    Thread.sleep(delay);
                    return Optional.ofNullable(users.get(inv.getArgument(0)));
                });
        doAnswer(inv -> {
            for (User user : users.values()) {
                System.out.println(user.getId() + " " + user.getStatus() + " " + user.getDate());
                if (user.getStatus() == Status.ONLINE) {
                    if ((new Timestamp(System.currentTimeMillis()).getTime()
                            - user.getDate().getTime()) / (60*1000) >= 5) {
                        user.setStatus(Status.AWAY);
                        users.put(user.getId(), user);
                    }
                }
            }

            return null;
        }).when(userRepository).setAwayStatus();
        userRepository.save(new User("Ivan", "Petrov", "mail", "555", Status.ONLINE, new Timestamp(System.currentTimeMillis())));
        userRepository.save(new User("Josh", "Doe", "gmail", "12345", Status.ONLINE, new Timestamp(System.currentTimeMillis())));
        userRepository.save(new User("Bob", "Lee", "mail", "2345", Status.ONLINE, new Timestamp(System.currentTimeMillis())));
        userRepository.save(new User("User", "Last", "email", "+921", Status.ONLINE, new Timestamp(System.currentTimeMillis())));
    }


    @Test
    public void getExistingUserTest() throws Exception {
        Long id = 2L;
        mockMvc.perform(get("/user")
                .param("id", id.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(objectMapper.writeValueAsString(users.get(id))));
    }

    @Test
    public void getNonexistentUserTest() throws Exception {
        Long id = 10L;
        mockMvc.perform(get("/user")
                .param("id", id.toString()))
                .andDo(print())
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(status().reason("No such User"))
                .andExpect(content().string(""));
    }

    @Test
    public void getUserWithNegativeIdTest() throws Exception {
        Long id = -2L;
        mockMvc.perform(get("/user")
                .param("id", id.toString()))
                .andDo(print())
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(status().reason("Wrong User id"))
                .andExpect(content().string(""));
    }

    @Test
    public void postUserWithFullInfoTest() throws Exception {
        User user = new User("Roman", "Fomin", "@gmail.com", "+7921");
        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(userId.get())));
    }

    @Test
    public void postUserWithNotFullInfoTest() throws Exception {
        User user = new User("Roman", "", "@gmail.com", "");
        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                .andExpect(status().reason("Not all fields are filled"))
                .andExpect(content().string(""));
    }


    @Test
    public void setStatusToAwayTest() throws Exception {
        User userStatus = new User(2L, Status.AWAY);

        mockMvc.perform(patch("/user")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(userStatus)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(objectMapper.writeValueAsString(
                        new UserStatusResp(
                                userStatus.getId(),
                                userStatus.getStatus(),
                                Status.ONLINE,
                                users.get(userStatus.getId()).getDate()
                        ))));
    }


    @Test
    public void setStatusToOnlineThenToOfflineTest() throws Exception {
        User user = users.get(3L);
        user.setStatus(Status.OFFLINE);
        user.setDate(new Timestamp(System.currentTimeMillis()));
        userRepository.save(user);
        User userStatus = new User(3L, Status.ONLINE);

        mockMvc.perform(patch("/user")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(userStatus)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(objectMapper.writeValueAsString(
                        new UserStatusResp(
                                userStatus.getId(),
                                userStatus.getStatus(),
                                Status.OFFLINE,
                                users.get(userStatus.getId()).getDate()
                        ))));
        Thread.sleep(1000);
        userStatus.setStatus(Status.OFFLINE);
        mockMvc.perform(patch("/user")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(userStatus)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(objectMapper.writeValueAsString(
                        new UserStatusResp(
                                userStatus.getId(),
                                userStatus.getStatus(),
                                Status.ONLINE,
                                users.get(userStatus.getId()).getDate()
                        ))));
        Thread.sleep(60*1000*6);
        assertEquals(Status.OFFLINE, users.get(userStatus.getId()).getStatus());
    }


    @Test
    public void setStatusToOnlineAndWait5SecondsTest() throws Exception {
        User user = users.get(4L);
        user.setStatus(Status.OFFLINE);
        user.setDate(new Timestamp(System.currentTimeMillis()));
        userRepository.save(user);
        User userStatus = new User(4L, Status.ONLINE);

        mockMvc.perform(patch("/user")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(userStatus)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(objectMapper.writeValueAsString(
                        new UserStatusResp(
                                userStatus.getId(),
                                userStatus.getStatus(),
                                Status.OFFLINE,
                                users.get(userStatus.getId()).getDate()
                        ))));


        assertEquals(Status.ONLINE, users.get(userStatus.getId()).getStatus());
        Thread.sleep(60*1000*4);
        assertEquals(Status.ONLINE, users.get(userStatus.getId()).getStatus());
        Thread.sleep(60*1000*2);
        assertEquals(Status.AWAY, users.get(userStatus.getId()).getStatus());
    }


    @Configuration
    public static class TestConfiguration {

        @Bean
        public UserController userController() {
            return userController;
        }

    }

}
