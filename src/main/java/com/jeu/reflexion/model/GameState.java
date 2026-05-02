package com.jeu.reflexion.model;

import java.io.Serializable;
import java.util.List;

public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Card> cards;
    private String level;
    private int gridCols;
    private int gridRows;
    private int totalPairs;
    private int matchedPairs;
    private int totalMoves;
    private int wrongMoves;
    private int score;
    private boolean gameOver;

    public static class Card implements Serializable {
        private static final long serialVersionUID = 1L;

        private int imageId;
        private boolean matched;

        public Card() {}

        public Card(int imageId) {
            this.imageId = imageId;
            this.matched = false;
        }

        public int getImageId() { return imageId; }
        public void setImageId(int imageId) { this.imageId = imageId; }

        public boolean isMatched() { return matched; }
        public void setMatched(boolean matched) { this.matched = matched; }
    }

    public List<Card> getCards() { return cards; }
    public void setCards(List<Card> cards) { this.cards = cards; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public int getGridCols() { return gridCols; }
    public void setGridCols(int gridCols) { this.gridCols = gridCols; }

    public int getGridRows() { return gridRows; }
    public void setGridRows(int gridRows) { this.gridRows = gridRows; }

    public int getTotalPairs() { return totalPairs; }
    public void setTotalPairs(int totalPairs) { this.totalPairs = totalPairs; }

    public int getMatchedPairs() { return matchedPairs; }
    public void setMatchedPairs(int matchedPairs) { this.matchedPairs = matchedPairs; }

    public int getTotalMoves() { return totalMoves; }
    public void setTotalMoves(int totalMoves) { this.totalMoves = totalMoves; }

    public int getWrongMoves() { return wrongMoves; }
    public void setWrongMoves(int wrongMoves) { this.wrongMoves = wrongMoves; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
}
