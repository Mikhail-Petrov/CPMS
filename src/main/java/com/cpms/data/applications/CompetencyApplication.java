package com.cpms.data.applications;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.NotImplementedException;

import com.cpms.data.AbstractDomainObject;
import com.cpms.data.DomainObject;
import com.cpms.data.entities.Competency;
import com.cpms.data.entities.Profile;
import com.cpms.data.entities.Skill;
import com.cpms.web.UserSessionData;

/**
 * Entity class for resident's applications to create new competencies within
 * system. Note that all dependencies are manually managed.
 * 
 * @see Competency
 * 
 * @author Gordeev Boris
 * @since 1.0
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "COMPETENCYAPPLICATION", uniqueConstraints =
		@UniqueConstraint(columnNames = {"OWNERID", "SKILLID"}, 
			name = "OwnerSkillUniqueApplication"))
public class CompetencyApplication extends AbstractDomainObject {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private long id;
	
	@Column(name = "LEVEL", nullable = false)
	@Max(100)
	@Min(1)
	private int level;
	
	@Column(name = "OWNERID")
	@NotNull
	private Long ownerId;
	
	@Column(name = "SKILLID")
	@NotNull
	private Long skillId;
	
	@Transient
	private List<EvidenceApplication> evidence;
	
	@Transient
	private Profile owner;
	
	@Transient
	private Skill skill;
	
	public CompetencyApplication(Long skillId, Long ownerId, int level) {
		this.level = level;
		this.skillId = skillId;
		this.ownerId = ownerId;
	}
	
	public CompetencyApplication() {}

	public List<EvidenceApplication> getEvidence() {
		if (evidence == null) {
			evidence = new ArrayList<EvidenceApplication>();
		}
		return evidence;
	}

	public void setEvidence(List<EvidenceApplication> evidence) {
		this.evidence = evidence;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public long getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}
	
	public Long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}

	public Long getSkillId() {
		return skillId;
	}

	public void setSkillId(Long skillId) {
		this.skillId = skillId;
	}

	public Profile getOwner() {
		return owner;
	}

	public void setOwner(Profile owner) {
		this.owner = owner;
	}

	public Skill getSkill() {
		return skill;
	}

	public void setSkill(Skill skill) {
		this.skill = skill;
	}

	/**
	 * Alternative equality function for competency types. Uses skills and owners
	 * to compare.
	 * 
	 * @param competency competency to compare with
	 * @return true if those competencies duplicate each other (their skills and
	 * owners are the same), otherwise false.
	 */
	public boolean duplicates(Competency competency) {
		if (competency == null) {
			return false;
		}
		if (competency.getOwner() == null || competency.getSkill() == null 
				|| this.getClass() == null || this.skillId != 0){
			return false;
		} else {
			return competency.getOwner().getId() == this.getOwnerId()
				&& competency.getSkill().getId() == this.getSkillId();
		}
	}
	
	/**
	 * Alternative equality function for competency types. Uses skills and owners
	 * to compare.
	 * 
	 * @param competency competency to compare with
	 * @return true if those competencies duplicate each other (their skills and
	 * owners are the same), otherwise false.
	 */
	public boolean duplicates(CompetencyApplication competency) {
		if (competency == null) {
			return false;
		}
		if (competency.getOwnerId() == 0 || competency.getSkillId() == 0 
				|| this.getClass() == null || this.skillId != 0){
			return false;
		} else {
			return competency.getOwnerId() == this.getOwnerId()
				&& competency.getSkillId() == this.getSkillId();
		}
	}

	@Override
	public Class<?> getEntityClass() {
		return Competency.class;
	}

	@Override
	public String getPresentationName() {
		//return "Competency Application #" + id;
		return UserSessionData.localizeText(
				"Заявка компетенции №", "Competency Application #") + id;
	}
	
	public String getCompetencyPresentationName() {
		if (getSkill() != null) {
			//return getSkill().getName() + " - " + getLevel();
			return UserSessionData.localizeText(getSkill().getName_RU(), getSkill().getName())
					 + " - " + getLevel();
		} else {
			return UserSessionData.localizeText("Скилл не найден", "Skill not found");
		}
	}

	@Override
	public <T extends DomainObject> T localize(Locale locale) {
		throw new NotImplementedException();
	}
}
