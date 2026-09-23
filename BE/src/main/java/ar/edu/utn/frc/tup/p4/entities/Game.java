package ar.edu.utn.frc.tup.p4.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate root of the reference game ("dice race").
 * The WHOLE game state lives here and in its children (players, cells, moves):
 * restarting the app never loses a game. Patterns (State/Strategy/Chain) are
 * stateless beans that receive this entity, mutate it, and the service persists it.
 */
@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owner = authenticated user who created it. Ownership is checked in the service. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, foreignKey = @ForeignKey(name = "fk_game_owner"))
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameStatus status;

    @Column(name = "board_size", nullable = false)
    private Integer boardSize;

    /** Index (turnOrder) of the player whose turn it is. */
    @Column(name = "current_turn", nullable = false)
    private Integer currentTurn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id", foreignKey = @ForeignKey(name = "fk_game_winner"))
    private Player winner;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("turnOrder ASC")
    private List<Player> players = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<Cell> cells = new ArrayList<>();

    /**
     * Optimistic locking: two concurrent rolls on the same game → the second
     * commit fails (ObjectOptimisticLockingFailureException → 409 in the handler).
     */
    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Touched on every change so each turn bumps @Version even if nothing else on the row changes. */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** Keeps both sides of the bidirectional relation in sync. */
    public void addPlayer(Player player) {
        player.setGame(this);
        players.add(player);
    }

    public void addCell(Cell cell) {
        cell.setGame(this);
        cells.add(cell);
    }

    public Player getCurrentPlayer() {
        return players.get(currentTurn);
    }

    public int getGoalPosition() {
        return boardSize - 1;
    }
}
