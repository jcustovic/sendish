package com.sendish.repository.querydsl.predicate;

import com.mysema.query.support.Expressions;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.expr.BooleanExpression;
import com.sendish.repository.model.jpa.QUserDetails;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsPredicate {

    public static final int MINUTES_BETWEEN_RECEIVED_PHOTOS = 10;

    public static Predicate searchUsersForSendingPool(DateTime latestUserPhotoReceivedDate) {
        DateTime now = DateTime.now();
        QUserDetails qUserDetails = QUserDetails.userDetails;
        List<BooleanExpression> andPredicates = new ArrayList<>();

        andPredicates.add(qUserDetails.lastInteractionTime.isNotNull()
                .and(qUserDetails.lastInteractionTime.gt(now.minusDays(1))));

        andPredicates.add(qUserDetails.receiveAllowedTime.isNull()
                .or(qUserDetails.receiveAllowedTime.loe(now)));

        if (latestUserPhotoReceivedDate != null)  {
            andPredicates.add(
        		qUserDetails.lastReceivedTime.isNull()
                	.or(
                		qUserDetails.lastReceivedTime.lt(now.minusMinutes(MINUTES_BETWEEN_RECEIVED_PHOTOS))
                        .and(qUserDetails.lastReceivedTime.goe(latestUserPhotoReceivedDate))
                    )
            );
        } else {
        	andPredicates.add(
    			qUserDetails.lastReceivedTime.isNull()
    				.or(qUserDetails.lastReceivedTime.lt(now.minusMinutes(MINUTES_BETWEEN_RECEIVED_PHOTOS)))
            );
        }

        andPredicates.add(qUserDetails.user.deleted.isFalse());
        andPredicates.add(qUserDetails.user.disabled.isFalse());

        return Expressions.allOf(andPredicates.toArray(new BooleanExpression[andPredicates.size()]));
    }

    public static Predicate searchOldUsersForSendingPool(DateTime oldestUserPhotoReceivedDate) {
        DateTime now = DateTime.now();
        QUserDetails qUserDetails = QUserDetails.userDetails;
        List<BooleanExpression> andPredicates = new ArrayList<>();

        andPredicates.add(qUserDetails.lastInteractionTime.isNotNull()
                .and(qUserDetails.lastInteractionTime.gt(now.minusDays(1))));

        andPredicates.add(qUserDetails.receiveAllowedTime.isNull()
                .or(qUserDetails.receiveAllowedTime.loe(now)));

        andPredicates.add(qUserDetails.lastReceivedTime.isNotNull());
		andPredicates.add(qUserDetails.lastReceivedTime.lt(oldestUserPhotoReceivedDate));
        andPredicates.add(qUserDetails.lastReceivedTime.lt(now.minusMinutes(MINUTES_BETWEEN_RECEIVED_PHOTOS)));

        andPredicates.add(qUserDetails.user.deleted.isFalse());
        andPredicates.add(qUserDetails.user.disabled.isFalse());

        return Expressions.allOf(andPredicates.toArray(new BooleanExpression[andPredicates.size()]));
    }

}
