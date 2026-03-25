package com.cmrit.demo.repository;

import com.cmrit.demo.model.AdminAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminActionRepository
        extends JpaRepository<AdminAction, Long> {

    // All actions by a specific admin
    List<AdminAction> findByAdminId(Long adminId);

    // All actions of a specific type
    List<AdminAction> findByActionType(String actionType);

    // Filter by both adminId and actionType
    List<AdminAction> findByAdminIdAndActionType(
            Long adminId, String actionType);
}