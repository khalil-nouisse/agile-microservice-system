package com.example.project_service.repository;

import com.example.project_service.model.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    List<ProjectMember> findByProjectId(UUID projectId);

    Optional<ProjectMember> findByProjectIdAndEmail(UUID projectId, String email);

    List<ProjectMember> findByUserIdAndStatus(UUID userId, com.example.project_service.model.MemberStatus status);
}
