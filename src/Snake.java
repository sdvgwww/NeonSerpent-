import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Модель змейки: сегменты, направление, жизни, счёт, энергия.
 * Отрисовка — НЕ здесь (см. GamePanel).
 */
public class Snake implements Config {

    public static class Segment {
        public double x, y;
        public Segment(double x, double y) { this.x = x; this.y = y; }
    }

    private final Deque<Segment> body = new ArrayDeque<>();
    private Vector2 direction;
    private Vector2 pendingDir;

    private int lives;
    private int score;
    private int energy;
    private int invulnTicks;
    private int dashTicks;
    private int dashCooldown;
    private int pendingGrow;

    public Snake() { reset(); }

    // ---------- геттеры ----------
    public Deque<Segment> getBody() { return body; }
    public Segment getHead()        { return body.peekFirst(); }
    public int getLives()           { return lives; }
    public int getScore()           { return score; }
    public int getEnergy()          { return energy; }
    public boolean isInvulnerable() { return invulnTicks > 0; }
    public int getDashCooldown()    { return dashCooldown; }

    // ---------- сеттеры/действия ----------
    public void setDirection(double dx, double dy) {
        Vector2 nd = new Vector2(dx, dy).normalized();
        if (nd.x * direction.x + nd.y * direction.y <= -0.9) return; // назад нельзя
        pendingDir = nd;
    }

    public void addScore(int points) { score += points; }

    public void addEnergy(int e) { energy = Math.min(ENERGY_MAX, energy + e); }

    public void spendEnergy(int e) {
        if (energy < e) throw new IllegalStateException("Not enough energy");
        energy -= e;
    }

    public void requestGrow(int segments) { pendingGrow += segments; }

    public void startDash() {
        if (dashTicks > 0 || dashCooldown > 0) return;
        dashTicks = DASH_DURATION;
        dashCooldown = DASH_COOLDOWN;
    }

    public void reset() {
        body.clear();
        direction = new Vector2(1, 0);
        pendingDir = direction;
        lives = SNAKE_START_LIVES;
        score = 0;
        energy = ENERGY_MAX;
        invulnTicks = 0;
        dashTicks = 0;
        dashCooldown = 0;
        pendingGrow = 0;
        for (int i = 0; i < SNAKE_START_LENGTH; i++) {
            body.addLast(new Segment(SNAKE_START_X - i * SNAKE_SPACING, SNAKE_START_Y));
        }
    }

    public void tick() {
        if (invulnTicks > 0)  invulnTicks--;
        if (dashCooldown > 0) dashCooldown--;

        double speed = currentSpeed();
        if (dashTicks > 0) {
            dashTicks--;
            speed *= DASH_MULTIPLIER;
        }

        direction = pendingDir;
        Segment head = body.peekFirst();
        double nx = head.x + direction.x * speed;
        double ny = head.y + direction.y * speed;
        body.addFirst(new Segment(nx, ny));

        if (pendingGrow > 0) pendingGrow--;
        else                 body.removeLast();
    }

    /** Возвращает true, если жизнь закончилась. */
    public boolean takeDamage() {
        if (invulnTicks > 0) return false;
        lives--;
        invulnTicks = SNAKE_INVULN_TICKS;
        if (lives <= 0) return true;

        body.clear();
        double cx = WINDOW_WIDTH / 2.0, cy = WINDOW_HEIGHT / 2.0;
        for (int i = 0; i < SNAKE_START_LENGTH; i++) {
            body.addLast(new Segment(cx - i * SNAKE_SPACING, cy));
        }
        direction = new Vector2(1, 0);
        pendingDir = direction;
        return false;
    }

    private double currentSpeed() {
        int eaten = score / CORE_SCORE;
        double s = SNAKE_BASE_SPEED
                + (eaten / (double) SNAKE_SPEED_UP_EVERY) * SNAKE_SPEED_STEP;
        return Math.min(s, SNAKE_MAX_SPEED);
    }
}
