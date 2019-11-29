package com.cpms.tests.facade;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   TestSkillDAO.class,
   TestProfileDAO.class,
   TestTaskDAO.class,
   TestFullTextSearch.class,
   TestApplicationsService.class,
   TestSubprofiler.class,
   TestProfileCompetencySearcher.class,
   TestProfileRanger.class,
   TestPossibilityAggregator.class
})
public class FacadeSuite {}
