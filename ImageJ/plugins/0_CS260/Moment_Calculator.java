import ij.*;
import ij.gui.*;
import ij.measure.*;
import ij.plugin.filter.*;
import ij.process.*;
import ij.text.*;

import java.awt.*;
import java.util.Date;

public class Moment_Calculator implements PlugInFilter, Measurements {
    ImagePlus imp;
    boolean done;
    static boolean firstTime = true;
    static boolean show_imageName = true;
    static boolean show_m00 = true;
    static boolean show_xC = true;
    static boolean show_yC = true;
    static boolean show_xxVar = true;
    static boolean show_yyVar = true;
    static boolean show_xyVar = true;
    static boolean show_xSkew = true;
    static boolean show_ySkew = true;
    static boolean show_xKurt = true;
    static boolean show_yKurt = true;
    static boolean show_orientation = true;
    static boolean show_eccentricity = true;
    static double dCutoff = 0.0; // default cutoff (minimum) value for calcs
    static double dFactor = 1.0; // default factor


    public int setup(String arg, ImagePlus imp) {
        if (IJ.versionLessThan("1.23k")) // needs the new PluginFilter interface
            return DONE;
        if (arg.equals("about")) {
            return DONE;
        }
        this.imp = imp;
        IJ.register(Moment_Calculator.class);
        return DOES_ALL + DOES_STACKS + NO_CHANGES;
    } // end of 'setup()' method

    public void run(ImageProcessor ip) {
        if (done)
            return;

        if (firstTime || Analyzer.getResultsTable().getCounter() == 0) {
            if (Analyzer.resetCounter()) {
                setMoments(); // similar to Analyze|Set Measurements
                firstTime = false;
            } else {
                return; // user canceled save changes dialog
            }
        }

        int measurements = Analyzer.getMeasurements(); // defined in Set Measurements dialog
        Analyzer.setMeasurements(measurements);
        Analyzer a = new Analyzer();
        Calibration cal = imp.getCalibration();
        ImageStatistics stats = ImageStatistics.getStatistics(ip, measurements, cal);

        // Declare & initialize variables

        double zero = 0.0;
        double m00 = zero;
        double m10 = zero, m01 = zero;
        double m20 = zero, m02 = zero, m11 = zero;
        double m30 = zero, m03 = zero, m21 = zero, m12 = zero;
        double m40 = zero, m04 = zero, m31 = zero, m13 = zero;
        double xC = zero, yC = zero;
        double xxVar = zero, yyVar = zero, xyVar = zero;
        double xSkew = zero, ySkew = zero;
        double xKurt = zero, yKurt = zero;
        double orientation = zero, eccentricity = zero;
        double currentPixel, xCoord, yCoord;

        String imageName = imp.getTitle();
        int width = ip.getWidth();
        int height = ip.getHeight();
        double pw = cal.pixelWidth;
        double ph = cal.pixelHeight;
        boolean isScaled = cal.scaled();
        boolean isCalibrated = cal.calibrated();
        String calUnits = cal.getValueUnit();
        String units = cal.getUnits();
        Roi roi = imp.getRoi();
        Rectangle r = ip.getRoi();
        byte[] mask = ip.getMaskArray();
        int maskCounter = 0;
        ip.setCalibrationTable(cal.getCTable());

        // Compute moments of order 0 & 1

        for (int y = r.y; y < (r.y + r.height); y++) {
            for (int x = r.x; x < (r.x + r.width); x++) {
                if (mask == null || mask[maskCounter++] != 0) {
                    xCoord = (x + 0.5) * pw; //this pixel's X calibrated coord. (e.g. cm)
                    yCoord = (y + 0.5) * ph; //this pixel's Y calibrated coord. (e.g. cm)
                    currentPixel = ip.getPixelValue(x, y);
                    currentPixel = currentPixel - dCutoff;
                    if (currentPixel < 0) currentPixel = zero; //gets rid of negative pixel values
                    currentPixel = dFactor * currentPixel;
                    /*0*/
                    m00 += currentPixel;
                    /*1*/
                    m10 += currentPixel * xCoord;
                    m01 += currentPixel * yCoord;
                }
            }
        }

        // Compute coordinates of centre of mass

        xC = m10 / m00;
        yC = m01 / m00;

        // Compute moments of orders 2, 3, 4

        // Reset index on "mask"
        maskCounter = 0;
        for (int y = r.y; y < (r.y + r.height); y++) {
            for (int x = r.x; x < (r.x + r.width); x++) {
                if (mask == null || mask[maskCounter++] != 0) {
                    xCoord = (x + 0.5) * pw; //this pixel's X calibrated coord. (e.g. cm)
                    yCoord = (y + 0.5) * ph; //this pixel's Y calibrated coord. (e.g. cm)
                    currentPixel = ip.getPixelValue(x, y);
                    currentPixel = currentPixel - dCutoff;
                    if (currentPixel < 0) currentPixel = zero; //gets rid of negative pixel values
                    currentPixel = dFactor * currentPixel;
                    /*2*/
                    m20 += currentPixel * (xCoord - xC) * (xCoord - xC);
                    m02 += currentPixel * (yCoord - yC) * (yCoord - yC);
                    m11 += currentPixel * (xCoord - xC) * (yCoord - yC);

                    /*3*/
                    m30 += currentPixel * (xCoord - xC) * (xCoord - xC) * (xCoord - xC);
                    m03 += currentPixel * (yCoord - yC) * (yCoord - yC) * (yCoord - yC);
                    m21 += currentPixel * (xCoord - xC) * (xCoord - xC) * (yCoord - yC);
                    m12 += currentPixel * (xCoord - xC) * (yCoord - yC) * (yCoord - yC);

                    /*4*/
                    m40 += currentPixel * (xCoord - xC) * (xCoord - xC) * (xCoord - xC) * (xCoord - xC);
                    m04 += currentPixel * (yCoord - yC) * (yCoord - yC) * (yCoord - yC) * (yCoord - yC);
                    m31 += currentPixel * (xCoord - xC) * (xCoord - xC) * (xCoord - xC) * (yCoord - yC);
                    m13 += currentPixel * (xCoord - xC) * (yCoord - yC) * (yCoord - yC) * (yCoord - yC);
                }
            }
        }

        // Normalize 2nd moments & compute VARIANCE around centre of mass
        xxVar = m20 / m00;
        yyVar = m02 / m00;
        xyVar = m11 / m00;

        // Normalize 3rd moments & compute SKEWNESS (symmetry) around centre of mass
        // source: Farrell et al, 1994, Water Resources Research, 30(11):3213-3223
        xSkew = m30 / (m00 * Math.pow(xxVar, (3.0 / 2.0)));
        ySkew = m03 / (m00 * Math.pow(yyVar, (3.0 / 2.0)));

        // Normalize 4th moments & compute KURTOSIS (peakedness) around centre of mass
        // source: Farrell et al, 1994, Water Resources Research, 30(11):3213-3223
        xKurt = m40 / (m00 * Math.pow(xxVar, 2.0)) - 3.0;
        yKurt = m04 / (m00 * Math.pow(yyVar, 2.0)) - 3.0;

        // Compute Orientation and Eccentricity
        // source: Awcock, G.J., 1995, "Applied Image Processing", pp. 162-165
        orientation = 0.5 * Math.atan2((2.0 * m11), (m20 - m02));
        orientation = orientation * 180. / Math.PI; //convert from radians to degrees
        eccentricity = (Math.pow((m20 - m02), 2.0) + (4.0 * m11 * m11)) / m00;

        a.saveResults(stats, roi); // store in system results table
        ResultsTable rt = Analyzer.getResultsTable(); // get the system results table

        if (show_imageName) rt.addLabel("Image", imageName);
        rt.addValue("Cutoff", dCutoff);
        rt.addValue("Factor", dFactor);
        if (show_m00) rt.addValue("Mass", m00);
        if (show_xC) rt.addValue("xC", xC);
        if (show_yC) rt.addValue("yC", yC);
        if (show_xxVar) rt.addValue("xxVar", xxVar);
        if (show_yyVar) rt.addValue("yyVar", yyVar);
        if (show_xyVar) rt.addValue("xyVar", xyVar);
        if (show_xSkew) rt.addValue("xSkew", xSkew);
        if (show_ySkew) rt.addValue("ySkew", ySkew);
        if (show_xKurt) rt.addValue("xKurt", xKurt);
        if (show_yKurt) rt.addValue("yKurt", yKurt);
        if (show_orientation) rt.addValue("Orient.", orientation);
        if (show_eccentricity) rt.addValue("Elong.", eccentricity);

        int counter = rt.getCounter();
        if (counter == 1) {
            updateHeadings(rt); // update the worksheet headings
            Date date = new Date(); //get today's date
            String comment = " [ " + date + " ]";
            IJ.write(comment);
            if (isScaled) {
                IJ.write(" Scaled image [ distance units = " + units + " ].");
            } else {
                IJ.write(" No spatial calibration [results in pixels].");
            }
            if (isCalibrated) {
                IJ.write(" Calibrated image [ density units = " + calUnits + " ].");
            } else {
                IJ.write(" No density calibration [uncalibrated results].");
            }
        }

        IJ.write(rt.getRowAsString(counter - 1));

    }

    /**
     * Prompt user to select moments to display & set cutoff value
     */
    public void setMoments() {
        GenericDialog gd = new GenericDialog("Set Spatial Moments");
        gd.addCheckbox("Image_Name  ", true);
        gd.addCheckbox("Total_Mass  ", true);
        gd.addCheckbox("X_Centre of Mass ", true);
        gd.addCheckbox("Y_Centre of Mass  ", true);
        gd.addCheckbox("X_Variance  ", true);
        gd.addCheckbox("Y_Variance  ", true);
        gd.addCheckbox("XY_Covariance  ", true);
        gd.addCheckbox("X_Skewness  ", true);
        gd.addCheckbox("Y_Skewness  ", true);
        gd.addCheckbox("X_Kurtosis  ", true);
        gd.addCheckbox("Y_Kurtosis  ", true);
        gd.addCheckbox("Orientation  ", true);
        gd.addCheckbox("Elongation  ", true);
        gd.addNumericField("Cutoff Value: ", dCutoff, 4);
        gd.addNumericField("Scaling Factor: ", dFactor, 4);
        gd.addMessage("  Note: Pixel values will be converted prior to \n" +
                "        moment calculations using:\n\n" +
                "           pixelValue = Factor*(pixelValue-Cutoff) \n");
        gd.showDialog();
        if (gd.wasCanceled()) {
            IJ.showMessage("Moment Calculator", "Default values will be used.");
            return;
        }
        show_imageName = gd.getNextBoolean();
        show_m00 = gd.getNextBoolean();
        show_xC = gd.getNextBoolean();
        show_yC = gd.getNextBoolean();
        show_xxVar = gd.getNextBoolean();
        show_yyVar = gd.getNextBoolean();
        show_xyVar = gd.getNextBoolean();
        show_xSkew = gd.getNextBoolean();
        show_ySkew = gd.getNextBoolean();
        show_xKurt = gd.getNextBoolean();
        show_yKurt = gd.getNextBoolean();
        show_orientation = gd.getNextBoolean();
        show_eccentricity = gd.getNextBoolean();
        dCutoff = (double) gd.getNextNumber();
        dFactor = (double) gd.getNextNumber();
    }

    public void updateHeadings(ResultsTable rt) {
        TextPanel tp = IJ.getTextPanel();
        if (tp == null)
            return;
        String worksheetHeadings = tp.getColumnHeadings();

        String tableHeadings = rt.getColumnHeadings();
        if (!worksheetHeadings.equals(tableHeadings))
            IJ.setColumnHeadings(tableHeadings);
    }

}