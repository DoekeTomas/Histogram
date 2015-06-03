/*
 * Framework code written for the Multimedia course taught in the first year
 * of the UvA Informatica bachelor.
 *
 * Nardi Lam, 2015 (based on code by I.M.J. Kamps, S.J.R. van Schaik, R. de Vries, 2013)
 */

package nl.uva.multimedia;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;

import nl.uva.multimedia.image.CameraImageSource;
import nl.uva.multimedia.image.FileImageSource;
import nl.uva.multimedia.image.ImageDisplayView;

/*
 * An activity containing the basics for an image processing application.
 */
public class ImageActivity extends Activity {

    /*** Source constants (position in list) ***/
    final int SOURCE_BACK_CAMERA = 0;
    final int SOURCE_FRONT_CAMERA = 1;
    final int SOURCE_IMAGE = 2;

    private CameraImageSource cis;
    private FileImageSource fis;

    /* Globale variabelen */
    public static int binsNr = 10;
    public static int colorNr = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_image);
        final ImageDisplayView imageView = (ImageDisplayView)findViewById(R.id.display_view);

        /* Create sources: */
        this.cis = new CameraImageSource(this);
        this.fis = new FileImageSource();

        Spinner sourceSpinner = (Spinner)this.findViewById(R.id.source_spinner);

        /* Initialiseer de slider */
        SeekBar seekBar = (SeekBar)this.findViewById(R.id.seekBar);
        seekBar.setProgress(10);

        final TextView textBins = (TextView)this.findViewById(R.id.textBins);
        final int minimumBins = 3;

        seekBar.setOnSeekBarChangeListener (
                new SeekBar.OnSeekBarChangeListener() {

                    /* Check of slider is veranderd */
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                        /* Zet het aantal bins gelijk aan de slider waarde */
                        if (i >= minimumBins) {
                            binsNr = i;
                            textBins.setText("Number of bins: " + Integer.toString(i));
                        }

                        /* Zet het aantal bins op 3 als slider waarde te klein wordt */
                        else {
                            binsNr = 3;
                            textBins.setText("Number of bins: 3");
                        }

                        /* Roep ImageDisplayView.onDraw(Canvas canvas) aan */
                        imageView.invalidate();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

        /* Switching between sources: */
        sourceSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        switch (position) {
                            case SOURCE_BACK_CAMERA:
                                ImageActivity.this.cis.switchTo(CameraImageSource.BACK_CAMERA);
                                ImageActivity.this.switchToCamera();
                                break;
                            case SOURCE_FRONT_CAMERA:
                                ImageActivity.this.cis.switchTo(CameraImageSource.FRONT_CAMERA);
                                ImageActivity.this.switchToCamera();
                                break;
                            case SOURCE_IMAGE:
                                ImageActivity.this.switchToImage();
                                break;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        /* Freeze switch: */
        ((CompoundButton)findViewById(R.id.freeze_toggle)).setOnCheckedChangeListener(
            new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ImageActivity.this.cis.setFrozen(isChecked);
            }
        });

        /* "Load image" button: */
        findViewById(R.id.load_image_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Start an external Activity for choosing an image, result is returned in
                 * onActivityResult(). */
                Intent it = new Intent();
                it.setType("image/*");
                it.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(it, "Load image from..."), SOURCE_IMAGE);
            }
        });

        /* Select the back camera as default source: */
        sourceSpinner.setSelection(SOURCE_BACK_CAMERA);
    }

    /* Check welke kleur is geselecteerd */
    public void onColorButtonClicked(View button) {
        final ImageDisplayView imageView = (ImageDisplayView)findViewById(R.id.display_view);

        switch(button.getId()) {
            case R.id.red:
                colorNr = 0;
                break;

            case R.id.green:
                colorNr = 1;
                break;

            case R.id.blue:
                colorNr = 2;
                break;
        }

        /* Roep ImageDisplayView.onDraw(Canvas canvas) aan */
        imageView.invalidate();
    }

    private void switchToCamera() {

        /* Set camera as active source: */
        ImageDisplayView idv = (ImageDisplayView)findViewById(R.id.display_view);
        if (idv.getImageSource() != this.cis) {
            idv.setImageSource(this.cis);
        }

        /* Switch out controls: */
        findViewById(R.id.load_image_button).setVisibility(View.GONE);
        findViewById(R.id.freeze_control).setVisibility(View.VISIBLE);
    }

    private void switchToImage() {
        /* Set image as active source: */
        ImageDisplayView idv = (ImageDisplayView)findViewById(R.id.display_view);
        if (idv.getImageSource() != this.fis) {
            idv.setImageSource(this.fis);
        }

        /* Switch out controls: */
        findViewById(R.id.load_image_button).setVisibility(View.VISIBLE);
        findViewById(R.id.freeze_control).setVisibility(View.GONE);
    }

    /* When an image is loaded, the result gets delivered back here: */
    protected void onActivityResult(int requestCode, int resultCode, Intent it) {
        super.onActivityResult(requestCode, resultCode, it);

        /* If we have an image... */
        if (requestCode == SOURCE_IMAGE && resultCode == RESULT_OK && it != null) {
            try {
                /* ...open an input stream and pass it to the FileImageSource: */
                fis.loadFromInputStream(this.getContentResolver().openInputStream(it.getData()));
            } catch (FileNotFoundException e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

}
