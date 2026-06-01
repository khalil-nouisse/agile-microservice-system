package com.example.project_service.service;

import com.example.project_service.dto.AssignRoleRequest;
import com.example.project_service.dto.CreateProjectRequest;
import com.example.project_service.dto.InviteMemberRequest;
import com.example.project_service.dto.ProjectMemberResponse;
import com.example.project_service.dto.ProjectResponse;
import com.example.project_service.dto.UpdateProjectRequest;
import com.example.project_service.event.ProjectEventPublisher;
import com.example.project_service.exception.BadRequestException;
import com.example.project_service.exception.NotFoundException;
import com.example.project_service.model.AgileMethodology;
import com.example.project_service.model.MemberStatus;
import com.example.project_service.model.Project;
import com.example.project_service.model.ProjectMember;
import com.example.project_service.model.ProjectRole;
import com.example.project_service.model.ProjectStatus;
import com.example.project_service.repository.ProjectMemberRepository;
import com.example.project_service.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final ProjectEventPublisher eventPublisher;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectMemberRepository memberRepository,
            ProjectEventPublisher eventPublisher
    ) {
        this.projectRepository = projectRepository;
        this.memberRepository = memberRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(UUID projectId) {
        return ProjectResponse.from(findProject(projectId));
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, UUID ownerId) {
        if (isBlank(request.getName())) {
            throw new BadRequestException("Project name is required");
        }

        Project project = new Project();
        project.setName(request.getName().trim());
        project.setDescription(request.getDescription());
        project.setOwnerId(ownerId);

        if (request.getMethodology() != null) {
            project.setMethodology(request.getMethodology());
        }

        Project savedProject = projectRepository.save(project);

        // The owner is also stored as a project member. This keeps membership
        // queries simple and gives the creator a clear role inside the project.
        ProjectMember ownerMember = new ProjectMember();
        ownerMember.setProject(savedProject);
        ownerMember.setUserId(ownerId);
        ownerMember.setRole(ProjectRole.PRODUCT_OWNER);
        ownerMember.setStatus(MemberStatus.ACTIVE);
        memberRepository.save(ownerMember);

        eventPublisher.publishProjectCreated(savedProject);

        return ProjectResponse.from(savedProject);
    }

    @Transactional
    public ProjectResponse updateProject(UUID projectId, UpdateProjectRequest request) {
        Project project = findProject(projectId);

        if (!isBlank(request.getName())) {
            project.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getMethodology() != null) {
            project.setMethodology(request.getMethodology());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(UUID projectId) {
        Project project = findProject(projectId);
        project.setStatus(ProjectStatus.ARCHIVED);
        projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getProjectMembers(UUID projectId) {
        findProject(projectId);

        return memberRepository.findByProjectId(projectId)
                .stream()
                .map(ProjectMemberResponse::from)
                .toList();
    }

    @Transactional
    public ProjectMemberResponse inviteMember(UUID projectId, InviteMemberRequest request) {
        Project project = findProject(projectId);

        if (isBlank(request.getEmail())) {
            throw new BadRequestException("Member email is required");
        }
        if (request.getRole() == null) {
            throw new BadRequestException("Member role is required");
        }

        String email = request.getEmail().trim().toLowerCase();
        memberRepository.findByProjectIdAndEmail(projectId, email)
                .ifPresent(existing -> {
                    throw new BadRequestException("Member is already invited to this project");
                });

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setEmail(email);
        member.setUserId(request.getUserId());
        member.setRole(request.getRole());

        ProjectMember savedMember = memberRepository.save(member);
        eventPublisher.publishMemberInvited(savedMember);

        return ProjectMemberResponse.from(savedMember);
    }

    @Transactional
    public ProjectMemberResponse assignRole(UUID projectId, UUID memberId, AssignRoleRequest request) {
        findProject(projectId);

        if (request.getRole() == null) {
            throw new BadRequestException("Role is required");
        }

        ProjectMember member = memberRepository.findById(memberId)
                .filter(existing -> existing.getProject().getId().equals(projectId))
                .orElseThrow(() -> new NotFoundException("Project member not found"));

        ProjectRole previousRole = member.getRole();
        member.setRole(request.getRole());

        ProjectMember savedMember = memberRepository.save(member);
        eventPublisher.publishRoleAssigned(savedMember, previousRole);

        return ProjectMemberResponse.from(savedMember);
    }

    private Project findProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
