package br.lab.testesubmissao.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "test_case")
public class TestCase {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "input_path", nullable = false)
    private String inputPath;

    @Column(name = "output_path", nullable = false)
    private String outputPath;

    @Column(nullable = false)
    private Integer points = 0;

    @Column(name = "is_sample", nullable = false)
    private Boolean isSample = false;
}