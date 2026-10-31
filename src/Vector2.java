public final class Vector2 {
    public final double x, y;

    public Vector2(double x, double y) { this.x = x; this.y = y; }

    public Vector2 add(Vector2 o) { return new Vector2(x + o.x, y + o.y); }
    public Vector2 sub(Vector2 o) { return new Vector2(x - o.x, y - o.y); }
    public Vector2 mul(double k)  { return new Vector2(x * k, y * k); }

    public double length() { return Math.hypot(x, y); }

    public Vector2 normalized() {
        double len = length();
        if (len < 1e-6) return new Vector2(0, 0);
        return new Vector2(x / len, y / len);
    }

    public double distanceTo(Vector2 o) { return sub(o).length(); }
}
