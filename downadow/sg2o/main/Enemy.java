package downadow.sg2o.main;

import java.awt.Image;

public class Enemy {
    public Image texture;
    public boolean visible;
    public boolean almost;
    public int x, y;
    
    public Enemy(Image texture, boolean visible, int x, int y) {
        this.texture = texture;
        this.visible = visible;
        this.x = x;
        this.y = y;
        almost = false;
    }
}

