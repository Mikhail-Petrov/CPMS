package com.cpms.data.entities;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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
import org.springframework.format.annotation.DateTimeFormat;

import com.cpms.data.AbstractDomainObject;
import com.cpms.exceptions.DataAccessException;

/**
 * Entity class for task.
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
@Entity
@Indexed
@Table(name = "TASK")
@AnalyzerDef(name = "userSearchAnalyzerTask",
	tokenizer = @TokenizerDef(factory = WhitespaceTokenizerFactory.class),
	filters = {
			@TokenFilterDef(factory = LowerCaseFilterFactory.class),
})
public class Task extends AbstractDomainObject {
	
	//TODO solve the osiv thing maybe
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@Column(name = "NAME", nullable = true, length = 100)
	@Field
	@Analyzer(definition = "userSearchAnalyzerTask")
	private String name;
	
	// text
	@Column(name = "ABOUT", nullable = true, length = 10000)
	private String about;
	
	// text
	@Column(name = "original", nullable = true, length = 10000)
	private String original;
	
	// text type
	@Column(name = "TYPE", nullable = true)
	private String type;

	@Column(name = "DUE", nullable = true)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Temporal(TemporalType.DATE)
	private Date dueDate;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "SOURCE", nullable = true)
	@Cascade({CascadeType.DETACH})
	private Language source;

	@Column(name = "TARGETS", nullable = true, length = 1000)
	private String target;
	
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "task", orphanRemoval = true)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE,
        CascadeType.MERGE, CascadeType.PERSIST})
	private Set<TaskRequirement> requirements;
	
	public Task() {}
	
	public Task(String name, String about) {
		this.name = name;
		this.about = about;
	}

	@Override
	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getAbout() {
		return about;
	}
	
	public void addRequirement(TaskRequirement newRequirement) {
		if (newRequirement == null || newRequirement.getSkill() == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (newRequirement.getLevel() > newRequirement.getSkill().getMaxLevel()
				|| newRequirement.getLevel() < 1) {
			throw new DataAccessException("Added requirement with invalied level"
					, null);
		}
		if (requirements == null) {
			getRequirements();
		}
		if (requirements.stream()
				.anyMatch(x -> x.duplicates(newRequirement))) {
			throw new DataAccessException("Duplicate competency insertion.", null);
		} else {
			this.requirements.add(newRequirement);
			newRequirement.setTask(this);
		}
	}
	
	public void removerRequirement(TaskRequirement oldRequirement) {
		if (oldRequirement == null) {
			throw new DataAccessException("Null value.", null);
		}
		if (this.equals(oldRequirement.getTask())) {
			oldRequirement.setTask(null);
			removeEntityFromManagedCollection(oldRequirement, requirements);
		}
	}

	public Set<TaskRequirement> getRequirements() {
		if (requirements == null) {
			requirements = new LinkedHashSet<TaskRequirement>();
		}
		return new LinkedHashSet<TaskRequirement>(requirements);
	}

	public void setRequirements(Set<TaskRequirement> requirements) {
		if (requirements == null) {
			throw new DataAccessException("Null value.", null);
		}
		this.requirements = requirements;
		this.requirements.forEach(x -> x.setTask(this));
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

	@Override
	public Class<?> getEntityClass() {
		return Task.class;
	}

	@Override
	public String getPresentationName() {
		return getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Task localize(Locale locale) {
		return this;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public Language getSource() {
		return source;
	}

	public void setSource(Language source) {
		this.source = source;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getOriginal() {
		return original;
	}

	public void setOriginal(String original) {
		this.original = original;
	}
}