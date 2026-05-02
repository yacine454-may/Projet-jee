package com.jeu.reflexion.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "saved_games")
public class SavedGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    private String level;
    private int moves;

    @Column(name = "wrong_moves")
    private int wrongMoves;

    private int score;

    @Column(name = "board_state", columnDefinition = "TEXT")
    private String boardState;

    @Column(name = "saved_at")
    private LocalDateTime savedAt;

    @PrePersist
    @PreUpdate
    public void updateSavedAt() {
        this.savedAt = LocalDateTime.now();
    }

    public String getFormattedDate() {
        if (savedAt == null) return "-";
        return savedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public int getMoves() { return moves; }
    public void setMoves(int moves) { this.moves = moves; }

    public int getWrongMoves() { return wrongMoves; }
    public void setWrongMoves(int wrongMoves) { this.wrongMoves = wrongMoves; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getBoardState() { return boardState; }
    public void setBoardState(String boardState) { this.boardState = boardState; }

    public LocalDateTime getSavedAt() { return savedAt; }
    public void setSavedAt(LocalDateTime savedAt) { this.savedAt = savedAt; }
}
