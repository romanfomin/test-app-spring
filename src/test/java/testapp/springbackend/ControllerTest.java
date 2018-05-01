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
import testapp.springbackend.controller.UserStatusController;
import testapp.springbackend.entity.Status;
import testapp.springbackend.entity.User;
import testapp.springbackend.entity.UserStatus;
import testapp.springbackend.entity.UserStatusResp;
import testapp.springbackend.repository.UserRepository;
import testapp.springbackend.repository.UserStatusRepository;

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

    @Mock
    private UserStatusRepository userStatusRepository;

    @InjectMocks
    private static UserStatusController userStatusController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private Map<Long, User> users = new HashMap<>();
    private Map<Long, UserStatus> statuses = new HashMap<>();
    private AtomicLong userId = new AtomicLong(0);

    @Before
    public void setUp() {
        int delay=1000;
        when(userRepository.save(any(User.class)))
                .then(inv -> {
                    Thread.sleep(delay);
                    User user = inv.getArgument(0);
                    user.setId(userId.incrementAndGet());
                    users.put(user.getId(), user);
                    return user;
                });
        when(userRepository.findById(anyLong()))
                .then(inv -> {
                    Thread.sleep(delay);
                    return Optional.ofNullable(users.get(inv.getArgument(0)));
                });
        when(userStatusRepository.save(any(UserStatus.class)))
                .then(inv -> {
                    Thread.sleep(delay);
                    UserStatus userStatus = inv.getArgument(0);
                    statuses.put(userStatus.getId(), userStatus);
                    return userStatus;
                });
        when(userStatusRepository.findById(anyLong()))
                .then(inv -> {
                    Thread.sleep(delay);
                    return Optional.ofNullable(statuses.get(inv.getArgument(0)));
                });
        doAnswer(inv -> {
            for (UserStatus userStatus : statuses.values()) {
                if (userStatus.getStatus() == Status.ONLINE) {
                    if ((new Timestamp(System.currentTimeMillis()).getTime()
                            - userStatus.getDate().getTime()) / (/* 60 * */ 1000) >= 5) {
                        userStatus.setStatus(Status.AWAY);
                        statuses.put(userStatus.getId(), userStatus);
                    }
                }
            }
            return null;
        }).when(userStatusRepository).setAwayStatus();
        users.put(userId.incrementAndGet(), new User("Ivan", "Petrov", "mail", "555"));
        users.put(userId.incrementAndGet(), new User("Josh", "Doe", "gmail", "12345"));
        users.put(userId.incrementAndGet(), new User("Bob", "Lee", "mail", "2345"));
        users.put(userId.incrementAndGet(), new User("User", "Last", "email", "+921"));
        statuses.put(2L, new UserStatus(2L, Status.OFFLINE, new Timestamp(System.currentTimeMillis())));
        statuses.put(3L, new UserStatus(3L, Status.OFFLINE, new Timestamp(System.currentTimeMillis())));
        statuses.put(4L, new UserStatus(4L, Status.OFFLINE, new Timestamp(System.currentTimeMillis())));
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
    public void setStatusToOfflineWithoutPreviousTest() throws Exception {
        UserStatus userStatus = new UserStatus(1L, Status.OFFLINE);

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
                                null,
                                statuses.get(userStatus.getId()).getDate()
                        ))));
    }


    @Test
    public void setStatusToAwayTest() throws Exception {
        UserStatus userStatus = new UserStatus(2L, Status.AWAY);

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
                                statuses.get(userStatus.getId()).getDate()
                        ))));
    }


    @Test
    public void setStatusToOnlineAndWait5SecondsTest() throws Exception {
//        statuses.put(4L, new UserStatus(4L, Status.OFFLINE, new Timestamp(System.currentTimeMillis())));
        UserStatus userStatus = new UserStatus(4L, Status.ONLINE);

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
                                statuses.get(userStatus.getId()).getDate()
                        ))));
        assertEquals(Status.ONLINE, statuses.get(userStatus.getId()).getStatus());
        Thread.sleep(4000);
        assertEquals(Status.ONLINE, statuses.get(userStatus.getId()).getStatus());
        Thread.sleep(4000);
        assertEquals(Status.AWAY, statuses.get(userStatus.getId()).getStatus());
    }


    @Test
    public void setStatusToOnlineThenToOfflineTest() throws Exception {
        statuses.put(3L, new UserStatus(3L, Status.OFFLINE, new Timestamp(System.currentTimeMillis())));
        UserStatus userStatus = new UserStatus(3L, Status.ONLINE);

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
                                statuses.get(userStatus.getId()).getDate()
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
                                statuses.get(userStatus.getId()).getDate()
                        ))));
        Thread.sleep(6000);
        assertEquals(Status.OFFLINE, statuses.get(userStatus.getId()).getStatus());
    }


    @Configuration
    public static class TestConfiguration {

        @Bean
        public UserController userController() {
            return userController;
        }

        @Bean
        public UserStatusController userStatusController() {
            return userStatusController;
        }

    }

}
