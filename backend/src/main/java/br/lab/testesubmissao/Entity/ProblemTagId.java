package br.lab.testesubmissao.Entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Embeddable;

@Embeddable
public class ProblemTagId implements Serializable {

    private UUID problemId;
    private UUID tagId;

    public ProblemTagId() {}

    public ProblemTagId(UUID problemId, UUID tagId) {
        this.problemId = problemId;
        this.tagId = tagId;
    }

    public UUID getProblemId() {
        return problemId;
    }

    public void setProblemId(UUID problemId) {
        this.problemId = problemId;
    }

    public UUID getTagId() {
        return tagId;
    }

    public void setTagId(UUID tagId) {
        this.tagId = tagId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProblemTagId that = (ProblemTagId) o;
        return Objects.equals(problemId, that.problemId) &&
               Objects.equals(tagId, that.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(problemId, tagId);
    }
}