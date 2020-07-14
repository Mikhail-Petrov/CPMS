package com.cpms.dao.implementations.jpa.repositories.system;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Term;

@Repository(value = "Term")
public interface TermsRepository  extends JpaRepository<Term, Long> {

	@Query("Select term from Term term where ISinnovation = 1")
	public List<Term> getInnovations();
	
	@Query("Select term from Term term where stem = :stem")
	public Term getTermByStem(@Param("stem") String stem);
	
	@Query(value = "Select top 25 * from Term term where stemtext like :query " +
			"order by ISinnovation desc, LEN(preferabletext), preferabletext", nativeQuery = true)
	public List<Term> findBySearch(@Param("query") String query);
	
	@Query(value = "select sum(k.count) from Document d inner join Keyword k on k.documentid = d.ID and k.termid = :term " + 
			"where d.creationDate >= convert(datetime, :start_date, 20) and d.creationDate < convert(datetime, :finish_date, 20)",
			nativeQuery = true)
	public Integer getTermSum(@Param("term") long term, @Param("start_date") String start_date, @Param("finish_date") String finish_date);

	@Query(value = "select count(d.id) from Document d inner join Keyword k on k.documentid = d.ID and k.termid = :term " + 
			"where d.creationDate >= convert(datetime, :start_date, 20) and d.creationDate < convert(datetime, :finish_date, 20)",
			nativeQuery = true)
	public Integer getTermDocCount(@Param("term") long term, @Param("start_date") String start_date, @Param("finish_date") String finish_date);

	@Query(value = "select count(d.id) from Document d " + 
			"where d.creationDate >= convert(datetime, :start_date, 20) and d.creationDate < convert(datetime, :finish_date, 20)",
			nativeQuery = true)
	public Integer getDocCount(@Param("start_date") String start_date, @Param("finish_date") String finish_date);

	@Query(value = "select top 25 d.id from Document d inner join Keyword k on k.documentid = d.ID and k.termid = :term " + 
			"group by d.id order by sum(k.count) desc",
			nativeQuery = true)
	public List<BigInteger> getTermDocsIDs(@Param("term") long term);
	
	@Query(value = "select top 25 d.id from Document d inner join Keyword k on k.documentid = d.ID and k.termid = :term " + 
			"group by d.id order by max(d.creationDate) desc",
			nativeQuery = true)
	public List<BigInteger> getTermKeysIDs(@Param("term") long term);

	@Query(value = "Select k.count from Keyword k where k.termid = :term and k.documentid = :doc",
			nativeQuery = true)
	public Integer getTermCount(@Param("term") long term, @Param("doc") long doc);
}
