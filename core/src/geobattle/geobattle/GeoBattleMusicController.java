package geobattle.geobattle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Disposable;

public final class GeoBattleMusicController implements Disposable {
    private final String[] tracks;

    private int currentTrack;

    private Music music;

    public GeoBattleMusicController(String[] tracks) {
        this.tracks = tracks;
    }

    public void nextTrack() {
        currentTrack = (int) (Math.random() * tracks.length);

        if (music != null) {
            music.stop();
            music.dispose();
        }

        music = Gdx.audio.newMusic(Gdx.files.internal(tracks[currentTrack]));
        music.setOnCompletionListener(new Music.OnCompletionListener() {
            @Override
            public void onCompletion(Music music) {
                nextTrack();
            }
        });
        music.play();
    }

    @Override
    public void dispose() {
        if (music != null) {
            music.dispose();
            music = null;
        }
    }
}
