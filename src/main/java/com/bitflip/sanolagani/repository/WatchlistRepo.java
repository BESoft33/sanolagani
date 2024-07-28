package com.bitflip.sanolagani.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bitflip.sanolagani.models.Watchlist;

public interface WatchlistRepo extends JpaRepository<Watchlist, Integer>{
	
	@Query(value = "SELECT * FROM watchlist WHERE user_id =:id ",nativeQuery = true)
	List<Watchlist> findByUserId(int id);

	@Query(value = "SELECT * FROM watchlist WHERE company_id =:company_id and user_id=:user_id ",nativeQuery = true)
	Watchlist findByCompanyId(int company_id,int user_id);
	
	

}
