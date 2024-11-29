<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>


<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<script>
	$(document).click.ready(function(){
		$('#testBtn').click(function(){
			$.ajax({
				url : '/usr/home/test',
				type : 'GET',
				data : {
					key1 : 'value1',
					key2 : 'value2'
				},
				dataType : 'json',
				success : function(test) {
					console.log(test);
					$('#testDiv').append('<div>' + test.data.key1 + '</div>');
				},
				error : function(xhr, status, error) {
					console.log(error);
				}
			})
		})
	})
</script>


<section class="px-32">
	<div>Lorem ipsum dolor sit amet, consectetur adipisicing elit. Perspiciatis laudantium quod expedita quae tenetur maxime modi commodi non esse minus nostrum voluptates necessitatibus ipsum reprehenderit architecto recusandae eligendi mollitia tempore.</div>
	<div>안녕하세요</div>
</section>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>