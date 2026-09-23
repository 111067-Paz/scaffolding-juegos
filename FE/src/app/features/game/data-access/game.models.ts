/**
 * Mirrors the backend DTOs with the EXACT JSON keys (snake_case contract).
 * Dates arrive already formatted by the backend ("dd-MM-yyyy HH:mm:ss"):
 * render them as-is; never pass them to `new Date()` (see fechas skill).
 */
export type GameStatus = 'WAITING' | 'IN_PROGRESS' | 'FINISHED';

export type CellType = 'NORMAL' | 'ADVANCE' | 'BACK' | 'LOSE_TURN' | 'TRAP' | 'GOAL';

export interface Player {
  id: number;
  name: string;
  turn_order: number;
  position: number;
  lives: number;
  skip_next_turn: boolean;
  alive: boolean;
}

export interface Cell {
  position: number;
  type: CellType;
}

export interface Game {
  id: number;
  status: GameStatus;
  board_size: number;
  current_player_id: number | null;
  winner_id: number | null;
  players: Player[];
  cells: Cell[];
  created_at: string;
}

export interface GameSummary {
  id: number;
  status: GameStatus;
  board_size: number;
  winner_name: string | null;
  created_at: string;
}

export interface Move {
  id: number;
  player_id: number;
  player_name: string;
  dice_value: number;
  from_position: number;
  to_position: number;
  cell_type: CellType;
  created_at: string;
}

export interface RollResult {
  move: Move;
  game: Game;
}

export interface GameCreateRequest {
  player_names: string[];
  board_size: number;
}

/** Presentation metadata per cell type: a lookup table instead of a growing switch. */
export const CELL_META: Readonly<Record<CellType, { label: string; icon: string; css: string }>> = {
  NORMAL: { label: 'Normal', icon: '', css: 'bg-white border-slate-200' },
  ADVANCE: { label: 'Avanza 2', icon: '⏩', css: 'bg-emerald-50 border-emerald-300' },
  BACK: { label: 'Retrocede 3', icon: '⏪', css: 'bg-amber-50 border-amber-300' },
  LOSE_TURN: { label: 'Pierde turno', icon: '⏸', css: 'bg-sky-50 border-sky-300' },
  TRAP: { label: 'Trampa: −1 vida', icon: '☠', css: 'bg-red-50 border-red-300' },
  GOAL: { label: 'Meta', icon: '🏁', css: 'bg-indigo-100 border-indigo-400' },
};

export const STATUS_LABEL: Readonly<Record<GameStatus, string>> = {
  WAITING: 'Esperando',
  IN_PROGRESS: 'En curso',
  FINISHED: 'Terminada',
};
