import java.awt.*;

/** Энергоядро: даёт очки и энергию, пульсирует. */
public class EnergyCore implements Config {

    public final double x, y;
    public boolean alive = true;
    private int pulse = 0;

    public EnergyCore(double x, double y) { this.x = x; this.y = y; }

    public void tick() { pulse++; }

    public boolean collidesWithHead(double hx, double hy) {
        return Math.hypot(x - hx, y - hy) < SNAKE_HEAD_RADIUS + CORE_RADIUS + 4;
    }

    public void render(Graphics2D g) {
        double glow = 4 + Math.sin(pulse * 0.15) * 3;
        g.setColor(CORE_GLOW);
        g.fillOval((int)(x - CORE_RADIUS - glow),
                (int)(y - CORE_RADIUS - glow),
                (int)((CORE_RADIUS + glow) * 2),
                (int)((CORE_RADIUS + glow) * 2));
        g.setColor(CORE_COLOR);
        g.fillOval((int)(x - CORE_RADIUS), (int)(y - CORE_RADIUS),
                CORE_RADIUS * 2, CORE_RADIUS * 2);
    }
}