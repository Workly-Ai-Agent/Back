package com.workly.hp657.domain.project.service

import com.workly.hp657.domain.project.dto.AddProjectMemberRequest
import com.workly.hp657.domain.project.dto.ProjectMemberResponse
import com.workly.hp657.domain.project.dto.UpdateProjectMemberRequest
import com.workly.hp657.domain.project.entity.ProjectMember
import com.workly.hp657.domain.project.entity.ProjectMemberRole
import com.workly.hp657.domain.project.repository.ProjectMemberRepository
import com.workly.hp657.domain.project.repository.ProjectRepository
import com.workly.hp657.domain.user.repository.UserRepository
import com.workly.hp657.domain.workspace.repository.WorkspaceMemberRepository
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProjectMemberService(
    private val projectRepository: ProjectRepository,
    private val projectMemberRepository: ProjectMemberRepository,
    private val userRepository: UserRepository,
    private val workspaceMemberRepository: WorkspaceMemberRepository
) {

    fun addMember(
        email: String,
        projectId: Long,
        request: AddProjectMemberRequest
    ): ProjectMemberResponse {

        val currentUser = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        val project = projectRepository.findById(projectId)
            .orElseThrow {
                IllegalArgumentException("프로젝트를 찾을 수 없습니다.")
            }

        // 현재 사용자가 Workspace 관리자 또는 프로젝트 리더인지 확인
        checkProjectManager(
            currentUser.id!!,
            projectId,
            project.workspace.id!!
        )

        // 추가할 사용자 조회
        val user = userRepository.findById(request.userId)
            .orElseThrow {
                IllegalArgumentException("추가할 사용자를 찾을 수 없습니다.")
            }

        // 추가할 사용자가 해당 Workspace의 멤버인지 확인
        val workspaceMember =
            workspaceMemberRepository.findByWorkspaceIdAndUserId(
                project.workspace.id!!,
                user.id!!
            )

        if (workspaceMember == null) {
            throw IllegalArgumentException(
                "해당 사용자는 이 Workspace의 멤버가 아닙니다."
            )
        }

        // 이미 프로젝트 멤버인지 확인
        if (
            projectMemberRepository.existsByProjectIdAndUserId(
                projectId,
                user.id!!
            )
        ) {
            throw IllegalArgumentException(
                "이미 프로젝트의 멤버입니다."
            )
        }

        val member = ProjectMember(
            project = project,
            user = user,
            role = ProjectMemberRole.valueOf(
                request.role.uppercase()
            )
        )

        return ProjectMemberResponse.from(
            projectMemberRepository.save(member)
        )
    }

    @Transactional(readOnly = true)
    fun getMembers(
        email: String,
        projectId: Long
    ): List<ProjectMemberResponse> {

        val currentUser = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        val project = projectRepository.findById(projectId)
            .orElseThrow {
                IllegalArgumentException("프로젝트를 찾을 수 없습니다.")
            }

        // Workspace 멤버인지 확인
        checkWorkspaceMember(
            currentUser.id!!,
            project.workspace.id!!
        )

        return projectMemberRepository
            .findAllByProjectId(projectId)
            .map(ProjectMemberResponse::from)
    }

    fun updateMember(
        email: String,
        projectId: Long,
        userId: Long,
        request: UpdateProjectMemberRequest
    ): ProjectMemberResponse {

        val currentUser = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        val project = projectRepository.findById(projectId)
            .orElseThrow {
                IllegalArgumentException("프로젝트를 찾을 수 없습니다.")
            }

        // 현재 사용자가 Workspace 관리자 또는 프로젝트 리더인지 확인
        checkProjectManager(
            currentUser.id!!,
            projectId,
            project.workspace.id!!
        )

        val member =
            projectMemberRepository.findByProjectIdAndUserId(
                projectId,
                userId
            )
                ?: throw IllegalArgumentException(
                    "프로젝트 멤버를 찾을 수 없습니다."
                )

        member.role = ProjectMemberRole.valueOf(
            request.role.uppercase()
        )

        return ProjectMemberResponse.from(member)
    }

    fun removeMember(
        email: String,
        projectId: Long,
        userId: Long
    ) {

        val currentUser = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }

        val project = projectRepository.findById(projectId)
            .orElseThrow {
                IllegalArgumentException("프로젝트를 찾을 수 없습니다.")
            }

        // 현재 사용자가 Workspace 관리자 또는 프로젝트 리더인지 확인
        checkProjectManager(
            currentUser.id!!,
            projectId,
            project.workspace.id!!
        )

        val member =
            projectMemberRepository.findByProjectIdAndUserId(
                projectId,
                userId
            )
                ?: throw IllegalArgumentException(
                    "프로젝트 멤버를 찾을 수 없습니다."
                )

        projectMemberRepository.delete(member)
    }

    private fun checkProjectManager(
        userId: Long,
        projectId: Long,
        workspaceId: Long
    ) {

        // Workspace 관리자 확인
        val workspaceMember =
            workspaceMemberRepository.findByWorkspaceIdAndUserId(
                workspaceId,
                userId
            )

        if (workspaceMember?.role?.name == "ADMIN") {
            return
        }

        // 프로젝트 리더 확인
        val projectMember =
            projectMemberRepository.findByProjectIdAndUserId(
                projectId,
                userId
            )

        if (projectMember?.role?.name != "LEADER") {
            throw AccessDeniedException(
                "프로젝트 멤버를 관리할 권한이 없습니다."
            )
        }
    }

    private fun checkWorkspaceMember(
        userId: Long,
        workspaceId: Long
    ) {

        val member =
            workspaceMemberRepository.findByWorkspaceIdAndUserId(
                workspaceId,
                userId
            )

        if (member == null) {
            throw AccessDeniedException(
                "Workspace 멤버가 아닙니다."
            )
        }
    }
}