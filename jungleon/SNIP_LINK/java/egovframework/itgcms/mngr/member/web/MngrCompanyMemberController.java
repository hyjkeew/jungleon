package egovframework.itgcms.mngr.member.web;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import egovframework.itgcms.core.code.service.MngrCodeService;
import egovframework.itgcms.core.code.service.MngrCodeVO;
import egovframework.itgcms.core.ipCountry.service.IpCountryService;
import egovframework.itgcms.core.ipCountry.service.IpCountryVO;
import egovframework.itgcms.core.joinformconfig.service.MngrJoinFormConfigSearchVO;
import egovframework.itgcms.core.joinformconfig.service.MngrJoinFormConfigService;
import egovframework.itgcms.core.member.service.MemberExtVO;
import egovframework.itgcms.core.member.service.MemberService;
import egovframework.itgcms.core.member.service.MemberVO;
import egovframework.itgcms.core.member.service.MngrMemberExcelView;
import egovframework.itgcms.core.persninfolog.service.MngrPersnInfoLogService;
import egovframework.itgcms.mngr.member.service.MngrCompanyMemberService;
import egovframework.itgcms.project.cominfo.service.CominfoService;
import egovframework.itgcms.project.cominfo.service.CominfoVO;
import egovframework.itgcms.util.CommUtil;
import egovframework.itgcms.util.CustomUtil;
import egovframework.itgcms.util.ExcelDownloadView;
import egovframework.rte.psl.dataaccess.util.EgovMap;
import egovframework.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;

/**
 * @파일명 : MngrCompanyMemberController.java
 * @파일정보 : 기업회원 등록 관리
 * @수정이력
 * @수정자 수정일 수정내용
 * @------- ------------ ----------------
 * @woonee 2014. 9. 4. 최초생성
 * @---------------------------------------
 * @author (주)아이티굿 개발팀
 * @since 2009. 01.14
 * @version 1.0 Copyright (C) ITGOOD All right reserved.
 */

@Controller
public class MngrCompanyMemberController {

	/** MemberService */
	@Resource(name = "memberService")
	private MemberService memberService;


	@Resource(name = "mngrMemberExcelView")
	private MngrMemberExcelView mngrMemberExcelView;

	@Resource(name="mngrCodeService")
	private MngrCodeService mngrCodeService;

	@Resource(name="mngrCompanyMemberService")
	private MngrCompanyMemberService mngrCompanyMemberServicesss;

	@Resource(name = "ipCountryService")
	private IpCountryService ipCountryService;

	@Resource(name = "cominfoService")
	private CominfoService cominfoService;

	@Resource(name="joinFormConfigService")
	private MngrJoinFormConfigService joinFormConfigService;

	@Resource(name = "mngrPersnInfoLogService")
	private MngrPersnInfoLogService mngrPersnInfoLogService;

	private static final Logger logger = LogManager.getLogger(MngrCompanyMemberController.class);

	/**
	 * 기업회원 리스트
	 * @param searchVO
	 * @param model
	 * @return
	 * @throws IOException, SQLException, RuntimeException
	 */
	@RequestMapping(value = "/_mngr_/member/company_list.do")
	public String mngrMemberList(@ModelAttribute("searchVO") MemberVO searchVO, ModelMap model) throws IOException, SQLException, RuntimeException {
		searchVO.setPageUnit(Integer.parseInt(searchVO.getViewCount()));
		searchVO.setPageSize(10);

		int page = Integer.parseInt(searchVO.getPage());

		PaginationInfo paginationInfo = new PaginationInfo();
		paginationInfo.setCurrentPageNo(page);
		paginationInfo.setRecordCountPerPage(searchVO.getPageUnit());
		paginationInfo.setPageSize(searchVO.getPageSize());

		searchVO.setFirstIndex(paginationInfo.getFirstRecordIndex());
		searchVO.setLastIndex(paginationInfo.getLastRecordIndex());
		searchVO.setRecordCountPerPage(paginationInfo.getRecordCountPerPage());

		/** paging setting */
		int totCnt = mngrCompanyMemberServicesss.selectCompanyMemberListTotCnt(searchVO);
		// pagesize, viewcount => searchVO에 설정
		paginationInfo.setTotalRecordCount(totCnt);
		List<EgovMap> resultList = mngrCompanyMemberServicesss.selectCompanyMemberList(searchVO);
		model.addAttribute("paginationInfo", paginationInfo);
		model.addAttribute("listNo", (totCnt - ((Integer.parseInt(searchVO.getPage()) - 1)* paginationInfo.getRecordCountPerPage()))); //페이지 No 의 시작 값 가상의 넘버링

		model.addAttribute("resultList", resultList);
		model.addAttribute("searchVO", searchVO);

		return "itgcms/mngr/member/company_list";
	}

	@RequestMapping(value="/_mngr_/member/mngrCompanyMemberExcelDown.do")
	public ModelAndView selectMngrCompanyMemberListExcelDown( ModelMap model,
			@ModelAttribute("searchVO") MemberVO searchVO, HttpServletRequest request) throws IOException, SQLException{

		ModelAndView mav = new ModelAndView(ExcelDownloadView.EXCEL_DOWN);

		// 일반회원 type = N => searchVO.etc2
		searchVO.setEtc2("N");
		searchVO.setExcelDown("Y");
		List<MemberVO> resultList = memberService.selectMemberList(searchVO);

		HashMap<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("dataList", resultList);

		//엑셀 템플릿에 넘겨줄 데이타
		mav.addObject("data", paramMap);

		//다운로드에 사용되어질 엑셀파일 템플릿
		mav.addObject(ExcelDownloadView.DOWN_EXCEL_TEMPLATE, "mngr.mngrMemberExcelDown");
		//다운로드시 표시될 파일명 (확장자는 자동으로 xls로 지정된다.)
		mav.addObject(ExcelDownloadView.DOWN_FILE_NM, "기업회원-" + CommUtil.getDatePattern("yyyy-MM-dd"));

		return mav;
	}

	/**
	 * 회원등록
	 * @param searchVO
	 * @param model
	 * @return
	 * @throws IOException, SQLException, RuntimeException
	 */
	@RequestMapping(value = "/_mngr_/member/company_input.do")
	public String mngrMemberRegist(@ModelAttribute("searchVO") MemberVO searchVO, ModelMap model) throws IOException, SQLException, RuntimeException {
		searchVO.setAct("REGIST");
		model.addAttribute("result", new MemberExtVO());
		model.addAttribute("cominfo", new CominfoVO());

		MngrCodeVO mngrCodeVO = new MngrCodeVO();
		mngrCodeVO.setSchCode("memstatuscd");
		model.addAttribute("statusCodeList", mngrCodeService.getMngrCodeList(mngrCodeVO));

		mngrCodeVO = new MngrCodeVO();
		mngrCodeVO.setSchCode("V015");//패스워드 질문
		model.addAttribute("pwdQuest", mngrCodeService.getMngrCodeList(mngrCodeVO));

		mngrCodeVO.setSchCode("V073");//관심사업
		model.addAttribute("concerns", mngrCodeService.getMngrCodeList(mngrCodeVO));

		mngrCodeVO.setSchCode("V007");//주소.
		model.addAttribute("addr", mngrCodeService.getMngrCodeList(mngrCodeVO));


		return "itgcms/mngr/member/company_edit";
	}

	/**
	 * 회원등록 처리
	 * @param memberVO
	 * @param model
	 * @return
	 * @throws SQLException
	 */
	@RequestMapping(value = "/_mngr_/member/company_input_proc.do", method = RequestMethod.POST)
	public String mngrMemberRegistProc(@ModelAttribute("memberVO") MemberExtVO memberVO,
			@ModelAttribute("cominfoVO") CominfoVO cominfoVO, ModelMap model, HttpServletRequest request) throws SQLException {

		// 아이디, 이름, 비밀번호, 비밀번호확인, 이메일, 부서
		if ("".equals(CommUtil.isNull(memberVO.getId(), "")))
			return CommUtil.doComplete(model, "오류", "아이디를 입력 해 주세요.", "history.back();");
		if ("".equals(CommUtil.isNull(memberVO.getPass(), "")))
			return CommUtil.doComplete(model, "오류", "비밀번호를 입력 해 주세요.", "history.back();");
		if ("".equals(CommUtil.isNull(memberVO.getPass2(), "")))
			return CommUtil.doComplete(model, "오류", "비밀번호 확인을 입력 해 주세요.", "history.back();");

		//아이디 첫글자 영문 체크
		if (!CommUtil.regEx("^[a-zA-Z]", memberVO.getId()))
			return CommUtil.doComplete(model, "오류", "아이디 첫글자는 영문자로 입력 해 주세요.", "history.back();");
		if (CommUtil.regEx("[^a-zA-Z0-9_]", memberVO.getId()))
			return CommUtil.doComplete(model, "오류", "아이디는 영문, 숫자, _ 만 입력 가능합니다.", "history.back();");
		if (memberVO.getId().length() < 4 || memberVO.getId().length() > 12)
			return CommUtil.doComplete(model, "오류", "아이디는 4 ~ 12자 이내로 입력 해 주세요.", "history.back();");

		//아이디 중복 검사
		MemberVO checkIdVO = new MemberVO();
		checkIdVO.setId(memberVO.getId());
		int resultIdCount = memberService.mSelectMemberCnt(checkIdVO);
		if (resultIdCount > 0)
			return CommUtil.doComplete(model, "오류", "중복된 아이디 입니다. 확인 후 다시 시도하세요.", "history.back();");


		//비밀번호 체크
		if (!(CommUtil.regEx("[0-9]", memberVO.getPass()) &&
				CommUtil.regEx("[a-zA-Z]", memberVO.getPass2()) &&
				memberVO.getPass().length() >= 8))
			return CommUtil.doComplete(model, "오류", "비밀번호는 영문 + 숫자 조합으로 8자 이상 입력 해 주세요.", "history.back();");

		if (!memberVO.getPass().equals(memberVO.getPass2()))
			return CommUtil.doComplete(model, "오류", "비밀번호와 비밀번호 확인이 일치하지 않습니다.", "history.back();");

		//이메일 형식
		if (!CommUtil.empty(memberVO.getEmail())){
			if (!CommUtil.isEmail(memberVO.getEmail()))
				return CommUtil.doComplete(model, "오류", "올바른 이메일 형식이 아닙니다. 확인 후 다시 시도해주세요.", "history.back();");
		}
		String ip = request.getRemoteAddr();
		IpCountryVO ipCountryVO = ipCountryService.getCountryNameAndCode(ip);
		memberVO.setRegIp(ip);
		memberVO.setRegCountryCd(ipCountryVO.getCountryCode());
		memberVO.setRegCountryName(ipCountryVO.getCountryName());

		UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
		ReadableUserAgent uai = parser.parse(request.getHeader("User-Agent"));
		memberVO.setRegBrowser(uai.getName());
		memberVO.setRegBrowserIcon(uai.getIcon());
		memberVO.setRegOs(uai.getOperatingSystem().getName());
		memberVO.setRegOsIcon(uai.getOperatingSystem().getIcon());

		//비밀번호 암호화
//		memberVO.setPass(CommUtil.getPass(memberVO.getPass()));
		String url = request.getRequestURL().toString();
		memberVO.setSchOpt1(url);
		try {
			memberVO.setSchOpt5(memberVO.getPass());
			memberVO.setPass(CommUtil.hexSha256(memberVO.getPass()));
		}catch(NoSuchAlgorithmException e1){
			logger.error("예외 상황 발생");
		}catch(IOException e2){
			logger.error("예외 상황 발생");
		}
		memberVO.setRegId(CommUtil.getMngrMemId());

		if("".equals(CommUtil.isNull(cominfoVO.getBusiRegNo(), ""))) {
			return CommUtil.doComplete(model, "오류", "사업자 등록번호를 확인 해 주세요.", "history.back();");
		}
		int cnt = cominfoService.selectCominfoCheckBusiRegNo(cominfoVO);
		if(cnt > 0) {
			return CommUtil.doComplete(model, "오류", "중복된 사업자등록번호 입니다.", "history.back();");
		}
		if (cominfoVO.getAddr01().indexOf("수정구") > -1 ) cominfoVO.setAreaCd("V007000001");
        else if (cominfoVO.getAddr01().indexOf("중원구") > -1 ) cominfoVO.setAreaCd("V007000002");
        else if (cominfoVO.getAddr01().indexOf("분당구") > -1 ) cominfoVO.setAreaCd("V007000003");
        else cominfoVO.setAreaCd("V007000004");

		memberVO.setAreaCd(cominfoVO.getAreaCd());

		// company 정보와 t_member 정보를 insert함.
		cominfoService.insertCominfo(memberVO, cominfoVO);

		return CommUtil.doComplete(model, "완료", "등록 되었습니다.", "location.href='company_list.do?" + memberVO.getQuery() + "'");
	}

	/**
	 * 기업회원정보 수정
	 * @param memberVO
	 * @param model
	 * @return
	 * @throws SQLException
	 */
	@RequestMapping(value = "/_mngr_/member/company_edit_proc.do")
	public String mngrMemberUpdateProc(@ModelAttribute("memberVO") MemberExtVO memberVO, @ModelAttribute("cominfoVO") CominfoVO cominfoVO, ModelMap model, HttpServletRequest request) throws SQLException {
		/*if ("".equals(CommUtil.isNull(memberVO.getEmail(), "")))
			return CommUtil.doComplete(model, "오류", "이메일주소를  입력 해 주세요.", "history.back();");*/
		if (!CommUtil.empty(memberVO.getEmail())){
			if (!CommUtil.isEmail(memberVO.getEmail()))
				return CommUtil.doComplete(model, "오류", "올바른 이메일 형식이 아닙니다. 확인 후 다시 시도해주세요.", "history.back();");
		}


		memberVO.setUpdId(CommUtil.getMngrMemId());
		memberVO.setUpdIp(request.getRemoteAddr());

		if (cominfoVO.getAddr01().indexOf("수정구") > -1 ) cominfoVO.setAreaCd("V007000001");
        else if (cominfoVO.getAddr01().indexOf("중원구") > -1 ) cominfoVO.setAreaCd("V007000002");
        else if (cominfoVO.getAddr01().indexOf("분당구") > -1 ) cominfoVO.setAreaCd("V007000003");
        else cominfoVO.setAreaCd("V007000004");

		memberVO.setAreaCd(cominfoVO.getAreaCd());

		cominfoVO.setId(memberVO.getId());
		cominfoVO.setModId(memberVO.getId());
		// company 정보와 t_member 정보를 update함.
		cominfoService.updateCominfo(memberVO, cominfoVO);

		return CommUtil.doComplete(model, "완료", "수정 되었습니다.", "location.href='company_edit.do?" + memberVO.getQuery() + "'");
	}
	/**
	 * 기업회원정보
	 * @param searchVO
	 * @param model
	 * @return
	 * @throws IOException, SQLException, RuntimeException
	 */
	@RequestMapping(value = "/_mngr_/member/company_edit.do")
	public String mngrMemberView(@ModelAttribute("searchVO") MemberVO searchVO, ModelMap model, HttpServletRequest request) throws IOException, SQLException, RuntimeException {
		searchVO.setAct("UPDATE");
		MemberVO result = memberService.selectMemberView(searchVO);

		MngrCodeVO mngrCodeVO = new MngrCodeVO();

		MngrJoinFormConfigSearchVO joinFormVO = joinFormConfigService.mngrGetJoinFormBySiteCode(result.getRegSiteCode(), null);
		model.addAttribute("joinForm", joinFormVO);

		mngrCodeVO.setSchCode("memstatuscd");
		model.addAttribute("statusCodeList", mngrCodeService.getMngrCodeList(mngrCodeVO));
		model.addAttribute("result", result);
		model.addAttribute("searchVO", searchVO);

		mngrCodeVO = new MngrCodeVO();
		mngrCodeVO.setSchCode("V015");//패스워드 질문
		model.addAttribute("pwdQuest", mngrCodeService.getMngrCodeList(mngrCodeVO));

		mngrCodeVO.setSchCode("V073");//관심사업
		model.addAttribute("concerns", mngrCodeService.getMngrCodeList(mngrCodeVO));

		mngrCodeVO.setSchCode("V007");//주소.
		model.addAttribute("addr", mngrCodeService.getMngrCodeList(mngrCodeVO));

		CominfoVO cominfo = new CominfoVO();
		cominfo.setId(searchVO.getId());

		cominfo = cominfoService.selectCominfoViewById(cominfo);

		model.addAttribute("cominfo", cominfo);
		/* 개인정보 조회 로그 기록 */
		if (request.getParameter("reason") != null) {
			EgovMap egovMap = CommUtil.getParameterEMap(request);
			egovMap.put("inqireId", CommUtil.getMngrMemId());
			egovMap.put("inqireNm", CommUtil.getMngrSessionVO().getName());
			egovMap.put("inqireIp", CommUtil.getClientIP(request));
			egovMap.put("regId", CommUtil.getMngrMemId());
			egovMap.put("mberId", request.getParameter("id"));
			egovMap.put("reason", request.getParameter("reason"));
			mngrPersnInfoLogService.regPersninfoLogProc(egovMap);
		}

		return "itgcms/mngr/member/company_edit";
	}
	/**
	 * 비밀번호 초기화
	 * @param memberVO
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/_mngr_/member/company_edit_initPass.do", method = RequestMethod.POST)
	public String mngrManagerInitPass(@ModelAttribute("memberVO") MemberVO memberVO, ModelMap model, HttpServletRequest request) {
		String url = request.getRequestURL().toString();
		memberVO.setSchOpt1(url);
		cominfoService.updateMemberInitPass(memberVO);
		return CommUtil.doComplete(model, "완료", "비밀번호가 초기화 되었습니다.", "location.href='company_edit.do?" + memberVO.getQuery() + "'");
	}

	/**
	 * 기업회원정보 삭제
	 * @param memberVO
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/_mngr_/member/company_delete_proc.do", method = RequestMethod.POST)
	public String mngrMemberDelProc(@ModelAttribute("memberVO") MemberVO memberVO, ModelMap model) {
		memberService.deleteMemberProc(memberVO);
		return CommUtil.doComplete(model, "완료", "삭제 되었습니다.", "location.href='company_list.do?" + memberVO.getQuery() + "'");
	}

	/**
	 * 기업회원정보 일괄 삭제
	 * @param memberVO
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/_mngr_/member/company_delete_chkProc.do", method = RequestMethod.POST)
	public String mngrMemberChkDelProc(@ModelAttribute("memberVO") MemberVO memberVO, ModelMap model) {
		memberService.deleteChkMemberProc(memberVO);
		return CommUtil.doComplete(model, "완료", "삭제 되었습니다.", "location.href='company_list.do?" + memberVO.getQuery() + "'");
	}


}