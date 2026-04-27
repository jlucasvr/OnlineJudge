package br.lab.testesubmissao.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "problem_tag")
public class ProblemTag {

    @EmbeddedId
    private ProblemTagId id;

    @ManyToOne
    @MapsId("problemId")
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @ManyToOne
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private Tag tag;

    public ProblemTag() {}

    public ProblemTag(Problem problem, Tag tag) {
        this.problem = problem;
        this.tag = tag;
        this.id = new ProblemTagId(problem.getId(), tag.getId());
    }

    public ProblemTagId getId() {
        return id;
    }

    public void setId(ProblemTagId id) {
        this.id = id;
    }

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }
}