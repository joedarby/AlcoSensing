package com.joedarby.alcosensing1.Sensing;

import android.util.Log;

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKSensorType;
import org.sensingkit.sensingkitlib.data.SKSensorData;

import java.io.File;

public class ThreeDimModelWriter extends ModelWriter {

    private static final int dataBufferWindowSize = 1000;
    private static final double threshold = 1.5;
    private BoundedLinkedList<AccelerometerReading> dataBuffer = new BoundedLinkedList<>(dataBufferWindowSize);
    private double sumX, sumY, sumZ;
    private double meanX, meanY, meanZ;
    private double sumSqX, sumSqY, sumSqZ;
    private double varianceX, varianceY, varianceZ;
    private long lastWriteTime;

    public ThreeDimModelWriter(SKSensorType moduleType, File sessionFolder, String filename) throws SKException {
        super(moduleType, sessionFolder, filename);
    }

    @Override
    public void onDataReceived(SKSensorType moduleType, SKSensorData moduleData) {
        String dataLine = moduleData.getDataInCSV();
        AccelerometerReading reading = new AccelerometerReading(dataLine);
        updateMeans(reading);
        updateVariances(reading);
        dataBuffer.add(reading);

        double deltaX = Math.abs(reading.x - meanX);
        double deltaY = Math.abs(reading.y - meanY);
        double deltaZ = Math.abs(reading.z - meanZ);

        if (dataBuffer.size() == dataBufferWindowSize) {
            if (deltaX > Math.sqrt(varianceX)*threshold || deltaY > Math.sqrt(varianceY)*threshold || deltaZ > Math.sqrt(varianceZ)*threshold || reading.time > (lastWriteTime + 500)) {
                super.onDataReceived(moduleType, moduleData);
                lastWriteTime = reading.time;
            }
        }
    }

    private class AccelerometerReading {
        long time;
        float x, y, z;

        AccelerometerReading(String dataLine) {
            String[] split = dataLine.split(",");

            try {
                time = Long.parseLong(split[0]);
                x = Float.parseFloat(split[1]);
                y = Float.parseFloat(split[2]);
                z = Float.parseFloat(split[3]);
            } catch (NumberFormatException e) {
                Log.e("ModelWriter", "Bad data");
            }
        }
    }

    private void updateMeans(AccelerometerReading reading) {
        if (dataBuffer.size() < dataBufferWindowSize) {
            sumX += reading.x;
            sumY += reading.y;
            sumZ += reading.z;
            meanX = sumX / (dataBuffer.size()+1);
            meanY = sumY / (dataBuffer.size()+1);
            meanZ = sumZ / (dataBuffer.size()+1);
        } else {
            AccelerometerReading oldestValue = dataBuffer.getFirst();
            sumX = sumX - oldestValue.x + reading.x;
            sumY = sumY - oldestValue.y + reading.y;
            sumZ = sumZ - oldestValue.z + reading.z;
            meanX = sumX / dataBuffer.size();
            meanY = sumY / dataBuffer.size();
            meanZ = sumZ / dataBuffer.size();
        }
    }

    private void updateVariances(AccelerometerReading reading) {
        if (dataBuffer.size() < dataBufferWindowSize) {
            sumSqX = sumSqX + (Math.pow((reading.x - meanX),2));
            sumSqY = sumSqY + (Math.pow((reading.y - meanY),2));
            sumSqZ = sumSqZ + (Math.pow((reading.z - meanZ),2));

        } else {
            AccelerometerReading oldestValue = dataBuffer.getFirst();
            sumSqX = sumSqX - (Math.pow((oldestValue.x - meanX),2)) + (Math.pow((reading.x - meanX),2));
            sumSqY = sumSqY - (Math.pow((oldestValue.y - meanY),2)) + (Math.pow((reading.y - meanY),2));
            sumSqZ = sumSqZ - (Math.pow((oldestValue.z - meanZ),2)) + (Math.pow((reading.z - meanZ),2));
        }
        if (dataBuffer.size() >0) {
            varianceX = sumSqX / dataBuffer.size();
            varianceY = sumSqY / dataBuffer.size();
            varianceZ = sumSqZ / dataBuffer.size();
        }

    }
}
