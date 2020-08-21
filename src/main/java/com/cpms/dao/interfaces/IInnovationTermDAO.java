package com.cpms.dao.interfaces;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import com.cpms.data.entities.Article;
import com.cpms.data.entities.Term;
import com.cpms.data.entities.TermAnswer;

/**
 * Extension for Term entity {@link IDAO} which allows for innovations.
 * 
 */
public interface IInnovationTermDAO {

	public List<Term> getInnovations(List<Long> cats, List<Long> trends);

	public List<Term> find(String query);
	
	public int getTermSum(List<Long> terms, Date startDate, Date finishDate);
	
	public int getTermDocCount(List<Long> terms, Date startDate, Date finishDate);
	
	public int getDocCount(Date startDate, Date finishDate, List<Long> cats, List<Long> trends);
	
	public List<BigInteger> getTermDocsIDs(List<Long> terms, int order);
	
	public Term getTermByStem(String stem);
	
	public int getTermCount(List<Long> terms, Long docid);

	public List<TermAnswer> getTermAnswers(Date startDate, Date endDate, Date oldStartDate, List<Long> cats, List<Long> trends);
	
	public void insertDC(boolean cat);
	
	public List<BigInteger> getLastDocs(Date startDate, List<Long> cats, List<Long> trends);
	
	public List<Object[]> getCatTrendForTerm(Term term);
}
