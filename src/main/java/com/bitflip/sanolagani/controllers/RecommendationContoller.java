package com.bitflip.sanolagani.controllers;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.bitflip.sanolagani.models.Company;
import com.bitflip.sanolagani.serviceimpl.RecommendationInitializer;

@Controller
public class RecommendationContoller {
	@Autowired
    RecommendationInitializer recommedationinit;
	
	@GetMapping("/recommend")
	public String homePage(Model model) {
		
		//Optional<List<Company>> result = Optional.ofNullable(recommedationinit.getRecommendCompanies());
		//if (!result.isEmpty()) {
			List<Company> companylist = recommedationinit.getRecommendCompanies();
			
	//	}
		return "index";
	}

}
