package com.cpms.dao.implementations.jpa;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpms.dao.implementations.jpa.repositories.system.DocumentCategoryRepository;
import com.cpms.dao.implementations.jpa.repositories.system.DocumentTrendRepository;
import com.cpms.dao.implementations.jpa.repositories.system.TermsRepository;
import com.cpms.dao.interfaces.IDraftableSkillDaoExtension;
import com.cpms.dao.interfaces.IInnovationTermDAO;
import com.cpms.data.entities.Article;
import com.cpms.data.entities.Term;
import com.cpms.data.entities.TermAnswer;

/**
 * Extension of {@link JPASkillDAO} that allows working with skill drafts.
 * 
 * @see IDraftableSkillDaoExtension
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Service
@Transactional("transactionManager")
public class JPATermInnovationDAO extends JPATermDAO 
					implements IInnovationTermDAO {

	protected DocumentCategoryRepository dcRepo;
	protected DocumentTrendRepository dtRepo;

	@Autowired
	@Qualifier(value = "DocumentCategory")
	public void setDCRepo(DocumentCategoryRepository dcRepo) {
		this.dcRepo = dcRepo;
	}

	@Autowired
	@Qualifier(value = "DocumentTrend")
	public void setDTRepo(DocumentTrendRepository dtRepo) {
		this.dtRepo = dtRepo;
	}
	
	@Override
	public List<Term> getInnovations(List<Long> cats, List<Long> trends) {
		return termRepo.getInnovations(cats, trends);
	}

	@Override
	public List<Term> find(String query) {
		return termRepo.findBySearch(query);
	}
	
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

	private String getDate(Date startDate) {
		return df.format(startDate);
	}
	
	@Override
	public List<TermAnswer> getTermAnswers(Date startDate, Date endDate, Date oldStartDate, List<Long> cats, List<Long> trends) {
		String start_date, end_date, old_start_date;
		start_date = getDate(startDate);
		end_date = getDate(endDate);
		old_start_date = getDate(oldStartDate);
		List<Object[]> ret = termRepo.getTermAnswers(start_date, end_date, old_start_date, cats, trends);
		List<TermAnswer> res = new ArrayList<>();
		for (Object[] o : ret)
			res.add(new TermAnswer(o));
		return res;
	}
	
	@Override
	public int getTermSum(List<Long> terms, Date startDate, Date finishDate) {
		String start_date, finish_date;
		start_date = getDate(startDate);
		finish_date = getDate(finishDate);
		Integer ret = termRepo.getTermSum(terms, start_date, finish_date);
		if (ret == null)
			ret = 0;
		return ret;
	}

	@Override
	public int getTermDocCount(List<Long> terms, Date startDate, Date finishDate) {
		String start_date, finish_date;
		start_date = getDate(startDate);
		finish_date = getDate(finishDate);
		Integer ret = termRepo.getTermDocCount(terms, start_date, finish_date);
		if (ret == null)
			ret = 0;
		return ret;
	}

	@Override
	public int getDocCount(Date startDate, Date finishDate, List<Long> cats, List<Long> trends) {
		String start_date, finish_date;
		start_date = getDate(startDate);
		finish_date = getDate(finishDate);
		Integer ret = trends == null ? termRepo.getDocCount(start_date, finish_date, cats) :
			termRepo.getDocCount(start_date, finish_date, cats, trends);
		if (ret == null)
			ret = 0;
		return ret;
	}

	@Override
	public List<BigInteger> getTermDocsIDs(List<Long> terms, int order) {
		return order > 0 ? termRepo.getTermKeysIDs(terms) : termRepo.getTermDocsIDs(terms);
	}

	@Override
	public Term getTermByStem(String stem) {
		return termRepo.getTermByStem(stem);
	}

	@Override
	public int getTermCount(List<Long> terms, Long docid) {
		Integer res = termRepo.getTermCount(terms, docid);
		if (res == null) res = 0;
		return res;
	}

	@Override
	public void insertDC(boolean cat) {
		if (cat) {
			dcRepo.deleteAll();
			termRepo.insertDC();
		} else {
			dtRepo.deleteAll();
			termRepo.insertDT();
		}
	}

	@Override
	public List<BigInteger> getLastDocs(Date startDate, List<Long> cats, List<Long> trends) {
		return cats == null ? termRepo.getLastDocs(getDate(startDate)) :
			termRepo.getLastDocs(getDate(startDate), cats, trends);
	}

	@Override
	public List<Object[]> getCatTrendForTerm(Term term) {
		return termRepo.getCatTrendForTerm(term.getId());
	}
}
