package geobattle.geobattle.screens.gamescreen.gamescreenmodedata;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.Iterator;

import geobattle.geobattle.game.GameState;
import geobattle.geobattle.game.buildings.Building;
import geobattle.geobattle.game.buildings.Hangar;
import geobattle.geobattle.map.GeoBattleMap;
import geobattle.geobattle.util.IntRect;

// Select hangars for attack mode
public final class SelectHangarsMode extends GameScreenModeData {
    // Hangars selected by player
    private ArrayList<Hangar> selectedHangars;

    // Game state
    private final GameState gameState;

    public SelectHangarsMode(int pointedTileX, int pointedTileY, GameState gameState) {
        super(pointedTileX, pointedTileY);
        this.gameState = gameState;
        this.selectedHangars = new ArrayList<Hangar>();
    }

    // Returns count of selected hangars
    public int getSelectedHangarsCount() {
        return selectedHangars.size();
    }

    // Returns iterator over selected hangars
    public Iterator<Hangar> getSelectedHangars() {
        return selectedHangars.iterator();
    }

    // Sets pointed tile and adds or removes selected hangar
    @Override
    public void setPointedTile(int x, int y, boolean fromTransition) {
        super.setPointedTile(x, y, fromTransition);

        if (fromTransition)
            return;

        Building clickedBuilding = gameState.getCurrentPlayer().getBuilding(x, y);

        if (clickedBuilding instanceof Hangar) {
            boolean removed = false;
            for (int hangar = 0; hangar < selectedHangars.size(); hangar++)
                if (selectedHangars.get(hangar).id == clickedBuilding.id) {
                    selectedHangars.remove(hangar);
                    removed = true;
                    break;
                }

            if (!removed) {
                Hangar hangar = (Hangar) clickedBuilding;
                if (hangar.units.getCount() > 0)
                    selectedHangars.add(hangar);
            }
        }
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer, GeoBattleMap map, GameState gameState, IntRect visible) {
        for (int hangar = 0; hangar < selectedHangars.size(); hangar++) {
            map.drawRegionRectSubTiles(
                    selectedHangars.get(hangar).x, selectedHangars.get(hangar).y,
                    selectedHangars.get(hangar).getSizeX(), selectedHangars.get(hangar).getSizeY(),
                    new Color(0, 0, 1, 0)
            );
        }
    }
}
