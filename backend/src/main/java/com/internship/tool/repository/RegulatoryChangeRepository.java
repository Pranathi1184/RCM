package com.internship.tool.repository;

import com.internship.tool.entity.ChangeStatus;
import com.internship.tool.entity.RegulatoryChange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RegulatoryChangeRepository extends JpaRepository<RegulatoryChange, Long> {

    // 1. Get all active records (ignoring soft-deleted ones)
    Page<RegulatoryChange> findAllByIsDeletedFalse(Pageable pageable);

    // 2. Filter by Status
    Page<RegulatoryChange> findByStatusAndIsDeletedFalse(ChangeStatus status, Pageable pageable);

    // 3. Find by Deadline Date Range
    Page<RegulatoryChange> findByDeadlineBetweenAndIsDeletedFalse(LocalDate startDate, LocalDate endDate, Pageable pageable);

    // 4. Custom Query for Search
    @Query("SELECT r FROM RegulatoryChange r WHERE r.isDeleted = false AND (" +
           "LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.category) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.regulatoryBody) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<RegulatoryChange> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 5. Dashboard KPIs
    long countByIsDeletedFalse();

    @Query("SELECT r.status, COUNT(r) FROM RegulatoryChange r WHERE r.isDeleted = false GROUP BY r.status")
    List<Object[]> countByStatus();

    @Query("SELECT r.priority, COUNT(r) FROM RegulatoryChange r WHERE r.isDeleted = false GROUP BY r.priority")
    List<Object[]> countByPriority();

    // 6. Scheduler Queries
    @Query("SELECT r FROM RegulatoryChange r WHERE r.isDeleted = false AND r.status NOT IN :excludedStatuses AND r.deadline < CURRENT_DATE")
    List<RegulatoryChange> findOverdueChanges(@Param("excludedStatuses") List<ChangeStatus> excludedStatuses);

    @Query("SELECT r FROM RegulatoryChange r WHERE r.isDeleted = false AND r.status NOT IN :excludedStatuses AND r.deadline = :targetDate")
    List<RegulatoryChange> findUpcomingDeadlines(@Param("excludedStatuses") List<ChangeStatus> excludedStatuses, @Param("targetDate") LocalDate targetDate);

    @Query("SELECT r FROM RegulatoryChange r WHERE r.isDeleted = false AND r.status NOT IN :excludedStatuses")
    List<RegulatoryChange> findActiveChangesForSummary(@Param("excludedStatuses") List<ChangeStatus> excludedStatuses);
}
