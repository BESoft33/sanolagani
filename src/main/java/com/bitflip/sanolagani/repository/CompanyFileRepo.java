package com.bitflip.sanolagani.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.bitflip.sanolagani.models.Company;
import com.bitflip.sanolagani.models.CompanyFile;
@Repository
public interface CompanyFileRepo extends JpaRepository<CompanyFile, Integer>{
    List<CompanyFile> findAllByCompany(Company company);
    @Query(value = "SELECT * FROM companyfile ORDER BY  uploaddate desc", nativeQuery = true)
	public List<CompanyFile> findAllData();
}
