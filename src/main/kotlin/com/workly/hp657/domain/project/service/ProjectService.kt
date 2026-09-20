package com.workly.hp657.domain.project.service

import com.workly.hp657.domain.project.dto.ProjectCreateRequest
import com.workly.hp657.domain.project.dto.ProjectResponse
import com.workly.hp657.domain.project.dto.ProjectUpdateRequest
import com.workly.hp657.domain.project.entity.Project
import com.workly.hp657.domain.project.entity.ProjectMember
import com.workly.hp657.domain.project.entity.ProjectMemberRole
import com.workly.hp657.domain.project.repository.ProjectMemberRepository
import com.workly.hp657.domain.project.repository.ProjectRepository
import com.workly.hp657.domain.user.repository.UserRepository
import com.workly.hp657.domain.workspace.repository.WorkspaceMemberRepository
import com.workly.hp657.domain.workspace.repository.WorkspaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val projectMemberRepository: ProjectMemberRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val workspaceMemberRepository: WorkspaceMemberRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun create(
        email: String,
        workspaceId: Long,
        request: ProjectCreateRequest
    ): ProjectResponse {

        val creator = userRepository.findByEmail(email)
            .orElseThrow {
                IllegalArgumentException("사용자를 찾을 수 없습니다.")
            }

        val workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow {
                IllegalArgumentException("워크스페이스를 찾을 수 없습니다.")
            }

        // 프로젝트 생성자가 해당 Workspace의 멤버인지 확인
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(
                workspaceId,
                creator.id!!
            )
        ) {
            throw IllegalArgumentException("워크스페이스의 멤버가 아닙니다.")
        }

        val leader = userRepository.findById(request.leaderId)
            .orElseThrow {
                IllegalArgumentException("프로젝트 리더를 찾을 수 없습니다.")
            }

        // 프로젝트 리더가 해당 Workspace의 멤버인지 확인
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(
                workspaceId,
                leader.id!!
            )
        ) {
            throw IllegalArgumentException("프로젝트 리더는 워크스페이스의 멤버여야 합니다.")
        }

        val project = Project(
            name = request.name,
            description = request.description,
            workspace = workspace,
            createdBy = creator,
            leader = leader
        )

        val savedProject = projectRepository.save(project)

        // 리더를 프로젝트 멤버로 자동 등록
        projectMemberRepository.save(
            ProjectMember(
                project = savedProject,
                user = leader,
                role = ProjectMemberRole.LEADER
            )
        )

        return ProjectResponse.from(savedProject)
    }

    fun getProjects(
        email: String,
        workspaceId: Long
    ): List<ProjectResponse> {

        val user = userRepository.findByEmail(email)
            .orElseThrow {
                IllegalArgumentException("사용자를 찾을 수 없습니다.")
            }

        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(
                workspaceId,
                user.id!!
            )
        ) {
            throw IllegalArgumentException("워크스페이스의 멤버가 아닙니다.")
        }

        return projectRepository
            .findAllByWorkspaceId(workspaceId)
            .map(ProjectResponse::from)
    }

    fun getProject(
        email: String,
        projectId: Long
    ): ProjectResponse {

        val user = userRepository.findByEmail(email)
            .orElseThrow {
                IllegalArgumentException("사용자를 찾을 수 없습니다.")
            }

        val project = projectRepository.findById(projectId)
            .orElseThrow {
                IllegalArgumentException("프로젝트를 찾을 수 없습니다.")
            }

        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(
                project.workspace.id!!,
                user.id!!
            )
        ) {
            throw IllegalArgumentException("워크스페이스의 멤버가 아닙니다.")
        }

        return ProjectResponse.from(project)
    }

    @Transactional
    fun update(
        email: String,
        projectId: Long,
        request: ProjectUpdateRequest
    ): ProjectResponse {

        val user = userRepository.findByEmail(email)
            .orElseThrow {
                IllegalArgumentException("사용자를 찾을 수 없습니다.")
            }

        val project = projectRepository.findById(projectId)
            .orElseThrow {
                IllegalArgumentException("프로젝트를 찾을 수 없습니다.")
            }

        validateWorkspaceMember(project, user.id!!)

        val leader = userRepository.findById(request.leaderId)
            .orElseThrow {
                IllegalArgumentException("프로젝트 리더를 찾을 수 없습니다.")
            }

        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(
                project.workspace.id!!,
                leader.id!!
            )
        ) {
            throw IllegalArgumentException("프로젝트 리더는 워크스페이스의 멤버여야 합니다.")
        }

        project.name = request.name
        project.description = request.description
        project.leader = leader

        return ProjectResponse.from(project)
    }

    @Transactional
    fun delete(
        email: String,
        projectId: Long
    ) {

        val user = userRepository.findByEmail(email)
            .orElseThrow {
                IllegalArgumentException("사용자를 찾을 수 없습니다.")
            }

        val project = projectRepository.findById(projectId)
            .orElseThrow {
                IllegalArgumentException("프로젝트를 찾을 수 없습니다.")
            }

        validateWorkspaceMember(project, user.id!!)

        projectMemberRepository.deleteAll(
            projectMemberRepository.findAllByProjectId(projectId)
        )

        projectRepository.delete(project)
    }

    private fun validateWorkspaceMember(
        project: Project,
        userId: Long
    ) {
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(
                project.workspace.id!!,
                userId
            )
        ) {
            throw IllegalArgumentException("워크스페이스의 멤버가 아닙니다.")
        }
    }
}