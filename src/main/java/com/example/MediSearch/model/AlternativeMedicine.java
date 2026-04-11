package com.example.MediSearch.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Maps a medicine to its alternatives.
 * When a medicine is sold, its salesCount increases.
 * Alternatives are fetched sorted by salesCount DESC.
 */
@Entity
@Table(name = "alternative_medicines")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AlternativeMedicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The medicine that has alternatives
    @ManyToOne
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    // The alternative medicine name (generic name e.g. "Paracetamol")
    private String genericName;

    // Composite: salesCount tracks how often this alternative was bought
    private Long salesCount = 0L;
}