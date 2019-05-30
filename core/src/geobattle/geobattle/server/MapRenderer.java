package geobattle.geobattle.server;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;

import geobattle.geobattle.map.GeoBattleCamera;

public interface MapRenderer extends Disposable {
    void drawAndReduceTiles(Batch batch, int xOffset, int yOffset, GeoBattleCamera camera);
}
