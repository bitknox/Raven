
package dk.itu.raven.join;

public class LinePlotter {

    interface Callback {
        void call(int x, int y);
    }

    public static void plotLine(Callback callback,
            int p1x1, int p1y1, int p2x1, int p2y1,
            int clip_xmin, int clip_ymin,
            int clip_xmax, int clip_ymax) {
        int x1 = p1x1;
        int y1 = p1y1;
        int x2 = p2x1;
        int y2 = p2y1;

        // Vertical line
        if (x1 == x2) {
            if (x1 < clip_xmin || x1 > clip_xmax)
                return;

            if (y1 <= y2) {
                if (y2 < clip_ymin || y1 > clip_ymax)
                    return;
                y1 = Math.max(y1, clip_ymin);
                y2 = Math.min(y2, clip_ymax);
                for (int y = y1; y < y2; y++)
                    callback.call(x1, y);
            } else {
                if (y1 < clip_ymin || y2 > clip_ymax)
                    return;
                y2 = Math.max(y2, clip_ymin);
                y1 = Math.min(y1, clip_ymax);
                for (int y = y1; y > y2; y--)
                    callback.call(x1, y);
            }
            return;
        }

        // Horizontal line

        if (y1 == y2) {
            if (y1 < clip_ymin || y1 > clip_ymax)
                return;

            if (x1 <= x2) {
                if (x2 < clip_xmin || x1 > clip_xmax)
                    return;
                x1 = Math.max(x1, clip_xmin);
                callback.call(x1, y1);
            } else {
                if (x1 < clip_xmin || x2 > clip_xmax)
                    return;
                x2 = Math.max(x2, clip_xmin);
                callback.call(x2, y1);
            }
            return;
        }

        // Now simple cases are handled, perform clipping checks.
        int sign_x, sign_y;
        if (x1 < x2) {
            if (x1 > clip_xmax || x2 < clip_xmin)
                return;
            sign_x = 1;
        } else {
            if (x2 > clip_xmax || x1 < clip_xmin)
                return;
            sign_x = -1;
            x1 = -x1;
            x2 = -x2;
            int temp = clip_xmin;
            clip_xmin = -clip_xmax;
            clip_xmax = -temp;
        }

        if (y1 < y2) {
            if (y1 > clip_ymax || y2 < clip_ymin)
                return;
            sign_y = 1;
        } else {
            if (y2 > clip_ymax || y1 < clip_ymin)
                return;
            sign_y = -1;
            y1 = -y1;
            y2 = -y2;
            int temp = clip_ymin;
            clip_ymin = -clip_ymax;
            clip_ymax = -temp;
        }

        int delta_x = x2 - x1;
        int delta_y = y2 - y1;
        int delta_x_step = 2 * delta_x;
        int delta_y_step = 2 * delta_y;
        int x_pos = x1;
        int y_pos = y1;

        if (delta_x >= delta_y) {
            int error = delta_y_step - delta_x;
            boolean set_exit = false;

            if (y1 < clip_ymin) {
                int temp = (2 * (clip_ymin - y1) - 1) * delta_x;
                int msd = temp / delta_y_step;
                x_pos += msd;

                if (x_pos > clip_xmax)
                    return;

                if (x_pos >= clip_xmin) {
                    int rem = temp - msd * delta_y_step;
                    y_pos = clip_ymin;
                    error -= rem + delta_x;
                    if (rem > 0) {
                        x_pos += 1;
                        error += delta_y_step;
                    }
                    set_exit = true;
                }
            }

            if (!set_exit && x1 < clip_xmin) {
                int temp = delta_y_step * (clip_xmin - x1);
                int msd = temp / delta_x_step;
                y_pos += msd;
                int rem = temp % delta_x_step;

                if (y_pos > clip_ymax || (y_pos == clip_ymax && rem >= delta_x))
                    return;

                x_pos = clip_xmin;
                error += rem;

                if (rem >= delta_x) {
                    y_pos += 1;
                    error -= delta_x_step;
                }
            }

            int x_pos_end = x2;
            if (y2 > clip_ymax) {
                int temp = delta_x_step * (clip_ymax - y1) + delta_x;
                int msd = temp / delta_y_step;
                x_pos_end = x1 + msd;
                if ((temp - msd * delta_y_step) == 0)
                    x_pos_end -= 1;
            }

            x_pos_end = Math.min(x_pos_end, clip_xmax) + 1;
            if (sign_y == -1)
                y_pos = -y_pos;
            if (sign_x == -1) {
                x_pos = -x_pos;
                x_pos_end = -x_pos_end;
            }
            delta_x_step -= delta_y_step;

            boolean firstFound = false;
            while (x_pos != x_pos_end) {
                if (!firstFound) {
                    callback.call(x_pos, y_pos);
                    firstFound = true;
                }

                if (error >= 0) {
                    y_pos += sign_y;
                    if (y_pos >= clip_ymin && y_pos <= clip_ymax)
                        callback.call(x_pos, y_pos);
                    error -= delta_x_step;
                } else {
                    error += delta_y_step;
                }
                x_pos += sign_x;
            }
        } else {
            int error = delta_x_step - delta_y;
            boolean set_exit = false;

            if (x1 < clip_xmin) {
                int temp = (2 * (clip_xmin - x1) - 1) * delta_y;
                int msd = temp / delta_x_step;
                y_pos += msd;

                if (y_pos > clip_ymax)
                    return;

                if (y_pos >= clip_ymin) {
                    int rem = temp - msd * delta_x_step;
                    x_pos = clip_xmin;
                    error -= rem + delta_y;
                    if (rem > 0) {
                        y_pos += 1;
                        error += delta_x_step;
                    }
                    set_exit = true;
                }
            }

            if (!set_exit && y1 < clip_ymin) {
                int temp = delta_x_step * (clip_ymin - y1);
                int msd = temp / delta_y_step;
                x_pos += msd;
                int rem = temp % delta_y_step;

                if (x_pos > clip_xmax || (x_pos == clip_xmax && rem >= delta_y))
                    return;

                y_pos = clip_ymin;
                error += rem;

                if (rem >= delta_y) {
                    x_pos += 1;
                    error -= delta_y_step;
                }
            }

            int y_pos_end = y2;
            if (x2 > clip_xmax) {
                int temp = delta_y_step * (clip_xmax - x1) + delta_y;
                int msd = temp / delta_x_step;
                y_pos_end = y1 + msd;

                if ((temp - msd * delta_x_step) == 0)
                    y_pos_end -= 1;
            }

            y_pos_end = Math.min(y_pos_end, clip_ymax) + 1;
            if (sign_x == -1)
                x_pos = -x_pos;
            if (sign_y == -1) {
                y_pos = -y_pos;
                y_pos_end = -y_pos_end;
            }
            delta_y_step -= delta_x_step;

            while (y_pos != y_pos_end) {
                callback.call(x_pos, y_pos);
                if (error >= 0) {
                    x_pos += sign_x;
                    error -= delta_y_step;
                } else {
                    error += delta_x_step;
                }
                y_pos += sign_y;
            }
        }
    }
}
