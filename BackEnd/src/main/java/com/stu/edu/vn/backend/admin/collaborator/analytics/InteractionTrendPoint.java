package com.stu.edu.vn.backend.admin.collaborator.analytics;

import java.time.LocalDate;

public record InteractionTrendPoint(LocalDate date, long likes, long comments, long reposts) { }
