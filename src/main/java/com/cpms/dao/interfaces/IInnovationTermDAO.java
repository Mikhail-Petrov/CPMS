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

	public List<Term> getInnovations();

	public List<Term> find(String query);
	
	public int getTermSum(Term term, Date startDate, Date finishDate);
	
	public int getTermDocCount(Term term, Date startDate, Date finishDate);
	
	public int getDocCount(Date startDate, Date finishDate, List<Long> cats, List<Long> trends);
	
	public List<BigInteger> getTermDocsIDs(Term term, int order);
	
	public Term getTermByStem(String stem);
	
	public int getTermCount(Long termid, Long docid);

	public List<TermAnswer> getTermAnswers(Date startDate, Date endDate, Date oldStartDate, List<Long> cats, List<Long> trends);
	
	public void insertDC(boolean cat);
	
	public List<BigInteger> getLastDocs(Date startDate);
}
