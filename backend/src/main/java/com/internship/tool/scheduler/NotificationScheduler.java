package com.internship.tool.scheduler;

import com.internship.tool.entity.ChangeStatus;
import com.internship.tool.entity.RegulatoryChange;
import com.internship.tool.repository.RegulatoryChangeRepository;
import com.internship.tool.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
public class NotificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);
    private final RegulatoryChangeRepository repository;
    private final EmailService emailService;

    // We don't want to alert on items that are already done
    private final List<ChangeStatus> excludedStatuses = Arrays.asList(ChangeStatus.IMPLEMENTED, ChangeStatus.ARCHIVED);

    public NotificationScheduler(RegulatoryChangeRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    // DEMO MODE: runs every 10 seconds (change back to "0 0 9 * * *" after demo)
    @Scheduled(cron = "*/10 * * * * *")
    public void sendDailyOverdueReminders() {
        logger.info("Running daily overdue reminder job...");
        List<RegulatoryChange> overdueItems = repository.findOverdueChanges(excludedStatuses);
        
        for (RegulatoryChange item : overdueItems) {
            emailService.sendOverdueAlert(item.getTitle(), item.getDeadline().toString());
        }
        logger.info("Finished processing {} overdue items.", overdueItems.size());
    }

    // DEMO MODE: runs every 10 seconds (change back to "0 0 9 * * *" after demo)
    @Scheduled(cron = "*/10 * * * * *")
    public void sendAdvanceDeadlineAlerts() {
        logger.info("Running advance deadline alert job...");
        LocalDate nextWeek = LocalDate.now().plusDays(7);
        List<RegulatoryChange> upcomingItems = repository.findUpcomingDeadlines(excludedStatuses, nextWeek);

        for (RegulatoryChange item : upcomingItems) {
            emailService.sendAdvanceDeadlineAlert(item.getTitle(), item.getDeadline().toString());
        }
        logger.info("Finished processing {} upcoming items.", upcomingItems.size());
    }

    // Runs every Monday at 9:00 AM
    @Scheduled(cron = "0 0 9 * * MON")
    public void sendWeeklySummary() {
        logger.info("Running weekly summary job...");
        List<RegulatoryChange> activeItems = repository.findActiveChangesForSummary(excludedStatuses);
        List<RegulatoryChange> overdueItems = repository.findOverdueChanges(excludedStatuses);

        if (!activeItems.isEmpty()) {
            emailService.sendWeeklySummary(activeItems.size(), overdueItems.size());
        }
        logger.info("Finished weekly summary job.");
    }
}
