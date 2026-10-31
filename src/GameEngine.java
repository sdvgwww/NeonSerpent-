import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Класс логики. Управляет миром, получает ссылку на панель (для repaint).
 * Никакой отрисовки — только состояние.
 */
public class GameEngine implements Config {

    public enum Phase { RUNNING, PAUSED, GAME_OVER }

    private final Snake snake = new Snake();
    private final List<Drone> drones = new ArrayList<>();
    private final List<EnergyCore> cores = new ArrayList<>();
    private final Random rng = new Random();
    private final GamePanel panel;  // ссылка на отрисовку

    private Phase phase = Phase.RUNNING;
    private int coreTimer    = 0;
    private int regenTimer   = 0;
    private int impulseFlash = 0;
    private double impulseX, impulseY;

    public GameEngine(GamePanel panel) {
        this.panel = panel;
        restart();
    }

    // ---------- геттеры для отрисовки ----------
    public Snake getSnake()            { return snake; }
    public List<Drone> getDrones()     { return drones; }
    public List<EnergyCore> getCores() { return cores; }
    public Phase getPhase()            { return phase; }
    public int getImpulseFlash()       { return impulseFlash; }
    public double getImpulseX()        { return impulseX; }
    public double getImpulseY()        { return impulseY; }

    // ---------- управление ----------
    public void restart() {
        snake.reset();
        drones.clear();
        cores.clear();
        phase = Phase.RUNNING;
        coreTimer = 0;
        regenTimer = 0;
        impulseFlash = 0;

        for (int i = 0; i < DRONE_COUNT; i++) {
            drones.add(new Drone(DRONE_START_X + i * DRONE_OFFSET_X,
                    DRONE_START_Y + i * DRONE_OFFSET_Y));
        }
        for (int i = 0; i < 3; i++) spawnCore();
    }

    public void togglePause() {
        if (phase == Phase.RUNNING)     phase = Phase.PAUSED;
        else if (phase == Phase.PAUSED) phase = Phase.RUNNING;
    }

    public void setDirection(double dx, double dy) {
        if (phase == Phase.RUNNING) snake.setDirection(dx, dy);
    }

    public void tryDash() {
        if (phase == Phase.RUNNING) snake.startDash();
    }

    public void fireImpulse(double mx, double my) {
        if (phase != Phase.RUNNING) return;
        if (snake.getEnergy() < IMPULSE_COST) return;

        snake.spendEnergy(IMPULSE_COST);
        impulseX = mx;
        impulseY = my;
        impulseFlash = IMPULSE_FLASH;

        Vector2 center = new Vector2(mx, my);
        for (Drone d : drones) {
            if (d.getPos().distanceTo(center) < IMPULSE_RADIUS) d.stun();
        }
    }

    // ---------- главный шаг ----------
    public void tick() {
        if (phase != Phase.RUNNING) return;

        if (impulseFlash > 0) impulseFlash--;

        if (++regenTimer >= ENERGY_REGEN_EVERY) {
            regenTimer = 0;
            snake.addEnergy(1);
        }

        snake.tick();

        checkSelfCollision();
        checkWallCollision();
        updateDrones();
        updateCores();
        spawnCoresIfNeeded();
        checkDroneCollision();
        checkCoreCollision();

        if (snake.getLives() <= 0) phase = Phase.GAME_OVER;
    }

    // ---------- приватные помощники ----------
    private void checkSelfCollision() {
        if (snake.isInvulnerable()) return;
        Snake.Segment head = snake.getHead();
        int skip = 0;
        for (Snake.Segment s : snake.getBody()) {
            if (skip++ < 4) continue;
            if (Math.hypot(s.x - head.x, s.y - head.y) < SNAKE_HEAD_RADIUS) {
                if (snake.takeDamage()) phase = Phase.GAME_OVER;
                return;
            }
        }
    }

    private void checkWallCollision() {
        if (snake.isInvulnerable()) return;
        Snake.Segment head = snake.getHead();
        if (head.x < 0 || head.x > WINDOW_WIDTH
                || head.y < 0 || head.y > WINDOW_HEIGHT) {
            if (snake.takeDamage()) phase = Phase.GAME_OVER;
        }
    }

    private void updateDrones() {
        Snake.Segment h = snake.getHead();
        Vector2 target = new Vector2(h.x, h.y);
        for (Drone d : drones) d.tick(target);
    }

    private void updateCores() {
        Iterator<EnergyCore> it = cores.iterator();
        while (it.hasNext()) {
            EnergyCore c = it.next();
            c.tick();
            if (!c.alive) it.remove();
        }
    }

    private void spawnCoresIfNeeded() {
        if (++coreTimer >= CORE_SPAWN_EVERY) {
            coreTimer = 0;
            if (cores.size() < MAX_CORES) spawnCore();
        }
    }

    private void spawnCore() {
        Snake.Segment head = snake.getHead();
        for (int attempt = 0; attempt < 30; attempt++) {
            double x = CORE_MARGIN + rng.nextDouble() * (WINDOW_WIDTH  - 2 * CORE_MARGIN);
            double y = CORE_MARGIN + rng.nextDouble() * (WINDOW_HEIGHT - 2 * CORE_MARGIN);
            if (Math.hypot(x - head.x, y - head.y) > CORE_SAFE_DIST) {
                cores.add(new EnergyCore(x, y));
                return;
            }
        }
    }

    private void checkDroneCollision() {
        if (snake.isInvulnerable()) return;
        Snake.Segment head = snake.getHead();
        for (Drone d : drones) {
            if (d.isStunned()) continue;
            if (d.collidesWithHead(head.x, head.y)) {
                if (snake.takeDamage()) phase = Phase.GAME_OVER;
                return;
            }
        }
    }

    private void checkCoreCollision() {
        Snake.Segment head = snake.getHead();
        Iterator<EnergyCore> it = cores.iterator();
        while (it.hasNext()) {
            EnergyCore c = it.next();
            if (c.collidesWithHead(head.x, head.y)) {
                c.alive = false;
                snake.addScore(CORE_SCORE);
                snake.requestGrow(2);
                snake.addEnergy(CORE_ENERGY_BONUS);
            }
        }
    }
}
