package geobattle.geobattle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Disposable;

// Music controller for game
public final class GeoBattleMusicController implements Disposable {
    // Array of tracks
    private final String[] tracks;

    // Music
    private Music music;

    // Volume of music
    private float volume;

    public GeoBattleMusicController(String[] tracks, float volume) {
        this.tracks = tracks;
        this.volume = volume;
    }

    // Starts playing next track
    public void nextTrack() {
        int currentTrack = (int) (Math.random() * tracks.length);

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
        music.setVolume(volume);
        music.play();
    }

    public void setVolume(float volume) {
        if (music != null)
            music.setVolume(volume);
        this.volume = volume;
    }

    @Override
    public void dispose() {
        if (music != null) {
            music.dispose();
            music = null;
        }
    }
}
