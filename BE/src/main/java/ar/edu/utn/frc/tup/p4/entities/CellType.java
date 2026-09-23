package ar.edu.utn.frc.tup.p4.entities;

/**
 * Board cell types. Every type except GOAL has a CellEffect strategy
 * (services/game/effects); GOAL is resolved by the TurnEngine (victory).
 * Adding a type = one enum constant + one strategy class, zero switch.
 */
public enum CellType {
    NORMAL,
    ADVANCE,
    BACK,
    LOSE_TURN,
    TRAP,
    GOAL
}
