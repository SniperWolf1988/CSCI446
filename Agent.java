
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Jacob Ziehli // Since nobody else gonna do this project...
 * 
 * 
 * Welcome to my nightmare. Mwhahahaha!!
 */
public class Agent {

    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    };

    public enum Action {
        SHOOT, STEP, PICKUP, EXIT
    };
    public World environment;
    public Tile start;
    public Direction current = Direction.RIGHT;
    public int x = 0, y = 0;
    private int score = 1000;
    private boolean hasArrow = true, wumpusKilled = false, hasGold = false; //One arrow at start. This agent will just "imagine" the Wumpus is dead if he kills it. IE. the simplest way I could think to make this work was to just disable death when agent kils the wumpus xD
    private HashMap<Integer, Tile> explored = new HashMap<Integer, Tile>();
    private HashMap<Integer, FrontierTile> frontier = new HashMap<Integer, FrontierTile>();
    public Stack<String> movement = new Stack<String>();

    public Agent(World w) {
        this.environment = w;
        this.start = environment.world[x][y];
        this.explored.put(environment.world[x][y].hashCode(), environment.world[x][y]);
        FrontierTile tmp = new FrontierTile();
        tmp.t = environment.world[x - 1][y]; //Above start.

        this.frontier.put(tmp.hashCode(), tmp);
        tmp.t = environment.world[x][y + 1]; //Right of start.
        this.frontier.put(tmp.hashCode(), tmp);
    }

    private void Search() {
        Tile temp;
        while (this.score > 0) {
            if (ThereIsDanger(environment.world[x][y])) {
                if((temp = FirstOtherOption()) != null)               {
                    //Take the other option
                    GoTo(temp);
                    
                } else  //If no other option...
                {
                    DoAction(Action.STEP);   //YOLO  xD 
                }
            } else 
            {
                //Go forward
                DoAction(Action.STEP);
            }
        }
    }

    private Tile FirstOtherOption() {
        Iterator it = frontier.keySet().iterator();
        FrontierTile tmp = frontier.get((Integer) it.next());
        while (tmp != null) {
            if (tmp.wumpusHere < 0.5f || tmp.pitHere < 0.5f) {
                if( x > 0 && explored.containsValue(environment.world[x-1][y]))
                {
                    return environment.world[x-1][y];
                }
                if( y > 0 && explored.containsValue(environment.world[x][y-1]))
                {
                    return environment.world[x][y-1];
                }
                
                if( x < environment.dimension - 1 && explored.containsValue(environment.world[x+1][y]))
                {
                    return environment.world[x+1][y];
                }if( x <  environment.dimension - 1 && explored.containsValue(environment.world[x][y+1]))
                {
                    return environment.world[x][y+1];
                }
                
            }
            tmp = frontier.get((Integer) it.next());
        }
        return null;
    }
    
    
    private void GoTo(Tile goal)
    {
        Tile current = environment.world[x][y];
        while(!current.equals(goal))
        {
            current = environment.world[x-1][y];
        }
    }

    private void DoAction(Action act) {
        if (act == Action.STEP) {
            DoStep();
        } else {
            if (act == Action.SHOOT && hasArrow) {
                DoShoot();
            } else {
                if (act == Action.PICKUP) {
                    PickUpGold();
                } else {
                    if (act == Action.EXIT) {
                        EXIT();
                    }
                }
            }
        }
    }

    private void PickUpGold() {
        if (!hasGold && ThereIsGoldHere()) {
            hasGold = true;
            score += 1000;
            tracking();
        }
    }

    private void EXIT() {
        if (x == environment.dimension - 1 && y == 0) {
            System.out.println("Agent finished!");
            System.out.println("Score was:\t" + score);

            if (hasGold) {
                System.out.println("Exited with gold.");
            } else {
                System.out.println("Agent wuzzed out.");
            }
        }
    }

    private void tracking() {
        System.out.println("Time to leave this place!")
        String Temp;
        while (movement.peek() != null){
            Temp = movement.pop();
            if(Temp == "UP"){
                current = Direction.DOWN;
            }
            else if (Temp == "DOWN"){
                current = Direction.UP;
            }
            else if (Temp == "RIGHT"){
                current = Direction.LEFT;
            }
            else{
                current = Direction.RIGHT;
            }
            DoStep();
        }
    }

    private void DoStep() {

        switch (current) { //Determine how we want to step based on current direction.
            case UP:
                if ((x - 1) > 0) //If in bounds
                {
                    x -= 1; //Move.
                    movement.push("UP");
                }
                break;
            case DOWN:
                if ((x + 1) < environment.world[0].length - 1) //Only using the world size to prevent out of bounds errors. The agent ignores the fact that we have access to it. No agent cheating! :D 
                {
                    x += 1;
                    movement.push("DOWN");
                }
                break;
            case LEFT:
                if ((y - 1) > 0) {
                    y -= 1;
                    movement.push("LEFT");
                }
                break;
            case RIGHT:
                if ((y + 1) < environment.world[0].length - 1) {
                    y += 1;
                    movement.push("RIGHT");
                }
                break;

        }

        score -= 1;
        Tile tile = environment.world[x][y];
        if (!explored.containsKey(tile.hashCode())) {
            explored.put(tile.hashCode(), tile);
        }

    }

    private void Die(boolean killedByWumpus) {
        System.err.println("Agent died!");
        System.err.println("Score was:\t" + score + "\tCause of death was:=\t" + (killedByWumpus ? "Killed by wumpus!" : "Out of points.")); //Used ternary operator because lazy.
    }

    private boolean ThereIsWumpusHere(int f, int g) {
        return !wumpusKilled && environment.world[f][g].object1 == Tile.Type.WUMPUS || environment.world[f][g].object2 == Tile.Type.WUMPUS;
    }

    private boolean ThereIsGoldHere() {
        return environment.world[x][y].object1 == Tile.Type.GOLD || environment.world[x][y].object2 == Tile.Type.GOLD;
    }

    private boolean ThereIsDanger(Tile t) {
        return t.object1 == Tile.Type.BREEZE || (t.object2 == Tile.Type.STENCH && !wumpusKilled);
    }

    private void DoShoot() {
        hasArrow = false;
        score -= 10;
        switch (current) { //Determine how we want to step based on current direction.
            case UP:
                for (int i = x; i > 0; i--) {
                    if (ThereIsWumpusHere(i, y)) {
                        wumpusKilled = true;
                        return;
                    }
                }
                break;
            case DOWN:
                for (int i = x; i < environment.world[0].length; i++) {
                    if (ThereIsWumpusHere(i, y)) {
                        wumpusKilled = true;
                        return;
                    }
                }
                break;
            case LEFT:
                for (int i = y; i > 0; i--) {
                    if (ThereIsWumpusHere(x, i)) {
                        wumpusKilled = true;
                        return;
                    }
                }
                break;
            case RIGHT:
                for (int i = y; i < environment.world[0].length; i++) {
                    if (ThereIsWumpusHere(x, i)) {
                        wumpusKilled = true;
                        return;
                    }
                }
                break;

        }

    }

}
