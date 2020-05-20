import javax.swing.*;
import java.awt.*;
import java.util.*;

public class Main {

    private static int WIDTH = 750;
    private static int HEIGHT = 600;

    static class ConvexHull extends JComponent {
        /* Drawing constants. */
        int delay;
        int radius;
        boolean running;

        /* Fields for convex hull. */
        Set<Point> points;
        LinkedList<Point> toSearch;
        LinkedList<Point> hull;

        ConvexHull() {
            Random r = new Random();
            this.points = new HashSet<>();

            /* Arbitrarily create 10 points that are distributed uniformly randomly within the rectangle
               [Main.WIDTH / 20, Main.WIDTH - Main.WIDTH / 10) x [Main.HEIGHT / 20 + Main.HEIGHT - Main.HEIGHT / 10). */
            while (this.points.size() < 15) {
                int x = Main.WIDTH / 20 + r.nextInt(Main.WIDTH - Main.WIDTH / 10);
                int y = Main.HEIGHT / 20 + r.nextInt(Main.HEIGHT - Main.HEIGHT / 10);
                this.points.add(new Point(x, y));
            }
            this.toSearch = new LinkedList<>(points);
            this.hull = new LinkedList<>();

            /* Arbitrarily set delay to be 1 sec, radius to be 10. */
            this.running = false;
            this.delay = 1000;
            this.radius = 10;
        }

        /* Returns the point with the lowest y-coordinate. */
        Point getLowest () {
            if (this.hull.isEmpty()) {
                return Collections.min(this.toSearch, (p1, p2) -> {
                    if (p1.y == p2.y)
                        return Integer.compare(p1.x, p2.x);
                    else
                        return Integer.compare(p1.y, p2.y);
                });
            }
            return this.hull.peekFirst();
        }

        /* Let Q be the left-most lowest point.
           Returns the angle between the horizontal line through Q and the line PQ that falls in the interval [0, pi). */
        double getAngle (Point p) {
            Point lowest = this.getLowest();
            return Math.atan2(p.y - lowest.y, p.x - lowest.x);
        }

        /* Returns the cross product between the line P1P2 and P2P3. */
        double getCrossProduct(Point p1, Point p2, Point p3) {
            return (p2.x - p1.x) * (p3.y - p1.y) - (p2.y - p1.y) * (p3.x - p1.x);
        }

        /* Performs and draws Graham Scan with delays between actions. */
        void run() throws InterruptedException {
            this.hull.push(this.getLowest());
            this.toSearch.remove(this.getLowest());
            this.toSearch.sort(Comparator.comparingDouble(this::getAngle));
            this.running = true;
            Thread.sleep(this.delay);
            this.repaint();

            while (this.toSearch.size() > 0) {
                Point top;
                Point nextToTop;
                Point p = this.toSearch.pop();
                do {
                    top = this.hull.pop();
                    nextToTop = this.hull.peek();
                } while (nextToTop != null && this.getCrossProduct(nextToTop, top, p) < 0);
                this.hull.push(top);
                this.hull.push(p);

                Thread.sleep(this.delay);
                this.repaint();
            }
        }

        /* Draws p. */
        void drawPoint (Graphics g, Color c, Point p) {
            g.setColor(c);
            g.fillOval(p.x, this.getHeight() - p.y, this.radius, this.radius);
        }

        /* Draws the line between p1 and p2. */
        void drawLine (Graphics g, Color c, Point p1, Point p2) {
            g.setColor(Color.BLACK);
            g.drawLine(p1.x, this.getHeight() - p1.y, p2.x, this.getHeight() - p2.y);
        }

        @Override
        public void paint(Graphics g) {
            /* Draw all points (to ensure that non-hull points are drawn. */
            for (Point p : this.points) {
                this.drawPoint(g, Color.BLACK, p);
            }

            /* Draw every point in the intermediate hull. */
            Point last = null;
            for (Point p : this.hull) {
                if (last != null) {
                    drawLine(g, Color.BLACK, p, last);
                }
                this.drawPoint(g, Color.GREEN, p);
                last = p;
            }

            if (this.toSearch.isEmpty()) {
                /* Draw final line to connect cycle. */
                if (last != null && !this.hull.isEmpty()) {
                    drawLine(g, Color.BLACK, this.hull.peekFirst(), last);
                }
            } else if (this.running) {
                /* Indicate the next point to look at. */
                drawPoint(g, Color.ORANGE, this.toSearch.peek());
            }
        }
    }
    public static void main(String[] args) throws InterruptedException {
        JFrame frame = new JFrame();
        ConvexHull ch = new ConvexHull();

        frame.setSize(Main.WIDTH, Main.HEIGHT);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(ch);
        frame.setVisible(true);

        ch.run();
    }
}
