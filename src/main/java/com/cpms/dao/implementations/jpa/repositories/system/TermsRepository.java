package com.cpms.dao.implementations.jpa.repositories.system;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cpms.data.entities.Article;
import com.cpms.data.entities.Term;

@Repository(value = "Term")
public interface TermsRepository  extends JpaRepository<Term, Long> {

	@Query(value = "Select * from Term term where id in (select termid from Termvariant where id in " +
			"(select TermVariantID from TASK where delDate is null))", nativeQuery = true)
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
			"inner join DocumentCategory dc on (d.id = dc.documentid and dc.categoryid in (:cats))\n" + 
			"inner join DocumentTrend dt on (d.id = dt.documentid and dt.trendid in (:trends))\n" + 
			"where d.creationDate >= convert(datetime, :start_date, 20) and d.creationDate < convert(datetime, :finish_date, 20)",
			nativeQuery = true)
	public Integer getDocCount(@Param("start_date") String start_date, @Param("finish_date") String finish_date,
			@Param("cats") List<Long> cats, @Param("trends") List<Long> trends);

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

	@Query(value = "select * from " + 
			"(select t.preferabletext as preferabletext, count(d.id) as N_new, count(dold.id) as N_old, t.id as term_id " +
			"from Keyword k inner join Term t on k.termid = t.id inner join Document dfilter on (k.documentid = dfilter.id and " +
			"dfilter.creationdate > convert(datetime, :old_start_date, 20) and  " +
			"dfilter.creationdate <= convert(datetime, :end_date, 20)) " +
			"inner join DocumentCategory dc on (dfilter.id = dc.documentid and dc.categoryid in (:cats)) " + 
			"inner join DocumentTrend dt on (dfilter.id = dt.documentid and dt.trendid in (:trends)) " + 
			"left join Document d on (k.documentid = d.id and " +
			"d.creationdate > convert(datetime, :start_date, 20) and " +
			"d.creationdate <= convert(datetime, :end_date, 20)) " + 
			"left join Document dold on (k.documentid = dold.id and " + 
			"dold.creationdate > convert(datetime, :old_start_date, 20) and  " + 
			"dold.creationdate <= convert(datetime, :start_date, 20)) " + 
			"group by t.id, t.preferabletext) as newselect  where newselect.N_new > 0",
			nativeQuery = true)
	public List<Object[]> getTermAnswers(@Param("start_date") String start_date, 
			@Param("end_date") String end_date, @Param("old_start_date") String old_start_date,
			@Param("cats") List<Long> cats, @Param("trends") List<Long> trends);

	@Modifying
	@Query(value = "insert into DocumentCategory (documentid, categoryid) select documentid, category from (" + 
			"select ct.category, k.documentid, sum(k.count) s " + 
			"from Category_Termvariant ct inner join Termvariant tv on tv.id = ct.variant " + 
			"inner join Term t on tv.termid = t.ID inner join Keyword k on t.id = k.termid " + 
			"left join DocumentCategory dc on dc.categoryid = ct.category and dc.documentid = k.documentid " + 
			"where dc.id is null group by ct.category, k.documentid) tab " + 
			"where s > 5",
			nativeQuery = true)
	public void insertDC();

	@Modifying
	@Query(value = "insert into DocumentTrend (documentid, trendid) select documentid, trend from (" + 
			"select ct.trend, k.documentid, sum(k.count) s " + 
			"from Trend_Termvariant ct inner join Termvariant tv on tv.id = ct.variant " + 
			"inner join Term t on tv.termid = t.ID inner join Keyword k on t.id = k.termid " + 
			"left join DocumentTrend dc on dc.trendid = ct.trend and dc.documentid = k.documentid " + 
			"where dc.id is null group by ct.trend, k.documentid) tab " + 
			"where s > 5",
			nativeQuery = true)
	public void insertDT();

	@Query(value = "select d.id from Document d " +
			"inner join DocumentCategory dc on (d.id = dc.documentid and dc.categoryid in (:cats)) " + 
			"inner join DocumentTrend dt on (d.id = dt.documentid and dt.trendid in (:trends)) " + 
			"where d.creationDate >= convert(datetime, :start_date, 20) order by d.creationDate desc",
			nativeQuery = true)
	public List<BigInteger> getLastDocs(@Param("start_date") String start_date,
			@Param("cats") List<Long> cats, @Param("trends") List<Long> trends);
}
