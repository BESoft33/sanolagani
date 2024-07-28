package com.bitflip.sanolagani.repository;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bitflip.sanolagani.models.Investment;
import com.bitflip.sanolagani.models.TrafficData;

public interface InvestmentRepo extends JpaRepository<Investment, Integer> {

@Query(value = "SELECT company_id,SUM(quantity),SUM(amount) AS total_shares\n"
		+ "FROM investment\n"
		+"where user_id =:id\n"
		+ "GROUP BY company_id\n"
		+" ",nativeQuery = true)
List<Object[]> getTotalSharesPerCompany(@Param("id")int id);

@Query("SELECT COALESCE(SUM(i.quantity),0) FROM Investment i WHERE i.company.id = :companyId AND i.user.id = :userId")
Integer getTotalQuantityByUserAndCompany(@Param("userId") int userId, @Param("companyId") int companyId);

@Query("SELECT COALESCE(SUM(i.amount),0) FROM Investment i WHERE i.user.id = :userId")
Integer getTotalInvestedAmount(@Param("userId") int userId);

@Query("SELECT COALESCE(SUM(amount),0) FROM Investment  WHERE DATE(investment_date_time)  = :date AND company_id=:id")
double getTotalInvestedByDate(@Param("date")Date date,int id);

@Query("SELECT COALESCE(count(DISTINCT user_id),0) FROM Investment  WHERE DATE(investment_date_time)  = :date AND company_id=:id")
int getTotalInvestedUserByDate(Date date,int id);

@Query("SELECT i FROM Investment i WHERE i.investment_date_time>=:ldt  AND company_id=:companyId")
List<Investment> findAllByCompany_id(int companyId,LocalDateTime ldt);

List<Investment> findAllByUser_id(int id);


}
