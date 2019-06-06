package com.cpms.web.controllers;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.cpms.dao.implementations.jpa.repositories.system.CompetencyRepository;
import com.cpms.data.entities.Requirements;
import com.cpms.data.entities.Skill;
import com.cpms.data.entities.Task;
import com.cpms.data.entities.TaskRequirement;
import com.cpms.exceptions.DependentEntityNotFoundException;
import com.cpms.facade.ICPMSFacade;

/**
 * Landing controller.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@Controller
@RequestMapping(path = "/")
@SuppressWarnings("unused")
public class Landing {

	private class FrequencyStat {
		public String skillName;
		public int hits;

		public FrequencyStat(String skillName, int hits) {
			this.skillName = skillName;
			this.hits = hits;
		}

		public String getSkillName() {
			return skillName;
		}

		public void setSkillName(String skillName) {
			this.skillName = skillName;
		}

		public int getHits() {
			return hits;
		}

		public void setHits(int hits) {
			this.hits = hits;
		}
	}

	private class AverageStat {
		public String skillName;
		public double average;

		public AverageStat(String skillName, double average) {
			this.skillName = skillName;
			this.average = average;
		}

		public String getSkillName() {
			return skillName;
		}

		public void setSkillName(String skillName) {
			this.skillName = skillName;
		}

		public double getAverage() {
			return average;
		}

		public void setAverage(double average) {
			this.average = average;
		}
	}

	private class SkillCompetencyFrequencyComparator<T extends Skill> implements Comparator<T> {
		@Override
		public int compare(T o1, T o2) {
			return o1.getImplementers().size() - o2.getImplementers().size();
		}
	}

	private class SkillCompetencyAverageComparator<T extends AverageStat> implements Comparator<T> {
		@Override
		public int compare(T o1, T o2) {
			//return o1.getAverage() > o2.getAverage() ? 1 : o1.getAverage() < o2.getAverage() ? -1 : 0;
			return Double.compare(o1.getAverage(), o2.getAverage());
		}
	}

	@Autowired
	@Qualifier(value = "facade")
	private ICPMSFacade facade;
	private CompetencyRepository competencyRepo;

	@Autowired
	@Qualifier(value = "Competency")
	public void setCompetencyRepo(CompetencyRepository competencyRepo) {
		this.competencyRepo = competencyRepo;
	}

	private final Comparator<Skill> comparatorFrequency = new SkillCompetencyFrequencyComparator<Skill>();
	private final Comparator<AverageStat> comparatorAverage = new SkillCompetencyAverageComparator<AverageStat>();

	private byte[] bytes = null;
	private MultipartFile ffile = null;
	
	@RequestMapping(path = "/", method = RequestMethod.GET)
	public String landing(Model model, HttpServletResponse response) {
		model.addAttribute("datesList", datesList().toArray());
		model.addAttribute("totalCompetencies", totalCompetencies());
		model.addAttribute("skillFrequencyList", skillFrequencyList());
		model.addAttribute("skillAveragesList", skillAveragesList());
		model.addAttribute("_VIEW_TITLE", "title.welcome");
		return "landing";
	}

	private List<Long[]> datesList() {
		final long millisInDay = 60 * 60 * 24 * 1000;
		Map<Long, Long> map = facade.getProfileDAO().getAll().stream().filter(x -> x.getCreatedDate() != null)
				.map(x -> (x.getCreatedDate().getTime() / millisInDay) * millisInDay)
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		List<Long[]> returnValue = new ArrayList<>();
		map.entrySet().forEach(x -> {
			returnValue.add(new Long[] { x.getKey(), x.getValue() });
		});
		Collections.sort(returnValue, (x, y) -> x[0] > y[0] ? 1 : -1);
		for (int i = 1; i < returnValue.size(); i++) {
			returnValue.get(i)[1] = returnValue.get(i)[1] + returnValue.get(i - 1)[1];
		}
		return returnValue;
	}

	private List<FrequencyStat> skillFrequencyList() {
		List<FrequencyStat> returnValue = new ArrayList<FrequencyStat>();
		List<Skill> topSkills = facade.getSkillDAO().getAll().stream().sorted(comparatorFrequency).limit(4)
				.collect(Collectors.toList());
		long hits = 0;
		for (int i = 0; i < 4; i++) {
			if (topSkills.size() <= i) {
				returnValue.add(new FrequencyStat("Undefinded", 0));
			} else {
				returnValue
						.add(new FrequencyStat(topSkills.get(i).getName(), topSkills.get(i).getImplementers().size()));
				hits += topSkills.get(i).getImplementers().size();
			}
		}
		returnValue.add(new FrequencyStat("Others", (int) (totalCompetencies() - hits)));
		return returnValue;
	}

	private List<AverageStat> skillAveragesList() {
		List<AverageStat> returnValue = facade.getSkillDAO().getAll().stream()
				.map(x -> new AverageStat(x.getName(), skillAverage(x))).sorted(comparatorAverage).limit(5)
				.collect(Collectors.toList());
		return returnValue;
	}

	private double skillAverage(Skill skill) {
		double totalValue = skill.getImplementers().stream().mapToDouble(x -> x.getLevel()).sum();
		return totalValue / (skill.getImplementers().size() / skill.getMaxLevel());
	}

	private long totalCompetencies() {
		return competencyRepo.count();
	}

}
