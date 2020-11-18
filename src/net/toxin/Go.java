package net.toxin;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Go extends JFrame {

    private static final int H = 768;
    private static final int W = 1600;

    private final View view = new View();
    private final Controller controller = new Controller();

    private final Color[] colors = new Color[7];
    private final List<Line> lines = new ArrayList<>();

    private final File sound = new File("res/7.wav");
    private final Image car = new ImageIcon("res/7.png").getImage();

    private int player = 0;
    private int speed = 300;
    private int count = 300;
    private int position = 0;

    private int roadWidth = 3000;
    private int segmentLen = 300;
    private int positionCam = 1500;
    private float depthCam = 0.75f;

    private boolean isUp, isLeft, isRight, isDown;

    public Go() {
        this.init();

        super.setDefaultCloseOperation(EXIT_ON_CLOSE);
        super.setFocusTraversalKeysEnabled(false);
        super.addKeyListener(controller);
        super.setLocationRelativeTo(null);
        super.setTitle("RoadToNoWhere");
        super.setLocation(0, 0);
        super.setResizable(false);
        super.setFocusable(true);
        super.setVisible(true);
        super.add(view);

        super.pack();
    }

    private void init() {
        this.generate();

        Timer timer = new Timer(60, controller);
        timer.setInitialDelay(0);
        timer.start();

        this.colors[0] = Color.BLACK;
        this.colors[1] = Color.WHITE;
        this.colors[2] = Color.RED;
        this.colors[3] = Color.YELLOW;
        this.colors[4] = new Color(16, 200, 16);
        this.colors[5] = new Color(0, 154, 0);
        this.colors[6] = new Color(0, 136, 255);

        this.music();
    }

    private void music() {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(this.sound));
            clip.loop(7);
            clip.start();
        } catch (Exception exc) {
            exc.printStackTrace(System.out);
        }
    }

    private void generate() {
        double roadAngel = 0;
        int roadLen = 0;

        for (int i = 0; i < count; i++) {
            Line line = new Line();

            if (roadLen == 0) {
                if (Math.random() < 0.5) roadAngel = Math.random() * 2.0 - 1.0;
                roadLen = (int) (5.0 * Math.random()) * 50;
            } else {
                line.curve = roadAngel;
                roadLen--;
            }

            line.dZ = i * segmentLen;

            this.lines.add(line);
        }
    }

    private class View extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            render(g);
        }

        private void render(Graphics g) {
            int startPos = position / segmentLen;
            double x = 0, dx = 0;

            for (int n = startPos; n < startPos + 300; n++) {
                Line curr = lines.get(n % count);
                Line prev = lines.get(Math.max(0, n - 1) % count);

                curr.compute(player - (int) x, positionCam, position);

                x += dx;
                dx += curr.curve;

                Color road = colors[0];
                Color mark = colors[((n / 4) % 2) == 0 ? 0 : 1];
                Color grass = colors[((n / 2) % 2) == 0 ? 4 : 5];
                Color rumble = colors[((n / 2) % 2) == 0 ? 3 : 2];

                draw(g, grass, 0, prev.sY, W, 0, curr.sY, W);
                draw(g, rumble, prev.sX, prev.sY, prev.sW * 1.2, curr.sX, curr.sY, curr.sW * 1.2);
                draw(g, road, prev.sX, prev.sY, prev.sW, curr.sX, curr.sY, curr.sW);
                draw(g, mark, prev.sX, prev.sY, prev.sW * 0.02, curr.sX, curr.sY, curr.sW * 0.02);
            }

            Color sky = colors[6];
            g.setColor(sky);
            g.fillRect(0, 0, W, H / 2);

            g.drawImage(car, W / 2 - 300 / 2, H / 2 + 50, 300, 300, null);
        }

        public void draw(Graphics g, Color c, double x1, double y1, double w1, double x2, double y2, double w2) {
            int[] xp = {(int) (x1 - w1), (int) (x2 - w2), (int) (x2 + w2), (int) (x1 + w1)};
            int[] yp = {(int) y1, (int) y2, (int) y2, (int) y1};

            g.setColor(c);
            g.fillPolygon(xp, yp, 4);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(W, H);
        }
    }

    private class Controller implements KeyListener, ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (isUp) position += speed;
            if (isLeft) player -= speed;
            if (isRight) player += speed;
            if (isDown && position >= speed) position -= speed;

            view.repaint();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            processKey(e.getKeyCode(), true);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            processKey(e.getKeyCode(), false);
        }

        @Override
        public void keyTyped(KeyEvent e) {
            // nothing
        }

        private void processKey(int code, boolean press) {
            if (code == KeyEvent.VK_W) isUp = press;
            else if (code == KeyEvent.VK_A) isLeft = press;
            else if (code == KeyEvent.VK_S) isDown = press;
            else if (code == KeyEvent.VK_D) isRight = press;
        }
    }

    private class Line {
        double dX, dY, dZ;
        double sX, sY, sW;
        double scale, curve;

        public Line() {
            this.dX = this.dY = this.dZ = this.curve = 0;
        }

        public void compute(int camX, int camY, int camZ) {
            this.scale = depthCam / (this.dZ - camZ);
            this.sX = (1 + scale * (dX - camX)) * W / 2;
            this.sY = (1 - scale * (dY - camY)) * H / 2;
            this.sW = scale * roadWidth * W / 2;
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(Go::new);
    }
}
