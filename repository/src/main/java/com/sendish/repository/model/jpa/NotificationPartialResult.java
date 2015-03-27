package com.sendish.repository.model.jpa;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;

@Entity
@Table(name = "notification_partial_result")
@SequenceGenerator(name = "idSequence", sequenceName = "notification_partial_result_seq", allocationSize = 1)
@AttributeOverride(name = "id", column = @Column(name="npr_id"))
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class NotificationPartialResult extends BaseEntity {

    private static final long        serialVersionUID = 1L;

    @ManyToOne(optional = false)
    @JoinColumn(name = "npr_notification_msg_id", nullable = false)
    private NotificationMessage      notification;

    @Column(name = "npr_send_date", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime                 sendDate;

    @Column(name = "npr_response_date", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime                 responseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "npr_platform_type", nullable = false)
    private NotificationPlatformType platformType;

    @Column(name = "npr_total_count", nullable = false)
    private Integer                  totalCount;

    @Column(name = "npr_failure_count", nullable = false)
    private Integer                  failureCount;

    // Getters & setters

    public final NotificationMessage getNotification() {
        return notification;
    }

    public final void setNotification(final NotificationMessage p_notification) {
        notification = p_notification;
    }

    public final DateTime getSendDate() {
        return sendDate;
    }

    public final void setSendDate(final DateTime p_sendDate) {
        sendDate = p_sendDate;
    }

    public final DateTime getResponseDate() {
        return responseDate;
    }

    public final void setResponseDate(final DateTime p_responseDate) {
        responseDate = p_responseDate;
    }

    public final NotificationPlatformType getPlatformType() {
        return platformType;
    }

    public final void setPlatformType(final NotificationPlatformType p_platformType) {
        platformType = p_platformType;
    }

    public final Integer getTotalCount() {
        return totalCount;
    }

    public final void setTotalCount(final Integer p_totalCount) {
        totalCount = p_totalCount;
    }

    public final Integer getFailureCount() {
        return failureCount;
    }

    public final void setFailureCount(final Integer p_failureCount) {
        failureCount = p_failureCount;
    }

}
