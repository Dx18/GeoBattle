package geobattle.geobattle.map.animations;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

// Info about animation
public final class AnimationInfo {
    // Frames of animation
    private TextureRegion[] frames;

    public AnimationInfo(Texture texture, int frameWidth, int frameHeight, int frameCount) {
        frames = new TextureRegion[frameCount];
        int framesInCol = texture.getHeight() / frameHeight;
        int framesInRow = texture.getWidth() / frameWidth;
        for (int y = 0; y < framesInCol; y++) {
            for (int x = 0; x < framesInRow; x++) {
                int index = y * framesInRow + x;
                if (index >= frameCount)
                    return;
                frames[index] = new TextureRegion(
                        texture, x * frameWidth, y * frameHeight,
                        frameWidth, frameHeight
                );
            }
        }
    }

    // Returns count of frames in animation
    public int getFrameCount() {
        return frames.length;
    }

    // Returns frame with index
    public TextureRegion getFrame(int frame) {
        return frames[frame];
    }
}
