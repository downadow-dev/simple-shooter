package downadow.sg2o.main;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.util.Random;

public class Main extends JPanel {
    private static JFrame fr;
    
    /* переменные передвижения */
    private static boolean forward = false, backward = false, left = false, right = false;
    
    private static final int G_AK47 = 0,
        G_PISTOL = 1,
        G_SNIPER_RIFLE = 2,
        G_BOMB = 3;
    private static int gun = G_AK47; // текущее оружие
    
    private static final int[] defaultAmmo = {90, 24, 30, 10};
    /* патроны */
    private static int[] ammo = new int[defaultAmmo.length];
    
    private static final int RIGHT = 0, LEFT = 1;
    /* направление пушки */
    private static int direction = RIGHT;
    
    /* позиция игрока на карте */
    private static int playerX = 0, playerY = 0;
    /* выстрел? */
    private static boolean shoot = false;
    
    private static int cursorX = 0, cursorY = 0;
    
    /* враги */
    private static Enemy[] bots = new Enemy[250];
    /* макс. количество врагов на карте одновременно */
    private static int maxBots = 5;
    
    /* пауза */
    private static boolean pause = false;
    
    /* количество очков */
    private static int count = 0;
    
    private static boolean shootImage = false;
    
    private static void gameOver() {
        System.out.println(count);
        System.exit(1);
    }
    
    private static boolean inHome() {
        return (playerX >= -100 && playerX <= 120 && playerY >= -200 && playerY <= 20);
    }
    
    public static void main(String[] args) {
        for(int i = 0; i < bots.length; i++)
            bots[i] = new Enemy(null, false, 0, 0);
        
        for(int i = 0; i < ammo.length; i++)
            ammo[i] = defaultAmmo[i];
        
        fr = new JFrame("Simple Shooter");
        fr.setSize(1360, 728);
        fr.setLayout(null);
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fr.setResizable(false);
        fr.setLocationRelativeTo(null);
        
        Main p = new Main();
        
        fr.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_W)
                    forward = true;
                else if(e.getKeyCode() == KeyEvent.VK_S)
                    backward = true;
                else if(e.getKeyCode() == KeyEvent.VK_A) {
                    direction = LEFT;
                    left = true;
                } else if(e.getKeyCode() == KeyEvent.VK_D) {
                    direction = RIGHT;
                    right = true;
                }
                /* пауза */
                else if(e.getKeyCode() == KeyEvent.VK_ESCAPE && !pause)
                    pause = true;
                else if(e.getKeyCode() == KeyEvent.VK_ESCAPE && pause)
                    pause = false;
            }
            
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_W)
                    forward = false;
                else if(e.getKeyCode() == KeyEvent.VK_S)
                    backward = false;
                else if(e.getKeyCode() == KeyEvent.VK_A)
                    left = false;
                else if(e.getKeyCode() == KeyEvent.VK_D)
                    right = false;
            }
            
            public void keyTyped(KeyEvent e) {}
        });
        
        p.addMouseListener(new MouseListener() {
            public void mousePressed(MouseEvent e) {
                /* смена оружия */
                if(SwingUtilities.isRightMouseButton(e)) {
                    if(gun == G_AK47)              gun = G_PISTOL;
                    else if(gun == G_PISTOL)       gun = G_SNIPER_RIFLE;
                    else if(gun == G_SNIPER_RIFLE) gun = (maxBots > 40 ? G_BOMB : G_AK47);
                    else if(gun == G_BOMB) gun = G_AK47;
                }
                /* выстрел */
                else if(SwingUtilities.isLeftMouseButton(e) && !shoot && !pause && ammo[gun] > 0 && !inHome()) {
                    new Thread() {
                        public void run() {
                            try {
                                shoot = true;
                                cursorX = e.getX();
                                cursorY = e.getY();
                                shootImage = true;
                                ammo[gun]--;
                                
                                for(int i = 0; i < maxBots; i++) {
                                    if(bots[i].visible &&
                                       (cursorX > (bots[i].x - playerX + (1360 / 2 - (gun != G_BOMB ? 33 : 83))) &&
                                        cursorX < (bots[i].x - playerX + (1360 / 2 - (gun != G_BOMB ? 33 : 83)))+(gun != G_BOMB ? 100 : 250)) &&
                                       (cursorY > (bots[i].y - playerY + (728 / 2 - (gun != G_BOMB ? 33 : 83))) &&
                                        cursorY < (bots[i].y - playerY + (728 / 2 - (gun != G_BOMB ? 33 : 83)))+(gun != G_BOMB ? 100 : 250))
                                      )
                                    {
                                        if(bots[i].almost || gun == G_SNIPER_RIFLE || gun == G_BOMB) {
                                            if(gun == G_BOMB) {
                                                bots[i].texture = new ImageIcon("res/boom.png").getImage();
                                                Thread.sleep(20);
                                            }
                                            bots[i].visible = false;
                                            count++;
                                        } else if(!bots[i].almost) {
                                            bots[i].almost = true;
                                        }
                                    }
                                }
                                Thread.sleep(20);
                                shootImage = false;
                                
                                if(gun == G_PISTOL)
                                    Thread.sleep(250);
                                else if(gun == G_SNIPER_RIFLE)
                                    Thread.sleep(900);
                                else
                                    Thread.sleep(5);
                                shoot = false;
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
            }
            
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseClicked(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {
                //pause = true;
            }
        });
        
        p.setLayout(null);
        p.setBounds(0, 0, 1360, 728);
        fr.add(p);
        fr.setVisible(true);
        
        /* обновление экрана */
        new Thread() {
            public void run() {
                while(true) {
                    try {
                        fr.repaint();
                        Thread.sleep(17);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        
        /* передвижение */
        new Thread() {
            public void run() {
                while(true) {
                    try {
                        if(forward && gun == G_PISTOL)      playerY -= 25;
                        else if(forward && (gun == G_AK47 || gun == G_BOMB)) playerY -= 20;
                        else if(forward)                    playerY -= 15;
                        
                        if(backward && gun == G_PISTOL)     playerY += 25;
                        else if(backward && (gun == G_AK47 || gun == G_BOMB)) playerY += 20;
                        else if(backward)                   playerY += 15;
                        
                        if(right && gun == G_PISTOL)        playerX += 25;
                        else if(right && (gun == G_AK47 || gun == G_BOMB)) playerX += 20;
                        else if(right)                      playerX += 15;
                        
                        if(left && gun == G_PISTOL)         playerX -= 25;
                        else if(left && (gun == G_AK47 || gun == G_BOMB)) playerX -= 20;
                        else if(left)                       playerX -= 15;
                        
                        Thread.sleep(50);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        
        /* логика врагов */
        new Thread() {
            public void run() {
                while(true) {
                    try {
                        if(!pause) {
                            for(int i = 0; i < maxBots && !inHome(); i++) {
                                if(bots[i].visible) {
                                    /* передвижение */
                                    if(bots[i].x > playerX)
                                        bots[i].x--;
                                    else if(bots[i].x < playerX)
                                        bots[i].x++;
                                    if(bots[i].y < playerY)
                                        bots[i].y++;
                                    if(bots[i].y > playerY)
                                        bots[i].y--;
                                    
                                    if(bots[i].x == playerX && bots[i].y == playerY)
                                        gameOver();
                                }
                            }
                            Thread.sleep(maxBots < 80 ? 10 : 8);
                        } else Thread.sleep(500);
                    } catch(Exception e) {}
                }
            }
        }.start();
        
        /* увеличение количества врагов */
        new Thread() {
            public void run() {
                loop:
                while(true) {
                    try {
                        Thread.sleep(50);
                        
                        if(!pause) {
                            for(int i = 0; i < maxBots; i++) {
                                if(bots[i].visible)
                                    continue loop;
                            }
                            
                            Thread.sleep(1000);
                            
                            if(maxBots < bots.length) maxBots += 5;
                            
                            for(int i = 0; i < maxBots; i++) {
                                bots[i].x = ((new Random().nextInt(5)) > 2 ?
                                    (600 + (new Random().nextInt(3000))) :
                                    -(700 + (new Random().nextInt(3000))));
                                
                                bots[i].y = ((new Random().nextInt(5)) > 2 ?
                                    (500 + (new Random().nextInt(3000))) :
                                    -(600 + (new Random().nextInt(3000))));
                                
                                bots[i].almost = false;
                                bots[i].texture = new ImageIcon("res/enemy.png").getImage();
                                bots[i].visible = true;
                            }
                        }
                    } catch(Exception e) {}
                }
            }
        }.start();
        
        /* перезарядка */
        new Thread() {
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(500);
                        if(inHome() && !forward && !backward && !left && !right)
                            ammo[gun] = defaultAmmo[gun];
                    } catch(Exception e) {e.printStackTrace();}
                }
            }
        }.start();
    }
    
    public void paint(Graphics g) {
        g.setColor(new Color(85, 85, 85));
        g.fillRect(0, 0, 1360, 728);
        
        g.drawImage(new ImageIcon(gun == G_BOMB ? "res/bomb.png" : ("res/gun" + gun + "_" + direction + (shootImage ? "_s" : "") + ".png")).getImage(),
            1360 / 2 - 20 + (left ? -10 : (right ? 10 : 0)), 728 / 2 - 20 + (forward ? -10 : (backward ? 10 : 0)),
            80, 80, null);
        
        g.drawImage(new ImageIcon("res/home.png").getImage(), 550 - playerX, 150 - playerY, 300, 300, null);
        g.setColor(new Color(0, 255, 255));
        g.drawRect(-playerX - 1600, -playerY - 1600, 4000, 4000);
        
        for(int i = 0; i < maxBots; i++) {
            if(bots[i].visible) {
                if(!bots[i].almost)
                    g.drawImage(bots[i].texture,
                        bots[i].x - playerX + (1360 / 2 - 33), bots[i].y - playerY + (728 / 2 - 33), 100, 100, null);
                else
                    g.drawImage(bots[i].texture,
                        bots[i].x - playerX + (1360 / 2 - 25), bots[i].y - playerY + (728 / 2 - 25), 75, 75, null);
            }
        }
        
        if(shootImage)
            g.drawImage(new ImageIcon("res/cursor.png").getImage(), cursorX-60, cursorY-60, 120, 120, null);
        
        g.setColor(new Color(255, 255, 255));
        g.setFont(new Font("Monospaced", Font.PLAIN, 13));
        
        g.drawString("" + count + "  (" + playerX + ", " + playerY + ") " + ammo[gun] + (pause ? "   [pause]" : ""), 15, 20);
    }
}

