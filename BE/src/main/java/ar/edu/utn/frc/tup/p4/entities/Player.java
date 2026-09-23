package ar.edu.utn.frc.tup.p4.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A participant of a game (hot-seat: several players share one browser).
 * Persisted like everything else in the game — never kept in memory.
 */
@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false, foreignKey = @ForeignKey(name = "fk_player_game"))
    private Game game;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(name = "turn_order", nullable = false)
    private Integer turnOrder;

    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false)
    private Integer lives;

    @Column(name = "skip_next_turn", nullable = false)
    private boolean skipNextTurn;

    public boolean isAlive() {
        return lives > 0;
    }
}
