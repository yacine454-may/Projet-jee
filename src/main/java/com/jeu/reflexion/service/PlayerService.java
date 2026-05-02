package com.jeu.reflexion.service;

import com.jeu.reflexion.model.Player;
import com.jeu.reflexion.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {

    private final PlayerRepository playerRepo;

    public PlayerService(PlayerRepository playerRepo) {
        this.playerRepo = playerRepo;
    }

    public Player register(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Le nom d'utilisateur est obligatoire");
        }
        if (password == null || password.length() < 4) {
            throw new RuntimeException("Le mot de passe doit contenir au moins 4 caractères");
        }
        if (playerRepo.findByUsername(username.trim()).isPresent()) {
            throw new RuntimeException("Ce nom d'utilisateur est déjà pris");
        }
        Player player = new Player();
        player.setUsername(username.trim());
        player.setPassword(password);
        return playerRepo.save(player);
    }

    public Optional<Player> login(String username, String password) {
        return playerRepo.findByUsername(username)
                .filter(p -> p.getPassword().equals(password));
    }

    public void updateBestScore(Player player, int score, String level) {
        if (score > player.getBestScore()) {
            player.setBestScore(score);
            player.setBestLevel(level);
            playerRepo.save(player);
        }
    }

    public List<Player> getLeaderboard() {
        return playerRepo.findTop10ByOrderByBestScoreDesc();
    }

    public Player findById(Long id) {
        return playerRepo.findById(id).orElse(null);
    }
}
