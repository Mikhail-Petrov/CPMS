package com.cpms.data.entities;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.apache.solr.analysis.LowerCaseFilterFactory;
import org.apache.solr.analysis.WhitespaceTokenizerFactory;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
import org.springframework.context.i18n.LocaleContextHolder;

import com.cpms.data.AbstractDomainObject;
import com.cpms.data.validation.BilingualValidation;
import com.cpms.exceptions.DataAccessException;

/**
 * Entity class for skill.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
@Entity
@Indexed
@Table(name = "SKILL")
@AnalyzerDef(name = "userSearchAnalyzerSkill",
	tokenizer = @TokenizerDef(factory = WhitespaceTokenizerFactory.class),
	filters = {
		@TokenFilterDef(factory = LowerCaseFilterFactory.class),
	})
@BilingualValidation(fieldOne="name", fieldTwo="name_RU", 
	nullable = true, minlength = 5, maxlength = 100)
@BilingualValidation(fieldOne="about", fieldTwo="about_RU", 
	nullable = true, minlength = 0, maxlength = 1000)
public class Skill extends AbstractDomainObject {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@Column(name = "NAME", nullable = true, length = 100)
	@Field
	@Analyzer(definition = "userSearchAnalyzerSkill")
	private String name;
	
	@Column(name = "NAME_RU", nullable = true, length = 100)
	@Field
	@Analyzer(definition = "userSearchAnalyzerSkill")
	private String name_RU;
	
	@Column(name = "ABOUT", nullable = true, length = 1000)
	private String about;
	
	@Column(name = "ABOUT_RU", nullable = true, length = 1000)
	private String about_RU;
	
	@Column(name = "MAXLEVEL", nullable = false)
	@Min(1)
	@Max(100)
	private int maxLevel;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "PARENT", nullable = true)
	@Cascade({CascadeType.DETACH})
	private Skill parent;
	
	@Column(name = "draft", nullable = false)
	private boolean draft = false;
	
	@Column(name = "owner", nullable = true)
	private Long owner;
	
	//TODO see if you can fetch this lazily with hibernate
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "parent")
	@Cascade({CascadeType.DETACH})
	private Set<Skill> children;
	
	@OneToMany(fetch = FetchType.EAGER,	mappedBy = "skill", orphanRemoval = true)
	@Cascade({CascadeType.DELETE, CascadeType.DETACH})
	private Set<Competency> implementers;
	
	@OneToMany(fetch = FetchType.EAGER,	mappedBy = "skill", orphanRemoval = true)
	@Cascade({CascadeType.DELETE, CascadeType.DETACH})
	private Set<TaskRequirement> implementersTask;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "skill", orphanRemoval = true)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE,
        CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH})
	private Set<SkillLevel> levels;
	
	public Skill() {}
	
	public Skill(String name, String about) {
		this.name = name;
		this.about = about;
	}
	
	public boolean isDraft() {
		return draft;
	}

	public void setDraft(boolean draft) {
		this.draft = draft;
	}

	public String getName_RU() {
		return name_RU;
	}

	public void setName_RU(String name_RU) {
		this.name_RU = name_RU;
	}

	public String getAbout_RU() {
		return about_RU;
	}

	public void setAbout_RU(String about_RU) {
		this.about_RU = about_RU;
	}

	public Long getOwner() {
		return owner;
	}

	public void setOwner(Long owner) {
		this.owner = owner;
	}

	public void addLevel(SkillLevel level) {
		if (level == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.levels == null) {
			this.getLevels();
		}
		if (!this.levels.stream().anyMatch(x -> x.getLevel() == level.getLevel())) {
			if (level.getLevel() < 1 || level.getLevel() > this.maxLevel) {
				throw new DataAccessException("Illegal skill level " + level.getLevel(),
						null);
			}
			this.levels.add(level);
			level.setSkill(this);
		} else {
			throw new DataAccessException("Attempt to insert duplicate levels.",
					null);
		}
	}
	
	public void removeLevel(SkillLevel level) {
		if (level == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.equals(level.getSkill())) {
			removeEntityFromManagedCollection(level, levels);
			level.setSkill(null);
		}
	}
	
	public Set<SkillLevel> getLevels() {
		if (levels == null) {
			levels = new LinkedHashSet<SkillLevel>() ;
		}
		return new LinkedHashSet<SkillLevel>(levels);
	}

	public void setLevels(Set<SkillLevel> levels) {
		if (levels == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.levels == null) {
			this.levels = levels;
		} else {
			throw new DataAccessException("Cannot insert, Hibernate will lose track",
					null);
		}
	}

	public void setImplementers(Set<Competency> implementers) {
		if (this.implementers != null) {
			throw new DataAccessException("Cannot insert, Hibernate will lose track",
					null);
		}
		this.implementers = implementers;
	}
	
	public Set<Competency> getImplementers() {
		if (implementers == null) {
			implementers = new LinkedHashSet<Competency>();
		}
		return new LinkedHashSet<Competency>(implementers);
	}

	public Set<TaskRequirement> getImplementersTask() {
		if (implementersTask == null) {
			implementersTask = new LinkedHashSet<TaskRequirement>();
		}
		return new LinkedHashSet<TaskRequirement>(implementersTask);
	}

	public void setImplementersTask(Set<TaskRequirement> implementersTask) {
		if (this.implementersTask != null) {
			throw new DataAccessException("Cannot insert, Hibernate will lose track",
					null);
		}
		this.implementersTask = implementersTask;
	}

	public Set<Skill> getChildren() {
		if (children == null) {
			children = new LinkedHashSet<Skill>() ;
		}
		return new LinkedHashSet<Skill>(children);
	}

	public void setChildren(Set<Skill> children) {
		if (this.children != null) {
			throw new DataAccessException("Cannot insert, Hibernate will lose track",
					null);
		}
		this.children = children;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAbout(String about) {
		this.about = about;
	}

	public void setMaxLevel(int maxLevel) {
		if (maxLevel < 1) {
			throw new DataAccessException("Illegal max level.", null);
		} else {
			if (this.maxLevel > maxLevel) {
				getLevels().stream().filter(x -> x.getLevel() > maxLevel)
					.forEach(x -> removeLevel(x));
			}
			this.maxLevel = maxLevel;
		}
	}

	public void setParent(Skill parent) {
		if (this.parent != null && this.parent.children != null) {
			this.parent.children.remove(this);
		}
		this.parent = parent;
		if (this.parent != null && this.parent.getChildren() != null 
				&& !this.parent.children.contains(this)) {
			this.parent.children.add(this);
		}
	}
	
	public void detachChildren() {
		if (children != null) {
			getChildren().forEach(x -> x.setParent(null));
			children.clear();
		}
	}

	@Override
	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Skill getParent() {
		return parent;
	}

	public String getAbout() {
		return about;
	}
	
	public int getMaxLevel() {
		return maxLevel;
	}
	
	/**
	 * Creates a new set of levels based on the ones saved. If there is no
	 * SkillLevel entity for one or more of the levels, inserts new SkillLevels
	 * into set described as "Undefined level".
	 * 
	 * @return set of levels which always contains getMaxLevel() amount of 
	 * levels.
	 */
	public Set<SkillLevel> getFullSkillLevels() {
		Set<SkillLevel> levels = this.getLevels();
		for(int i = 1; i <= maxLevel; i++) {
			final int levelId = i;
			if (levels.stream().filter(x -> x.getLevel() == levelId)
					.findFirst().orElse(null) == null) {
				SkillLevel level = new SkillLevel();
				level.setAbout("Undefined level");
				level.setAbout_RU("Описание отсутствует");
				level.setLevel(i);
				levels.add(level);
			}
		}
		return levels.stream()
				.sorted((x,y) -> {return x.getLevel() - y.getLevel();})
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	@Override
	public Class<?> getEntityClass() {
		return Skill.class;
	}

	@Override
	public String getPresentationName() {
		Locale locale = LocaleContextHolder.getLocale();
		return localizeBilingualField(getName(), name_RU, locale);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Skill localize(Locale locale) {
		Skill returnValue = new Skill();
		returnValue.setDraft(isDraft());
		returnValue.setName(getName());
		returnValue.setName_RU(getName_RU());
		returnValue.setId(getId());
		returnValue.setChildren(getChildren());
		returnValue.setMaxLevel(getMaxLevel());
		returnValue.setParent(getParent());
		returnValue.setAbout(
				localizeBilingualField(getAbout(), getAbout_RU(), locale));
		getFullSkillLevels()
			.forEach(x -> returnValue.addLevel(x.localize(locale)));
		return returnValue;
	}
	
	
}
