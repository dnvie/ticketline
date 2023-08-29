package at.ac.tuwien.sepm.groupphase.backend.entity;

import at.ac.tuwien.sepm.groupphase.backend.entity.enums.SectorType;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private SectorType type;

    @ManyToOne
    @JoinColumn(name = "seat_map_id")
    private SeatMap seatMap;

    @OneToMany(mappedBy = "sector", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Seat> seats;

    @Column(nullable = false, length = 500)
    private String name;
    @Column(nullable = false)
    private BigDecimal price;

    private Integer orientation;

    @Nullable
    private Integer lodgeSize;

    private Integer seatMapRow;

    private Integer seatMapColumn;

    @Column(nullable = false)
    @Min(value = 0)
    private Integer length;

    @Column(nullable = false)
    @Min(value = 0)
    private Integer width;

    private Boolean noUpdate;

    private Boolean standingSector;
}
