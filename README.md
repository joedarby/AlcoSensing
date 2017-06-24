# AlcoSensing App

AlcoSensing is an Android app developed as part of my MSc Computer Science final research project titled "Can alcohol consumption and its acute effects be detected from smartphone sensor data".

The app was made publicly available on the Play Store for users to download and act as participants in the study. The app allows users to sign up and give consent to the study. It then runs on users' devices during the study period (around 1 month) and periodically gathers sensor data from users' devices. After each period of sensing, users are then presented with a notification asking them to complete a survey. The survey asks users about their alcohol consumption during the relevant period.

The app uses the Google Places API for geofencing and place recognition in an attempt to intelligently predict when users may be consuming alcohol. Separately from this the app will also carry out sensing periodically based on a schedule which is more likely to trigger sensing at times when users are more likely to be drinking.

The app makes use of SensingKit (https://github.com/SensingKit) for sensor control and data gathering and ResearchStack (https://github.com/ResearchStack) for parts of the user interface.

The data gathered from this app will be used to develop a model allowing the identification of inebriation from mobile sensor readings.

With thanks to David (https://github.com/Leyths) and Minos (https://github.com/minoskt) for their contributions.
