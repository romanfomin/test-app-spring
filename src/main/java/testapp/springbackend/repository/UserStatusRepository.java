package testapp.springbackend.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import testapp.springbackend.entity.UserStatus;

import java.util.Optional;

@Repository
public interface UserStatusRepository extends CrudRepository<UserStatus,Long> {

    Optional<UserStatus> findById(Long id);
    UserStatus save(UserStatus s);
}
