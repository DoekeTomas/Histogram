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
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

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

        /* If there is an image to be drawn: */
        if (this.currentImage != null) {
            int pixels = this.imageWidth * this.imageHeight;

            /* Arrays voor rood, groen en blauwe pixels */
            int[] redColor     = new int[pixels];
            int[] greenColor   = new int[pixels];
            int[] blueColor    = new int[pixels];

            /* Arrays voor resultaten van berekeningen */
            int[] totalColor        = new int[3];
            int[] mean              = new int[3];
            int[] median            = new int[3];
            int[] deviationsTotal   = new int[3];
            double[] deviation    = new double[3];


            /* Zet alle pixels in array & berekent totale kleurwaardes */
            for (int i = 0; i < pixels; i++) {
                redColor[i]     = Color.red(this.currentImage[i]);
                greenColor[i]   = Color.green(this.currentImage[i]);
                blueColor[i]    = Color.blue(this.currentImage[i]);

                totalColor[0] = totalColor[0] + redColor[i];
                totalColor[1] = totalColor[1] + greenColor[i];
                totalColor[2] = totalColor[2] + blueColor[i];
            }

            /* Arrays sorteren   */
            Arrays.sort(redColor);
            Arrays.sort(greenColor);
            Arrays.sort(blueColor);

            /* Gemiddelde berekenen */
            for (int i = 0; i < 3; i++) {
                mean[i] = totalColor[i] / pixels;
            }

            /* Mediaan berekenen */
            median[0] = redColor[pixels / 2];
            median[1] = greenColor[pixels / 2];
            median[2] = blueColor[pixels / 2];

            /* Standaard afwijking berekenen */
            for (int i = 0; i < pixels; i++) {
                deviationsTotal[0] += Math.pow(redColor[i] - mean[0], 2);
                deviationsTotal[1] += Math.pow(greenColor[i] - mean[1], 2);
                deviationsTotal[2] += Math.pow(blueColor[i] - mean[2], 2);
            }

            for (int i = 0; i < 3; i++) {
                deviation[i] = Math.sqrt(deviationsTotal[i] / pixels);
            }

            /* Bereken de hoekpunten van de histogram zodat op elk device
             * de volledige canvas wordt gebruikt voor het tekenen van de
             * grafiek
             */
            int left = 100;
            int top = 200;
            int right = this.getWidth() - 100;
            int bottom = this.getHeight() - 100;

            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(3);
            paint.setTextSize(40);

            /* x- & y-as tekenen */
            canvas.drawLine(left, top, left, bottom, paint);
            canvas.drawLine(left, bottom, right, bottom, paint);

            /* Legenda schrijven */
            paint.setColor(Color.GRAY);
            canvas.drawText("intensity", right - 200, bottom + 50, paint);
            canvas.save();
            canvas.rotate(-90, left - 30, top + 350);
            canvas.drawText("amount of pixels", left - 30, top + 350, paint);
            canvas.restore();

            int colorNr = ImageActivity.colorNr;

            /* Print resultaten van berekeningen */
            paint.setColor(Color.BLACK);
            canvas.drawText("0", left - 50, bottom + 50, paint);
            canvas.drawText("255", right + 20, bottom + 50, paint);
            canvas.drawText("Median: " + median[colorNr], left - 50, top - 130, paint);
            canvas.drawText("Mean: " + mean[colorNr], left - 50, top - 85, paint);
            canvas.drawText("Standard deviation: " + (int)(deviation[colorNr]), left - 50, top - 40, paint);

            /* Kleur om te bekijken in grafiek */
            int[] graphColor = new int[pixels];

            switch(colorNr) {
                case 0:
                    graphColor = redColor;
                    break;
                case 1:
                    graphColor = greenColor;
                    break;
                case 2:
                    graphColor = blueColor;
                    break;
                default:
                    break;
            }

            /* Bin gegevens */
            int binsNr = ImageActivity.binsNr;
            int[] bins = new int[binsNr];
            double binSize = 255 / binsNr;
            float binWidth = (right - left) / binsNr;

            /* Tel per bin hoeveel pixels er in de bin zitten */
            int nr = 0;
            for (int i = 0; i < binsNr; i++) {
                while (nr < pixels && graphColor[nr] <= i * binSize) {
                    bins[i]++;
                    nr++;
                }
            }

            /* Berekent hoogte van grootste staaf */
            int maxValueBin = 0;
            for (int i = 0; i < binsNr; i++) {
                if (bins[i] > maxValueBin) {
                    maxValueBin = bins[i];
                }

            }

            /* Hoogte die per pixel wordt toegevoegd aan de staaf */
            double binHeight = (double)(bottom - top) / maxValueBin;

            /* Zet kleur van de staven */
            int[] colors   = new int[3];
            colors[0] = Color.RED;
            colors[1] = Color.GREEN;
            colors[2] = Color.BLUE;

            paint.setColor(colors[colorNr]);

            /* Tekent histogram */
            for (int i = 0; i < binsNr; i++) {
                paint.setStrokeWidth(0);
                canvas.drawRect((left+1) + (i * binWidth), (float)((bottom) - (bins[i] * binHeight)),
                                (left) + ((i+1) * binWidth), bottom, paint);
            }

        }
    }

    /*** Source selection ***/

    private ImageSource source = null;

    public void doCalculations(int pixels) {

    }

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
