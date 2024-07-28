package com.bitflip.sanolagani.controllers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bitflip.sanolagani.models.Company;
import com.bitflip.sanolagani.models.User;
import com.bitflip.sanolagani.repository.CompanyRepo;
import com.bitflip.sanolagani.service.AdminService;
import com.bitflip.sanolagani.serviceimpl.RecommendationInitializer;
import com.bitflip.sanolagani.serviceimpl.SentimentPreprocessor;
import com.bitflip.sanolagani.serviceimpl.UserServiceImpl;

@Controller
public class HomeController {
	@Autowired
	UserController usercontroller;

	@Autowired
	AdminService adminservice;
	@Autowired
	private CompanyRepo company_repo;
	@Autowired
	private SentimentPreprocessor pre;
	@Autowired
	RecommendationInitializer recommedationinit;
	@Autowired
	AdminController admin_controller;

	@Autowired
	UserServiceImpl userservice;

	@GetMapping({ "/", "/home" })
	public String homePage(Model model, RedirectAttributes redirectAttributes) {

		User user = usercontroller.getCurrentUser();
		if (user != null) {
			if (user.getRole().equals("COMPANY")) {
				if (user.getCompany().getPwd_change().equals("false")) {
					redirectAttributes.addFlashAttribute("email", user.getEmail());
					return "redirect:/change_password";
				} else {
					return "redirect:/company/companydashboard";
				}
			}
			if (user.getRole().equals("ADMIN")) {
				return "redirect:/admin/admindashboard";
			}
		}

		List<Company> company_list = company_repo.findAll();
		LocalDateTime now = LocalDateTime.now();
		if (company_list.isEmpty()) {
			return "index";
		}
		for (Company company : company_list) {
			LocalDateTime created_date = company.getCreated();
			String time = company.getTimespanforraisingcapital();
			String[] timespansplit = time.split(" ", 2);
			int timespan = Integer.parseInt(timespansplit[0]);

			if (company.getStatus().equals("raising") && now.isAfter(created_date.plusDays(timespan))) {
				company.setStatus("finish");
				company_repo.save(company);

			}

		}

		Map<String, Integer> totalUsersInvestedMap = new HashMap<>();
		Map<String, Integer> remainingdaysmap = new HashMap<>();
		Map<String, Integer> totalApplyShareMap = new HashMap<>();

		for (Company company : company_list) {
			totalUsersInvestedMap.put(company.getCompanyname(), admin_controller.getTotalNumberOfUserInvested(company));
			remainingdaysmap.put(company.getCompanyname(), calculateRemainingDays(company));
			totalApplyShareMap.put(company.getCompanyname(), admin_controller.getTotalNumberOfShareApplied(company));

		}

		List<Company> basedoncapital = company_repo.findAll();
		Optional<List<Company>> result = Optional.ofNullable(basedoncapital);

		Collections.sort(basedoncapital, Comparator.comparing(Company::getPreviouslyraisedcapital).reversed());

		List<Company> companybasedondate = company_repo.findAllCompanyBasesOnCreationalDates();
		Optional<List<Company>> result1 = Optional.ofNullable(companybasedondate);

		List<Company> diversifiedcompanylist = recommedationinit.getRecommendCompanies();

		List<Company> c_list = pre.getCompaniesWithGoodSentiment();

		if (result != null & result1 != null) {
			if(basedoncapital.size()>=3) {
    		    model.addAttribute("companybasedoncapital", basedoncapital.subList(0,6));
			}else {
   		        model.addAttribute("companybasedoncapital", basedoncapital);

			}
			if(companybasedondate.size()>=3) {
			    model.addAttribute("companybasedondate", companybasedondate.subList(0, 6));
			}else {
				model.addAttribute("companybasedondate", companybasedondate);

			}
			
			if(diversifiedcompanylist!=null&&diversifiedcompanylist.size()>=3) {
				model.addAttribute("diversifiedcompanylist", diversifiedcompanylist.subList(0, 6));

			}else {
				model.addAttribute("diversifiedcompanylist", diversifiedcompanylist);

			}
			if(c_list!=null&&c_list.size()>=3) {
				model.addAttribute("c_list", c_list.subList(0, 3));

			}else {
				model.addAttribute("c_list", c_list);

			}
			model.addAttribute("totalUsersInvestedMap", totalUsersInvestedMap);
			model.addAttribute("remainingdaysmap", remainingdaysmap);
			model.addAttribute("totalApplyShareMap", totalApplyShareMap);

			return "index";
		}

		return "index";
	}

	@GetMapping("/logout")
	public String logout(HttpServletRequest request) {
		request.getSession(false).invalidate();

		return "redirect:/home";
	}

	public int calculateRemainingDays(Company company) {
		LocalDateTime currentDateTime = LocalDateTime.now();

		LocalDateTime registrationDateTime = company.getCreated(); // Replace with your own logic to get the registration date and time
		String time = company.getTimespanforraisingcapital();
		String[] timespansplit = time.split(" ", 2);
		int timespan = Integer.parseInt(timespansplit[0]);
		LocalDateTime endDateTime = registrationDateTime.plusDays(timespan);
		int remainingDays = (int) ChronoUnit.DAYS.between(currentDateTime, endDateTime);
		return remainingDays;

	}

}
