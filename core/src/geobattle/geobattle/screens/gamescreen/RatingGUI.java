package geobattle.geobattle.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisImage;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import geobattle.geobattle.GeoBattleAssets;
import geobattle.geobattle.rating.RatingEntry;

// GUI for rating
public final class RatingGUI {
    // GUI skin
    private Skin skin;

    // Root dialog
    public final VisDialog root;

    // Entries of rating
    public final VisTable ratingEntries;

    // Entry of rating for current player
    public final VisTable currentPlayerRatingEntry;

    // Array list with entries of rating
    private final ArrayList<RatingEntry> rating;

    // Count of rating entries per page
    private int entriesPerPage;

    // Current page
    private int currentPage;

    public RatingGUI(AssetManager assetManager, GameScreen screen) {
        skin = assetManager.get(GeoBattleAssets.GUI_SKIN);

        root = new VisDialog(screen.getI18NBundle().get("rating"));

        rating = new ArrayList<RatingEntry>();
        ratingEntries = new VisTable();
        currentPlayerRatingEntry = new VisTable();

        init(screen);
    }

    public void init(final GameScreen screen) {
        final float listHeight = Gdx.graphics.getHeight() - 210 - 2 * Gdx.graphics.getPpcY() * 0.9f;
        final float entryHeight = Gdx.graphics.getPpcY() * 0.6f + 10;
        entriesPerPage = (int) (listHeight / entryHeight);
        updateItems(screen);

        root.getContentTable().clear();
        root.getButtonsTable().clear();
        root.getContentTable().pad(20);

        root.getContentTable().add(ratingEntries)
                .width(Gdx.graphics.getWidth() - 120)
                .height(Gdx.graphics.getHeight() - 210 - 2 * Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);
        root.getContentTable().row();
        root.getContentTable().add(currentPlayerRatingEntry)
                .width(Gdx.graphics.getWidth() - 120)
                .height(Gdx.graphics.getPpcY() * 0.9f)
                .pad(5);

        VisImageButton close = new VisImageButton("buttonBack");
        close.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                root.hide();
            }
        });
        root.getButtonsTable().add(close)
                .width(Gdx.graphics.getPpcX() * 0.9f)
                .height(Gdx.graphics.getPpcY() * 0.9f);

        VisImageButton update = new VisImageButton("buttonUpdate");
        update.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.getGameEvents().onRatingRequestEvent();
            }
        });
        root.getButtonsTable().add(update)
                .width(Gdx.graphics.getPpcX() * 0.9f)
                .height(Gdx.graphics.getPpcY() * 0.9f);

        VisImageButton prevPage = new VisImageButton("buttonPrev");
        prevPage.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                currentPage--;
                updateItems(screen);
            }
        });
        root.getButtonsTable().add(prevPage)
                .width(Gdx.graphics.getPpcX() * 0.9f)
                .height(Gdx.graphics.getPpcY() * 0.9f);

        VisImageButton nextPage = new VisImageButton("buttonNext");
        nextPage.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                currentPage++;
                updateItems(screen);
            }
        });
        root.getButtonsTable().add(nextPage)
                .width(Gdx.graphics.getPpcX() * 0.9f)
                .height(Gdx.graphics.getPpcY() * 0.9f);

        root.center();
    }

    // Creates view for entry
    private VisTable createView(int place, final RatingEntry ratingEntry, final GameScreen screen) {
        VisTable result = new VisTable();

        VisTable root = new VisTable();
        root.setBackground(ratingEntry.isCurrentPlayer() ? "listItemSelected" : "listItemDeselected");

        if (place > 3) {
            VisLabel label = new VisLabel(String.valueOf(place), "large");
            label.setAlignment(Align.center);
            root.add(label)
                    .width(Gdx.graphics.getPpcX() * 0.6f)
                    .height(Gdx.graphics.getPpcY() * 0.6f);
        } else {
            root.add(new VisImage(String.format(Locale.US, "award%d", place)))
                    .width(Gdx.graphics.getPpcX() * 0.6f)
                    .height(Gdx.graphics.getPpcY() * 0.6f);
        }

        root.add(new VisLabel(ratingEntry.getName()))
                .expandX()
                .height(Gdx.graphics.getPpcY() * 0.6f)
                .align(Align.center);

        root.add(new VisLabel(String.valueOf(ratingEntry.wealth)))
                .width(120)
                .height(Gdx.graphics.getPpcY() * 0.6f)
                .align(Align.center);

        VisImageButton toBase = new VisImageButton("buttonToOtherBase");
        toBase.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                screen.onMoveToBase(ratingEntry.playerId);
                RatingGUI.this.root.hide();
            }
        });
        root.add(toBase)
                .width(Gdx.graphics.getPpcX() * 0.6f)
                .height(Gdx.graphics.getPpcY() * 0.6f);

        result.add(root)
                .growX()
                .padTop(5)
                .padBottom(5);

        return result;
    }

    // Updates table of items
    private void updateItems(GameScreen screen) {
        currentPage = Math.min(MathUtils.ceil((float) rating.size() / entriesPerPage) - 1, currentPage);
        currentPage = Math.max(currentPage, 0);

        ratingEntries.clear();

        int startIndex = entriesPerPage * currentPage;
        for (int index = startIndex; index < startIndex + entriesPerPage && index < rating.size(); index++) {
            ratingEntries.add(createView(index + 1, rating.get(index), screen))
                    .growX();
            ratingEntries.row();
        }

        ratingEntries.top();
    }

    public void setRating(RatingEntry[] rating, GameScreen screen) {
        this.rating.clear();
        Arrays.sort(rating, new Comparator<RatingEntry>() {
            @Override
            public int compare(RatingEntry entry1, RatingEntry entry2) {
                return entry2.wealth - entry1.wealth;
            }
        });
        Collections.addAll(this.rating, rating);
        updateItems(screen);

        currentPlayerRatingEntry.clear();
        for (int index = 0; index < this.rating.size(); index++) {
            if (this.rating.get(index).isCurrentPlayer()) {
                currentPlayerRatingEntry.add(createView(index + 1, this.rating.get(index), screen))
                        .growX();
                break;
            }
        }
    }
}
