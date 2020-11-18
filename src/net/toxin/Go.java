package net.toxin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Go extends JFrame {

    private static final int W = 1600;
    private static final int H = 768;

    private final Color[] colors = new Color[6];
    private final DrawPanel panel = new DrawPanel();
    private final List<Line> lines = new ArrayList<>();

    private int player = 0;
    private int speed = 200;
    private int count = 1500;
    private int position = 0;

    private int roadWidth = 2000;
    private int segmentLen = 200;
    private int positionCam = 1500;
    private float depthCam = 0.75f;

    public Go() {
        this.init();

        super.setDefaultCloseOperation(EXIT_ON_CLOSE);
        super.setFocusTraversalKeysEnabled(false);
        super.addKeyListener(new KeyListener());
        super.setLocationRelativeTo(null);
        super.setTitle("RoadToNoWhere");
        super.setLocation(0, 0);
        super.setResizable(false);
        super.setFocusable(true);
        super.setVisible(true);
        super.add(panel);

        super.pack();
    }

    private void init() {
        for (int i = 0; i < count; i++) {
            Line line = new Line();
            line.dZ = i * segmentLen;

            if (i > 100 && i < 300) line.curve = -0.5; // TODO RANDOM

            this.lines.add(line);
        }

        this.colors[0] = Color.BLACK;
        this.colors[1] = new Color(16, 200, 16);
        this.colors[2] = new Color(0, 154, 0);
        this.colors[3] = Color.WHITE;
        this.colors[4] = Color.RED;
        this.colors[5] = Color.BLUE;
    }

    private class DrawPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            render(g);
        }

        private void render(Graphics g) {
            int startPos = position / segmentLen;
            double x = 0, dx = 0;

            for (int n = startPos; n < startPos + 300; n++) {
                Line l = lines.get(n % count);
                Line p = lines.get(Math.max(0, n - 1) % count);

                l.project(player - (int) x, positionCam, position);

                x += dx;
                dx += l.curve;

                Color grass = colors[((n / 2) % 2) == 0 ? 1 : 2];
                Color rumble = colors[((n / 2) % 2) == 0 ? 3 : 4];
                Color mark = colors[((n / 4) % 2) == 0 ? 0 : 3];
                Color road = colors[0];

                draw(g, grass, 0, p.sY, W, 0, l.sY, W);
                draw(g, rumble, p.sX, p.sY, p.sW * 1.2, l.sX, l.sY, l.sW * 1.2);
                draw(g, road, p.sX, p.sY, p.sW, l.sX, l.sY, l.sW);
                draw(g, mark, p.sX, p.sY, p.sW * 0.05, l.sX, l.sY, l.sW * 0.05);
            }

            g.setColor(Color.BLUE);
            g.fillRect(0, 0, W, H / 2);
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

    private class KeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_UP) position += speed;
            if (e.getKeyCode() == KeyEvent.VK_LEFT) player -= speed;
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) player += speed;
            if (e.getKeyCode() == KeyEvent.VK_DOWN && position >= speed) position -= speed;

            panel.repaint();
        }
    }

    private class Line {
        double dX, dY, dZ;
        double sX, sY, sW;
        double scale, curve;

        public Line() {
            this.dX = this.dY = this.dZ = this.curve = 0;
        }

        public void project(int camX, int camY, int camZ) {
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
