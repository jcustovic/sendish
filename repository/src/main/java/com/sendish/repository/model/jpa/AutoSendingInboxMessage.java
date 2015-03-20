package com.sendish.repository.model.jpa;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;

import javax.persistence.*;

@Entity
@Table(name = "auto_sending_inbox_message")
@SequenceGenerator(name = "idSequence", sequenceName = "auto_sending_inbox_message_seq", allocationSize = 1)
@AttributeOverride(name = "id", column = @Column(name = "asim_id"))
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AutoSendingInboxMessage extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "asim_im_id")
    private InboxMessage inboxMessage;

    @Column(name = "asim_active", nullable = false)
    private Boolean active;

    @Column(name = "asim_priority", nullable = false)
    private Integer priority;

    @Column(name = "asim_after_days")
    private Integer afterDays;

    @Column(name = "asim_on_date")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentLocalDate")
    private LocalDate onDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asim_city_id")
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asim_country_id")
    private Country country;

    // Getters & setters

    public InboxMessage getInboxMessage() {
        return inboxMessage;
    }

    public void setInboxMessage(InboxMessage inboxMessage) {
        this.inboxMessage = inboxMessage;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getAfterDays() {
        return afterDays;
    }

    public void setAfterDays(Integer afterDays) {
        this.afterDays = afterDays;
    }

    public LocalDate getOnDate() {
        return onDate;
    }

    public void setOnDate(LocalDate onDate) {
        this.onDate = onDate;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

}
