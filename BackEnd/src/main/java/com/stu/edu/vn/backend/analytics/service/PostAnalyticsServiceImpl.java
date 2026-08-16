package com.stu.edu.vn.backend.analytics.service;

import com.stu.edu.vn.backend.analytics.dto.PostAnalyticsResponse;
import com.stu.edu.vn.backend.analytics.repository.PostAnalyticsRepository;
import com.stu.edu.vn.backend.common.exception.BusinessException;
import com.stu.edu.vn.backend.common.exception.ErrorCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Chuẩn hóa khoảng ngày UTC và bảo đảm mọi khối trên màn hình dùng cùng một cửa sổ dữ liệu. */
@Service
@Transactional(readOnly = true)
public class PostAnalyticsServiceImpl implements PostAnalyticsService {
    private static final int MAX_DAYS = 366;
    private final PostAnalyticsRepository repository;
    private final Clock clock;

    public PostAnalyticsServiceImpl(PostAnalyticsRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public PostAnalyticsResponse getAnalytics(String range, String fromValue, String toValue) {
        LocalDate today = LocalDate.now(clock);
        LocalDate to = today;
        LocalDate from;
        try {
            if (fromValue != null || toValue != null) {
                if (fromValue == null || toValue == null) throw new DateTimeParseException("missing", "", 0);
                from = LocalDate.parse(fromValue);
                to = LocalDate.parse(toValue);
            } else {
                from = switch (range == null ? "30D" : range.toUpperCase()) {
                    case "7D" -> today.minusDays(6);
                    case "30D" -> today.minusDays(29);
                    case "90D" -> today.minusDays(89);
                    case "6M" -> today.minusMonths(6).plusDays(1);
                    case "1Y" -> today.minusYears(1).plusDays(1);
                    default -> throw new DateTimeParseException("range", range, 0);
                };
            }
        } catch (DateTimeParseException exception) {
            throw new BusinessException(ErrorCode.POST_ANALYTICS_RANGE_INVALID);
        }
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        if (from.isAfter(to) || to.isAfter(today) || days > MAX_DAYS) {
            throw new BusinessException(ErrorCode.POST_ANALYTICS_RANGE_INVALID);
        }

        var fromTime=from.atStartOfDay(); var toExclusive=to.plusDays(1).atStartOfDay();
        var previousFrom=from.minusDays(days).atStartOfDay(); var previousTo=from.atStartOfDay();
        Map<String,Long> statuses=repository.countStatuses();
        long created=repository.countPosts(fromTime,toExclusive), previousCreated=repository.countPosts(previousFrom,previousTo);
        long interactionTotal=repository.countInteractions(fromTime,toExclusive), previousInteractions=repository.countInteractions(previousFrom,previousTo);
        Map<String,Long> interaction=repository.summarizeInteractions(fromTime,toExclusive);
        Map<LocalDate,Long> daily=repository.countDailyPosts(fromTime,toExclusive);
        List<PostAnalyticsResponse.TrendPoint> trend=new ArrayList<>();
        for(LocalDate day=from; !day.isAfter(to); day=day.plusDays(1)) trend.add(new PostAnalyticsResponse.TrendPoint(day,daily.getOrDefault(day,0L)));
        Map<String,Long> moderation=repository.summarizeModeration(fromTime,toExclusive);
        long action=moderation.getOrDefault("RESOLVED_ACTION_TAKEN",0L), noViolation=moderation.getOrDefault("RESOLVED_NO_VIOLATION",0L);
        long moderationTotal=moderation.getOrDefault("OPEN",0L)+action+noViolation;
        long published=statuses.getOrDefault("PUBLISHED",0L), hidden=statuses.getOrDefault("HIDDEN",0L), deleted=statuses.getOrDefault("DELETED",0L);
        var interactionSummary=new PostAnalyticsResponse.InteractionSummary(interaction.get("likes"),interaction.get("comments"),interaction.get("saves"),interaction.get("reposts"),interactionTotal,ratio(interactionTotal,created));
        return new PostAnalyticsResponse(from,to,
                new PostAnalyticsResponse.Kpis(published+hidden+deleted,published,created,change(created,previousCreated),hidden,deleted,interactionTotal,change(interactionTotal,previousInteractions)),
                new PostAnalyticsResponse.StatusDistribution(published,hidden,deleted), List.copyOf(trend), interactionSummary,
                repository.findTopPosts(fromTime,toExclusive),
                new PostAnalyticsResponse.ModerationSummary(moderation.getOrDefault("OPEN",0L),action,noViolation,percentage(action,moderationTotal)),
                repository.findMostReported(fromTime,toExclusive));
    }

    private BigDecimal change(long current,long previous){
        if(previous==0) return current==0 ? BigDecimal.ZERO : null;
        return BigDecimal.valueOf(current-previous).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(previous),1,RoundingMode.HALF_UP);
    }
    private BigDecimal ratio(long value,long base){return base==0?BigDecimal.ZERO:BigDecimal.valueOf(value).divide(BigDecimal.valueOf(base),1,RoundingMode.HALF_UP);}
    private BigDecimal percentage(long value,long base){return base==0?BigDecimal.ZERO:BigDecimal.valueOf(value*100).divide(BigDecimal.valueOf(base),1,RoundingMode.HALF_UP);}
}
