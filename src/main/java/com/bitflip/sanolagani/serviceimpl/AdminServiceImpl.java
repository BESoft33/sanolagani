package com.bitflip.sanolagani.serviceimpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.bitflip.sanolagani.document.service.ExtractTablesFromPDF;
import com.bitflip.sanolagani.models.Company;
import com.bitflip.sanolagani.models.RefundRequestData;
import com.bitflip.sanolagani.models.UnverifiedCompanyDetails;
import com.bitflip.sanolagani.models.User;
import com.bitflip.sanolagani.repository.CompanyRepo;
import com.bitflip.sanolagani.repository.RefundRequestRepo;
import com.bitflip.sanolagani.repository.UnverifiedCompanyRepo;
import com.bitflip.sanolagani.repository.UserRepo;
import com.bitflip.sanolagani.service.AdminService;

@Service
public class AdminServiceImpl implements AdminService {
	private static final String character = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private static final int pwd_length = 10;

	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	UnverifiedCompanyRepo unverified_repo;
	@Autowired
	CompanyRepo company_repo;
	@Autowired
	UserRepo user_repo;
	@Autowired
	ExtractTablesFromPDF tablesFromPDF;
	@Autowired
	RefundRequestRepo refund_repo;
	
	
	private UnverifiedCompanyDetails unverified_details;
	List<Company> moneyList = new ArrayList<>();

	@Override
	public void saveUnverifiedCompany(UnverifiedCompanyDetails un_company) {
		this.unverified_details = un_company;
		unverified_repo.save(un_company);
	}

	@Override
	public List<UnverifiedCompanyDetails> fetchAll() {
		List<UnverifiedCompanyDetails> details = unverified_repo.findAll();
		return details;
	}

	@Override
	public void deleteData(int id) {
		UnverifiedCompanyDetails details = unverified_repo.getById(id);
		String pdf_file_name = details.getFilename();
		String path = "../sanolagani/src/main/resources/static/unverified_details/";

		String pdf_path = path + pdf_file_name;
		File pdf_file = new File(pdf_path);
		pdf_file.delete();

		String pan_filename = details.getPan_image_name();
		File pan_file = new File(path + pan_filename);
		pan_file.delete();

		String cit_frontname = details.getCitizenship_fname();
		File cit_front = new File(path + cit_frontname);
		cit_front.delete();

		String cit_backname = details.getCitizenship_bname();
		File cit_back = new File(path + cit_backname);
		cit_back.delete();

		String image_name = details.getImage();
		File image = new File(path + image_name);
		image.delete();
		unverified_repo.deleteById(id);
	}

	@Override
	public void saveVerifiedCompany(int id, Company company, User user) {
		String plain_password = generatePassword();
		String encodedPassword = encodePassword(plain_password);
		unverified_details = unverified_repo.getById(id);
		sendPasswordEmail(unverified_details.getEmail(), plain_password);// sending password email after regisrtating
		user.setFname(unverified_details.getFname());
		user.setLname(unverified_details.getLname());
		company.setCompanyname(unverified_details.getCompanyname());
		user.setEmail(unverified_details.getEmail());
		user.setPhnum(unverified_details.getPhnum());
		company.setSector(unverified_details.getSector());
		company.setWebsiteurl(unverified_details.getWebsiteurl());
		company.setPreviouslyraisedcapital(unverified_details.getRaisedcapital());
		company.setPrice_per_share(unverified_details.getPrice_per_share());
		company.setTimespanforraisingcapital(unverified_details.getTimespanforraisingcapital());
		company.setFilename(unverified_details.getFilename());
		company.setPan_image_name(unverified_details.getPan_image_name());
		company.setCitizenship_fname(unverified_details.getCitizenship_fname());
		company.setCitizenship_bname(unverified_details.getCitizenship_bname());
		company.setMaximum_quantity(unverified_details.getMaximum_quantity());
		company.setImage(unverified_details.getImage());
		company.setStatus("raising");
		company.setPwd_change("false");
		user.setAddress(unverified_details.getAddress());
		user.setPassword(encodedPassword);
		user.setRole(company.getRole());
		// user_repo.save(user);
		company.setUser(user);
		company_repo.save(company);
		try {
			int company_id = transferUploadedFile(company);
			System.out.println("----------------------------------------------------------------------------------------");
			System.out.println("file transfered.\n Extracting tables from pdf for company " + company.getId()
					+ " and company name " + company.getCompanyname() + " .");
		System.out.println("----------------------------------------------------------------------------------------");
	
			tablesFromPDF.extractAllTables(company_repo.getReferenceById(company_id));
			System.out.println("tables extracted.");
		} catch (IOException e) {
			e.printStackTrace();
		}

		deleteData(id);
	}

	public int transferUploadedFile(Company company) throws IOException {
		// for pdf file
		List<Company> companylist = company_repo.findAll();
		int company_id = 0;
		
		for (Company companies : companylist) {
			if (companies.getUser().getEmail().equals(company.getUser().getEmail())) {
				company_id = companies.getId();
				break;
			}
		}

		File makingdir = new File("../sanolagani/src/main/resources/documents/" + company_id);
		String sourcepath = "../sanolagani/src/main/resources/static/unverified_details/";
		String pdf_name = company.getFilename();
		String cit_frontname = company.getCitizenship_fname();
		String cit_backname = company.getCitizenship_bname();
		String pan_name = company.getPan_image_name();
		String image_name = company.getImage();
		makingdir.mkdir();

		String destinationpath = "../sanolagani/src/main/resources/documents/" + company_id + "/";

		// for pdf file
		Path source_pdf_path = Path.of(sourcepath + pdf_name);
		Path pdfdestinationpath = Path.of(destinationpath + pdf_name);
		Files.copy(source_pdf_path, pdfdestinationpath, StandardCopyOption.REPLACE_EXISTING);

		Path source_citf_path = Path.of(sourcepath + cit_frontname);
		Path citf_destinationpath = Path.of(destinationpath + cit_frontname);
		Files.copy(source_citf_path, citf_destinationpath, StandardCopyOption.REPLACE_EXISTING);

		Path source_citb_path = Path.of(sourcepath + cit_backname);
		Path citb_destinationpath = Path.of(destinationpath + cit_backname);
		Files.copy(source_citb_path, citb_destinationpath, StandardCopyOption.REPLACE_EXISTING);

		Path source_pan_path = Path.of(sourcepath + pan_name);
		Path pandestinationpath = Path.of(destinationpath + pan_name);
		Files.copy(source_pan_path, pandestinationpath, StandardCopyOption.REPLACE_EXISTING);

		Path source_image_path = Path.of(sourcepath + image_name);
		Path imagedestinationpath = Path.of(destinationpath + image_name);
		Files.copy(source_image_path, imagedestinationpath, StandardCopyOption.REPLACE_EXISTING);
		return company_id;

	}

	public static String generatePassword() {
		StringBuilder sb = new StringBuilder();
		SecureRandom random = new SecureRandom();

		for (int i = 0; i < pwd_length; i++) {
			int randomIndex = random.nextInt(character.length());
			sb.append(character.charAt(randomIndex));
		}

		return sb.toString();
	}

	public static String encodePassword(String plainPassword) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String encoded = encoder.encode(plainPassword);
		boolean isPasswordMatches = encoder.matches(plainPassword, encoded);
		return encoded;
	}

	public void sendPasswordEmail(String to, String password) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject("Company Registered Sucessfully");
		message.setText("your company is sucessfully registered and the authentication details is email:" + to
				+ " password:" + password + ". Regards:seetal raya from sanolagani project");
		mailSender.send(message);
	}

	@Override
	public List<Company> getAllCompany() {
		List<Company> companylist = company_repo.findAll();

		return companylist;
	}

	@Override
	public boolean saveAdmin(User user,String email) {
		String plain_password = generatePassword();
		String encodedPassword = encodePassword(plain_password);
		sendAdminEmail(email, plain_password);// sending password email after regisrtating
        user.setPassword(encodedPassword);
        user.setRole("ADMIN");
        user_repo.save(user);
		return true;
	}

	public void sendAdminEmail(String email, String plain_password) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo("raayaseetal@gmail.com");
		message.setSubject("Admin Registered Sucessfully");
		message.setText("The authentication details for admin is :" + email
				+ " password:" + plain_password + ". Regards:seetal raya from sanolagani project");
		mailSender.send(message);
	}

	@Override
	public void getRefundApprove(int id,String status) {
		RefundRequestData refund = refund_repo.getReferenceById(id);
		sendRefundEmail(refund,status);
		refund_repo.delete(refund);
		
	}

	public void sendRefundEmail(RefundRequestData refund, String status) {
         User user = refund.getUser();
         Company company = refund.getCompany();
        SimpleMailMessage message = new SimpleMailMessage();
        System.out.println(user.getEmail());
        String email =user.getEmail();
 		message.setTo(email);
 		message.setSubject("Refund Request");
 		if(status.equalsIgnoreCase("approve")&&refund.getQuantity()>0) {
 		message.setText("The request for the refund of amount:" + refund.getAmount()
 				+ " on:" + refund.getRefund_date_time() + " invested on company " +company.getCompanyname()
 				+" is approved. Regards:seetal raya from sanolagani project");
 		
 		}
 		if(status.equalsIgnoreCase("approve")&&refund.getQuantity()<0) {
 	 		message.setText("The request for the refund of amount:" + refund.getAmount()
 	 				+ " on:" + refund.getRefund_date_time() + " invested on collateral is approved. Regards:seetal raya from sanolagani project");
 	 		
 	 		}
 		if(status.equalsIgnoreCase("reject")) {
 	 		message.setText("The request for the refund of amount:" + refund.getAmount()
 	 				+ " on:" + refund.getRefund_date_time() + " is rejected. Regards:seetal raya from sanolagani project");
 	 		
 		
		
	}
 		mailSender.send(message);
	}	
	
	
}