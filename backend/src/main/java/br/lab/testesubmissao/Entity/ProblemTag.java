package br.lab.testesubmissao.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "problem_tag")
@IdClass(ProblemTagId.class)
public class ProblemTag {

    @Id
    @ManyToOne
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @Id
    @ManyToOne
    @JoinColumn(name = "tag_id")
    private Tag tag;
}
