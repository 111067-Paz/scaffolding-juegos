package ar.edu.utn.frc.tup.p4.services.game.state;

import ar.edu.utn.frc.tup.p4.entities.Game;
import ar.edu.utn.frc.tup.p4.entities.GameStatus;
import ar.edu.utn.frc.tup.p4.entities.Move;
import ar.edu.utn.frc.tup.p4.entities.Player;
import ar.edu.utn.frc.tup.p4.exceptions.InvalidGameStateException;
import ar.edu.utn.frc.tup.p4.exceptions.InvalidMoveException;
import ar.edu.utn.frc.tup.p4.services.game.DiceRoller;
import ar.edu.utn.frc.tup.p4.services.game.TurnEngine;
import ar.edu.utn.frc.tup.p4.services.game.validation.MoveValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static ar.edu.utn.frc.tup.p4.support.GameFixtures.gameInProgress;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@DisplayName("Game states")
class GameStatesTest {

    @Nested
    @DisplayName("WaitingState")
    class Waiting {

        private final WaitingState state = new WaitingState();

        @Test
        @DisplayName("start moves the game to IN_PROGRESS with the first player's turn")
        void start_fromWaiting_setsInProgress() {
            Game game = gameInProgress(10, "Ana", "Beto");
            game.setStatus(GameStatus.WAITING);
            game.setCurrentTurn(1);

            state.start(game);

            assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
            assertEquals(0, game.getCurrentTurn());
            assertEquals(GameStatus.WAITING, state.supports());
        }

        @Test
        @DisplayName("roll is rejected before the game starts")
        void roll_whileWaiting_throws() {
            Game game = gameInProgress(10, "Ana", "Beto");
            assertThrows(InvalidGameStateException.class,
                    () -> state.roll(game, game.getPlayers().get(0)));
        }
    }

    @Nested
    @DisplayName("FinishedState")
    class Finished {

        private final FinishedState state = new FinishedState();

        @Test
        @DisplayName("start and roll are rejected in the terminal phase")
        void anyAction_whenFinished_throws() {
            Game game = gameInProgress(10, "Ana", "Beto");
            assertEquals(GameStatus.FINISHED, state.supports());
            assertThrows(InvalidGameStateException.class, () -> state.start(game));
            assertThrows(InvalidGameStateException.class,
                    () -> state.roll(game, game.getPlayers().get(0)));
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("InProgressState")
    class InProgress {

        @Mock
        private MoveValidator firstValidator;

        @Mock
        private MoveValidator secondValidator;

        @Mock
        private DiceRoller diceRoller;

        @Mock
        private TurnEngine turnEngine;

        private InProgressState state;
        private Game game;
        private Player ana;

        @BeforeEach
        void setUp() {
            state = new InProgressState(List.of(firstValidator, secondValidator), diceRoller, turnEngine);
            game = gameInProgress(10, "Ana", "Beto");
            ana = game.getPlayers().get(0);
        }

        @Test
        @DisplayName("start is rejected while in progress")
        void start_whenInProgress_throws() {
            assertEquals(GameStatus.IN_PROGRESS, state.supports());
            assertThrows(InvalidGameStateException.class, () -> state.start(game));
        }

        @Test
        @DisplayName("roll runs the whole chain in order, plays and passes the turn")
        void roll_withoutWinner_validatesPlaysAndAdvances() {
            // GIVEN
            Move move = new Move();
            when(diceRoller.roll()).thenReturn(4);
            when(turnEngine.play(game, ana, 4)).thenReturn(move);
            when(turnEngine.findWinner(game, ana)).thenReturn(Optional.empty());

            // WHEN
            Move result = state.roll(game, ana);

            // THEN
            assertSame(move, result);
            InOrder order = inOrder(firstValidator, secondValidator, turnEngine);
            order.verify(firstValidator).validate(game, ana);
            order.verify(secondValidator).validate(game, ana);
            order.verify(turnEngine).play(game, ana, 4);
            order.verify(turnEngine).advanceTurn(game);
            assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        }

        @Test
        @DisplayName("roll with a winner finishes the game and does not rotate the turn")
        void roll_withWinner_finishesGame() {
            when(diceRoller.roll()).thenReturn(6);
            when(turnEngine.play(game, ana, 6)).thenReturn(new Move());
            when(turnEngine.findWinner(game, ana)).thenReturn(Optional.of(ana));

            state.roll(game, ana);

            assertEquals(GameStatus.FINISHED, game.getStatus());
            assertSame(ana, game.getWinner());
            verify(turnEngine, never()).advanceTurn(game);
        }

        @Test
        @DisplayName("a failing link cuts the chain: no dice, no move")
        void roll_whenValidatorFails_stopsBeforePlaying() {
            doThrow(new InvalidMoveException("not your turn")).when(firstValidator).validate(game, ana);

            assertThrows(InvalidMoveException.class, () -> state.roll(game, ana));

            verify(secondValidator, never()).validate(game, ana);
            verify(diceRoller, never()).roll();
            verify(turnEngine, never()).play(any(), any(), anyInt());
        }
    }

    @Nested
    @DisplayName("GameStateRegistry")
    class Registry {

        @Test
        @DisplayName("get returns the state for the status, and fails for a missing one")
        void get_resolvesRegisteredStatesOnly() {
            WaitingState waiting = new WaitingState();
            GameStateRegistry registry = new GameStateRegistry(List.of(waiting));

            assertSame(waiting, registry.get(GameStatus.WAITING));
            assertThrows(IllegalStateException.class, () -> registry.get(GameStatus.FINISHED));
        }
    }
}
