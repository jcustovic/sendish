package com.sendish.repository.model.jpa;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

@Entity
@Table(name = "auth_user")
@SequenceGenerator(name = "idSequence", sequenceName = "auth_user_seq", allocationSize = 1)
@AttributeOverride(name = "id", column = @Column(name = "au_id"))
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class User extends BaseEntity {

	private static final long serialVersionUID = 1L;

	public static final int USERNAME_MAX_LENGTH = 100;
	private static final int MD5_PASS_MAX_LENGTH = 65;
	public static final int EMAIL_MAX_LENGTH = 80;
	private static final int UUID_LENGTH = 36;
	private static final int NICKNAME_LENGTH = 8;

	@Column(name = "au_username", length = USERNAME_MAX_LENGTH, nullable = false, unique = true)
	private String username;

	@Column(name = "au_password", length = MD5_PASS_MAX_LENGTH, nullable = false)
	private String password;

	@Column(name = "au_email", length = EMAIL_MAX_LENGTH, unique = true)
	private String email;

	@Column(name = "au_nickname", length = NICKNAME_LENGTH)
	private String nickname;

	@Column(name = "au_firstname")
	private String firstName;

	@Column(name = "au_lastname")
	private String lastName;

	@Enumerated(EnumType.STRING)
	@Column(name = "au_gender")
	private Gender gender;

	@Column(name = "au_birthdate", nullable = false, updatable = false)
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
	private LocalDate birthdate;

	@Column(name = "au_email_confirmed", nullable = false)
	private Boolean emailConfirmed = false;

	@Column(name = "au_disabled", nullable = false)
	private Boolean disabled = false;

	@Column(name = "au_deleted", nullable = false)
	private Boolean deleted = false;

	@Column(name = "au_email_registration", nullable = false)
	private Boolean emailRegistration = false;

	@Column(name = "au_verification_code", length = UUID_LENGTH)
	private String verificationCode;

	@OneToOne(mappedBy = "user", fetch = FetchType.EAGER)
	private UserDetails details;

	@Column(name = "au_created_date", nullable = false, updatable = false)
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime createdDate;

	@Column(name = "au_modified_date", nullable = false)
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime modifiedDate;

	public User() {
		// Hibernate
	}

	public User(String username, String password, String email) {
		this.username = username;
		this.password = password;
		this.email = email;
	}

	@PrePersist
	public final void markCreatedDate() {
		final DateTime now = DateTime.now();
		createdDate = now;
		modifiedDate = now;
	}

	@PreUpdate
	public final void updateModifyDate() {
		modifiedDate = DateTime.now();
	}
	
	public boolean isUserActive() {
		return !(deleted || disabled);
	}

	// Getters & setters

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public LocalDate getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(LocalDate birthdate) {
		this.birthdate = birthdate;
	}

	public Boolean getEmailConfirmed() {
		return emailConfirmed;
	}

	public void setEmailConfirmed(Boolean emailConfirmed) {
		this.emailConfirmed = emailConfirmed;
	}

	public Boolean getDisabled() {
		return disabled;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}

	public Boolean getEmailRegistration() {
		return emailRegistration;
	}

	public void setEmailRegistration(Boolean emailRegistration) {
		this.emailRegistration = emailRegistration;
	}

	public UserDetails getDetails() {
		return details;
	}

	public void setDetails(UserDetails details) {
		this.details = details;
	}

	public DateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(DateTime createdDate) {
		this.createdDate = createdDate;
	}

	public DateTime getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(DateTime modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

}
