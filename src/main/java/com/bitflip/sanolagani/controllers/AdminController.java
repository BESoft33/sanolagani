package com.bitflip.sanolagani.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.bitflip.sanolagani.models.Collateral;
import com.bitflip.sanolagani.models.Company;
import com.bitflip.sanolagani.models.Transaction;
import com.bitflip.sanolagani.models.UnverifiedCompanyDetails;
import com.bitflip.sanolagani.models.User;
import com.bitflip.sanolagani.models.Investment;
import com.bitflip.sanolagani.models.RefundRequestData;
import com.bitflip.sanolagani.repository.CollateralRepo;
import com.bitflip.sanolagani.repository.CompanyRepo;
import com.bitflip.sanolagani.repository.InvestmentRepo;
import com.bitflip.sanolagani.repository.RefundRequestRepo;
import com.bitflip.sanolagani.repository.TransactionRepo;
import com.bitflip.sanolagani.repository.UserRepo;
import com.bitflip.sanolagani.service.AdminService;
import java.math.BigInteger;

@Controller
public class AdminController {
	@Autowired
	AdminService admin_service;
	@Autowired
	private TransactionRepo transactionRepository;
	@Autowired
	private UserRepo userrepo;
	@Autowired
	private CompanyRepo companyrepo;
	@Autowired
	private InvestmentRepo investmentrepo;
	@Autowired
	private CollateralRepo collateralrepo;
	@Autowired
	private RefundRequestRepo refundrepo;
	@GetMapping("/admin/admindashboard")
	public String getAdminDashboardPage(Model model) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime onehourago = now.minusHours(24);
		List<Transaction> transactions = transactionRepository.findByTransactionDateBetween(onehourago, now);
		Map<String, Double> aggregatedData = aggregateData(transactions);

		List<String> keysList = new ArrayList<>(aggregatedData.keySet());
		List<Double> valuesList = new ArrayList<>(aggregatedData.values());

		Long usercount = userrepo.getTotalUsers();
		Long companycount = companyrepo.getTotalcompany();
		Long transactioncount = transactionRepository.getTotalTransaction();

		model.addAttribute("usercount", usercount);
		model.addAttribute("companycount", companycount);
		model.addAttribute("transactioncount", transactioncount);
		model.addAttribute("labels", keysList);
		model.addAttribute("transactionAmounts", valuesList);

		return "admindashboard";
	}

	@GetMapping("/admin/viewusers")
	public String getAllUsers(Model model) {
		List<User> user_list = new ArrayList<>();
		List<User> alluser_list = userrepo.findAll();
		if (alluser_list.isEmpty()) {

			return "viewuser";
		}
		for (User user : alluser_list) {

			if (user.getRole().equals("USER")) {
				user_list.add(user);
			}
		}
		model.addAttribute("userlist", user_list);
		return "viewuser";
	}

	@GetMapping("/admin/viewinvestment")

	public String getAllInvestment(Model model) {
		List<Investment> investment_list = investmentrepo.findAll();
		if (investment_list.isEmpty()) {
			return "viewinvestment";
		}
		model.addAttribute("investment_list", investment_list);
		return "viewinvestment";
	}

	@GetMapping("/admin/viewcollateral")

	public String getAllCollateral(Model model) {
		List<Collateral> collateral_list = collateralrepo.findAll();
		if (collateral_list.isEmpty()) {
			return "viewcollateral";
		}
		model.addAttribute("collateral_list", collateral_list);
		for (Collateral collateral : collateral_list) {
			collateral.getTransaction().getPayment_token();
		}
		return "viewcollateral";
	}

	@GetMapping("/admin/managecompany")
	public String getunverifiedcompany(Model model) {
		List<UnverifiedCompanyDetails> details = admin_service.fetchAll();
		if (details != null) {
			model.addAttribute("UnverfiedCompanies", details);
			return "managecompany";
		
		} else {
			return "redirect:/home";
		}
	}

	@GetMapping("/admin/managecompany/edit/{id}")
	public String deleteUnverifiedData(@PathVariable("id") String ids) {
		int id = Integer.parseInt(ids);
		admin_service.deleteData(id);
		return "redirect:/admin/managecompany";
	}

	@GetMapping("/admin/managecompany/edit/save/{id}")
	public String addVerifiedCompany(@PathVariable("id") String ids, Company company, User user) {
		int id = Integer.parseInt(ids);
		admin_service.saveVerifiedCompany(id, company, user);
		return "redirect:/admin/admindashboard";
	}

	@GetMapping("/admin/viewcompany")
	public String viewAllCompany(Model model) {
		List<Company> company_list = companyrepo.findAll();
		if (company_list.isEmpty()) {

			return "viewcompany";

		}
		model.addAttribute("company_list", company_list);
		return "viewcompany";
	}

	@GetMapping("/admin/reports")
	public String viewReportDetails(Model model) {
		List<Investment> invest_list = investmentrepo.findAll();
		if (invest_list.isEmpty()) {
			return "adminreport";
		}
		model.addAttribute("invest_list", invest_list);
		return "adminreport";
	}


	@GetMapping("/admin/refunddata")

	public String getRefundData(Model model) {
		List<RefundRequestData> refund_list = refundrepo.findAll();
		if (refund_list.isEmpty()) {
			return "viewrefunddata";
		}
		model.addAttribute("refund_list", refund_list);
		return "viewrefunddata";
	}

	@GetMapping("/admin/companystatistics")

	public String getCompanyStats(Model model) {
		List<Company> company_list = companyrepo.findAll();
		Map<String, Integer> totalUsersInvestedMap = new HashMap<>();
		Map<String, Integer> totalApplyShareMap = new HashMap<>();
		for (Company company : company_list) {
			totalUsersInvestedMap.put(company.getCompanyname(), getTotalNumberOfUserInvested(company));
			totalApplyShareMap.put(company.getCompanyname(), getTotalNumberOfShareApplied(company));

		}
		model.addAttribute("company_list", company_list);
		model.addAttribute("totalUsersInvestedMap", totalUsersInvestedMap);
		model.addAttribute("totalApplyShareMap", totalApplyShareMap);
		return "companysummary";
	}

	@GetMapping("/admin/companies/statisticsgraph")

	public String getStatisticsGraph(Model model) {
		List<Double> total_raised_share = new ArrayList<>();
		List<Integer> total_applied_share = new ArrayList<>();
		List<BigInteger> total_capital_offer = new ArrayList<>();
		List<Double> total_capital_raised = new ArrayList<>();
		List<String> companyname_list = new ArrayList<>();
		List<Company> company_list = companyrepo.findAll();
		if (company_list.isEmpty()) {
			return "companystatisticsgraph";
		}
		for (Company company : company_list) {
			BigInteger total_amounts = company.getPreviouslyraisedcapital();
			double total_amount = total_amounts.doubleValue();
			companyname_list.add(company.getCompanyname());
			total_raised_share.add(total_amount / company.getPrice_per_share());
			total_applied_share.add(getTotalNumberOfShareApplied(company));
			total_capital_offer.add(total_amounts);
			total_capital_raised.add(getTotalNumberOfShareApplied(company) * company.getPrice_per_share());
		}

		model.addAttribute("companyname_list", companyname_list);
		model.addAttribute("total_raised_share", total_raised_share);
		model.addAttribute("total_applied_share", total_applied_share);
		model.addAttribute("total_capital_offer", total_capital_offer);
		model.addAttribute("total_capital_raised", total_capital_raised);

		return "companystatisticsgraph";
	}
	
	@GetMapping("/admin/refund/{status}/{id}")
	public String getRefundApprove(@PathVariable("id") int id,@PathVariable(value="status") String status) {	
		admin_service.getRefundApprove(id,status);
		return "redirect:/admin/refunddata";
	}

	public static Map<String, Double> aggregateData(List<Transaction> transactions) {
		Map<String, Double> aggregatedData = new LinkedHashMap<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

		LocalDateTime currentTime = LocalDateTime.now();
		LocalDateTime startTime = currentTime.minusHours(24).withMinute(0).withSecond(0).withNano(0);

		while (startTime.isBefore(currentTime)) {
			final LocalDateTime currentHour = startTime;
			LocalDateTime endTime = startTime.plusHours(1);
			String label = startTime.format(formatter);
			double totalAmount = transactions.stream()
					.filter(transaction -> transaction.getTransaction_date_time().isAfter(currentHour)
							&& transaction.getTransaction_date_time().isBefore(endTime))
					.mapToDouble(Transaction::getAmount).sum();

			aggregatedData.put(label, totalAmount);

			startTime = endTime;
		}
		return aggregatedData;
	}

	
	

	public int getTotalNumberOfUserInvested(Company company) {
		List<Investment> investment_list = company.getInvestments();
		return (int) investment_list.stream().map(Investment::getUser).distinct().count();
	}

	public int getTotalNumberOfShareApplied(Company company) {
		List<Investment> investment_list = company.getInvestments();
		return (int) investment_list.stream().mapToInt(Investment::getQuantity).sum();
	}

}
