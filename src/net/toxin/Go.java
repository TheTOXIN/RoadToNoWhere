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
    private final View panel = new View();
    private final List<Line> lines = new ArrayList<>();

    private final int speed = 200;
    private final int roadCount = 300;
    private final int roadWidth = 2000;
    private final int segmentLen = 200;
    private final int positionCam = 1500;
    private final float depthCam = 0.75f;

    private int player = 0;
    private int counter = 0;
    private int position = 0;

    public Go() {
        this.init();

        super.setDefaultCloseOperation(EXIT_ON_CLOSE);
        super.setFocusTraversalKeysEnabled(false);
        super.addKeyListener(new Controller());
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
        this.colors[0] = Color.BLACK;
        this.colors[1] = new Color(16, 200, 16);
        this.colors[2] = new Color(0, 154, 0);
        this.colors[3] = Color.WHITE;
        this.colors[4] = Color.RED;
        this.colors[5] = Color.BLUE;
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

            System.out.println(startPos);

            for (int n = startPos; n < startPos + roadCount; n++) {
                 if (n >= counter) new Line();

                Line curr = lines.get(n);
                Line prev = lines.get(Math.max(0, n - 1));

                curr.compute(player - (int) x, positionCam, position);

                x += dx;
                dx += curr.curve;

                Color grass = colors[((n / 2) % 2) == 0 ? 1 : 2];
                Color rumble = colors[((n / 2) % 2) == 0 ? 3 : 4];
                Color mark = colors[((n / 4) % 2) == 0 ? 0 : 3];
                Color road = colors[0];

                draw(g, grass, 0, prev.sY, W, 0, curr.sY, W);
                draw(g, rumble, prev.sX, prev.sY, prev.sW * 1.2, curr.sX, curr.sY, curr.sW * 1.2);
                draw(g, road, prev.sX, prev.sY, prev.sW, curr.sX, curr.sY, curr.sW);
                draw(g, mark, prev.sX, prev.sY, prev.sW * 0.05, curr.sX, curr.sY, curr.sW * 0.05);
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

    private class Controller extends KeyAdapter {
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
            counter++;

            this.dX = this.dY = this.curve = 0;
            this.dZ = counter * segmentLen;

            if (counter > 100 && counter < 300) this.curve = -0.5; // TODO RANDOM

            lines.add(this);
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
