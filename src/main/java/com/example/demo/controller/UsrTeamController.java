package com.example.demo.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.ResultData;
import com.example.demo.dto.Rq;
import com.example.demo.dto.Team;
import com.example.demo.dto.TeamMember;
import com.example.demo.dto.TeamReply;
import com.example.demo.service.MemberService;
import com.example.demo.service.TeamReplyService;
import com.example.demo.service.TeamService;
import com.example.demo.util.Util;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class UsrTeamController {

	private TeamService teamService;
	private MemberService memberService;
	private TeamReplyService teamReplyService;

	public UsrTeamController(TeamService teamService, MemberService memberService, TeamReplyService teamReplyService) {
		this.teamService = teamService;
		this.memberService = memberService;
		this.teamReplyService = teamReplyService;
	}

	@GetMapping("/usr/team/createTeam") // 대소문자 수정
	public String createTeam() { // 메서드 이름도 카멜 케이스로 수정
	    return "usr/team/CreateTeam";
	}

	@PostMapping("/usr/team/doCreateTeam")
	@ResponseBody
	public String doCreateTeam(String teamName, String region, String slogan, @RequestParam("teamImage") MultipartFile teamImage, HttpServletRequest req) {
		
		Rq rq = (Rq) req.getAttribute("rq");
	    try {
	        // MultipartFile을 byte[]로 변환
	        byte[] teamImageBytes = teamImage.getBytes();

	        // 세션에서 로그인한 사용자의 ID 가져오기
	        Integer createdBy = (Integer) rq.getLoginedMemberId();  // "memberId"는 세션에 저장된 사용자 ID

	        if (createdBy == null) {
	            return Util.jsReturn("로그인 정보가 없습니다.", null);
	        }
	        
	        // 팀 가입 서비스 호출
	        teamService.joinTeam(teamName, region, slogan, teamImageBytes, createdBy);
	    } catch (IOException e) {
	        return Util.jsReturn("파일 업로드 실패", null);
	    }

	    return Util.jsReturn(String.format("%s팀의 창단을 환영합니다~", teamName), "/");
	}
	
	@GetMapping("/usr/team/teamDupChk")
	@ResponseBody
	public ResultData loginIdDupChk(String teamName) {
		
		Team team = teamService.getTeamByTeamName(teamName);
		
		if (team != null) {
			return ResultData.from("F-1", String.format("[ %s ]은(는) 이미 사용중인 팀명입니다", teamName));
		}
		
		return ResultData.from("S-1", String.format("[ %s ]은(는) 사용가능한 팀명입니다", teamName));
	}
	
	
	@GetMapping("/usr/team/myTeam")
	public String myTeam(HttpServletRequest req, Model model) {
	    Rq rq = (Rq) req.getAttribute("rq");
	    Integer createdBy = rq.getLoginedMemberId();


	    List<Team> teams = teamService.getTeamsByCreatedBy(createdBy);

	    List<TeamMember> pendingRequests = new ArrayList<>();

	    for (Team team : teams) {

	        List<TeamMember> teamRequests = teamService.getPendingJoinRequests(team.getId());
	        pendingRequests.addAll(teamRequests);

	    }

	    model.addAttribute("teams", teams);
	    model.addAttribute("pendingRequests", pendingRequests);


	    return "usr/team/myTeam";
	}

	
	
	@GetMapping("/usr/team/teamList")
	public String teamList(Model model,  @RequestParam(defaultValue = "1") int page,  @RequestParam(defaultValue = "teamName") String searchType, @RequestParam(defaultValue = "") String searchKeyword) {

	    int limitFrom = (page - 1) * 10;

	    List<Team> teams = teamService.getTeams(limitFrom, searchType, searchKeyword);
	    int teamsCnt = teamService.teamsCnt(searchType, searchKeyword);

	    int totalPagesCnt = (int) Math.ceil((double) teamsCnt / 10);

	    int from = ((page - 1) / 5) * 5 + 1;
	    int end = from + 4;

	    if (end > totalPagesCnt) {
	        end = totalPagesCnt;
	    }

	    model.addAttribute("teams", teams);
	    model.addAttribute("teamsCnt", teamsCnt);
	    model.addAttribute("totalPagesCnt", totalPagesCnt);
	    model.addAttribute("from", from);
	    model.addAttribute("end", end);
	    model.addAttribute("page", page);
	    model.addAttribute("searchType", searchType);
	    model.addAttribute("searchKeyword", searchKeyword);

	    return "usr/team/teamList";
	}
	
	
	@GetMapping("/usr/team/detail")
	public String teamDetail(HttpServletRequest req, HttpServletResponse resp, Model model, int id) {
	    // Existing view counting logic
	    Cookie[] cookies = req.getCookies();
	    boolean isViewed = false;

	    if (cookies != null) {
	        for (Cookie cookie : cookies) {
	            if (cookie.getName().equals("viewedTeam_" + id)) {
	                isViewed = true;
	                break;
	            }
	        }
	    }

	    if (!isViewed) {
	        teamService.increaseViews(id);
	        Cookie cookie = new Cookie("viewedTeam_" + id, "true");
	        cookie.setMaxAge(60*30);
	        resp.addCookie(cookie);
	    }
	    
	    // Fetch team replies
	    List<TeamReply> replies = teamReplyService.getReplies(id);
	    
	    Team team = teamService.getTeambyId(id);
	        
	    model.addAttribute("team", team);
	    model.addAttribute("replies", replies);
	    
	    return "usr/team/detail";
	}
	
	
	
	@GetMapping("/usr/team/doDelete")
	@ResponseBody
	public String doDelete(int id, String teamName, @SessionAttribute("loginedMemberId") int loginedMemberId) {
	    // 해당 팀 정보 가져오기
	    Team team = teamService.getTeamById(id);
	    
	    // 로그인한 사용자와 팀 리더가 일치하는지 확인
	    if (team.getCreatedBy() != loginedMemberId) {
	        return Util.jsReturn("팀 리더만 팀을 해체할 수 있습니다.", "myTeam");
	    }

	    // 팀 삭제 처리
	    teamService.doDeleteTeam(teamName);
	    
	    return Util.jsReturn(String.format("[ %s ] 팀을 해체했습니다.", teamName), "/");
	}
	
	@GetMapping("/usr/team/modify")
	public String modify(@RequestParam("id") int id, Model model) {
	    Team team = teamService.getTeamById(id);
	    model.addAttribute("team", team);
	    return "usr/team/modify";
	}

	@PostMapping("/usr/team/doModify")
	@ResponseBody
	public String doModify(int id, String teamName, String region, String slogan,
	    @RequestParam(value = "teamImage", required = false) MultipartFile teamImage,
	    @SessionAttribute("loginedMemberId") int loginedMemberId
	) {
	    // 팀 정보 가져오기
	    Team team = teamService.getTeamById(id);

	    // 팀 리더 확인
	    if (team == null || team.getCreatedBy() != loginedMemberId) {
	        return Util.jsReturn("팀 리더만 팀을 수정할 수 있습니다.", "myTeam");
	    }

	    byte[] teamImageBytes = null;
	    if (teamImage != null && !teamImage.isEmpty()) {
	        try {
	            teamImageBytes = teamImage.getBytes();
	        } catch (IOException e) {
	            e.printStackTrace();
	            return Util.jsReturn("이미지 업로드 중 오류가 발생했습니다.", "myTeam");
	        }
	    }

	    // 팀 수정 처리
	    teamService.doModifyTeam(id, teamName, region, slogan, teamImageBytes);

	    return Util.jsReturn(String.format("[ %s ] 팀 정보가 수정되었습니다.", teamName), "/usr/team/myTeam");
	}

	
	@PostMapping("/usr/team/saveResults")
	@ResponseBody
	public String saveResults( @RequestParam("teamId") int teamId, @RequestParam("wins") int wins, @RequestParam("draws") int draws, @RequestParam("losses") int losses, HttpServletRequest req) {
	    // 총 승점 계산 (승=3점, 무=1점, 패=0점)
	    int points = (wins * 3) + draws;

	    // 로그인 정보 가져오기
	    Rq rq = (Rq) req.getAttribute("rq");
	    int loginedMemberId = rq.getLoginedMemberId();

	    // 서비스 호출하여 데이터 업데이트
	    teamService.updateTeamResults(teamId, wins, draws, losses, points, loginedMemberId);

	    return Util.jsReturn("팀 성적이 업데이트되었습니다.", "/usr/team/myTeam");
	}
	
	@GetMapping("/usr/team/rankings")
	public String rankings(Model model) {
	    List<Team> rankedTeams = teamService.getRankedTeams();
	    model.addAttribute("rankedTeams", rankedTeams);
	    return "usr/team/rankings";
	}
	
	@GetMapping("/usr/team/reservation")
	public String showReservationPage(Model model, @RequestParam(defaultValue = "1") int page) {
	    try {
	        StringBuilder urlBuilder = new StringBuilder("http://openapi.seoul.go.kr:8088");
	        urlBuilder.append("/" + URLEncoder.encode("6779454974676f6834334550777359", "UTF-8")); /* 인증키 */
	        urlBuilder.append("/" + URLEncoder.encode("json", "UTF-8")); /* 요청파일타입 */
	        urlBuilder.append("/" + URLEncoder.encode("ListPublicReservationSport", "UTF-8")); /* 서비스명 */
	        urlBuilder.append("/" + URLEncoder.encode("1", "UTF-8")); /* 요청시작위치 */
	        urlBuilder.append("/" + URLEncoder.encode("40", "UTF-8")); /* 요청종료위치 */
	        urlBuilder.append("/" + URLEncoder.encode("풋살장", "UTF-8"));

	        URL url = new URL(urlBuilder.toString());
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("GET");
	        conn.setRequestProperty("Content-type", "application/json");

	        BufferedReader rd;
	        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
	            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
	        } else {
	            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
	        }

	        StringBuilder sb = new StringBuilder();
	        String line;
	        while ((line = rd.readLine()) != null) {
	            sb.append(line);
	        }
	        rd.close();
	        conn.disconnect();

	        // Parse JSON and extract reservation details
	        ObjectMapper objectMapper = new ObjectMapper();
	        JsonNode rootNode = objectMapper.readTree(sb.toString());
	        JsonNode rowsNode = rootNode.path("ListPublicReservationSport").path("row");

	        List<Map<String, String>> reservations = new ArrayList<>();
	        for (JsonNode node : rowsNode) {
	            Map<String, String> reservation = new HashMap<>();
	            reservation.put("placeName", node.path("PLACENM").asText());
	            reservation.put("url", node.path("SVCURL").asText());
	            reservation.put("startDate", node.path("RCPTBGNDT").asText());
	            reservation.put("endDate", node.path("RCPTENDDT").asText());
	            reservation.put("imgUrl", node.path("IMGURL").asText());
	            reservation.put("startTime", node.path("V_MIN").asText());
	            reservation.put("endTime", node.path("V_MAX").asText());
	            reservation.put("paymentMethod", node.path("PAYATNM").asText());
	            reservation.put("serviceStatus", node.path("SVCSTATNM").asText());
	            reservation.put("useTgtInfo", node.path("USETGTINFO").asText());

	            reservations.add(reservation);
	        }

	        // Pagination logic
	        int totalItems = reservations.size();
	        int itemsPerPage = 6;
	        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
	        int startIndex = (page - 1) * itemsPerPage;
	        int endIndex = Math.min(startIndex + itemsPerPage, totalItems);

	        // Ensure the sublist indices are valid
	        if (startIndex >= totalItems) {
	            startIndex = totalItems;  // If startIndex is beyond the totalItems, set it to totalItems
	        }

	        if (startIndex < endIndex) {
	            List<Map<String, String>> paginatedReservations = reservations.subList(startIndex, endIndex);
	            model.addAttribute("reservations", paginatedReservations);
	        } else {
	            model.addAttribute("reservations", new ArrayList<>());  // Return an empty list if invalid range
	        }

	        // Pagination page range calculation (5 pages at a time)
	        int fromPage = Math.max(1, (page - 1) / 5 * 5 + 1); // Ensure fromPage starts at 1
	        int toPage = Math.min(fromPage + 4, totalPages);

	        model.addAttribute("page", page);
	        model.addAttribute("totalPages", totalPages);
	        model.addAttribute("fromPage", fromPage);
	        model.addAttribute("toPage", toPage);

	        return "usr/team/reservation";
	    } catch (Exception e) {
	        e.printStackTrace();
	        model.addAttribute("errorMessage", "API 호출 중 오류가 발생했습니다: " + e.getMessage());
	        return "usr/team/reservation";
	    }
	}
	
	@GetMapping("/usr/team/requestJoin")
	@ResponseBody
	public String requestJoin(@RequestParam("teamId") int teamId, HttpServletRequest req) {
	    Rq rq = (Rq) req.getAttribute("rq");
	    Integer loginedMemberId = rq.getLoginedMemberId();

	    teamService.createJoinRequest(teamId, loginedMemberId);

	    return Util.jsReturn("팀 가입 요청이 완료되었습니다.", "/usr/team/teamList");
	}
	
}
