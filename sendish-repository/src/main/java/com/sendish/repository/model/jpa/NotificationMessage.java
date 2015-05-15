package com.sendish.repository.model.jpa;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity
@Table(name = "notification_message")
@SequenceGenerator(name = "idSequence", sequenceName = "notification_message_seq", allocationSize = 1)
@AttributeOverride(name = "id", column = @Column(name="nm_id"))
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class NotificationMessage extends BaseEntity {

    private static final long  serialVersionUID = 1L;

    @Column(name = "nm_ref_id")
    private Long               referenceId;

    @Column(name = "nm_notification_type", nullable = false)
    private String             type;

    @Column(name = "nm_created_date", nullable = false, updatable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime           createdDate;

    @Column(name = "nm_done_sending_date")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime           doneSending;

    @Column(name = "nm_finished_date")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime           finishedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "nm_status", nullable = false)
    private NotificationStatus status;

    @Column(name = "nm_gcm_count", nullable = false)
    private Integer            gcmCount         = Integer.valueOf(0);

    @Column(name = "nm_gcm_success", nullable = false)
    private Integer            gcmSuccess       = Integer.valueOf(0);

    @Column(name = "nm_apns_count", nullable = false)
    private Integer            apnsCount        = Integer.valueOf(0);

    @Column(name = "nm_apns_success", nullable = false)
    private Integer            apnsSuccess      = Integer.valueOf(0);

    @PrePersist
    public final void onPersist() {
        createdDate = DateTime.now();
    }

    // Getters & setters

    public final Long getReferenceId() {
        return referenceId;
    }

    public final void setReferenceId(final Long p_referenceId) {
        referenceId = p_referenceId;
    }

    public final String getType() {
        return type;
    }

    public final void setType(final String p_type) {
        type = p_type;
    }

    public final DateTime getCreatedDate() {
        return createdDate;
    }

    public final DateTime getDoneSending() {
        return doneSending;
    }

    public final void setDoneSending(final DateTime p_doneSending) {
        doneSending = p_doneSending;
    }

    public final DateTime getFinishedDate() {
        return finishedDate;
    }

    public final void setFinishedDate(final DateTime p_finishedDate) {
        finishedDate = p_finishedDate;
    }

    public final NotificationStatus getStatus() {
        return status;
    }

    public final void setStatus(final NotificationStatus p_status) {
        status = p_status;
    }

    public final Integer getGcmCount() {
        return gcmCount;
    }

    public final void setGcmCount(final Integer p_gcmCount) {
        gcmCount = p_gcmCount;
    }

    public final Integer getGcmSuccess() {
        return gcmSuccess;
    }

    public final void setGcmSuccess(final Integer p_gcmSuccess) {
        gcmSuccess = p_gcmSuccess;
    }

    public final Integer getApnsCount() {
        return apnsCount;
    }

    public final void setApnsCount(final Integer p_apnsCount) {
        apnsCount = p_apnsCount;
    }

    public final Integer getApnsSuccess() {
        return apnsSuccess;
    }

    public final void setApnsSuccess(final Integer p_apnsSuccess) {
        apnsSuccess = p_apnsSuccess;
    }

}
