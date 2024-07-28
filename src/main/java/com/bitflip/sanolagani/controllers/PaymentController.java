package com.bitflip.sanolagani.controllers;

import com.bitflip.sanolagani.models.Collateral;
import com.bitflip.sanolagani.models.Investment;
import com.bitflip.sanolagani.models.Notification;
import com.bitflip.sanolagani.models.Transaction;
import com.bitflip.sanolagani.models.User;
import com.bitflip.sanolagani.repository.CollateralRepo;
import com.bitflip.sanolagani.service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    @Autowired
    PaymentService paymentService;
    @Autowired
    CollateralRepo collaretal_repo;
    @Autowired
    UserController user_controller;

    @GetMapping("/verifypayment")
    public String verifyPayment(@RequestParam("token") String token,
            @RequestParam("amount") String amount,
            @RequestParam(value = "companyId") int companyid,
            @RequestParam(value = "remarks", required = false) String remarks,
            Transaction transaction, Investment investment, Collateral collateral, Notification notification)
            throws JsonProcessingException {
        Map<String, String> details = new HashMap<>();
        details.put("token", token);
        details.put("amount", amount);
        boolean result = paymentService.verifyPayment(details);
        if (result) {
            double amt = Double.parseDouble(amount);
            amt /= 100;
            amount = String.valueOf(amt);
            paymentService.saveTransactionDetails(token, amount, companyid, transaction, remarks, investment,
                    collateral, notification);
        }
        return "redirect:/user/dashboard";
    }

    @PostMapping("/paywithcollateral")
    public String payWithCollateral(@RequestParam(value = "companyId") int id,
            @RequestParam("amount") double amount,
            Transaction transaction,
            Investment investment,
            Collateral collaterals, Notification notification) {
        User user = user_controller.getCurrentUser();
    	Collateral collateral = user.getCollateral();
    	if(collateral.getCollateral_amount()>=amount) {
    	String amounts = String.valueOf(amount);
    	paymentService.saveTransactionDetails("collateral", amounts,id, transaction, 
    											"paying with collateral", investment, collaterals,notification);
    	
    	double amt = collateral.getCollateral_amount();
    	collateral.setCollateral_amount(amt-amount);
    	collaretal_repo.save(collateral);
    	return "redirect:/user/dashboard";
    	}
        return "redirect:/home";
    }

}
