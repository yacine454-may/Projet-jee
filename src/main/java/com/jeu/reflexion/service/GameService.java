package com.jeu.reflexion.service;

import com.jeu.reflexion.model.GameState;
import com.jeu.reflexion.model.Player;
import com.jeu.reflexion.model.SavedGame;
import com.jeu.reflexion.repository.SavedGameRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

@Service
public class GameService {

    private final SavedGameRepository savedGameRepo;

    public GameService(SavedGameRepository savedGameRepo) {
        this.savedGameRepo = savedGameRepo;
    }

    public GameState createGame(String level) {
        int pairs, cols, rows;
        switch (level.toUpperCase()) {
            case "EASY":   pairs = 8;  cols = 4; rows = 4; break;
            case "MEDIUM": pairs = 12; cols = 6; rows = 4; break;
            default:       pairs = 18; cols = 6; rows = 6; break;
        }

        List<Integer> imageIds = new ArrayList<>();
        for (int i = 1; i <= pairs; i++) {
            imageIds.add(i);
            imageIds.add(i);
        }
        Collections.shuffle(imageIds);

        List<GameState.Card> cards = new ArrayList<>();
        for (int id : imageIds) {
            cards.add(new GameState.Card(id));
        }

        GameState state = new GameState();
        state.setCards(cards);
        state.setLevel(level.toUpperCase());
        state.setGridCols(cols);
        state.setGridRows(rows);
        state.setTotalPairs(pairs);
        state.setMatchedPairs(0);
        state.setTotalMoves(0);
        state.setWrongMoves(0);
        state.setScore(pairs * 100);
        state.setGameOver(false);
        return state;
    }

    public boolean flip(GameState state, int i1, int i2) {
        List<GameState.Card> cards = state.getCards();

        if (i1 < 0 || i2 < 0 || i1 >= cards.size() || i2 >= cards.size() || i1 == i2) {
            return false;
        }
        if (cards.get(i1).isMatched() || cards.get(i2).isMatched()) {
            return false;
        }

        state.setTotalMoves(state.getTotalMoves() + 1);
        boolean matched = cards.get(i1).getImageId() == cards.get(i2).getImageId();

        if (matched) {
            cards.get(i1).setMatched(true);
            cards.get(i2).setMatched(true);
            state.setMatchedPairs(state.getMatchedPairs() + 1);
            boolean gameOver = state.getMatchedPairs() == state.getTotalPairs();
            state.setGameOver(gameOver);
        } else {
            state.setWrongMoves(state.getWrongMoves() + 1);
            int newScore = Math.max(0, state.getTotalPairs() * 100 - state.getWrongMoves() * 10);
            state.setScore(newScore);
        }
        return true;
    }

    public void saveGame(Player player, GameState state) throws Exception {
        String encodedState = serializeGameState(state);

        SavedGame save = savedGameRepo.findByPlayerAndLevel(player, state.getLevel())
                .orElse(new SavedGame());

        save.setPlayer(player);
        save.setLevel(state.getLevel());
        save.setBoardState(encodedState);
        save.setMoves(state.getTotalMoves());
        save.setWrongMoves(state.getWrongMoves());
        save.setScore(state.getScore());
        savedGameRepo.save(save);
    }

    public GameState loadGame(SavedGame savedGame) throws Exception {
        return deserializeGameState(savedGame.getBoardState());
    }

    private String serializeGameState(GameState state) throws Exception {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(state);
            oos.flush();
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        }
    }

    private GameState deserializeGameState(String data) throws Exception {
        byte[] raw = Base64.getDecoder().decode(data);
        try (ByteArrayInputStream bis = new ByteArrayInputStream(raw);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (GameState) ois.readObject();
        }
    }
}
