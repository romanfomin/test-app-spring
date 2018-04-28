package testapp.springbackend.repository;

import org.springframework.data.repository.CrudRepository;
import testapp.springbackend.entity.User;

import java.util.List;

public interface UserRepository extends CrudRepository<User,Integer> {
}
