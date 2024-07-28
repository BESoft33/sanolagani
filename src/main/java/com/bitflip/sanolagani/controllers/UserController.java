package com.bitflip.sanolagani.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bitflip.sanolagani.models.Collateral;
import com.bitflip.sanolagani.models.Company;
import com.bitflip.sanolagani.models.Feedback;
import com.bitflip.sanolagani.models.Investment;
import com.bitflip.sanolagani.models.RefundRequestData;
import com.bitflip.sanolagani.models.Transaction;
import com.bitflip.sanolagani.models.User;
import com.bitflip.sanolagani.repository.CompanyRepo;
import com.bitflip.sanolagani.repository.FeedbackRepo;
import com.bitflip.sanolagani.repository.InvestmentRepo;
import com.bitflip.sanolagani.repository.UserRepo;
import com.bitflip.sanolagani.serviceimpl.UserServiceImpl;

import java.util.List;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Controller
public class UserController {
	@Autowired
	UserRepo userrepo;
	@Autowired
	InvestmentRepo invest_repo;
	@Autowired
	CompanyRepo company_repo;
	@Autowired
	UserServiceImpl userservice;
	@Autowired
	AdminController admin_controller;
	@Autowired
	FeedbackRepo feedback_repo;

	
	@GetMapping("/user/dashboard")
	public String myDashboard(Model model) {
		User user = getCurrentUser();
		List<Investment> investment_list = user.getInvestments();
		Collateral collateral = user.getCollateral();
		if (collateral == null) {
			model.addAttribute("totalcollateralamount", 0);
		} else {
			model.addAttribute("totalcollateralamount", collateral.getCollateral_amount());

		}
		if (investment_list != null) {
			List<Object> totalsharelist = new ArrayList<>();
			List<String> companynamelist = new ArrayList<>();
			List<Object> totalamountlist = new ArrayList<>();
			List<Object[]> investmentlist = invest_repo.getTotalSharesPerCompany(user.getId());
			for (Object[] obj : investmentlist) {
				String companyname = company_repo.getCompanyName(obj[0]);
				companynamelist.add(companyname);
				totalsharelist.add(obj[1]);
				totalamountlist.add(obj[2]);
			}

			double totalInvestment = investment_list.stream()
					.mapToDouble(Investment::getAmount)
					.sum();
			int size = investmentlist.size();
			int totalshare = investment_list.stream()
					.mapToInt(Investment::getQuantity).sum();
			model.addAttribute("id", user.getId());
			model.addAttribute("totalshare", totalshare);
			model.addAttribute("size", size);
			model.addAttribute("user", user.getFname());
			model.addAttribute("totalInvestment", totalInvestment);
			model.addAttribute("totalsharelist", totalsharelist);
			model.addAttribute("companynamelist", companynamelist);
			model.addAttribute("totalamountlist", totalamountlist);
			return "dashboard";
		}
		return "dashboard";
	}
	
	@GetMapping("/user/riskinfo")
	public String riskInformation(Model model) {

		Map<String, Double> sectorInvestments = new HashMap<>();
		User user = getCurrentUser();

		List<Investment> investment_list = user.getInvestments();

		for (Investment investment : investment_list) {
			String sector = investment.getCompany().getSector();
			double amount = investment.getAmount();
			sectorInvestments.put(sector, sectorInvestments.getOrDefault(sector, 0.0) + amount);

		}

		// Calculate the total investment amount
		double totalInvestment = investment_list.stream()
				.mapToDouble(Investment::getAmount)
				.sum();

		// Find the maximum investment amount in a single sector
		double maxSectorInvestment = sectorInvestments.values().stream()
				.max(Double::compareTo)
				.orElse(0.0);

		// Calculate the diversification percentage
		double diversificationPercentage = (maxSectorInvestment / totalInvestment) * 100;

		// Determine the diversification level based on the percentage
		int diversificationLevel;
		if (diversificationPercentage >= 50.0) {
			diversificationLevel = 1;
		} else if (diversificationPercentage <= 2.0) {
			diversificationLevel = 10;
		} else {
			double range = 48.0 / 9;
			diversificationLevel = 10 - (int) Math.ceil((diversificationPercentage - 2.0) / range);
		}

		// for line graph
		if (investment_list != null) {
			List<String> companynamelist = new ArrayList<>();
			List<Object> totalamountlist = new ArrayList<>();
			List<Object[]> investmentlist = invest_repo.getTotalSharesPerCompany(user.getId());
			for (Object[] obj : investmentlist) {
				String companyname = company_repo.getCompanyName(obj[0]);
				companynamelist.add(companyname);
				totalamountlist.add(obj[2]);
			}
            model.addAttribute("id",user.getId());
			model.addAttribute("diversificationlevel", diversificationLevel);
			model.addAttribute("user", user.getFname());
			model.addAttribute("companynamelist", companynamelist);
			model.addAttribute("totalamountlist", totalamountlist);
			return "diversificationgraph";
		}

		return "diversificationgraph";
	}
	
	@GetMapping("/user/loadfund")
	public String loadCollateral(Model model) {
		User user = getCurrentUser();
		Collateral collateral = user.getCollateral();
		if (collateral == null) {
			model.addAttribute("totalcollateralamount", 0);
		} else {
			model.addAttribute("totalcollateralamount", collateral.getCollateral_amount());

		}
		model.addAttribute("id", user.getId());
		return "load_fund";
	}
	
	@GetMapping("/user/collateralsummery/{id}")
	public String getCollateralHistory(@PathVariable("id") int id,Model model) {
		List<Transaction>  transactionlist= userservice.getCollateralSummery(id);
		if(transactionlist ==null) {
			return "redirect:/user/dashboard";
		}
		User user = userrepo.getReferenceById(id);
		model.addAttribute("id", user.getId());
		model.addAttribute("name", user.getFname());
		model.addAttribute("transactionlist", transactionlist);
		return "Collateral_summary";
	}
	@GetMapping("/user/fundhistory/{id}")
	public String getAllFundHistory(@PathVariable("id") int id,Model model) {
		List<Transaction> transactionlist = userservice.getFundHistory(id);
		if (transactionlist == null) {
			return "redirect:/dashboard";
		}
		User user = userrepo.getReferenceById(id);
		model.addAttribute("id", user.getId());
		model.addAttribute("name", user.getFname());
		model.addAttribute("transactionlist", transactionlist);
		return "fundhistory";
	}
	
	
	@GetMapping("/user/refund/collateral")
	public String getCollateralRefund(Model model) {
		User user = getCurrentUser();
		Collateral collateral = user.getCollateral();
		if (collateral == null) {
			model.addAttribute("totalcollateralamount", 0);
		} else {
			model.addAttribute("totalcollateralamount", collateral.getCollateral_amount());

		}
		model.addAttribute("id", user.getId());
		model.addAttribute("user", user);
		model.addAttribute("userphone", user.getPhnum());
		return "refund";
	}
	
	@PostMapping("/user/refundCollateral")
	public String refundProcessing(@RequestParam("amount") double amount,RefundRequestData refundrequest) {
		User user = getCurrentUser();
		boolean result = userservice.processRefundCollateralRequest(-1, amount,refundrequest, user);
		if(result) {
			return "redirect:/user/dashboard";
		}
		
		return "redirect:/user/refund/collateral";
	}
	
	
	
	
	@GetMapping("/user/tables/refund/{id}")
	public String refundInvestment(@PathVariable("id") int id,RefundRequestData refundrequest) {
		User user = getCurrentUser();
		boolean result = userservice.processRefundRequest(id,refundrequest,user);
		if(result) {
		return "redirect:/user/investmenthistory";
	}
		return "redirect:/user/dashboard";
	}
	
	@GetMapping("/user/investmenthistory")
	public String getInvestmentHistory(Model model) {
		User user = getCurrentUser();
		List<Investment> investmentlist = user.getInvestments();
		model.addAttribute("id", user.getId());

		if (investmentlist.isEmpty()) {
			return "redirect:/user/dashboard";
		}
		for (Investment investment : investmentlist) {
			userservice.changeStatus(investment);
		}
		model.addAttribute("name", user.getFname());
		model.addAttribute("investmentlist", investmentlist);
		return "investment-details";
	}
	
	
	@GetMapping("/user/myinfo")
	public String getUserDetails(Model model) {
		User user = getCurrentUser();
		double totalinvestedamount = invest_repo.getTotalInvestedAmount(user.getId());
		model.addAttribute("id", user.getId());
		model.addAttribute("user", user);
		model.addAttribute("totalInvestment", totalinvestedamount);
		return "my_information";
	}

	@GetMapping("/user/myinvestment")
	public String getMyInvestment(Model model) {
		User user = getCurrentUser();
		Map<String, Integer> totalUsersInvestedMap = new HashMap<>();
		Map<String, Integer> remainingdaysmap = new HashMap<>();
		Map<String, Integer> totalApplyShareMap = new HashMap<>();
		Set<String> sectorlist = new HashSet<>();
		Set<Company> companylist = new HashSet<>();
		List<Investment> myinvestmentlist = invest_repo.findAllByUser_id(user.getId());

		for (Investment investment : myinvestmentlist) {
			companylist.add(investment.getCompany());
		}

		for (Company company : companylist) {
			totalUsersInvestedMap.put(company.getCompanyname(), admin_controller.getTotalNumberOfUserInvested(company));
			remainingdaysmap.put(company.getCompanyname(), calculateRemainingDays(company));
			totalApplyShareMap.put(company.getCompanyname(), admin_controller.getTotalNumberOfShareApplied(company));
			sectorlist.add(company.getSector());
		}
		model.addAttribute("path", "investment");
		model.addAttribute("id", user.getId());
		model.addAttribute("sectorlist", sectorlist);
		model.addAttribute("totalUsersInvestedMap", totalUsersInvestedMap);
		model.addAttribute("remainingdaysmap", remainingdaysmap);
		model.addAttribute("totalApplyShareMap", totalApplyShareMap);
		model.addAttribute("companies", companylist);
		return "company-list";
	}

	@GetMapping("/user/edit/details")
	public String getUserDetailsPage(Model model) {
		User user = getCurrentUser();

		model.addAttribute("user", user);
		model.addAttribute("id", user.getId());
		return "edit_userprofile";
	}

	@PostMapping("/user/edit/savedetails")
	public String saveEditDetails(@ModelAttribute("user") User user) {

		User login_user = getCurrentUser();
		User fetch_user = userrepo.getReferenceById(login_user.getId());
		fetch_user.setAddress(user.getAddress());
		fetch_user.setEmail(user.getEmail());
		fetch_user.setFname(user.getFname());
		fetch_user.setLname(user.getLname());
		fetch_user.setPhnum(user.getPhnum());
		userrepo.save(fetch_user);
		System.out.println("save user");
		
		return "redirect:/user/myinfo";
	}
	@PostMapping("/user/feedback/{id}")
	public String postFeedback(@PathVariable("id") Integer id,Feedback feedback,@RequestParam("feedbacktext") String feedbacktext) {
		
		Company company = company_repo.getReferenceById(id);
		feedback.setCompany(company);
		feedback.setUser(getCurrentUser());
		feedback.setFeedbacktext(feedbacktext);
		feedback_repo.save(feedback);
		
		return "redirect:/user/company/"+id;
	}
	
	

	
	public User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			// Assuming you have a UserRepository to retrieve the user by username
			return userrepo.findByEmail(userDetails.getUsername());
		}
		return null; // Or handle the case where the user is not authenticated
	}

	public int calculateRemainingDays(Company company) {
		LocalDateTime currentDateTime = LocalDateTime.now();

		LocalDateTime registrationDateTime = company.getCreated(); // Replace with your own logic to get the
																	// registration date and time
		String time = company.getTimespanforraisingcapital();
		String[] timespansplit = time.split(" ", 2);
		int timespan = Integer.parseInt(timespansplit[0]);
		LocalDateTime endDateTime = registrationDateTime.plusDays(timespan);
		int remainingDays = (int) ChronoUnit.DAYS.between(currentDateTime, endDateTime);
		return remainingDays;

	}

}
