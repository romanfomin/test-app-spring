package testapp.springbackend.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import testapp.springbackend.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User,Long> {

    Optional<User> findById(Long id);
    User save(User s);

    @Query("UPDATE statuses as s SET s.status='AWAY' WHERE s.date <= (CURRENT_TIMESTAMP - INTERVAL '5 minutes')::date AND s.status = 'ONLINE'")
    void setAwayStatus();
}
