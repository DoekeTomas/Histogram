/*
 * Framework code written for the Multimedia course taught in the first year
 * of the UvA Informatica bachelor.
 *
 * Nardi Lam, 2015 (based on code by I.M.J. Kamps, S.J.R. van Schaik, R. de Vries, 2013)
 */

package nl.uva.multimedia.image;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.nfc.Tag;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Arrays;

import nl.uva.multimedia.ImageActivity;
import nl.uva.multimedia.R;

/*
 * This is a View that displays incoming images.
 */
public class ImageDisplayView extends View implements ImageListener {

    /*** Constructors ***/

    public ImageDisplayView(Context context) {
        super(context);
    }

    public ImageDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageDisplayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /*** Image drawing ***/

    private int[] currentImage = null;
    private int imageWidth, imageHeight;

    @Override
    public void onImage(int[] argb, int width, int height) {
        /* When we recieve an image, simply store it and invalidate the View so it will be
         * redrawn. */
        this.currentImage = argb;
        this.imageWidth = width;
        this.imageHeight = height;
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: Hier wordt een afbeelding op het scherm laten zien!
        // Je zou hier dus code kunnen plaatsen om iets anders weer te geven.

        /* If there is an image to be drawn: */
        if (this.currentImage != null) {
            int pixels = this.imageWidth * this.imageHeight;

            int[] redColor     = new int[pixels];
            int[] greenColor   = new int[pixels];
            int[] blueColor    = new int[pixels];

            int[] totalColor        = new int[3];
            int[] mean              = new int[3];
            int[] median            = new int[3];
            int[] deviationsTotal   = new int[3];
            double[] deviation    = new double[3];


            // Maak een array van de groen intensiteit per pixel
            for (int i = 0; i < pixels; i++) {
                redColor[i]     = Color.red(this.currentImage[i]);
                greenColor[i]   = Color.green(this.currentImage[i]);
                blueColor[i]    = Color.blue(this.currentImage[i]);

                totalColor[0] = totalColor[0] + redColor[i];
                totalColor[1] = totalColor[1] + greenColor[i];
                totalColor[2] = totalColor[2] + blueColor[i];
            }

            /* Mean berekenen */
            mean[0] = totalColor[0] / pixels;
            mean[1] = totalColor[1] / pixels;
            mean[2] = totalColor[2] / pixels;

            /* Arrays sorteren & mediaan berekenen */

            Arrays.sort(redColor);
            Arrays.sort(greenColor);
            Arrays.sort(blueColor);

            median[0] = redColor[pixels / 2];
            median[1] = greenColor[pixels / 2];
            median[2] = blueColor[pixels / 2];

            /* Standaard afwijking berekenen */

            for (int i = 0; i < pixels; i++) {
                deviationsTotal[0] += Math.pow(redColor[i] - mean[0], 2);
                deviationsTotal[1] += Math.pow(greenColor[i] - mean[1], 2);
                deviationsTotal[2] += Math.pow(blueColor[i] - mean[2], 2);
            }

            deviation[0] = Math.sqrt(deviationsTotal[0] / pixels);
            deviation[1] = Math.sqrt(deviationsTotal[1] / pixels);
            deviation[2] = Math.sqrt(deviationsTotal[2] / pixels);

            /* Center the image... */
            int left = 100;
            int top = 200;
            int right = this.getWidth() - 100;
            int bottom = this.getHeight() - 100;

            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(3);
            paint.setTextSize(40);

            canvas.drawLine(left, top, left, bottom, paint);
            canvas.drawLine(left, bottom, right, bottom, paint);

            canvas.drawText("0", left - 50, bottom + 50, paint);
            canvas.drawText("255", right + 20, bottom + 50, paint);
            canvas.drawText("Median: " + median[1], left - 50, top - 130, paint);
            canvas.drawText("Mean: " + mean[1], left - 50, top - 85, paint);
            canvas.drawText("Standard deviation: " + (int)(deviation[1]), left - 50, top - 40, paint);

            int binsNr = ImageActivity.binsNr;
            int[] bins = new int[binsNr];
            double binSize = 255 / binsNr;
            float binWidth = (right - left) / binsNr;

            int nr = 0;
            for (int i = 0; i < binsNr; i++) {
                while (nr < pixels && greenColor[nr] <= i * binSize) {
                    bins[i]++;
                    nr++;
                }
            }

            int maxValueBin = 0;
            for (int i = 0; i < binsNr; i++) {
                if (bins[i] > maxValueBin) {
                    maxValueBin = bins[i];
                }
            }

            double binHeight = (double)(bottom - top) / maxValueBin;

            Color[] colors    = new Color[3];


            paint.setColor(olors[1]);

            for (int i = 0; i < binsNr; i++) {
                paint.setStrokeWidth(0);
                canvas.drawRect((left+1) + (i * binWidth), (float)((bottom) - (bins[i] * binHeight)),
                                (left) + ((i+1) * binWidth), bottom, paint);
            }

        }
    }

    /*** Source selection ***/

    private ImageSource source = null;

    public void setImageSource(ImageSource source) {
        if (this.source != null) {
            this.source.setOnImageListener(null);
        }
        source.setOnImageListener(this);
        this.source = source;
    }

    public ImageSource getImageSource() {
        return this.source;
    }

}
