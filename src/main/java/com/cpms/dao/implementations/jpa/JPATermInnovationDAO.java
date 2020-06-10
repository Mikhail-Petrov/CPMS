package com.cpms.dao.implementations.jpa;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cpms.dao.interfaces.IDraftableSkillDaoExtension;
import com.cpms.dao.interfaces.IInnovationTermDAO;
import com.cpms.data.entities.Term;

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
	
	
	@Override
	public List<Term> getInnovations() {
		return termRepo.getInnovations();
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
	public int getTermSum(Term term, Date startDate, Date finishDate) {
		String start_date, finish_date;
		start_date = getDate(startDate);
		finish_date = getDate(finishDate);
		Integer ret = termRepo.getTermSum(term.getId(), start_date, finish_date);
		if (ret == null)
			ret = 0;
		return ret;
	}

	@Override
	public int getTermDocCount(Term term, Date startDate, Date finishDate) {
		String start_date, finish_date;
		start_date = getDate(startDate);
		finish_date = getDate(finishDate);
		Integer ret = termRepo.getTermDocCount(term.getId(), start_date, finish_date);
		if (ret == null)
			ret = 0;
		return ret;
	}

	@Override
	public int getDocCount(Date startDate, Date finishDate) {
		String start_date, finish_date;
		start_date = getDate(startDate);
		finish_date = getDate(finishDate);
		Integer ret = termRepo.getDocCount(start_date, finish_date);
		if (ret == null)
			ret = 0;
		return ret;
	}

	@Override
	public List<BigInteger> getTermDocsIDs(Term term, int order) {
		return order > 0 ? termRepo.getTermKeysIDs(term.getId()) : termRepo.getTermDocsIDs(term.getId());
	}
}
