/**
 * ClueReasoner.java - project skeleton for a propositional reasoner
 * for the game of Clue.  Unimplemented portions have the comment "TO
 * BE IMPLEMENTED AS AN EXERCISE".  The reasoner does not include
 * knowledge of how many cards each player holds.  See
 * http://cs.gettysburg.edu/~tneller/nsf/clue/ for details.
 *
 * @author Todd Neller
 * @version 1.0
 *

Copyright (C) 2005 Todd Neller

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

Information about the GNU General Public License is available online at:
  http://www.gnu.org/licenses/
To receive a copy of the GNU General Public License, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
02111-1307, USA.

 */

import java.io.*;
import java.util.*;

public class ClueReasoner 
{
    private int numPlayers;
    private int playerNum;
    private int numCards;
    private SATSolver solver;    
    private String caseFile = "cf";
    private String[] players = {"sc", "mu", "wh", "gr", "pe", "pl"};
    private String[] suspects = {"mu", "pl", "gr", "pe", "sc", "wh"};
    private String[] weapons = {"kn", "ca", "re", "ro", "pi", "wr"};
    private String[] rooms = {"ha", "lo", "di", "ki", "ba", "co", "bi", "li", "st"};
    private String[] cards;

    public ClueReasoner()
    {
        numPlayers = players.length;

        // Initialize card info
        cards = new String[suspects.length + weapons.length + rooms.length];
        int i = 0;
        for (String card : suspects)
            cards[i++] = card;
        for (String card : weapons)
            cards[i++] = card;
        for (String card : rooms)
            cards[i++] = card;
        numCards = i;

        // Initialize solver
        solver = new SATSolver();
        addInitialClauses();
    }

    private int getPlayerNum(String player) 
    {
        if (player.equals(caseFile))
            return numPlayers;
        for (int i = 0; i < numPlayers; i++)
            if (player.equals(players[i]))
                return i;
        System.out.println("Illegal player: " + player);
        return -1;
    }

    private int getCardNum(String card)
    {
        for (int i = 0; i < numCards; i++)
            if (card.equals(cards[i]))
                return i;
        System.out.println("Illegal card: " + card);
        return -1;
    }

    private int getPairNum(String player, String card) 
    {
        return getPairNum(getPlayerNum(player), getCardNum(card));
    }

    private int getPairNum(int playerNum, int cardNum)
    {
        return playerNum * numCards + cardNum + 1;
    }    

    public void addInitialClauses()
    {
        
        // Each card is in at least one place (including case file).
        for (int c = 0; c < numCards; c++) {
            int[] clause = new int[numPlayers + 1];
            for (int p = 0; p <= numPlayers; p++)
                clause[p] = getPairNum(p, c);
            solver.addClause(clause);
        }    
        
        // If a card is one place, it cannot be in another place.
        for (int c = 0; c < numCards; c++) {
            //For all the card, we go on 2-pair clauses
            //either player i does not have the crad or the player k does not have a card
            for (int p = 0; p <= numPlayers; p++) {
                for (int pt = p + 1; pt <= numPlayers; pt++) {
                    int[] clause = new int[2];
                    clause[0] = -getPairNum(p, c);
                    clause[1] = -getPairNum(pt, c);
                    solver.addClause(clause);
                }
            }
        }

        //At least one suspects must be in the caseFile
        int[] suspectsCase = new int[suspects.length];
        for (int s = 0; s < suspects.length; s++) {
            suspectsCase[s] = getPairNum(caseFile, suspects[s]);
        }
        solver.addClause(suspectsCase);

        //At least one weapons must be in the caseFile
        int[] weaponsCase = new int[weapons.length];
        for (int w = 0; w < weapons.length; w++) {
            weaponsCase[w] = getPairNum(caseFile, weapons[w]);
        }
        solver.addClause(weaponsCase);

        //At least one room must be in the caseFile
        int [] roomsCase = new int[rooms.length];
        for (int r = 0; r < rooms.length; r++) {
            roomsCase[r] = getPairNum(caseFile, rooms[r]);
        }
        solver.addClause(roomsCase);


        // No two cards in each category can both be in the case file.

        //No 2 suspects
        for (int s = 0; s < suspects.length; s++) {
            for (int o = s + 1; o < suspects.length; o++) {
                suspectsCase = new int[2];
                suspectsCase[0] = -getPairNum(caseFile, suspects[s]);
                suspectsCase[1] = -getPairNum(caseFile, suspects[o]);
                solver.addClause(suspectsCase);
            }
        }
        //No 2 suspects
        for (int w = 0; w < weapons.length; w++) {
            for (int o = w + 1; o < weapons.length; o++) {
                weaponsCase = new int[2];
                weaponsCase[0] = -getPairNum(caseFile, weapons[w]);
                weaponsCase[1] = -getPairNum(caseFile, weapons[o]);
                solver.addClause(suspectsCase);
            }
        }
        //No 2 rooms
        for (int r = 0; r < rooms.length; r++) {
            for (int o = r + 1; o < rooms.length; o++) {
                roomsCase = new int[2];
                roomsCase[0] = -getPairNum(caseFile, rooms[r]);
                roomsCase[1] = -getPairNum(caseFile, rooms[o]);
                solver.addClause(roomsCase);
            }
        }
    }


    public void hand(String player, String[] cards) 
    {
        solver.clearQueryClauses();
        playerNum = getPlayerNum(player);
        for (int c = 0; c < cards.length; c++) {
            int [] clause1 = new int[1];

            //1. set the player from whose perspective we are reasoning from
            //2. note that the given card are in the possesion of the player
            clause1[0] = getPairNum(player, cards[c]);

            solver.addClause(clause1);
        }
    }

    public void suggest(String suggester, String card1, String card2, 
                        String card3, String refuter, String cardShown) 
    {
        if(cardShown != null) {
            int numSuggester = getPlayerNum(suggester);
            int refuterNum = getPlayerNum(refuter);
            int cardShownNum = getCardNum(cardShown);

            int [] clause = new int[1];
            clause[0] = getPairNum(refuterNum, cardShownNum);
            solver.addClause(clause);

            for (int i = numSuggester + 1; i < refuterNum; i++) {
                String curPlayer = players[i];
                int [] suspect = { -getPairNum(curPlayer, card1) };
                int [] weapon = { -getPairNum(curPlayer, card2) };
                int [] room = { -getPairNum(curPlayer, card3) };

                solver.addClause(suspect);
                solver.addClause(weapon);
                solver.addClause(room);
            }
        } else {
            if (refuter != null) {
                int numSuggester = getPlayerNum(suggester);
                int refuterNum = getPlayerNum(refuter);

                int [] clause = new int [3];
                clause[0] = getPairNum(refuter, card1);
                clause[1] = getPairNum(refuter, card2);
                clause[2] = getPairNum(refuter, card3);
                solver.addClause(clause);

                for (int i = numSuggester + 1; i < refuterNum; i++) {
                    String curPlayer = players[i];
                    int [] suspect = { -getPairNum(curPlayer, card1) };
                    int [] weapon = { -getPairNum(curPlayer, card2) };
                    int [] room = { -getPairNum(curPlayer, card3) };

                    solver.addClause(suspect);
                    solver.addClause(weapon);
                    solver.addClause(room);
                }

            } else {
                int [] suspect = { getPairNum(suggester, card1), getPairNum(caseFile, card1) };
                int [] weapon = { getPairNum(suggester, card2), getPairNum(caseFile, card2)  };
                int [] room = { getPairNum(suggester, card3), getPairNum(caseFile, card3)  };

                solver.addClause(suspect);
                solver.addClause(weapon);
                solver.addClause(room);
            }
        }
    }

    public void accuse(String accuser, String card1, String card2, 
                       String card3, boolean isCorrect)
    {
        if (isCorrect) {
            int [] suspect = { getPairNum(caseFile, card1)};
            int [] weapon = { getPairNum(caseFile, card2)};
            int [] room = { getPairNum(caseFile, card3) };

            solver.addClause(suspect);
            solver.addClause(weapon);
            solver.addClause(room);
        } else {
            int [] clause = { -getPairNum(caseFile, card1), -getPairNum(caseFile, card2), -getPairNum(caseFile, card3)};
            solver.addClause(clause);
        }
    }

    public int query(String player, String card) 
    {
        return solver.testLiteral(getPairNum(player, card));
    }

    public String queryString(int returnCode) 
    {
        if (returnCode == SATSolver.TRUE)
            return "Y";
        else if (returnCode == SATSolver.FALSE)
            return "n";
        else
            return "-";
    }
        
    public void printNotepad() 
    {
        PrintStream out = System.out;
        for (String player : players)
            out.print("\t" + player);
        out.println("\t" + caseFile);
        for (String card : cards) {
            out.print(card + "\t");
            for (String player : players) 
                out.print(queryString(query(player, card)) + "\t");
            out.println(queryString(query(caseFile, card)));
        }
    }
        
    public static void main(String[] args) 
    {
        ClueReasoner cr = new ClueReasoner();
        String[] myCards = {"wh", "li", "st"};
        cr.hand("sc", myCards);
        cr.suggest("sc", "sc", "ro", "lo", "mu", "sc");
        cr.suggest("mu", "pe", "pi", "di", "pe", null);
        cr.suggest("wh", "mu", "re", "ba", "pe", null);
        cr.suggest("gr", "wh", "kn", "ba", "pl", null);
        cr.suggest("pe", "gr", "ca", "di", "wh", null);
        cr.suggest("pl", "wh", "wr", "st", "sc", "wh");
        cr.suggest("sc", "pl", "ro", "co", "mu", "pl");
        cr.suggest("mu", "pe", "ro", "ba", "wh", null);
        cr.suggest("wh", "mu", "ca", "st", "gr", null);
        cr.suggest("gr", "pe", "kn", "di", "pe", null);
        cr.suggest("pe", "mu", "pi", "di", "pl", null);
        cr.suggest("pl", "gr", "kn", "co", "wh", null);
        cr.suggest("sc", "pe", "kn", "lo", "mu", "lo");
        cr.suggest("mu", "pe", "kn", "di", "wh", null);
        cr.suggest("wh", "pe", "wr", "ha", "gr", null);
        cr.suggest("gr", "wh", "pi", "co", "pl", null);
        cr.suggest("pe", "sc", "pi", "ha", "mu", null);
        cr.suggest("pl", "pe", "pi", "ba", null, null);
        cr.suggest("sc", "wh", "pi", "ha", "pe", "ha");
        cr.suggest("wh", "pe", "pi", "ha", "pe", null);
        cr.suggest("pe", "pe", "pi", "ha", null, null);
        cr.suggest("sc", "gr", "pi", "st", "wh", "gr");
        cr.suggest("mu", "pe", "pi", "ba", "pl", null);
        cr.suggest("wh", "pe", "pi", "st", "sc", "st");
        cr.suggest("gr", "wh", "pi", "st", "sc", "wh");
        cr.suggest("pe", "wh", "pi", "st", "sc", "wh");
        cr.suggest("pl", "pe", "pi", "ki", "gr", null);
        cr.printNotepad();
        cr.accuse("sc", "pe", "pi", "bi", true);
    }           
}
