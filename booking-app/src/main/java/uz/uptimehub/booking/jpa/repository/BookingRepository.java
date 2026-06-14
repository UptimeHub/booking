package uz.uptimehub.booking.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.uptimehub.booking.dto.booking.Status;
import uz.uptimehub.booking.jpa.entity.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {

    @Query("""
                select count(b) > 0
                from Booking b
                where b.resourceId = :resourceId
                and b.status = :status
                and b.startTime < :endTime
                and b.endTime > :startTime
            """)
    boolean existsOverlappingBooking(
            @Param("resourceId") UUID resourceId,
            @Param("status") Status status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("""
                select b
                from Booking b
                where b.resourceId = :resourceId
                and b.status in :statuses
                and b.startTime < :to
                and b.endTime > :from
                order by b.startTime asc, b.endTime asc
            """)
    List<Booking> findBlockingBookingsForResource(
            @Param("resourceId") UUID resourceId,
            @Param("statuses") List<Status> statuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
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

    Page<Booking> findByUserId(
            UUID userId,
            Pageable pageable
    );

    @Query("""
                select b
                from Booking b
                where b.userId = :userId
                and (:status is null or b.status = :status)
                and (:resourceId is null or b.resourceId = :resourceId)
                and (:from is null or b.startTime >= :from)
                and (:to is null or b.endTime <= :to)
            """)
    Page<Booking> findMyBookings(
            @Param("userId") UUID userId,
            @Param("status") Status status,
            @Param("resourceId") UUID resourceId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    Optional<Booking> findByIdAndUserId(
            UUID id,
            UUID userId
    );

    @Query("""
                select b
                from Booking b
                where b.organizationId = :organizationId
                and (:userId is null or b.userId = :userId)
                and (:resourceId is null or b.resourceId = :resourceId)
                and (:status is null or b.status = :status)
                and (cast(:from as timestamp) is null or b.startTime >= :from)
                and (cast(:to as timestamp) is null or b.endTime <= :to)
            """)
    Page<Booking> searchBookings(
            @Param("organizationId") UUID organizationId,
            @Param("userId") UUID userId,
            @Param("resourceId") UUID resourceId,
            @Param("status") Status status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    @Query("""
                select b
                from Booking b
                where b.organizationId = :organizationId
                and b.resourceId = :resourceId
                and b.status = :status
                and b.startTime <= :now
                and b.endTime >= :now
            """)
    List<Booking> findCurrentBookingsForResource(
            @Param("organizationId") UUID organizationId,
            @Param("resourceId") UUID resourceId,
            @Param("status") Status status,
            @Param("now") LocalDateTime now
    );

    Optional<Booking> findByIdAndOrganizationId(
            UUID id,
            UUID organizationId
    );

}
