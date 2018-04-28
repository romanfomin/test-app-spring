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
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import testapp.springbackend.controller.UserController;
import testapp.springbackend.entity.User;
import testapp.springbackend.repository.UserRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
public class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private static UserController userController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private Map<Long, User> db = new HashMap<>();
    private AtomicLong userId = new AtomicLong(0);

    @Before
    public void setUp() {
        when(userRepository.save(any(User.class)))
                .then(inv -> {
                    User user = inv.getArgument(0);
                    user.setId(userId.incrementAndGet());
                    db.put(user.getId(), user);
                    return user;
                });
        when(userRepository.findById(anyLong()))
                .then(inv -> Optional.ofNullable(db.get(inv.getArgument(0))));
        userRepository.save(new User("Ivan"));
        userRepository.save(new User("Josh"));
        userRepository.save(new User("Bob"));
    }


    @Test
    public void getExistingUserTest() throws Exception {
        Long id = 2L;
        mockMvc.perform(get("/user")
                .param("id", id.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(objectMapper.writeValueAsString(db.get(id))));
    }

    @Test
    public void getNonexistentUserTest() throws Exception {
        Long id = 10L;
        mockMvc.perform(get("/user")
                .param("id", id.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(content().json(objectMapper.writeValueAsString(db.get(id))));
    }


    @Configuration
    public static class TestConfiguration {

        @Bean
        public UserController userController() {
            return userController;
        }

    }

}
