package com.bitflip.sanolagani.serviceimpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import  java.util.Map;
import java.util.HashMap;
import com.bitflip.sanolagani.models.User;
import com.bitflip.sanolagani.models.Collateral;
import com.bitflip.sanolagani.models.Investment;
import com.bitflip.sanolagani.models.RefundRequestData;
import com.bitflip.sanolagani.models.Transaction;
import com.bitflip.sanolagani.repository.CollateralRepo;
import com.bitflip.sanolagani.repository.InvestmentRepo;
import com.bitflip.sanolagani.repository.RefundRequestRepo;
import com.bitflip.sanolagani.repository.TransactionRepo;
import com.bitflip.sanolagani.repository.UserRepo;
import com.bitflip.sanolagani.service.UserService;


@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepo userrepo;
	@Autowired
	TransactionRepo transaction_repo;
	@Autowired
	InvestmentRepo invest_repo;
	@Autowired
	RefundRequestRepo refund_repo;
	@Autowired 
	EmailService email_service;
	@Autowired
	CollateralRepo collateral_repo;
	
	ScheduledExecutorService executorService;
	
	Map<String,String> emailpwdmap = new HashMap<>();
    boolean result = true;
	List<Integer> idlist = new ArrayList<>();
	String encode_password="";
	
	
	@Override
	public void saveUser(User u) {
		userrepo.save(u);
	}

	@Override
	public boolean checkEmail(String email) {
		User user = userrepo.findByEmail(email);
		if(user==null) {
			return false;
		}
		return true;
	}

	@Override
	public void updatePassword(String email, String password,boolean result) {
	    this.result=result;
		List<User> userlist = userrepo.findAll();
		for(User user:userlist) {
			if(result && user.getEmail().equals(email)) {
				 this.encode_password=AdminServiceImpl.encodePassword(password);
				 user.setPassword(encode_password);
				 if(user.getRole().equals("COMPANY")) {
					user.getCompany().setPwd_change("true");
				 }
				  userrepo.save(user);	        
				email_service.sendChangePasswordMail(email);
			
			}
		}
	}
	
	

	public List<Transaction> getCollateralSummery(int id) {
          List<Transaction> transaction_list = new ArrayList<>();
		List<Transaction> transactionlist = transaction_repo.findAll();
		if(!transactionlist.isEmpty()) {
			for(Transaction transaction:transactionlist) {
				if(transaction.getUser().getId()==id&&transaction.getTransaction_type().equals("collateral")) {
					transaction_list.add(transaction);
				}
			}
			return transaction_list;
			
		}
		return null;
		
	}


	public List<Transaction> getFundHistory(int id) {
		List<Transaction> transaction_list = new ArrayList<>();
		List<Transaction> transactionlist = transaction_repo.findAll();
		if(!transactionlist.isEmpty()) {
			for(Transaction transaction:transactionlist) {
				if(transaction.getUser().getId()==id) {
					transaction_list.add(transaction);
				}
			}
			return transaction_list;
			
		}
		return null;
		
	}

	public boolean processRefundRequest(int id, RefundRequestData refundrequest, User user) {
		LocalDateTime datetime = LocalDateTime.now(); 
		Investment investment = invest_repo.getReferenceById(id);
		if(datetime.isBefore(investment.getRefundableUntil())) {
			refundrequest.setCompany(investment.getCompany());
			refundrequest.setAmount(investment.getAmount());
			refundrequest.setQuantity(investment.getQuantity());
			refundrequest.setRefund_date_time(datetime);
			refundrequest.setUser(user);
			List<Transaction> transactionlist = transaction_repo.findAll();
			for(Transaction transaction:transactionlist) {
				if(transaction.getId()==investment.getTransaction().getId()) {
						refundrequest.setTransaction(transaction);
				}
			}
			refund_repo.save(refundrequest);
			email_service.sendRefundMail(user,investment,investment.getCompany());
			invest_repo.deleteById(id);
			return true;
		}
		return false;
		
	}

	public void changeStatus(Investment investment) {
		LocalDateTime datetime = LocalDateTime.now();
		if(!datetime.isBefore(investment.getRefundableUntil())) {
			investment.setStatus("non refundable");
			invest_repo.save(investment);
		}
	}

	public boolean processRefundCollateralRequest(int id,double amount, RefundRequestData refundrequest, User user) {
		
		Collateral collateral = user.getCollateral();
		if(collateral.getCollateral_amount()>=amount) {
			double amounts = collateral.getCollateral_amount()-amount;
			collateral.setCollateral_amount(amounts);
			collateral_repo.save(collateral);
			saveRefundRequest(refundrequest,amount,user,collateral);
			return true;
		}
		
		
		
		return false;
	}

	public void saveRefundRequest(RefundRequestData refundrequest,double amount,User user,Collateral collateral) {
		Transaction transaction = collateral.getTransaction();
		RefundRequestData refunddatas = new  RefundRequestData();
		List<RefundRequestData> refunddatalist = refund_repo.findAll();
        boolean result = false;
			for(RefundRequestData refunddata: refunddatalist) {
				if(refunddata.getTransaction().getId()==transaction.getId()) {
					result = true;
					refunddatas=refunddata;
					break;
				}
			}
			if(result) {
				double amounts = refunddatas.getAmount();
				refunddatas.setAmount(amount+amounts);
				refund_repo.save(refunddatas);
			}else {
		
		LocalDateTime datetime = LocalDateTime.now(); 
		refundrequest.setAmount(amount);
		refundrequest.setCompany(null);
		refundrequest.setUser(user);
		refundrequest.setQuantity(-1);
		refundrequest.setTransaction(transaction);
		refundrequest.setRefund_date_time(datetime);
		refund_repo.save(refundrequest);
			}
	}

	

}
