package edu.wctc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DiceGame
{
    private final List<Player> players;
    private final List<Die> dice;
    private final int maxRolls;
    private Player currentPlayer;

    DiceGame (int countPlayers, int countDice, int maxRolls)
    {
        if (countPlayers < 2) throw new IllegalArgumentException();
        players = new ArrayList<>();
        dice = new ArrayList<>();
        currentPlayer = new Player();
        players.add(currentPlayer);
        for (int i = 1; i < countPlayers; i++)
        {
            players.add(new Player());
        }
        for (int i = 0; i < countDice; i++)
        {
            dice.add(new Die(6));
        }
        this.maxRolls =  maxRolls;
    }

    private boolean allDiceHeld()
    {
        return dice.stream().allMatch(Die::isBeingHeld);
    }

    public boolean autoHold(int faceValue)
    {
        if (isHoldingDie(faceValue)) return true;
        if (dice.stream().filter(die -> !die.isBeingHeld()).anyMatch(r -> r.getFaceValue() == faceValue))
        {
            dice.stream()
                    .filter(die -> !die.isBeingHeld() && die.getFaceValue() == faceValue)
                    .findFirst()
                    .ifPresent(Die::holdDie);
            return true;
        }
        else return false;
    }

    public boolean currentPlayerCanRoll()
    {
        return currentPlayer.getRollsUsed() != maxRolls && !allDiceHeld();
    }

    public int getCurrentPlayerNumber()
    {
        return currentPlayer.getPlayerNumber();
    }

    public int getCurrentPlayerScore()
    {
        return currentPlayer.getScore();
    }

    public String getDiceResults()
    {
        return dice.stream().map(die -> die.toString() + " ")
                .collect(Collectors.joining());
    }

    public String getFinalWinner()
    {

        return players.stream().max(Comparator.comparingInt(Player::getWins)).toString();
    }

    public String getGameResults()
    {
        players.forEach(player ->
        {
            if (player.getScore() == players.stream().mapToInt(Player::getScore).max().orElse(0)) player.addWin();
            else player.addLoss();
        });

        return players.stream()
                .sorted(Comparator.comparingInt(Player::getScore).reversed())
                .map(player -> player.toString() + " ")
                .collect(Collectors.joining());
    }

    public boolean isHoldingDie(int faceValue)
    {
        return dice.stream()
                .anyMatch(die -> die.isBeingHeld() && die.getFaceValue() == faceValue);
    }


    public boolean nextPlayer()
    {
        List<Player> remainingPlayers = players.stream().filter(player -> player.getRollsUsed() != maxRolls)
                .collect(Collectors.toList());

        if (remainingPlayers.isEmpty())
        {
            return false;
        }
        else
        {
            currentPlayer = remainingPlayers.get(0);
            return true;
        }
    }

    public void playerHold(char dieNum)
    {
        dice.stream().filter(die -> die.getDieNum() == dieNum).findFirst().ifPresent(Die::holdDie);
    }

    public void resetDice()
    {
        dice.forEach(Die::resetDie);
    }

    public void resetPlayers()
    {
        players.forEach(Player::resetPlayer);
    }


    public void rollDice()
    {
        currentPlayer.roll();
        dice.forEach(Die::rollDie);
    }

    public void scoreCurrentPlayer()
    {
        boolean crewCheck = false;
        boolean captainCheck = false;
        boolean shipCheck = false;

        for (Die die : dice)
        {
            switch (die.getFaceValue())
            {
                case 4: if (die.isBeingHeld()) crewCheck = true;
                case 5: if (die.isBeingHeld()) captainCheck = true;
                case 6: if (die.isBeingHeld()) shipCheck = true;
            }
        }
        if (crewCheck && captainCheck && shipCheck)
        {
            int score = 0;
            for (Die die : dice)
            {
                score += die.getFaceValue();
            }
            currentPlayer.setScore(score - 15);
        }
    }

    public void startNewGame()
    {
        players.sort(Comparator.comparingInt(Player::getScore));
        currentPlayer = players.stream().max(Comparator.comparing(Player::getScore)).orElse(null);
        resetPlayers();
    }
}
