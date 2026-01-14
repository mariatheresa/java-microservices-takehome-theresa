package sg.com.gic.orderprocessingsystem.eventbus.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sg.com.gic.orderprocessingsystem.eventbus.entity.OutboxEventEntity;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity,Long> {
  List<OutboxEventEntity> findByEventTypeAndProcessedFalse(String eventType);

}
