package com.bitflip.sanolagani.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.bitflip.sanolagani.models.Company;
import com.bitflip.sanolagani.models.User;
import com.bitflip.sanolagani.models.Watchlist;

import com.bitflip.sanolagani.repository.CompanyRepo;
import com.bitflip.sanolagani.repository.WatchlistRepo;

@Controller
public class WatchlistController {
	
	@Autowired
	CompanyRepo company_repo;
	@Autowired
	UserController usercontroller;
	@Autowired
	WatchlistRepo watchlist_repo;
	@Autowired
	AdminController admin_controller;
	@Autowired
	HomeController homecontroller; 

	@GetMapping("/user/company/addtowatchlist/{id}")
     public String addCompanyToWatchlist(@PathVariable("id") int id,Watchlist watchlist) {
		User user = usercontroller.getCurrentUser();
		watchlist.setUser(user);
		watchlist.setCompany_id(id);
		watchlist_repo.save(watchlist);
		return "redirect:/user/company/"+id;
	}
	
	   
    @GetMapping("/user/company/deletewatchlist/{id}")
      public String deleteWatchlist(@PathVariable("id") int id) {
    	    User user = usercontroller.getCurrentUser();
			Watchlist watchlist = watchlist_repo.findByCompanyId(id,user.getId());
			watchlist_repo.delete(watchlist);
			return "redirect:/user/company/"+id;
	}
	
	@GetMapping("/user/company/mywatchlist")
	public String getMyWatchlisted(Model model) {
		User user = usercontroller.getCurrentUser();

		List<Watchlist> watchlist = watchlist_repo.findByUserId(user.getId());
		if(watchlist.isEmpty()) {
			return "watchlist";
		}
		List<Company> companylist = new ArrayList<>();
		for(Watchlist watch:watchlist) {
		  int id = watch.getCompany_id();
		  Company company = company_repo.getReferenceById(id);
		  companylist.add(company);
		}
		 Map<String, Integer> totalUsersInvestedMap = new HashMap<>();
			Map<String,Integer> remainingdaysmap = new HashMap<>();
			Map<String, Integer> totalApplyShareMap = new HashMap<>();

	        for(Company company:companylist) {
				totalUsersInvestedMap.put(company.getCompanyname(), admin_controller.getTotalNumberOfUserInvested(company));
	            remainingdaysmap.put(company.getCompanyname(), homecontroller.calculateRemainingDays(company));
				totalApplyShareMap.put(company.getCompanyname(),admin_controller.getTotalNumberOfShareApplied(company));
	        }
	        model.addAttribute("totalUsersInvestedMap", totalUsersInvestedMap);
	        model.addAttribute("remainingdaysmap", remainingdaysmap);
	        model.addAttribute("totalApplyShareMap", totalApplyShareMap);
	        model.addAttribute("companies", companylist);
		return "watchlist";
	}
}
