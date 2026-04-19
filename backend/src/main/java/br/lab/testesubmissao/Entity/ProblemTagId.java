package br.lab.testesubmissao.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table
public class ProblemTagId implements Serializable {

    @Id
    private Long id;
    private UUID problem;
    private UUID tag;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    // equals e hashCode
}