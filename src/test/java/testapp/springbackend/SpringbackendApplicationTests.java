package testapp.springbackend;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import testapp.springbackend.controller.UserController;
import testapp.springbackend.entity.User;
import testapp.springbackend.repository.UserRepository;

import java.util.Optional;

import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class SpringbackendApplicationTests {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserController userController;

    @Test
    public void testMethod() {
        when(userRepository.findById(5)).thenReturn(Optional.of(new User("Ivan")));
        assertEquals("Ivan",userController.findUserById(5).get().getName());
    }

}
