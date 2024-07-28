package com.bitflip.sanolagani.models;

import java.math.BigInteger;
import java.time.LocalDateTime;
//import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import java.util.Set;
//
//import javax.persistence.CascadeType;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.*;


import org.hibernate.annotations.CreationTimestamp;
import org.springframework.stereotype.Component;

@Component
@Entity
@Table(name = "company")
public class Company implements java.io.Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	@Column
	private String companyname;
	@Column
	private String phnum;
	@Column
	private String sector;
	@Column
	private String websiteurl;
	@Column
	private BigInteger previouslyraisedcapital;
	@Column
	private double price_per_share;
	@Column
	private int minimum_quantity;
	@Column
	private String timespanforraisingcapital;
	@Column
	private String filename;
	@Column
	private String pan_image_name;
	@Column
	private String citizenship_fname;
	@Column
	private String citizenship_bname;
	@Column
	private String image;

	private String role;
	@Column
	private int maximum_quantity;
	@Column
	private String status;
	@Column
	private String pwd_change;

	@OneToMany(mappedBy = "company")
	private List<BoardMembers> boardmembers;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	@OneToMany(mappedBy = "company")
	private List<CompanyFile> companyfilelist;
	
	
	
	@OneToMany(mappedBy = "company")
	private List<CompanyArticles> companydetaileddescription;

	@OneToMany(mappedBy = "company")
	private List<Investment> investments;

	@OneToMany(mappedBy = "company")
	private List<RefundRequestData> refundrequest;

	@OneToMany(mappedBy = "company")
	private List<Feedback> feedbacklist;

	@CreationTimestamp
	private LocalDateTime created;
	
	public Company() {
		this.minimum_quantity = 1;
		this.role = "COMPANY";
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append(id);
		builder.append(", companyname=");
		builder.append(companyname);
		
		builder.append(", previouslyraisedcapital=");
		builder.append(previouslyraisedcapital);
		builder.append(", price_per_share=");
		builder.append(price_per_share);
		builder.append(", minimum_quantity=");
		builder.append(minimum_quantity);
		builder.append(", timespanforraisingcapital=");
		builder.append(timespanforraisingcapital);
		builder.append(", filename=");
		builder.append(filename);
		builder.append(", pan_image_name=");
		builder.append(pan_image_name);
		builder.append(", citizenship_fname=");
		builder.append(citizenship_fname);
		builder.append(", citizenship_bname=");
		return builder.toString();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRole() {
		return role;
	}

	
	public String getSector() {
		return sector;
	}

	public double getPrice_per_share() {
		return price_per_share;
	}

	public void setPrice_per_share(double price_per_share) {
		this.price_per_share = price_per_share;
	}

	public List<Investment> getInvestments() {
		return investments;
	}

	public int getMinimum_quantity() {
		return minimum_quantity;
	}

	public void setMinimum_quantity(int minimum_quantity) {
		this.minimum_quantity = minimum_quantity;
	}

	public void setInvestments(List<Investment> investments) {
		this.investments = investments;
	}

	public String getCompanyname() {
		return companyname;
	}

	public void setCompanyname(String companyname) {
		this.companyname = companyname;
	}



	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getWebsiteurl() {
		return websiteurl;
	}

	public void setWebsiteurl(String websiteurl) {
		this.websiteurl = websiteurl;
	}

	public BigInteger getPreviouslyraisedcapital() {
		return previouslyraisedcapital;
	}

	public void setPreviouslyraisedcapital(BigInteger previouslyraisedcapital) {
		this.previouslyraisedcapital = previouslyraisedcapital;
	}

	public String getTimespanforraisingcapital() {
		return timespanforraisingcapital;
	}

	public void setTimespanforraisingcapital(String timespanforraisingcapital) {
		this.timespanforraisingcapital = timespanforraisingcapital;
	}

	public String getPan_image_name() {
		return pan_image_name;
	}

	public void setPan_image_name(String pan_image_name) {
		this.pan_image_name = pan_image_name;
	}

	public String getCitizenship_fname() {
		return citizenship_fname;
	}

	public void setCitizenship_fname(String citizenship_name) {
		this.citizenship_fname = citizenship_name;
	}



	public String getCitizenship_bname() {
		return citizenship_bname;
	}

	public void setCitizenship_bname(String citizenship_bname) {
		this.citizenship_bname = citizenship_bname;
	}

	public int getMaximum_quantity() {
		return maximum_quantity;
	}

	public void setMaximum_quantity(int maximum_quantity) {
		this.maximum_quantity = maximum_quantity;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getPhnum() {
		return phnum;
	}

	public void setPhnum(String phnum) {
		this.phnum = phnum;
	}

	public List<Feedback> getFeedbacklist() {
		return feedbacklist;
	}

	public void setFeedbacklist(List<Feedback> feedbacklist) {
		this.feedbacklist = feedbacklist;
	}


	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	public List<RefundRequestData> getRefundrequest() {
		return refundrequest;
	}

	public void setRefundrequest(List<RefundRequestData> refundrequest) {
		this.refundrequest = refundrequest;
	}
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPwd_change() {
		return pwd_change;
	}

	public void setPwd_change(String pwd_change) {
		this.pwd_change = pwd_change;
	}

	public List<CompanyFile> getCompanyfilelist() {
		return companyfilelist;
	}

	public void setCompanyfilelist(List<CompanyFile> companyfilelist) {
		this.companyfilelist = companyfilelist;
	}

	public List<BoardMembers> getBoardmembers() {
		return boardmembers;
	}

	public void setBoardmembers(List<BoardMembers> boardmembers) {
		this.boardmembers = boardmembers;
	}

	public List<CompanyArticles> getCompanydetaileddescription() {
		return companydetaileddescription;
	}

	public void setCompanydetaileddescription(List<CompanyArticles> companydetaileddescription) {
		this.companydetaileddescription = companydetaileddescription;
	}

	


   
  
}