package br.lab.testesubmissao.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "verdict")
public class Verdict {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @ManyToOne
    @JoinColumn(name = "test_case_id", nullable = false)
    private TestCase testCase;

    // ✅ CORRIGIDO: era length = 10, mas "wrong_answer" = 12 chars,
    //    "time_limit_exceeded" = 19, "memory_limit_exceeded" = 21.
    //    length = 30 cobre todos os casos possíveis.
    @Column(nullable = false, length = 30)
    private String result;

    @Column(name = "execution_time_ms")
    private Integer executionTimeMs;

    @Column(name = "memory_used_kb")
    private Integer memoryUsedKb;

    @Column(name = "checker_output", columnDefinition = "TEXT")
    private String checkerOutput;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Submission getSubmission() { return submission; }
    public void setSubmission(Submission submission) { this.submission = submission; }

    public TestCase getTestCase() { return testCase; }
    public void setTestCase(TestCase testCase) { this.testCase = testCase; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public Integer getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Integer executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public Integer getMemoryUsedKb() { return memoryUsedKb; }
    public void setMemoryUsedKb(Integer memoryUsedKb) { this.memoryUsedKb = memoryUsedKb; }

    public String getCheckerOutput() { return checkerOutput; }
    public void setCheckerOutput(String checkerOutput) { this.checkerOutput = checkerOutput; }
}
