package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.dto.Article;
import com.example.demo.dto.ResultData;
import com.example.demo.service.ArticleService;
import com.example.demo.util.Util;

import jakarta.servlet.http.HttpSession;

@Controller
public class UsrArticleController {

	private ArticleService articleService;

	public UsrArticleController(ArticleService articleService) {
		this.articleService = articleService;
	}

	@GetMapping("/usr/article/doWrite")
	@ResponseBody
	public ResultData<Article> doWrite(HttpSession session, String title, String body) {

		int loginedMemberId = -1;

		if (session.getAttribute("loginedMemberId") != null) {

			loginedMemberId = (int) session.getAttribute("loginedMemberId");
		}

		if (loginedMemberId == -1) {
			return ResultData.from("F-1", "로그인 후 이용할 수 있는 기능입니다");
		}

		if (Util.isEmpty(title)) {
			return ResultData.from("F-2", "제목을 입력해주세요.");
		}

		if (Util.isEmpty(body)) {
			return ResultData.from("F-3", "내용을 입력해주세요.");
		}

		articleService.writeArticle(loginedMemberId, title, body);

		int id = articleService.getLastInsertId();

		return ResultData.from("S-1", String.format("%d번 게시물을  작성했습니다.", id), articleService.getArticlebyId(id));
	}

	@GetMapping("/usr/article/showList")
	@ResponseBody
	public ResultData<List<Article>> showList() {

		List<Article> article = articleService.getArticles();

		if (article.size() == 0) {
			return ResultData.from("F-1", "게시물이 없습니다.");
		}

		return ResultData.from("S-1", "게시물", article);

	}

	@GetMapping("usr/article/showDetail")
	@ResponseBody
	public ResultData<Article> showDetail(int id) {

		Article foundArticle = articleService.getArticlebyId(id);

		if (foundArticle == null) {
			return ResultData.from("F-1", String.format("%d번 게시물은  존재하지 않습니다.", id));

		}

		return ResultData.from("S-1", String.format("%d번 게시물  상세보기.", id), foundArticle);

	}

	@GetMapping("/usr/article/doModify")
	@ResponseBody
	public ResultData<Article> doModify(HttpSession session, int id, String title, String body) {

		int loginedMemberId = -1;

		if (session.getAttribute("loginedMemberId") != null) {

			loginedMemberId = (int) session.getAttribute("loginedMemberId");
		}

		if (loginedMemberId == -1) {
			return ResultData.from("F-1", "로그인 후 이용할 수 있는 기능입니다");
		}

		Article foundArticle = articleService.getArticlebyId(id);

		if (foundArticle == null) {
			return ResultData.from("F-2", String.format("%d번 게시물은 없습니다.", id));

		}

		if (loginedMemberId != foundArticle.getMemberId()) {
			return ResultData.from("F-3", "해당 게시글에 대한 권한이 없습니다");
		}

		articleService.doModify(id, title, body);

		return ResultData.from("S-1", id + "번 게시물을 수정했습니다.", articleService.getArticlebyId(id));

	}

	@GetMapping("/usr/article/doDelete")
	@ResponseBody
	public ResultData doDelete(HttpSession session, int id) { // 데이터가 없어서 안써도 되지만 노란줄 신경쓰이면 아무거나 쓰면 됨

		int loginedMemberId = -1;

		if (session.getAttribute("loginedMemberId") != null) {

			loginedMemberId = (int) session.getAttribute("loginedMemberId");
		}

		if (loginedMemberId == -1) {
			return ResultData.from("F-1", "로그인 후 이용할 수 있는 기능입니다");
		}
		Article foundArticle = articleService.getArticlebyId(id);

		if (foundArticle == null) {
			return ResultData.from("F-3", String.format("%d번 게시물은  존재하지 않습니다.", id));
		}

		if ((int) session.getAttribute("loginedMemberId") != foundArticle.getMemberId()) {
			return ResultData.from("F-2", "해당 게시글에 대한 권한이 없습니다");
		}

		articleService.doDelete(id);

		return ResultData.from("S-1", String.format("%d번 게시물을  삭제했습니다.", id));

	}

}
