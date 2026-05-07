package com.internship.tool;

import com.internship.tool.entity.ChangeStatus;
import com.internship.tool.entity.Priority;
import com.internship.tool.entity.RegulatoryChange;
import com.internship.tool.repository.RegulatoryChangeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final RegulatoryChangeRepository repository;

    public DataLoader(RegulatoryChangeRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() == 0) {
            seedData();
        }
    }

    private void seedData() {
        List<RegulatoryChange> changes = new ArrayList<>();
        
        String[] categories = {"Compliance", "Risk", "Legal", "Financial", "Operational"};
        String[] bodies = {"SEC", "FINRA", "GDPR", "RBI", "FCA"};
        
        for (int i = 1; i <= 30; i++) {
            changes.add(RegulatoryChange.builder()
                    .title("Regulatory Update #" + i)
                    .description("This is a seeded description for regulatory update number " + i + ". It covers important changes in " + categories[i % 5] + " requirements.")
                    .regulatoryBody(bodies[i % 5])
                    .category(categories[i % 5])
                    .status(i % 4 == 0 ? ChangeStatus.APPROVED : (i % 3 == 0 ? ChangeStatus.SUBMITTED : ChangeStatus.DRAFT))
                    .priority(i % 3 == 0 ? Priority.P1 : (i % 2 == 0 ? Priority.P2 : Priority.P3))
                    .impactScore(new BigDecimal("5.0").add(new BigDecimal(i % 5)))
                    .effectiveDate(LocalDate.now().plusDays(i))
                    .deadline(LocalDate.now().plusDays(i + 30))
                    .assignedTo("user" + (i % 5 + 1) + "@example.com")
                    .isDeleted(false)
                    .build());
        }
        
        repository.saveAll(changes);
        System.out.println(">>> Seeded 30 realistic regulatory changes into the database.");
    }
}
