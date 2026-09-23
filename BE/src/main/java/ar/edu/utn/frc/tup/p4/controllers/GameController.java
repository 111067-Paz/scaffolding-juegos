package ar.edu.utn.frc.tup.p4.controllers;

import ar.edu.utn.frc.tup.p4.configs.JwtAuthInterceptor;
import ar.edu.utn.frc.tup.p4.dtos.GameCreateRequest;
import ar.edu.utn.frc.tup.p4.dtos.GameDTO;
import ar.edu.utn.frc.tup.p4.dtos.GameSummaryDTO;
import ar.edu.utn.frc.tup.p4.dtos.MoveDTO;
import ar.edu.utn.frc.tup.p4.dtos.RollRequest;
import ar.edu.utn.frc.tup.p4.dtos.RollResultDTO;
import ar.edu.utn.frc.tup.p4.services.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Thin controller: the user id comes from the token (request attribute set by
 * JwtAuthInterceptor), NEVER from the body. No try/catch: domain exceptions are
 * translated by the GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@Tag(name = "Games", description = "Reference game: dice race (create, start, roll, history)")
public class GameController {

    private final GameService gameService;

    @PostMapping
    @Operation(summary = "Create a game (2-4 hot-seat players)")
    public ResponseEntity<GameDTO> create(
            @RequestAttribute(JwtAuthInterceptor.USER_ID_ATTRIBUTE) Long userId,
            @Valid @RequestBody GameCreateRequest request) {
        GameDTO created = gameService.create(userId, request);
        return ResponseEntity.created(URI.create("/api/games/" + created.getId())).body(created);
    }

    @GetMapping
    @Operation(summary = "List my games, newest first")
    public ResponseEntity<List<GameSummaryDTO>> findMine(
            @RequestAttribute(JwtAuthInterceptor.USER_ID_ATTRIBUTE) Long userId) {
        return ResponseEntity.ok(gameService.findMine(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one of my games with players and board")
    public ResponseEntity<GameDTO> findById(
            @RequestAttribute(JwtAuthInterceptor.USER_ID_ATTRIBUTE) Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(gameService.findById(id, userId));
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start a WAITING game")
    public ResponseEntity<GameDTO> start(
            @RequestAttribute(JwtAuthInterceptor.USER_ID_ATTRIBUTE) Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(gameService.start(id, userId));
    }

    @PostMapping("/{id}/roll")
    @Operation(summary = "Roll the dice for the player whose turn it is")
    public ResponseEntity<RollResultDTO> roll(
            @RequestAttribute(JwtAuthInterceptor.USER_ID_ATTRIBUTE) Long userId,
            @PathVariable Long id,
            @Valid @RequestBody RollRequest request) {
        return ResponseEntity.ok(gameService.roll(id, userId, request));
    }

    @GetMapping("/{id}/moves")
    @Operation(summary = "Move history of one of my games")
    public ResponseEntity<List<MoveDTO>> findMoves(
            @RequestAttribute(JwtAuthInterceptor.USER_ID_ATTRIBUTE) Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(gameService.findMoves(id, userId));
    }
}
