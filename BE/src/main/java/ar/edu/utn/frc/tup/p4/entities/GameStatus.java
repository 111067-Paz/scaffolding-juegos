package ar.edu.utn.frc.tup.p4.entities;

/**
 * Lifecycle phases of a game. Persisted as STRING (readable, safe to reorder).
 * Each phase has its own behavior class (State pattern, see services/game/state).
 */
public enum GameStatus {
    WAITING,
    IN_PROGRESS,
    FINISHED
}
