import java.awt.Color;
import java.awt.Font;

/**
 * Интерфейс констант — все настройки игры в одном месте.
 * Магических чисел в остальном коде нет.
 */
public interface Config {

    // ---------- Окно ----------
    int WINDOW_WIDTH  = 960;
    int WINDOW_HEIGHT = 720;
    String WINDOW_TITLE = "Neon Serpent";

    // ---------- Игровой цикл ----------
    int  FPS          = 60;
    long FRAME_DELAY  = 1000L / FPS;

    // ---------- Сетка фона ----------
    int GRID_STEP = 40;

    // ---------- Змейка ----------
    int    SNAKE_START_LENGTH   = 5;
    int    SNAKE_START_LIVES    = 3;
    double SNAKE_BASE_SPEED     = 4.0;
    double SNAKE_MAX_SPEED      = 8.0;
    int    SNAKE_SPEED_UP_EVERY = 4;
    double SNAKE_SPEED_STEP     = 0.3;
    int    SNAKE_INVULN_TICKS   = 90;
    int    SNAKE_START_X        = 200;
    int    SNAKE_START_Y        = 360;
    int    SNAKE_SPACING        = 14;
    double SNAKE_HEAD_RADIUS    = 12.0;

    // ---------- Рывок ----------
    double DASH_MULTIPLIER = 2.4;
    int    DASH_DURATION   = 10;
    int    DASH_COOLDOWN   = 90;

    // ---------- Дроны ----------
    int    DRONE_COUNT        = 2;
    double DRONE_SPEED        = 1.8;
    double DRONE_CHASE_SPEED  = 2.6;
    double DRONE_CHASE_RADIUS = 220.0;
    int    DRONE_SIZE         = 26;
    int    DRONE_STUN_TICKS   = 120;
    int    DRONE_START_X      = 200;
    int    DRONE_START_Y      = 140;
    int    DRONE_OFFSET_X     = 400;
    int    DRONE_OFFSET_Y     = 60;

    // ---------- Импульс (мышь) ----------
    double IMPULSE_RADIUS = 140.0;
    int    IMPULSE_COST   = 30;
    int    IMPULSE_FLASH  = 18;

    // ---------- Энергия ----------
    int ENERGY_MAX         = 100;
    int ENERGY_REGEN_EVERY = 40;
    int CORE_ENERGY_BONUS  = 15;

    // ---------- Энергоядра ----------
    int CORE_RADIUS      = 8;
    int CORE_SCORE       = 10;
    int CORE_SPAWN_EVERY = 70;
    int MAX_CORES        = 6;
    int CORE_MARGIN      = 60;
    int CORE_SAFE_DIST   = 100;

    // ---------- Цвета ----------
    Color BG_TOP       = new Color(10, 8, 26);
    Color BG_BOTTOM    = new Color(30, 8, 50);
    Color GRID_COLOR   = new Color(255, 255, 255, 18);
    Color SNAKE_HEAD   = new Color(120, 255, 200);
    Color SNAKE_BODY_A = new Color(60, 200, 255);
    Color SNAKE_BODY_B = new Color(160, 80, 255);
    Color SNAKE_GLOW   = new Color(80, 220, 255, 90);
    Color DRONE_BODY   = new Color(255, 90, 120);
    Color DRONE_STUN   = new Color(150, 150, 180);
    Color CORE_COLOR   = new Color(255, 230, 90);
    Color CORE_GLOW    = new Color(255, 200, 40, 80);
    Color IMPULSE_RING = new Color(140, 220, 255, 200);
    Color HUD_TEXT     = new Color(225, 240, 255);
    Color HUD_WARN     = new Color(255, 90, 120);
    Color OVERLAY      = new Color(0, 0, 0, 170);

    // ---------- Шрифты ----------
    Font FONT_HUD   = new Font("Consolas", Font.BOLD, 18);
    Font FONT_TITLE = new Font("Consolas", Font.BOLD, 52);
    Font FONT_SUB   = new Font("Consolas", Font.PLAIN, 20);
    Font FONT_HINT  = new Font("Consolas", Font.PLAIN, 14);
}