package com.workly.hp657.domain.chat.repository
import com.workly.hp657.domain.chat.entity.Message
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
interface MessageRepository : JpaRepository<Message, Long> {
    @Query("select m from Message m where m.workspace.id = :workspaceId and ((:projectId is null and m.project is null) or m.project.id = :projectId) and ((:receiverId is null and m.receiver is null) or (m.sender.id = :userId and m.receiver.id = :receiverId) or (m.sender.id = :receiverId and m.receiver.id = :userId)) order by m.createdAt asc")
    fun findChannel(@Param("workspaceId") workspaceId: Long, @Param("projectId") projectId: Long?, @Param("receiverId") receiverId: Long?, @Param("userId") userId: Long): List<Message>
    @Query(
        """
        select m from Message m
        where m.workspace.id = :workspaceId
          and ((m.sender.id = :userId and m.receiver.id = :otherUserId)
            or (m.sender.id = :otherUserId and m.receiver.id = :userId))
        order by m.createdAt asc
        """
    )
    fun findConversation(
        @Param("workspaceId") workspaceId: Long,
        @Param("userId") userId: Long,
        @Param("otherUserId") otherUserId: Long
    ): List<Message>
}
