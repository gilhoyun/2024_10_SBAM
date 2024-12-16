package com.example.demo.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.demo.dto.Chat;
import com.example.demo.dto.ChatRoom;

@Mapper
public interface ChatDao {

    // 채팅방 생성
    @Insert("""
        INSERT INTO chat_room (
            room_id, 
            room_name, 
            created_by, 
            created_at, 
            participant_count
        ) VALUES (
            #{roomId}, 
            #{roomName}, 
            #{createdBy}, 
            #{createdAt}, 
            1
        )
    """)
    void insertChatRoom(ChatRoom chatRoom);

    // 모든 채팅방 조회
    @Select("""
        SELECT 
            room_id AS roomId, 
            room_name AS roomName, 
            created_by AS createdBy, 
            created_at AS createdAt, 
            participant_count AS participantCount
        FROM chat_room
        ORDER BY created_at DESC
    """)
    List<ChatRoom> selectAllChatRooms();

    // 특정 채팅방 조회
    @Select("""
        SELECT 
            room_id AS roomId, 
            room_name AS roomName, 
            created_by AS createdBy, 
            created_at AS createdAt, 
            participant_count AS participantCount
        FROM chat_room
        WHERE room_id = #{roomId}
    """)
    ChatRoom selectChatRoomById(@Param("roomId") String roomId);

    // 메시지 저장
    @Insert("""
        INSERT INTO chat_message (
            room_id, 
            sender, 
            content, 
            timestamp, 
            is_read
        ) VALUES (
            #{roomId}, 
            #{sender}, 
            #{content}, 
            #{timestamp}, 
            #{isRead}
        )
    """)
    void insertMessage(Chat chat);

    // 특정 채팅방의 채팅 히스토리 조회
    @Select("""
        SELECT 
            id, 
            room_id AS roomId, 
            sender, 
            content, 
            timestamp, 
            is_read AS isRead
        FROM chat_message
        WHERE room_id = #{roomId}
        ORDER BY timestamp ASC
    """)
    List<Chat> selectChatHistory(@Param("roomId") String roomId);

    // 채팅방 삭제
    @Delete("""
        DELETE FROM chat_room
        WHERE room_id = #{roomId}
    """)
    void deleteChatRoom(@Param("roomId") String roomId);

    // 특정 채팅방의 메시지 삭제
    @Delete("""
        DELETE FROM chat_message
        WHERE room_id = #{roomId}
    """)
    void deleteMessagesByRoomId(@Param("roomId") String roomId);

    // 메시지 읽음 상태 업데이트
    @Update("""
        UPDATE chat_message
        SET is_read = #{isRead}
        WHERE room_id = #{roomId}
    """)
    void updateMessageReadStatus(@Param("roomId") String roomId, @Param("isRead") boolean isRead);

    // 채팅방 참가자 수 업데이트
    @Update("""
        UPDATE chat_room
        SET participant_count = #{count}
        WHERE room_id = #{roomId}
    """)
    void updateParticipantCount(@Param("roomId") String roomId, @Param("count") int count);
}
