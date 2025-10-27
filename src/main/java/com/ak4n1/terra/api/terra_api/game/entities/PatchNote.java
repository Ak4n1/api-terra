package com.ak4n1.terra.api.terra_api.game.entities;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "patch_notes")
public class PatchNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ElementCollection
    @CollectionTable(name = "patch_note_contents", joinColumns = @JoinColumn(name = "patch_note_id"))
    @Column(name = "line")
    private List<String> content;

    @Temporal(TemporalType.DATE)
    private Date releaseDate; // corregido typo: releaseData → releaseDate

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<String> getContent() { return content; }
    public void setContent(List<String> content) { this.content = content; }

    public Date getReleaseDate() { return releaseDate; }
    public void setReleaseDate(Date releaseDate) { this.releaseDate = releaseDate; }
}
