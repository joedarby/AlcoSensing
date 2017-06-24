package com.joedarby.alcosensing1.Sensing;

import android.util.Log;

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKExceptionErrorCode;
import org.sensingkit.sensingkitlib.SKSensorDataListener;
import org.sensingkit.sensingkitlib.SKSensorType;
import org.sensingkit.sensingkitlib.data.SKSensorData;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ModelWriter implements SKSensorDataListener {

    @SuppressWarnings("unused")
    private static final String TAG = "ModelWriter";

    private final SKSensorType moduleType;

    private File mFile;
    private BufferedOutputStream mFileBuffer;

    public ModelWriter(SKSensorType moduleType, File sessionFolder, String filename) throws SKException {

        this.moduleType = moduleType;
        this.mFile = createFile(sessionFolder, filename);

        try {
            this.mFileBuffer = new BufferedOutputStream(new FileOutputStream(mFile));
        }
        catch (FileNotFoundException ex) {
            throw new SKException(TAG, "File could not be found.", SKExceptionErrorCode.UNKNOWN_ERROR);
        }

    }

    public void flush() throws SKException {

        try {
            mFileBuffer.flush();
        }
        catch (IOException ex) {
            throw new SKException(TAG, ex.getMessage(), SKExceptionErrorCode.UNKNOWN_ERROR);
        }
    }

    public void close() throws SKException {

        try {
            mFileBuffer.close();
        }
        catch (IOException ex) {
            throw new SKException(TAG, ex.getMessage(), SKExceptionErrorCode.UNKNOWN_ERROR);
        }
    }

    private File createFile(File sessionFolder, String filename) throws SKException {

        File file = new File(sessionFolder, filename + ".csv");

        if (file.exists())
            file.delete();

        try {
            if (!file.createNewFile()) {
                throw new SKException(TAG, "File could not be created.", SKExceptionErrorCode.UNKNOWN_ERROR);
            }
        }
        catch (IOException ex) {
            throw new SKException(TAG, ex.getMessage(), SKExceptionErrorCode.UNKNOWN_ERROR);
        }

        // Make file visible
        //MediaScannerConnection.scanFile(getBaseContext(), new String[]{file.getAbsolutePath()}, null, null);

        return file;
    }

    @Override
    public void onDataReceived(SKSensorType moduleType, SKSensorData moduleData) {

        if (mFileBuffer != null) {

            // Build the data line
            String dataLine = moduleData.getDataInCSV() + "\n";

            // Write in the FileBuffer
            try {
                mFileBuffer.write(dataLine.getBytes());
            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage());
            }
        }
    }

}

