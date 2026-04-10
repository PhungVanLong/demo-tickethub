package demo.ticket_app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import demo.ticket_app.entity.Seat;
import jakarta.persistence.LockModeType;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findBySeatMapIdOrderByRowLabelAscColNumberAsc(Long seatMapId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id IN :ids")
    List<Seat> findByIdInForUpdate(@Param("ids") List<Long> ids);
}
