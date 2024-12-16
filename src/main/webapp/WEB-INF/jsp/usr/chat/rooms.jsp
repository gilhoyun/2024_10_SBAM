<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>

<%@ include file="/WEB-INF/jsp/common/header.jsp"%>

<section class="px-auto py-8">
	<div class="max-w-4xl mx-auto p-6 bg-white rounded-lg shadow-md">
		<h2 class="text-2xl font-bold mb-4">채팅 방 목록</h2>

		<c:if test="${rq.getLoginedMemberId() != -1}">
			<div class="mb-4">
				<button id="createRoomBtn"
					class="btn bg-stone-400 text-white rounded hover:bg-stone-500">
					새 채팅방 만들기</button>
			</div>
		</c:if>
		<div id="chatRoomList" class="space-y-4">
			<c:forEach var="room" items="${rooms}">
				<div class="card bg-base-100 shadow-xl">
					<div class="card-body">
						<h3 class="card-title">${room.roomName}</h3>
						<p>참여 인원: ${room.participantCount}명</p>
						<div class="card-actions justify-end">
							<a href="/usr/chat/room/${room.roomId}"
								class="btn bg-stone-400 text-white rounded hover:bg-stone-500">
								입장 </a>
						</div>
					</div>
				</div>
			</c:forEach>
		</div>

		<!-- Create Room Modal -->
		<dialog id="createRoomModal" class="modal">
		<div class="modal-box">
			<h3 class="font-bold text-lg">새 채팅방 만들기</h3>
			<div class="form-control w-full">
				<label class="label"> <span class="label-text">채팅방 이름</span>
				</label> <input type="text" id="roomNameInput"
					class="input input-bordered w-full" placeholder="채팅방 이름을 입력하세요" />
			</div>
			<div class="modal-action">
				<button id="submitRoomBtn"
					class="btn bg-stone-400 text-white rounded hover:bg-stone-500">만들기</button>
				<button onclick="document.getElementById('createRoomModal').close()"
					class="btn bg-red-400 text-white rounded hover:bg-red-500">취소</button>
			</div>
		</div>
		</dialog>
	</div>
</section>

<script>
	$(function() {
		const userId = '${rq.getLoginedMemberId()}';

		$('#createRoomBtn').click(function() {
			document.getElementById('createRoomModal').showModal();
		});

		$('#submitRoomBtn')
				.click(
						function() {
							const roomName = $('#roomNameInput').val().trim();

							if (roomName) {
								$
										.ajax({
											url : '/usr/chat/rooms',
											type : 'POST',
											contentType : 'application/json',
											data : JSON.stringify({
												roomName : roomName,
												createdBy : userId
											}),
											success : function(room) {
												// Redirect to the newly created room
												window.location.href = `/usr/chat/room/\${room.roomId}`;
											},
											error : function(xhr) {
												alert('채팅방 생성 중 오류가 발생했습니다.');
											}
										});
							} else {
								alert('채팅방 이름을 입력해주세요.');
							}
						});
	});
</script>

<%@ include file="/WEB-INF/jsp/common/footer.jsp"%>