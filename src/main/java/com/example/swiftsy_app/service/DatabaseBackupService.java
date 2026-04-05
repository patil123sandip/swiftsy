package com.example.swiftsy_app.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DatabaseBackupService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseBackupService.class);

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.backup.enabled:true}")
    private boolean backupEnabled;

    @Value("${app.backup.path:./backups/}")
    private String backupPathStr;

    @Value("${app.backup.on-startup:true}")
    private boolean backupOnStartup;

    public DatabaseBackupService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        if (backupEnabled && backupOnStartup) {
            log.info("Executing on-startup database backup...");
            performBackup("startup");
        }
    }

    // Runs based on cron expression in application.properties. Default is midnight every day: "0 0 0 * * ?"
    @Scheduled(cron = "${app.backup.cron:0 0 0 * * ?}")
    public void scheduledBackup() {
        if (backupEnabled) {
            log.info("Executing scheduled database backup...");
            performBackup("scheduled");
        }
    }

    private void performBackup(String prefix) {
        try {
            File backupDir = new File(backupPathStr);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("%s_backup_%s.zip", prefix, timestamp);
            File backupFile = new File(backupDir, fileName);

            // H2 specific backup command
            String sql = String.format("BACKUP TO '%s'", backupFile.getAbsolutePath());
            jdbcTemplate.execute(sql);

            log.info("Successfully backed up database to: {}", backupFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("Failed to backup database", e);
        }
    }
}
