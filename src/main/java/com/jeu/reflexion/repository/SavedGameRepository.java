package com.jeu.reflexion.repository;

import com.jeu.reflexion.model.Player;
import com.jeu.reflexion.model.SavedGame;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SavedGameRepository extends JpaRepository<SavedGame, Long> {
    List<SavedGame> findByPlayerOrderBySavedAtDesc(Player player);
    Optional<SavedGame> findByPlayerAndLevel(Player player, String level);
}
