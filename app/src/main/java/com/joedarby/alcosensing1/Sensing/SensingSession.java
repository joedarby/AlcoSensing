package com.joedarby.alcosensing1.Sensing;

import android.content.Context;
import android.hardware.SensorManager;

import com.joedarby.alcosensing1.Helpers.StatusHelper;

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKExceptionErrorCode;
import org.sensingkit.sensingkitlib.SKSensorType;
import org.sensingkit.sensingkitlib.SensingKitLib;
import org.sensingkit.sensingkitlib.SensingKitLibInterface;
import org.sensingkit.sensingkitlib.configuration.SKAbstractConfiguration;
import org.sensingkit.sensingkitlib.configuration.SKAccelerometerConfiguration;
import org.sensingkit.sensingkitlib.configuration.SKAudioLevelConfiguration;
import org.sensingkit.sensingkitlib.configuration.SKBatteryConfiguration;
import org.sensingkit.sensingkitlib.configuration.SKGyroscopeConfiguration;
import org.sensingkit.sensingkitlib.configuration.SKLocationConfiguration;
import org.sensingkit.sensingkitlib.configuration.SKMagnetometerConfiguration;
import org.sensingkit.sensingkitlib.configuration.SKMotionActivityConfiguration;
import org.sensingkit.sensingkitlib.configuration.SKNotificationConfiguration;
import org.sensingkit.sensingkitlib.configuration.SKRotationConfiguration;
import org.sensingkit.sensingkitlib.configuration.SKScreenStatusConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SensingSession {

    @SuppressWarnings("unused")
    private static final String TAG = "SensingSession";

    // SensingKit
    private SensingKitLibInterface mSensingKitLib;
    private boolean isSensing = false;

    // Session Folder
    private File mSessionFolder;

    // Models

    private final Map<SKSensorType, ConfigModelPair> sensors = new HashMap<>();

    public SensingSession(Context context) throws SKException {

        String fileName = StatusHelper.getFileLabel(context);

        // Init SensingKit
        mSensingKitLib = SensingKitLib.getSensingKitLib(context);

        // Create the folder
        mSessionFolder = createFolder(context);

        //Init Sensors and ModelWriters
        sensors.put(SKSensorType.AUDIO_LEVEL,
                new ConfigModelPair(
                        new ModelWriter(SKSensorType.AUDIO_LEVEL, mSessionFolder, fileName + "Audio"),
                        new SKAudioLevelConfiguration())
        );
        sensors.put(SKSensorType.ACCELEROMETER,
                new ConfigModelPair(
                        new ThreeDimModelWriter(SKSensorType.ACCELEROMETER, mSessionFolder, fileName + "Accelerometer"),
                        createAccelerometerConfig())
        );
        //sensors.put(SKSensorType.GRAVITY, new ModelWriter(SKSensorType.GRAVITY, mSessionFolder, "Gravity"));
        //sensors.put(SKSensorType.LINEAR_ACCELERATION, new ModelWriter(SKSensorType.LINEAR_ACCELERATION, mSessionFolder, "LinearAcceleration"));
        sensors.put(SKSensorType.GYROSCOPE,
                new ConfigModelPair(new ThreeDimModelWriter(SKSensorType.GYROSCOPE, mSessionFolder, fileName + "Gyroscope") ,
                        createGyroscopeConfig())
        );
        /*sensors.put(SKSensorType.ROTATION,
                new ConfigModelPair(
                        new ModelWriter(SKSensorType.ROTATION, mSessionFolder, "Rotation"),
                        createRotationConfig())
        );*/
        sensors.put(SKSensorType.MAGNETOMETER, new ConfigModelPair(
                new ThreeDimModelWriter(SKSensorType.MAGNETOMETER, mSessionFolder, fileName + "Magnetometer"),
                createMagnetometerConfig())
        );

        sensors.put(SKSensorType.LOCATION,
                new ConfigModelPair(
                        new ModelWriter(SKSensorType.LOCATION, mSessionFolder, fileName + "Location"),
                        createLocationConfig())
        );
        sensors.put(SKSensorType.BATTERY,
                new ConfigModelPair(
                        new ModelWriter(SKSensorType.BATTERY, mSessionFolder, fileName + "Battery"),
                        new SKBatteryConfiguration())
        );
        sensors.put(SKSensorType.NOTIFICATION,
                new ConfigModelPair(
                        new ModelWriter(SKSensorType.NOTIFICATION, mSessionFolder, fileName + "Notifications"),
                        new SKNotificationConfiguration())
        );
        sensors.put(SKSensorType.SCREEN_STATUS,
                new ConfigModelPair(
                        new ModelWriter(SKSensorType.SCREEN_STATUS, mSessionFolder, fileName + "ScreenStatus"),
                        new SKScreenStatusConfiguration())
        );
        sensors.put(SKSensorType.MOTION_ACTIVITY,
                new ConfigModelPair(
                        new ModelWriter(SKSensorType.MOTION_ACTIVITY, mSessionFolder, fileName + "MotionActivity"),
                        new SKMotionActivityConfiguration())
        );

        for (SKSensorType sensor : sensors.keySet()) {
            if (mSensingKitLib.isSensorAvailable(sensor)) {
                // Register Sensors
                ConfigModelPair cmp = sensors.get(sensor);
                if (cmp.configIsSet())
                    try {
                        mSensingKitLib.registerSensor(sensor, cmp.getConfig());
                    } catch (SKException e) {
                        e.printStackTrace();
                    }
                else
                    try {
                        mSensingKitLib.registerSensor(sensor);
                    } catch (SKException e) {
                        e.printStackTrace();
                    }
                // Subscribe ModelWriters
                mSensingKitLib.subscribeSensorDataListener(sensor, sensors.get(sensor).m);
            }
        }
    }


    public void start() throws SKException {

        this.isSensing = true;
        // Start
        for (SKSensorType sensor : sensors.keySet())
            if (mSensingKitLib.isSensorRegistered(sensor))
                mSensingKitLib.startContinuousSensingWithSensor(sensor);
    }

    public void stop() throws SKException {

        this.isSensing = false;

        // Stop
        for (SKSensorType sensor : sensors.keySet())
            if (mSensingKitLib.isSensorRegistered(sensor)) {
                if (mSensingKitLib.isSensorSensing(sensor)) {
                    mSensingKitLib.stopContinuousSensingWithSensor(sensor);
                    sensors.get(sensor).getModelWriter().flush();
                }
            }

    }

    public void close() throws SKException {
        for (SKSensorType sensor : sensors.keySet())
            if (mSensingKitLib.isSensorRegistered(sensor)) {
                // Unsubscribe ModelWriters
                mSensingKitLib.unsubscribeSensorDataListener(sensor, sensors.get(sensor).getModelWriter());
                // Deregister Sensors
                mSensingKitLib.deregisterSensor(sensor);
                // Close
                sensors.get(sensor).getModelWriter().close();
            }
    }

    public boolean isSensing() {
        return this.isSensing;
    }

    private File createFolder(Context context) throws SKException {

        // Create App folder: CrowdSensing
        File appFolder = new File(context.getFilesDir().getAbsolutePath() + "/SensorData/data/");

        if (!appFolder.exists()) {
            if (!appFolder.mkdirs()) {
                throw new SKException(TAG, "Folder could not be created.", SKExceptionErrorCode.UNKNOWN_ERROR);
            }
        }

        return appFolder;
    }

    private SKAbstractConfiguration createLocationConfig() throws SKException {
        SKLocationConfiguration locationCF = new SKLocationConfiguration();
        locationCF.setFastestInterval(1000); //milliseconds
        locationCF.setInterval(1000*60*2);
        locationCF.setPriority(SKLocationConfiguration.Priority.BALANCED_POWER_ACCURACY);
        return locationCF;
    }

    private SKAbstractConfiguration createAccelerometerConfig() {
        SKAccelerometerConfiguration accelerometerCf = new SKAccelerometerConfiguration();
        accelerometerCf.setSamplingRate(SensorManager.SENSOR_DELAY_GAME);
        return accelerometerCf;
    }

    private SKAbstractConfiguration createGyroscopeConfig() {
        SKGyroscopeConfiguration gyroCF = new SKGyroscopeConfiguration();
        gyroCF.setSamplingRate(SensorManager.SENSOR_DELAY_GAME);
        return gyroCF;
    }

    private SKRotationConfiguration createRotationConfig() {
        SKRotationConfiguration rotationCF = new SKRotationConfiguration();
        rotationCF.setSamplingRate(SensorManager.SENSOR_DELAY_GAME);
        return rotationCF;
    }

    private SKMagnetometerConfiguration createMagnetometerConfig() {
        SKMagnetometerConfiguration magCF = new SKMagnetometerConfiguration();
        magCF.setSamplingRate(SensorManager.SENSOR_DELAY_GAME);
        return magCF;
    }


    private class ConfigModelPair {

        private final ModelWriter m;
        private final SKAbstractConfiguration c;

        public ConfigModelPair(ModelWriter model) {
            m = model;
            c = null;
        }

        public ConfigModelPair(ModelWriter model, SKAbstractConfiguration config) {
            m = model;
            c = config;
        }

        public boolean configIsSet() {return !(c == null);}

        public ModelWriter getModelWriter() {return m;}

        public SKAbstractConfiguration getConfig() {return c;}
    }

}
