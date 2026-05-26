package uz.uptimehub.booking.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uptimehub.booking.dto.booking.Status;
import uz.uptimehub.booking.jpa.entity.Booking;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    boolean existsByResourceIdAndStatusAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
            UUID resourceId,
            Status status,
            LocalDateTime endTime,
            LocalDateTime startTime
    );

    @Modifying
    @Query("""
                update Booking b
                set b.status = :expiredStatus
                where b.status = :activeStatus
                and b.endTime < :now
            """)
    int markExpiredBookings(
            @Param("activeStatus") Status activeStatus,
            @Param("expiredStatus") Status expiredStatus,
            @Param("now") LocalDateTime now
    );
}