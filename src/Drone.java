import java.awt.*;

/**
 * Дрон-охотник: патрулирует по кругу, но если змейка близко — преследует.
 * Оглушается импульсом.
 */
public class Drone implements Config {

    private Vector2 pos;
    private final Vector2 anchor;
    private double patrolAngle = 0;
    private int stunTicks = 0;
    private int wingPhase = 0;

    public Drone(double x, double y) {
        this.pos    = new Vector2(x, y);
        this.anchor = new Vector2(x, y);
    }

    public Vector2 getPos()    { return pos; }
    public boolean isStunned() { return stunTicks > 0; }
    public void stun()         { stunTicks = DRONE_STUN_TICKS; }

    public void tick(Vector2 target) {
        wingPhase++;
        if (stunTicks > 0) { stunTicks--; return; }

        if (pos.distanceTo(target) < DRONE_CHASE_RADIUS) {
            Vector2 dir = target.sub(pos).normalized();
            pos = pos.add(dir.mul(DRONE_CHASE_SPEED));
        } else {
            patrolAngle += 0.02;
            double tx = anchor.x + Math.cos(patrolAngle) * 110;
            double ty = anchor.y + Math.sin(patrolAngle) * 80;
            Vector2 dir = new Vector2(tx - pos.x, ty - pos.y).normalized();
            pos = pos.add(dir.mul(DRONE_SPEED));
        }
        double m = DRONE_SIZE;
        pos = new Vector2(
                Math.max(m, Math.min(WINDOW_WIDTH  - m, pos.x)),
                Math.max(m, Math.min(WINDOW_HEIGHT - m, pos.y)));
    }

    public boolean collidesWithHead(double hx, double hy) {
        return pos.distanceTo(new Vector2(hx, hy))
                < SNAKE_HEAD_RADIUS + DRONE_SIZE / 2.0;
    }

    public void render(Graphics2D g) {
        int cx = (int) pos.x, cy = (int) pos.y, r = DRONE_SIZE / 2;
        Color body = isStunned() ? DRONE_STUN : DRONE_BODY;
        double flap = Math.sin(wingPhase * 0.3) * 4;

        g.setColor(body.darker());
        g.fillPolygon(new int[]{cx - r - 8, cx - r + 4, cx - r + 4},
                new int[]{(int)(cy + flap), cy - 6, cy + 6}, 3);
        g.fillPolygon(new int[]{cx + r + 8, cx + r - 4, cx + r - 4},
                new int[]{(int)(cy - flap), cy - 6, cy + 6}, 3);

        g.setColor(body);
        g.fillOval(cx - r, cy - r, r * 2, r * 2);

        g.setColor(isStunned() ? Color.DARK_GRAY : Color.WHITE);
        g.fillOval(cx - 4, cy - 4, 8, 8);
        g.setColor(Color.BLACK);
        g.fillOval(cx - 2, cy - 2, 4, 4);
    }
}
