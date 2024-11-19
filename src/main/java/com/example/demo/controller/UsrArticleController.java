package com.example.demo.controller;

import java.text.Format;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.dto.Article;
import com.example.demo.dto.Board;
import com.example.demo.dto.ResultData;
import com.example.demo.dto.Rq;
import com.example.demo.service.ArticleService;
import com.example.demo.util.Util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class UsrArticleController {

	private ArticleService articleService;

	public UsrArticleController(ArticleService articleService) {
		this.articleService = articleService;
	}

	@GetMapping("/usr/article/write")
	public String write() {

		return "usr/article/write";
	}
	
	@PostMapping("/usr/article/doWrite")
	@ResponseBody
	public String doWrite(HttpServletRequest req, int boardId ,String title, String body) {

		Rq rq = (Rq) req.getAttribute("rq");

		articleService.writeArticle(rq.getLoginedMemberId(), boardId, title, body);

		int id = articleService.getLastInsertId();

		return Util.jsReturn(String.format("%d번 게시물을 작성했습니다.", id), String.format("detail?id=%d", id));
	}

	@GetMapping("/usr/article/list")
	public String showList(Model model, int boardId) {
		
		Board board = articleService.getBoardId(boardId);
		
		List<Article> articles = articleService.getArticles(boardId);
		
		model.addAttribute("articles", articles);
		model.addAttribute("board", board);
		
		return "usr/article/list";
	}

	@GetMapping("/usr/article/detail")
	public String showDetail(HttpSession session, Model model, int id) {

		//로그인 체크 interceptor에서 실행(jsp는 그냥 요청에 접근 가능하므로 model로 보내지 않고 그냥 jsp에서 loginedMemberId를 쓸 수 있다)
		
		Article article = articleService.getArticlebyId(id);
		
		model.addAttribute("article", article);

		return "usr/article/detail";
	}
	
	@GetMapping("/usr/article/modify")
	public String modify(Model model, int id) {
		
		Article article = articleService.getArticlebyId(id);
		
		model.addAttribute("article", article);
		
		return "usr/article/modify";
	}

	@PostMapping("/usr/article/doModify")
	@ResponseBody
	public String doModify(HttpServletRequest req, int id, String title, String body) {
		
		Rq rq = (Rq) req.getAttribute("rq");

		Article article = articleService.getArticlebyId(id);

		if (article == null) {
			return Util.jsReturn(String.format("%d번 게시물은 존재하지않습니다.", article.getId()), "/");
		}

		if (rq.getLoginedMemberId() != article.getMemberId()) {
			return Util.jsReturn("해당 게시글에 대한 권한이 없습니다", "/");
		}

		articleService.doModify(id, title, body);

		return Util.jsReturn(String.format("%d번 게시물을 수정했습니다.", article.getId()), String.format("detail?id=%d", id));

	}
//	114번째 줄 = 수정후 다시 디테일로 돌아가기
	
	@GetMapping("/usr/article/doDelete")
	@ResponseBody
	public String doDelete(HttpSession session, int id) { // 데이터가 없어서 안써도 되지만 노란줄 신경쓰이면 아무거나 쓰면 됨

		articleService.doDelete(id);

		return Util.jsReturn(String.format("%d번 게시물을 삭제했습니다.", id), "list");

	}

}
