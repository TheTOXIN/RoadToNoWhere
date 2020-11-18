package net.toxin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class Go extends JFrame {

    private static final int W = 1600;
    private static final int H = 768;

    private final Panel panel = new Panel();
    private final List<Line> lines = new ArrayList<>();

    private int pos = 0;
    private int player = 0;
    private int count = 1500;

    private int roadWidth = 2000;
    private int segmentLen = 200;
    private double deepCam = 0.84;

    public Go() {
        for (int i = 0; i < count; i++) {
            Line line = new Line();
            line.z = i * segmentLen;

            if (i > 200 && i < 700) {
                line.curve = 0.5;
            }

            this.lines.add(line);
        }

        super.setDefaultCloseOperation(EXIT_ON_CLOSE);
        super.setLocationRelativeTo(null);
        super.setTitle("RoadToNoWhere");
        super.setLocation(0, 0);
        super.setResizable(false);
        super.setVisible(true);
        super.add(panel);

        pack();
    }

    private class Panel extends JPanel {

        public Panel() {
            InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = getActionMap();

            String keyUp = "VK_UP";
            KeyStroke strokeUp = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
            inputMap.put(strokeUp, keyUp);
            actionMap.put(keyUp, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pos += 200;
                    panel.repaint();
                }
            });

            String keyDown = "VK_DOWN";
            KeyStroke strokeDown = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
            inputMap.put(strokeDown, keyDown);
            actionMap.put(keyDown, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (pos >= 200) {
                        pos -= 200;
                        panel.repaint();
                    }
                }
            });

            String keyLeft = "VK_LEFT";
            KeyStroke strokeLeft = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
            inputMap.put(strokeLeft, keyLeft);
            actionMap.put(keyLeft, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    player -= 200;
                    panel.repaint();
                }
            });

            String keyRight = "VK_RIGHT";
            KeyStroke strokeRight = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
            inputMap.put(strokeRight, keyRight);
            actionMap.put(keyRight, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    player += 200;
                    panel.repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            drawValues(g);
        }

        private void drawValues(Graphics g) {

            int startPos = pos / segmentLen;
            double x = 0, dx = 0;

            for (int n = startPos; n < startPos + 300; n++) {
                Line l = lines.get(n % count);
                l.project(player - (int) x, 1500, pos);

                x += dx;
                dx += l.curve;

                Color grass = ((n / 2) % 2) == 0 ? new Color(16, 200, 16) : new Color(0, 154, 0);
                Color rumble = ((n / 2) % 2) == 0 ? new Color(255, 255, 255) : new Color(255, 0, 0);
                Color road = Color.black;

                Line p;
                if (n == 0) {
                    p = l;
                } else {
                    p = lines.get((n - 1) % count);
                }

                drawQuad(g, grass, 0, (int) p.sY, W, 0, (int) l.sY, W);
                drawQuad(g, rumble, (int) p.sX, (int) p.sY, (int) (p.sW * 1.2), (int) l.sX, (int) l.sY, (int) (l.sW * 1.2));
                drawQuad(g, road, (int) p.sX, (int) p.sY, (int) (p.sW), (int) l.sX, (int) l.sY, (int) (l.sW));
            }

            g.setColor(Color.BLUE);
            g.fillRect(0, 0, W, H / 2);
        }

        public void drawQuad(Graphics g, Color c, int x1, int y1, int w1, int x2, int y2, int w2) {
            int np = 4;

            int[] xp = {x1 - w1, x2 - w2, x2 + w2, x1 + w1};
            int[] yp = {y1, y2, y2, y1};

            g.setColor(c);
            g.fillPolygon(xp, yp, np);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(W, H);
        }
    }

    private class Line {
        double x, y, z;
        double sX, sY, sW;
        double scale, curve;

        public Line() {
            this.x = this.y = this.z = this.curve = 0;
        }

        public void project(int camX, int camY, int camZ) {
            this.scale = deepCam / (this.z - camZ);
            this.sX = (1 + scale * (x - camX)) * W / 2;
            this.sY = (1 - scale * (y - camY)) * H / 2;
            this.sW = scale * roadWidth * W / 2;
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(Go::new);
    }
}
