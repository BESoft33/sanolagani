package com.bitflip.sanolagani.controllers;

import com.bitflip.sanolagani.models.BoardMembers;
import com.bitflip.sanolagani.models.Company;
import com.bitflip.sanolagani.models.CompanyArticles;
import com.bitflip.sanolagani.models.CompanyFile;
import com.bitflip.sanolagani.models.Investment;
import com.bitflip.sanolagani.models.Notification;
import com.bitflip.sanolagani.models.TrafficData;
import com.bitflip.sanolagani.models.User;
import com.bitflip.sanolagani.models.Watchlist;
import com.bitflip.sanolagani.repository.*;
import com.bitflip.sanolagani.serviceimpl.RecommendationInitializer;
import com.bitflip.sanolagani.serviceimpl.SentimentPreprocessor;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.LinkedHashMap;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bitflip.sanolagani.controllers.AdminController;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class CompanyDetailsController {
	@Autowired
	CompanyRepo companyRepo;
	@Autowired
	UserController usercontroller;
	@Autowired
	InvestmentRepo investrepo;
	@Autowired
	UserRepo userrepo;
	@Autowired
	TrafficDataRepo trafficrepo;
	@Autowired
	NotificationRepo notificationrepo;

	@Autowired
	CompanyArticlesRepo articlesRepo;

	@Autowired
	HomeController homecontroller;
	@Autowired
	CompanyFileRepo cmprepo;
	@Autowired
	private SentimentPreprocessor pre;
	@Autowired
	RecommendationInitializer recommedationinit;

	@Autowired
	AdminController admin_controller;

	@Autowired
	WatchlistRepo watchlistrepo;

	@Autowired
	BoardMembersRepo boardMembersRepo;

	@Autowired
	CompanyFileRepo companyFileRepo;

	@GetMapping("/user/company")
	public String getAllCompany(Model model) {
		List<Company> companylist = companyRepo.findAll();
		if (companylist.isEmpty()) {
			return "company-list";
		}
		Map<String, Integer> totalUsersInvestedMap = new HashMap<>();
		Map<String, Integer> remainingdaysmap = new HashMap<>();
		Map<String, Integer> totalApplyShareMap = new HashMap<>();
		Set<String> sectorlist = new HashSet<>();

		for (Company company : companylist) {
			totalUsersInvestedMap.put(company.getCompanyname(), admin_controller.getTotalNumberOfUserInvested(company));
			remainingdaysmap.put(company.getCompanyname(), homecontroller.calculateRemainingDays(company));
			totalApplyShareMap.put(company.getCompanyname(), admin_controller.getTotalNumberOfShareApplied(company));

			sectorlist.add(company.getSector());
		}

		model.addAttribute("sectorlist", sectorlist);
		model.addAttribute("totalUsersInvestedMap", totalUsersInvestedMap);
		model.addAttribute("remainingdaysmap", remainingdaysmap);
		model.addAttribute("totalApplyShareMap", totalApplyShareMap);
		model.addAttribute("companies", companylist);
		return "company-list";
	}

	@GetMapping("/change_password")
	public String changePassword(Model model) {
		String email = (String) model.asMap().get("email");
		String failed = (String) model.asMap().get("failed");
		if(failed!=null) {
			model.addAttribute("error", "New and the old password cannot be same");
		}
		if (email == null) {
			User user = usercontroller.getCurrentUser();
			model.addAttribute("email", user.getEmail());
			return "changeinitialpassword";

		}
		model.addAttribute("email", email);
		return "changeinitialpassword";
	}

	@PostMapping("/change/initial/password")
	public String getInitialPasswordChanged(@RequestParam("email") String email,
			@RequestParam("oldpassword") String oldpass, @RequestParam("password") String newpassword,RedirectAttributes redirectAttributes) {

		User user = userrepo.findByEmail(email);
		String hashpwd = user.getPassword();
		String plainpwd = oldpass;
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		boolean isMatch = passwordEncoder.matches(plainpwd, hashpwd);

		boolean matchcurrentandold = passwordEncoder.matches(newpassword, hashpwd);
		if (matchcurrentandold) {
         redirectAttributes.addFlashAttribute("failed", "failed");
			return "redirect:/change_password";
		}

		if (isMatch && user.getRole().equalsIgnoreCase("COMPANY")) {
			user.setPassword(passwordEncoder.encode(newpassword));
			user.getCompany().setPwd_change("true");
			userrepo.save(user);
			return "redirect:/home";
		} else if (isMatch && user.getRole().equalsIgnoreCase("USER")) {
			user.setPassword(passwordEncoder.encode(newpassword));
			userrepo.save(user);
			return "redirect:/home";
		} else if (isMatch && user.getRole().equalsIgnoreCase("ADMIN")) {
			user.setPassword(passwordEncoder.encode(newpassword));
			userrepo.save(user);
			return "redirect:/home";
		} else {
			return "redirect:/change_password";
		}
	}

	@GetMapping("/user/company/{id}")
	public String getCompany(@PathVariable("id") Integer id, Model model, TrafficData trafficdata) {
		List<TrafficData> trafficdatalist = trafficrepo.findAll();
		boolean is_invested = true;
		LocalDateTime nowday = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE");
		String dayString = nowday.format(formatter);
		if (trafficdatalist.isEmpty()) {
			trafficdata.setCompanyid(id);
			trafficdata.setCount(1);
			trafficdata.setVisitmonth(dayString);
			trafficrepo.save(trafficdata);
		} else if (trafficdatalist.stream().anyMatch(trafficData -> trafficData.getCompanyid() == id)) {
			for (TrafficData traffic : trafficdatalist) {
				String day = traffic.getVisitmonth();
				if (dayString.equals(day) && traffic.getCompanyid() == id) {
					int count = traffic.getCount();
					count += 1;
					traffic.setCount(count);
					trafficrepo.save(traffic);
					break;
				} else if (!dayString.equals(day) && traffic.getCompanyid() == id) {
					trafficdata.setCompanyid(id);
					trafficdata.setCount(1);
					trafficdata.setVisitmonth(dayString);
					trafficrepo.save(trafficdata);
					break;
				}
			}
		} else {
			trafficdata.setCompanyid(id);
			trafficdata.setCount(1);
			trafficdata.setVisitmonth(dayString);
			trafficrepo.save(trafficdata);

		}

		String status = "on limit";
		User user = usercontroller.getCurrentUser();
		Company company = companyRepo.getReferenceById(id);
		List<CompanyArticles> allArticles = articlesRepo.findByCompanyId(id);
		model.addAttribute("articles", allArticles);
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime created_date = company.getCreated();
		String time = company.getTimespanforraisingcapital();
		String[] timespansplit = time.split(" ", 2);
		int timespan = Integer.parseInt(timespansplit[0]);

		Integer numberofshare_peruser = investrepo.getTotalQuantityByUserAndCompany(user.getId(), company.getId());
		if (numberofshare_peruser == 0) {
			is_invested = false;
		}
		if (company.getMaximum_quantity() <= numberofshare_peruser) {
			status = "limit reached";
		} else if (now.isAfter(created_date.plusDays(timespan))) {
			status = "time finish";

		}
		List<BoardMembers> boardMembers = boardMembersRepo.findAllByCompany(company);

		List<Watchlist> watchlist = watchlistrepo.findByUserId(user.getId());
		List<Integer> companyids = new ArrayList<>();
		boolean iswatchlistedblank = true;
		if (!watchlist.isEmpty()) {
			iswatchlistedblank = false;
			for (Watchlist watch : watchlist) {
				if (watch.getCompany_id() == id) {
					companyids.add(watch.getCompany_id());
				}
			}
			model.addAttribute("companyids", companyids);
		}
		model.addAttribute("is_invested", is_invested);
		model.addAttribute("iswatchlistedblank", iswatchlistedblank);
		model.addAttribute("status", status);
//		LocalDateTime formatteddate = company.getCreated();
//		String[] formattedDate = formatteddate.toString().split("T");
//		// GETTING THE DATE AND EXCLUDING THE TIME
//		String formatteddata = formattedDate[0];
//		model.addAttribute("formattedDate", formatteddata);
		model.addAttribute("company", company);
		model.addAttribute("boardMembers", boardMembers);
		
		//retriveing companyfile and  comparing id of company with fk id of companyfile and then retrieving comoany id and companyfile title
		List<CompanyFile>cmpfile=cmprepo.findAll();
		List<CompanyFile>cmpfiles=new ArrayList<>();
		for(CompanyFile file:cmpfile) {
			if(id.equals(file.getCompany().getId())){
			   cmpfiles.add(file);
			}
		}
		model.addAttribute("file", cmpfiles);
		return "company-info";
	}

	@GetMapping("/user/company/details/{id}")
	public String getInvestCompanyDetails(@PathVariable("id") int id, Model model) {
		Company company = companyRepo.getReferenceById(id);
		User user = usercontroller.getCurrentUser();
		Integer total_quantity_invested = investrepo.getTotalQuantityByUserAndCompany(user.getId(), company.getId());
		int remaining_quantity = company.getMaximum_quantity() - total_quantity_invested;
		model.addAttribute("company", company);
		model.addAttribute("remainingQuantity", remaining_quantity);
		return "details";
	}

	// company dashboard

	
	
	@GetMapping("/company/companydashboard")
	public String getDashboard(Model model) {
		User user = usercontroller.getCurrentUser();
		int id = user.getCompany().getId();
		LocalDateTime currentDate = LocalDateTime.now();
		Map<String, Integer> pastSixdays = new LinkedHashMap<>();
		Map<String, Double> pastSixdaysinvestmentamount = new LinkedHashMap<>();
		Map<String, Integer> totalUsersInvestedMap = new HashMap<>();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE");

		for (int i = 6; i >= 0; i--) {
			LocalDateTime month = currentDate.minusDays(i);
			String dayString = month.format(formatter).toLowerCase();
			pastSixdays.put(dayString, 0);
			pastSixdaysinvestmentamount.put(dayString, 0.0);
			totalUsersInvestedMap.put(dayString, 0);
		}
		List<TrafficData> trafficdatalist = trafficrepo.findAllByCompanyid(id);
		if (!trafficdatalist.isEmpty()) {
			for (TrafficData trafficData : trafficdatalist) {
				String visitMonth = trafficData.getVisitmonth().toLowerCase();
				int count = trafficData.getCount();
				pastSixdays.put(visitMonth, count);
			}

		}

		List<Investment> investmentlist = investrepo.findAllByCompany_id(id,currentDate.minusDays(6));
		if (!investmentlist.isEmpty()) {
			String dayname = "";
			for (Investment investment : investmentlist) {
				LocalDateTime investedtime = investment.getInvestment_date_time();
				LocalDate dates = investedtime.toLocalDate();
				Date date = Date.valueOf(dates);
				DayOfWeek dayofweek = investedtime.getDayOfWeek();
				if (!dayname.equalsIgnoreCase(dayofweek.name())) {
					dayname = dayofweek.name().toLowerCase();
					double amount = investrepo.getTotalInvestedByDate(date,id);
					pastSixdaysinvestmentamount.put(dayname, amount);
					int useracquistionnumber = investrepo.getTotalInvestedUserByDate(date,id);
					totalUsersInvestedMap.put(dayname, useracquistionnumber);

				}

			}

		}
		List<Double> investedamount = new ArrayList<>(pastSixdaysinvestmentamount.values());
		model.addAttribute("investedamount", investedamount);

		List<Integer> totaluseracquisition = new ArrayList<>(totalUsersInvestedMap.values());
		model.addAttribute("totaluseracquisition", totaluseracquisition);

		List<Integer> trafficvalues = new ArrayList<>(pastSixdays.values());
		model.addAttribute("trafficvalues", trafficvalues);

		List<String> labels = new ArrayList<>(pastSixdays.keySet());
		int count = notificationrepo.countByCompanyidAndIsreadFalse(id);
		model.addAttribute("unread", count);
		model.addAttribute("labels", labels);
		return "companydashboard";
	}


	@GetMapping("/company/notification")
	public String getNotification(Model model) {
		User user = usercontroller.getCurrentUser();
		int companyid = user.getCompany().getId();
		List<Notification> notificationlist = notificationrepo.findAllByCompanyid(companyid);
		markNotificationAsRead(notificationlist);
		Collections.reverse(notificationlist);

		model.addAttribute("notificationlist", notificationlist);
		return "notification";
	}

	public void markNotificationAsRead(List<Notification> notificationlist) {
		for (Notification notifications : notificationlist) {
			Optional<Notification> optionalNotification = notificationrepo.findById(notifications.getId());
			optionalNotification.ifPresent(notification -> {
				notification.setIsread(true);
				notificationrepo.save(notification);
			});
		}
	}

	@GetMapping("/company/investment/details")
	public String getCompanyInvestmentDetails(Model model) {
		Company company = usercontroller.getCurrentUser().getCompany();
		List<Investment> investmentlist = company.getInvestments();
		model.addAttribute("investmentlist", investmentlist);
		return "company_investment-details";
	}

	@GetMapping("/user/company/filter/{sector}/{path}")
	public String getCompanyBySector(@PathVariable(value = "sector", required = false) String sector,
	                                 @PathVariable(value = "path", required = false) String path ,Model model) {


		List<Company> companylist = new ArrayList<>();
		Set<String> sectorlist = new HashSet<>();
        User user = usercontroller.getCurrentUser();
		List<Company> sectorstatuslist = new ArrayList<>();
		List<Investment> myinvestmentlist = investrepo.findAllByUser_id(user.getId());

		if(path.equalsIgnoreCase("investment")){
		    for (Investment investment : myinvestmentlist) {
			companylist.add(investment.getCompany());
		}

	} else{
		 companylist = companyRepo.findAll();

}
		if (companylist.isEmpty()) {
			return "company-list";
		} else {
			for (Company company : companylist) {
				if (company.getSector().equalsIgnoreCase(sector)) {
					sectorstatuslist.add(company);
				}
			}
		}
        
		Map<String, Integer> totalUsersInvestedMap = new HashMap<>();
		Map<String, Integer> remainingdaysmap = new HashMap<>();
		Map<String, Integer> totalApplyShareMap = new HashMap<>();

		for (Company company : companylist) {
			totalUsersInvestedMap.put(company.getCompanyname(), admin_controller.getTotalNumberOfUserInvested(company));
			remainingdaysmap.put(company.getCompanyname(), homecontroller.calculateRemainingDays(company));
			totalApplyShareMap.put(company.getCompanyname(), admin_controller.getTotalNumberOfShareApplied(company));
			sectorlist.add(company.getSector());
		}
		model.addAttribute("sectorlist", sectorlist);
		model.addAttribute("totalUsersInvestedMap", totalUsersInvestedMap);
		model.addAttribute("remainingdaysmap", remainingdaysmap);
		model.addAttribute("totalApplyShareMap", totalApplyShareMap);
		model.addAttribute("companies", sectorstatuslist);
		return "company-list";
	}

	@GetMapping("company/overview")
	public String getCompanyOverview(Model model) {
		User user = usercontroller.getCurrentUser();
		Company company = user.getCompany();
		model.addAttribute("bod", company);
		return "company-overview";
	}

	@PostMapping("company/overview")
	public String postCompanyOverview(@RequestParam("title") String title, @RequestParam("description") String article ) {
		User user = usercontroller.getCurrentUser();
		Company company = user.getCompany();
		CompanyArticles companyArticles = new CompanyArticles();
		companyArticles.setAuthor(user);
		companyArticles.setTitle(title);
		companyArticles.setFull_text(article);
		companyArticles.setCompany(company);
		
		articlesRepo.save(companyArticles);
		return "company-overview";
	}

	@GetMapping("/company/management")
	public String getCompanyManagement() {
		User user = usercontroller.getCurrentUser();
		Company company = user.getCompany();
		List<BoardMembers> boardMembers = boardMembersRepo.findAllByCompany(company);
		return "company-management";
	}

	@PostMapping(value = "/company/management")
	public String postCompanyManagement(@RequestBody MultiValueMap<String, String> formData, BoardMembers member) {
		Integer fields = Integer.parseInt(formData.getFirst("numberOfInputFields"));
		Set<BoardMembers> members = new HashSet<>();
		User user = usercontroller.getCurrentUser();
		Company company = user.getCompany();
		for (int i = 0; i < fields; i++) {
			String firstName = formData.getFirst("firstName-" + i);
			String middleName = formData.getFirst("middleName-" + i);
			String lastName = formData.getFirst("lastName-" + i);
			String title = formData.getFirst("title-" + i);
			String socialLink = formData.getFirst("socialLink-" + i);

			member.setFirstName(firstName);
			member.setMiddleName(middleName);
			member.setLastName(lastName);
			member.setPosition(title);
			members.add(member);
			member.setCompany(company);
			member = new BoardMembers();
		}
		if (members != null) {
			boardMembersRepo.saveAll(members);
		}
		return "company-management";
	}

	@GetMapping("/company/all-articles")
	public String viewArticles(Model model) {
		User user = userrepo.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
		Company company = companyRepo.findById(user.getCompany().getId()).get();
		List<CompanyArticles> article_list = articlesRepo.findByCompanyId(company.getId());
		if (article_list.isEmpty()) {
			return "articles";
		}
		model.addAttribute("articles", article_list);
		return "articles";
	}

	@GetMapping("/company/reports")
	public String getCompanyDocuments(Model model) {
		User user = usercontroller.getCurrentUser();
		Company company = user.getCompany();

		List<CompanyFile> cfile = cmprepo.findAllData();
		List<CompanyFile> file = new ArrayList<>();
		for (CompanyFile companyFile : cfile) {
			if (user.getCompany().getId() == companyFile.getCompany().getId()) {
				System.out.println(user.getCompany().getId());
				System.out.println(companyFile.getCompany().getId());
				file.add(companyFile);
				
//			System.out.println(companyFile.getUploaddate());
//			System.out.println(companyFile.getFilename());
			}
		}
		model.addAttribute("companyreport", file);



		// List<CompanyFile> companyfiles = companyFileRepo.findAllByCompany(company);
		// if(companyfiles.size() > 0)
		// 	model.addAttribute("files", companyfiles);
		return "company-reports";
	}

	@PostMapping("/company/report")
	public String setCompanyOverview(Model model, @RequestParam("audit_report") MultipartFile audit_report,
			CompanyFile cmpfile) throws IllegalStateException, IOException {

		if (!audit_report.isEmpty()) {
			User user = usercontroller.getCurrentUser();
			cmpfile.setCompany(user.getCompany());
			cmpfile.setFilename(audit_report.getOriginalFilename());
			cmpfile.setFiletype("pdf");
            cmprepo.save(cmpfile);
			int id = user.getCompany().getId();
			String fileName = audit_report.getOriginalFilename();
			
			// Specify the directory where you want to save the PDF file
			String uploadDir = "../sanolagani/src/main/resources/documents/" + id + "/" + fileName;
//			Path baseDirPath = Paths.get(uploadDir);
//			if (!Files.exists(baseDirPath)) {
//                Files.createDirectories(baseDirPath);
//            }
			// Save the file to the specified directory
			File saveFile = new File(uploadDir);
			FileOutputStream outputStream = new FileOutputStream(saveFile);
			outputStream.write(audit_report.getBytes());
			System.out.println();
			outputStream.close();



			List<CompanyFile> cfile = cmprepo.findAllData();
			List<CompanyFile> file = new ArrayList<>();
			for (CompanyFile companyFile : cfile) {
				if (user.getCompany().getId() == companyFile.getCompany().getId()) {
					System.out.println(user.getCompany().getId());
					System.out.println(companyFile.getCompany().getId());
					file.add(companyFile);
					
	//			System.out.println(companyFile.getUploaddate());
	//			System.out.println(companyFile.getFilename());
				}
			}
			model.addAttribute("companyreport", file);



			return "redirect:/company/reports";
		}
		return "company-reports";
	}

	@GetMapping("/file/{fileName}/{id}")
	public ResponseEntity<InputStreamResource> displayPdf(@PathVariable String fileName, @PathVariable Integer id)
			throws IOException {
		// Logic to retrieve the PDF file from the file system
		String filePath = "../sanolagani/src/main/resources/documents/" + id + "/" + fileName;
		File file = new File(filePath);

		// Read the file content into a byte array
		byte[] content = new byte[(int) file.length()];
		FileInputStream fis = new FileInputStream(file);
		fis.read(content);
		fis.close();

		// Create a ByteArrayResource with the file content
		ByteArrayResource resource = new ByteArrayResource(content);

		// Set the response headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData(fileName, fileName);

		// Return the PDF file as a response entity
		return ResponseEntity.ok().headers(headers).body(new InputStreamResource(resource.getInputStream()));
	}
}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	


