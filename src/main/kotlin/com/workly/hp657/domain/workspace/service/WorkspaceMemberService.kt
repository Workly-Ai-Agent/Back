package com.workly.hp657.domain.workspace.service

import com.workly.hp657.domain.user.repository.UserRepository
import com.workly.hp657.domain.workspace.dto.WorkspaceMemberAddRequest
import com.workly.hp657.domain.workspace.dto.WorkspaceMemberResponse
import com.workly.hp657.domain.workspace.dto.WorkspaceMemberUpdateRequest
import com.workly.hp657.domain.workspace.entity.WorkspaceMember
import com.workly.hp657.domain.workspace.repository.WorkspaceMemberRepository
import com.workly.hp657.domain.workspace.repository.WorkspaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class WorkspaceMemberService(
    private val workspaceMemberRepository: WorkspaceMemberRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun addMember(
        workspaceId: Long,
        request: WorkspaceMemberAddRequest
    ): WorkspaceMemberResponse {

        val workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow {
                IllegalArgumentException(
                    "워크스페이스를 찾을 수 없습니다."
                )
            }

        // 이메일로 사용자 조회
        val user = userRepository.findByEmail(request.email)
            .orElseThrow {
                IllegalArgumentException(
                    "해당 이메일의 사용자를 찾을 수 없습니다."
                )
            }

        val userId = user.id
            ?: throw IllegalArgumentException(
                "사용자 ID가 존재하지 않습니다."
            )

        if (
            workspaceMemberRepository.existsByWorkspaceIdAndUserId(
                workspaceId,
                userId
            )
        ) {
            throw IllegalArgumentException(
                "이미 워크스페이스에 등록된 사용자입니다."
            )
        }

        val member = WorkspaceMember(
            workspace = workspace,
            user = user,
            role = request.role
        )

        return workspaceMemberRepository
            .save(member)
            .toResponse()
    }

    fun getMembers(
        workspaceId: Long
    ): List<WorkspaceMemberResponse> {

        validateWorkspace(workspaceId)

        return workspaceMemberRepository
            .findAllByWorkspaceId(workspaceId)
            .map { it.toResponse() }
    }

    fun getMember(
        workspaceId: Long,
        memberId: Long
    ): WorkspaceMemberResponse {

        val member = findMember(memberId)

        validateMemberWorkspace(
            member,
            workspaceId
        )

        return member.toResponse()
    }

    @Transactional
    fun updateRole(
        workspaceId: Long,
        memberId: Long,
        request: WorkspaceMemberUpdateRequest
    ): WorkspaceMemberResponse {

        val member = findMember(memberId)

        validateMemberWorkspace(
            member,
            workspaceId
        )

        member.role = request.role

        return member.toResponse()
    }

    @Transactional
    fun removeMember(
        workspaceId: Long,
        memberId: Long
    ) {

        val member = findMember(memberId)

        validateMemberWorkspace(
            member,
            workspaceId
        )

        workspaceMemberRepository.delete(member)
    }

    private fun findMember(
        memberId: Long
    ): WorkspaceMember {

        return workspaceMemberRepository.findById(memberId)
            .orElseThrow {
                IllegalArgumentException(
                    "워크스페이스 멤버를 찾을 수 없습니다."
                )
            }
    }

    private fun validateWorkspace(
        workspaceId: Long
    ) {

        if (!workspaceRepository.existsById(workspaceId)) {
            throw IllegalArgumentException(
                "워크스페이스를 찾을 수 없습니다."
            )
        }
    }

    private fun validateMemberWorkspace(
        member: WorkspaceMember,
        workspaceId: Long
    ) {

        if (member.workspace.id != workspaceId) {
            throw IllegalArgumentException(
                "해당 워크스페이스의 멤버가 아닙니다."
            )
        }
    }

    private fun WorkspaceMember.toResponse():
            WorkspaceMemberResponse {

        return WorkspaceMemberResponse(
            id = id!!,
            workspaceId = workspace.id!!,
            userId = user.id!!,
            role = role,
            joinedAt = joinedAt
        )
    }
}
