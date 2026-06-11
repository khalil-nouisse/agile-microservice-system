package com.example.project_service.service;

import com.example.project_service.dto.AssignRoleRequest;
import com.example.project_service.dto.CreateProjectRequest;
import com.example.project_service.dto.InviteMemberRequest;
import com.example.project_service.dto.ProjectMemberResponse;
import com.example.project_service.dto.ProjectResponse;
import com.example.project_service.event.ProjectEventPublisher;
import com.example.project_service.exception.BadRequestException;
import com.example.project_service.feign.AuthServiceClient;
import com.example.project_service.model.AgileMethodology;
import com.example.project_service.model.Project;
import com.example.project_service.model.ProjectMember;
import com.example.project_service.model.ProjectRole;
import com.example.project_service.repository.ProjectMemberRepository;
import com.example.project_service.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository memberRepository;

    @Mock
    private ProjectEventPublisher eventPublisher;

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void createProjectStoresProjectOwnerAndPublishesEvent() {
        UUID ownerId = UUID.randomUUID();

        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Agile Platform");
        request.setDescription("Student MVP");
        request.setMethodology(AgileMethodology.SCRUM);

        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberRepository.save(any(ProjectMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectResponse response = projectService.createProject(request, ownerId);

        assertEquals("Agile Platform", response.getName());
        assertEquals(ownerId, response.getOwnerId());

        verify(memberRepository).save(any(ProjectMember.class));
        verify(eventPublisher).publishProjectCreated(any(Project.class));
    }

    @Test
    void inviteMemberRejectsDuplicateEmail() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project();
        ReflectionTestUtils.setField(project, "id", projectId);
        project.setName("Agile Platform");
        project.setOwnerId(UUID.randomUUID());

        InviteMemberRequest request = new InviteMemberRequest();
        request.setEmail("dev@example.com");
        request.setRole(ProjectRole.DEV);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(memberRepository.findByProjectIdAndEmail(projectId, "dev@example.com"))
                .thenReturn(Optional.of(new ProjectMember()));

        assertThrows(BadRequestException.class, () -> projectService.inviteMember(projectId, request));
    }

    @Test
    void assignRoleUpdatesMemberRoleAndPublishesEvent() {
        UUID projectId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        UUID ownerId = UUID.randomUUID();

        Project project = new Project();
        ReflectionTestUtils.setField(project, "id", projectId);
        project.setName("Agile Platform");
        project.setOwnerId(ownerId);

        ProjectMember member = new ProjectMember();
        ReflectionTestUtils.setField(member, "id", memberId);
        member.setProject(project);
        member.setEmail("qa@example.com");
        member.setRole(ProjectRole.QA);

        AssignRoleRequest request = new AssignRoleRequest();
        request.setRole(ProjectRole.SCRUM_MASTER);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(memberRepository.save(member)).thenReturn(member);

        ProjectMemberResponse response = projectService.assignRole(projectId, memberId, request, ownerId);

        assertEquals(ProjectRole.SCRUM_MASTER, response.getRole());
        verify(eventPublisher).publishRoleAssigned(member, ProjectRole.QA);
    }
}
