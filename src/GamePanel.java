import javax.swing.*;
import java.awt.*;
import java.awt.GradientPaint;
import java.awt.event.*;

/**
 * Класс отрисовки и ввода. Содержит ссылку на движок.
 * Реализует Runnable — игровой цикл в отдельном потоке.
 */
public class GamePanel extends JPanel implements Config, Runnable {

    private final GameEngine engine;
    private final Thread gameThread;
    private volatile boolean running = true;

    private boolean kUp, kDown, kLeft, kRight, kShift;

    public GamePanel() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(BG_TOP);
        setFocusable(true);
        setDoubleBuffered(true);

        engine = new GameEngine(this);
        installInput();

        gameThread = new Thread(this, "NeonSerpent-Loop");
        gameThread.start();
    }

    public void stop() {
        running = false;
        try { gameThread.join(500); }
        catch (InterruptedException ignored) { }
    }

    // ---------- ввод ----------
    private void installInput() {
        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (engine.getPhase() == GameEngine.Phase.GAME_OVER) {
                    engine.restart();
                    return;
                }
                if (e.getButton() == MouseEvent.BUTTON1) {
                    engine.fireImpulse(e.getX(), e.getY());
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    engine.togglePause();
                }
            }
        };
        addMouseListener(mouse);

        KeyAdapter keys = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W: case KeyEvent.VK_UP:    kUp    = true; break;
                    case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  kDown  = true; break;
                    case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  kLeft  = true; break;
                    case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: kRight = true; break;
                    case KeyEvent.VK_SHIFT: kShift = true; break;
                    case KeyEvent.VK_SPACE:
                        if (engine.getPhase() == GameEngine.Phase.GAME_OVER) engine.restart();
                        else                                                 engine.togglePause();
                        break;
                    case KeyEvent.VK_R:      engine.restart(); break;
                    case KeyEvent.VK_ESCAPE: System.exit(0);   break;
                }
            }
            @Override public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W: case KeyEvent.VK_UP:    kUp    = false; break;
                    case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  kDown  = false; break;
                    case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  kLeft  = false; break;
                    case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: kRight = false; break;
                    case KeyEvent.VK_SHIFT: kShift = false; break;
                }
            }
        };
        addKeyListener(keys);
    }

    private void applyInput() {
        if (kUp)         engine.setDirection(0, -1);
        else if (kDown)  engine.setDirection(0,  1);
        else if (kLeft)  engine.setDirection(-1, 0);
        else if (kRight) engine.setDirection( 1, 0);

        if (kShift) engine.tryDash();
    }

    // ---------- игровой цикл ----------
    @Override public void run() {
        long last = System.nanoTime();
        while (running) {
            applyInput();
            engine.tick();
            repaint();

            long elapsed = (System.nanoTime() - last) / 1_000_000;
            long sleep = FRAME_DELAY - elapsed;
            if (sleep > 0) {
                try { Thread.sleep(sleep); }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            last = System.nanoTime();
        }
    }

    // ---------- отрисовка ----------
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2);
        drawImpulse(g2);
        drawCores(g2);
        drawSnake(g2);
        drawDrones(g2);
        drawHUD(g2);
        drawHint(g2);
        drawOverlays(g2);

        g2.dispose();
    }

    private void drawBackground(Graphics2D g) {
        g.setPaint(new GradientPaint(0, 0, BG_TOP, 0, WINDOW_HEIGHT, BG_BOTTOM));
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        g.setColor(GRID_COLOR);
        for (int x = 0; x < WINDOW_WIDTH;  x += GRID_STEP) g.drawLine(x, 0, x, WINDOW_HEIGHT);
        for (int y = 0; y < WINDOW_HEIGHT; y += GRID_STEP) g.drawLine(0, y, WINDOW_WIDTH, y);
    }

    private void drawSnake(Graphics2D g) {
        Snake snake = engine.getSnake();
        if (snake.isInvulnerable() && (System.currentTimeMillis() / 80) % 2 == 0) return;

        int total = snake.getBody().size();
        int idx = 0;
        for (Snake.Segment s : snake.getBody()) {
            double t = idx / (double) Math.max(1, total - 1);
            Color c = lerp(SNAKE_BODY_A, SNAKE_BODY_B, t);
            int size = Math.max(6, 16 - idx / 2);
            int half = size / 2;

            if (idx == 0) {
                g.setColor(SNAKE_GLOW);
                g.fillOval((int)(s.x - half - 6), (int)(s.y - half - 6),
                        size + 12, size + 12);
                g.setColor(SNAKE_HEAD);
            } else {
                g.setColor(c);
            }
            g.fillRoundRect((int)(s.x - half), (int)(s.y - half), size, size, 8, 8);
            idx++;
        }
    }

    private void drawCores(Graphics2D g) {
        for (EnergyCore c : engine.getCores()) c.render(g);
    }

    private void drawDrones(Graphics2D g) {
        for (Drone d : engine.getDrones()) d.render(g);
    }

    private void drawImpulse(Graphics2D g) {
        if (engine.getImpulseFlash() <= 0) return;
        double t = engine.getImpulseFlash() / (double) IMPULSE_FLASH;
        int r = (int)(IMPULSE_RADIUS * (1 - t * 0.4));
        g.setColor(new Color(IMPULSE_RING.getRed(), IMPULSE_RING.getGreen(),
                IMPULSE_RING.getBlue(), (int)(180 * t)));
        g.setStroke(new BasicStroke(3f));
        g.drawOval((int)(engine.getImpulseX() - r),
                (int)(engine.getImpulseY() - r), r * 2, r * 2);
        g.setStroke(new BasicStroke(1f));
    }

    private void drawHUD(Graphics2D g) {
        Snake snake = engine.getSnake();

        g.setFont(FONT_HUD);
        g.setColor(HUD_TEXT);
        g.drawString("SCORE " + snake.getScore(), 20, 28);

        for (int i = 0; i < snake.getLives(); i++) {
            g.setColor(SNAKE_HEAD);
            g.fillRoundRect(WINDOW_WIDTH - 30 - i * 28, 14, 20, 14, 6, 6);
        }

        int barW = 200, barH = 14;
        int barX = WINDOW_WIDTH / 2 - barW / 2, barY = 14;
        g.setColor(new Color(255, 255, 255, 40));
        g.fillRoundRect(barX, barY, barW, barH, 8, 8);
        g.setColor(CORE_COLOR);
        int fill = (int)(barW * (snake.getEnergy() / (double) ENERGY_MAX));
        g.fillRoundRect(barX, barY, fill, barH, 8, 8);
        g.setColor(HUD_TEXT);
        g.setFont(FONT_HINT);
        g.drawString("ENERGY", barX, barY + barH + 14);

        g.setFont(FONT_HUD);
        g.setColor(snake.getDashCooldown() > 0 ? HUD_WARN : HUD_TEXT);
        g.drawString("DASH " + (snake.getDashCooldown() > 0
                        ? (snake.getDashCooldown() / 10) + "s"
                        : "READY"),
                20, 56);
    }

    private void drawHint(Graphics2D g) {
        g.setFont(FONT_HINT);
        g.setColor(new Color(200, 220, 255, 140));
        g.drawString("WASD/стрелки — движение | Shift — рывок | ЛКМ — импульс | "
                        + "Space — пауза | R — заново | Esc — выход",
                20, WINDOW_HEIGHT - 20);
    }

    private void drawOverlays(Graphics2D g) {
        if (engine.getPhase() == GameEngine.Phase.RUNNING) return;

        g.setColor(OVERLAY);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        String title, sub;
        if (engine.getPhase() == GameEngine.Phase.PAUSED) {
            title = "PAUSED";
            sub   = "Space / ПКМ — продолжить,  R — новая игра,  Esc — выход";
        } else {
            title = "GAME OVER";
            sub   = "SCORE " + engine.getSnake().getScore()
                    + "   |   Space / клик / R — новая игра";
        }

        g.setFont(FONT_TITLE);
        g.setColor(engine.getPhase() == GameEngine.Phase.GAME_OVER ? HUD_WARN : HUD_TEXT);
        FontMetrics fmT = g.getFontMetrics();
        g.drawString(title, (WINDOW_WIDTH - fmT.stringWidth(title)) / 2,
                WINDOW_HEIGHT / 2 - 20);

        g.setFont(FONT_SUB);
        g.setColor(HUD_TEXT);
        FontMetrics fmS = g.getFontMetrics();
        g.drawString(sub, (WINDOW_WIDTH - fmS.stringWidth(sub)) / 2,
                WINDOW_HEIGHT / 2 + 40);
    }

    /** Линейная интерполяция двух цветов. */
    private static Color lerp(Color a, Color b, double t) {
        t = Math.max(0, Math.min(1, t));
        int r  = (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t);
        int gr = (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t);
        return new Color(r, gr, bl);
    }
}