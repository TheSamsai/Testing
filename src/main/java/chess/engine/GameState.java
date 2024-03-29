/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chess.engine;

import java.util.ArrayList;
import java.util.Arrays;
import chess.model.Side;
import chess.bot.TestBot;
import org.json.JSONObject;

/**
 * Stores the state of a game of chess
 * Provides access to currently available moves
 */
public class GameState {
    
    public String id;
    
    public String playingBlack;
    public String playingWhite;
    
    public Side playing;
    public Side turn = Side.WHITE;
    
    // Stores the remaining time in milliseconds
    public long whiteTime;
    public long blackTime;
    
    public ArrayList<String> moves;
    
    public TestBot engine = new TestBot("");
    
    public GameState() {
        this.moves = new ArrayList();
    }
    
    public int getMoveCount() {
        return moves.size();
    }
    
    public int getTurnCount() {
        return 1 + moves.size() / 2;
    }
    
    public long getRemainingTime() {
        if (playing == Side.WHITE) {
            return this.whiteTime;
        } else {
            return this.blackTime;
        }
    }
    
    public long getRemainingTimeOpponent() {
        if (playing == Side.WHITE) {
            return this.blackTime;
        } else {
            return this.whiteTime;
        }
    }
    public boolean myTurn() {
        return this.turn == this.playing;
    }
    
    /**
     * Parses a full game state from a gameFull JSON object
     * <b>Note:</b> Use only to gain the initial game state, game state should
     * be updated via updateFromJson()
     *
     * @param json String of JSON data according to https://lichess.org/api#operation/botGameStream
     * @return A full, initial game state
     */
    public static GameState parseFromJson(String json) {
        GameState gameState = new GameState();
        
        JSONObject jsonGameState = new JSONObject(json);
        
        if (jsonGameState.getString("type").equals("gameFull")) {
            gameState.id = jsonGameState.getString("id");
            
            gameState.playingWhite = jsonGameState.getJSONObject("white").optString("id");
            gameState.playingBlack = jsonGameState.getJSONObject("black").optString("id");
            
            String[] moves = jsonGameState.getJSONObject("state").getString("moves").split(" ");
            
            gameState.moves = new ArrayList<>(Arrays.asList(moves));
            
            gameState.whiteTime = jsonGameState.getJSONObject("state").getInt("wtime");
            gameState.blackTime = jsonGameState.getJSONObject("state").getInt("btime");
        }
        
        return gameState;
    }

    /**
     * Update a GameState object from JSON
     *
     * @param json String of JSON data according to https://lichess.org/api#operation/botGameStream
     */
    public void updateFromJson(String json) {
        JSONObject jsonGameState = new JSONObject(json);
        
        if (jsonGameState.getString("type").equals("gameFull")) {
            this.id = jsonGameState.getString("id");
            
            this.playingWhite = jsonGameState.getJSONObject("white").optString("id");
            this.playingBlack = jsonGameState.getJSONObject("black").optString("id");
            
            String[] moves = new String[0];  //lisätty new osa         
            if (!jsonGameState.getJSONObject("state").getString("moves").isEmpty()) {
                moves = jsonGameState.getJSONObject("state").getString("moves").trim().split(" ");
            } 
            
            this.moves = new ArrayList<>(Arrays.asList(moves));
            
            for (String i : this.moves) {
                System.out.println(i);
            }
            
            this.whiteTime = jsonGameState.getJSONObject("state").getInt("wtime");
            this.blackTime = jsonGameState.getJSONObject("state").getInt("btime");
        } else if (jsonGameState.getString("type").equals("gameState")) {
            String[] moves = jsonGameState.getString("moves").split(" ");
            
            this.moves = new ArrayList<>(Arrays.asList(moves));
            
            this.whiteTime = jsonGameState.getInt("wtime");
            this.blackTime = jsonGameState.getInt("btime");
        } else {
            // This would only have chat stuff, we probably don't need it.
        }
        
        parseLatestMove();
    }
    /**
     * Sets time for Player, used by XBoardHandler
     * @param time
     */
    public void setTimePlayer(long time) {
        if (playing == Side.WHITE) {
            this.whiteTime = time;
        } else {
            this.blackTime = time;
        }
    }
    /**
     * Sets time for Opponent, used by XBoardHandler
     * @param time
     */
    public void setTimeOpponent(long time) {
        if (playing == Side.BLACK) {
            this.whiteTime = time;
        } else {
            this.blackTime = time;
        }
    }
    /**
     * Parses a move in UCI move into the chess engine's move data type and
     * updates the engine's board state
     */
    public void parseLatestMove() {
        this.engine = new TestBot("");
        // We play all of the moves onto a new board to ensure a previously
        // started game can be resumed correctly, inefficient but it works
        if (!this.moves.isEmpty()) {
            this.moves.forEach(moveString -> {
                String startingString = moveString.substring(0, 2).toUpperCase();
                String endingString = moveString.substring(2, 4).toUpperCase();
                String promoteString = moveString.length() > 4 ? moveString
                        .substring(4).toUpperCase() : "".toUpperCase();
                this.engine.setMove(startingString, endingString, promoteString);
            });
        }
    }
    
    /**
     * Sets the current gamestate with moves passed as the parameters.
     * @param moves 0-n moves in UCI format
     */
    public void setMoves(String... moves) {
        this.moves = new ArrayList(Arrays.asList(moves));
    }
}
