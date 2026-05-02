package com.jeu.reflexion.controller;

import com.jeu.reflexion.model.GameState;
import com.jeu.reflexion.model.Player;
import com.jeu.reflexion.model.SavedGame;
import com.jeu.reflexion.repository.SavedGameRepository;
import com.jeu.reflexion.service.GameService;
import com.jeu.reflexion.service.PlayerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class GameController {

    private final GameService gameService;
    private final PlayerService playerService;
    private final SavedGameRepository savedGameRepo;

    public GameController(GameService gameService, PlayerService playerService,
                          SavedGameRepository savedGameRepo) {
        this.gameService = gameService;
        this.playerService = playerService;
        this.savedGameRepo = savedGameRepo;
    }

    private Player getCurrentPlayer(HttpSession session) {
        Long id = (Long) session.getAttribute("playerId");
        if (id == null) return null;
        return playerService.findById(id);
    }

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        Player player = getCurrentPlayer(session);
        if (player == null) return "redirect:/login";

        model.addAttribute("player", player);
        model.addAttribute("savedGames", savedGameRepo.findByPlayerOrderBySavedAtDesc(player));
        model.addAttribute("topScores", playerService.getLeaderboard());
        return "home";
    }

    @GetMapping("/game/new")
    public String newGame(@RequestParam String level, HttpSession session) {
        Player player = getCurrentPlayer(session);
        if (player == null) return "redirect:/login";

        GameState state = gameService.createGame(level);
        session.setAttribute("gameState", state);
        return "redirect:/game";
    }

    @GetMapping("/game")
    public String game(HttpSession session, Model model) {
        Player player = getCurrentPlayer(session);
        if (player == null) return "redirect:/login";

        GameState state = (GameState) session.getAttribute("gameState");
        if (state == null) return "redirect:/home";

        model.addAttribute("state", state);
        model.addAttribute("player", player);
        String message = (String) session.getAttribute("gameMessage");
        if (message != null) {
            model.addAttribute("gameMessage", message);
            session.removeAttribute("gameMessage");
        }
        return "game";
    }

    @PostMapping("/game/flip")
    public String flip(@RequestParam int i1, @RequestParam int i2, HttpSession session) {
        Player player = getCurrentPlayer(session);
        if (player == null) return "redirect:/login";

        GameState state = (GameState) session.getAttribute("gameState");
        if (state == null) return "redirect:/home";

        boolean validMove = gameService.flip(state, i1, i2);
        if (!validMove) {
            session.setAttribute("gameMessage", "Coup invalide, veuillez réessayer.");
            return "redirect:/game";
        }

        if (state.isGameOver()) {
            playerService.updateBestScore(player, state.getScore(), state.getLevel());
        }
        return "redirect:/game";
    }

    @PostMapping("/game/save")
    public String save(HttpSession session) {
        Player player = getCurrentPlayer(session);
        if (player == null) return "redirect:/login";

        GameState state = (GameState) session.getAttribute("gameState");
        if (state == null) {
            session.setAttribute("gameMessage", "Aucune partie en cours.");
            return "redirect:/home";
        }

        if (state.isGameOver()) {
            session.setAttribute("gameMessage", "La partie est terminée.");
            return "redirect:/game";
        }

        try {
            gameService.saveGame(player, state);
            session.setAttribute("gameMessage", "Partie sauvegardée !");
        } catch (Exception e) {
            session.setAttribute("gameMessage", "Erreur lors de la sauvegarde.");
        }
        return "redirect:/game";
    }

    @GetMapping("/game/load/{id}")
    public String load(@PathVariable Long id, HttpSession session) {
        Player player = getCurrentPlayer(session);
        if (player == null) return "redirect:/login";

        Optional<SavedGame> saved = savedGameRepo.findById(id);
        if (saved.isPresent() && saved.get().getPlayer().getId().equals(player.getId())) {
            try {
                GameState state = gameService.loadGame(saved.get());
                session.setAttribute("gameState", state);
                return "redirect:/game";
            } catch (Exception e) {
                // fall through
            }
        }
        return "redirect:/home";
    }

    @PostMapping("/game/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        Player player = getCurrentPlayer(session);
        if (player == null) return "redirect:/login";

        savedGameRepo.findById(id).ifPresent(sg -> {
            if (sg.getPlayer().getId().equals(player.getId())) {
                savedGameRepo.delete(sg);
            }
        });
        return "redirect:/home";
    }

    @GetMapping("/scores")
    public String scores(HttpSession session, Model model) {
        Player player = getCurrentPlayer(session);
        if (player == null) return "redirect:/login";

        model.addAttribute("players", playerService.getLeaderboard());
        model.addAttribute("currentPlayer", player);
        return "scores";
    }
}
