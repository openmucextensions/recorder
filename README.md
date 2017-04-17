# Building Flight Recorder
This OpenMUC application automatically logs available channels and provides the logged values for further analysis. The main features are:

* Automated configuration of channels to log from driver's channel scan (updated periodically)
* Common sampling and logging intervals, synchronized to full hours for better post-processing
* Blacklists for devices or channels that shouldn't be logged
* Data will be formated in tables for export
* Optimized export format for Microsoft Excel
* Time-based average for logged values

The following sections describe the implemented features in detail.

## Automated channel configuration
Every time the component will be initiated as well as periodically afterwards, all installed drivers will be scanned for new or updates devices and channels. The update interval is 24 hours by default and can be changed using the `org.openmucextensions.app.recorder.updatePeriod` system property (value in milliseconds). The component first retrieves a list of all installed drivers and performs a scan for devices as well as a scan for channels afterwards. If the scan returns channels that aren't configured yet, new channel definitions will be created and stored in OpenMUC's `channels.xml` file. Drivers that don't support scan for channels will be ignored, a warning message will be logged in this case. Drivers, devices or channels that should be ignored (thus that shouldn't get logged) can be defined in a blacklist file. The following table shows all system properties that influence the behavior of the automated channel configuration:

| Property | Description | Default |
|:--------------|:-----------|:------|
| loggingInterval | The logging interval that will be set for new channels in ms | 60.000 |
| samplingInterval | The sampling interval that will be set for new channels in ms | 10.000 |
| updatePeriod | The automated channel configuration update period in ms | 86.400.000 (24 hours) |
| blacklistFilename | The blacklist filename | ./conf/blacklist.xml |

All properties start with `org.openmucextensions.app.recorder.`, so the full property name is this prefix followed by the property name in the table above.

## Blacklist configuration
Drivers, devices or channels that should be ignored can be defined in a blacklist file.

## Data export using the Apache Felix Gogo console
The component provides a console command for exporting logged data. The data export format is optimized for processing in analysis applications like Microsoft Excel. The following table shows an example of the export data format:

| Timestamp | Channel_01 | Channel_02 | Channel_03 | ... |
|:---|:---|:---|:---|:---|
| 2000-01-01 00:00:00 | value | value | value | |
| 2000-01-01 00:15:00 | value | value | value | |
| 2000-01-01 00:30:00 | value | value | value | |
| 2000-01-01 00:45:00 | value | value | value | |
| ... | | | | |

Start and end time as well as the interval between two timestamps can be set. If data is missing for a certain timestamp or channel, the field in the table is left blank. Note that the logging interval and the interval in the export table must be concerted.

The console command for exporting data is `exporttable` with scope `recorder`. Type `help exporttable` in the console to get a short usage description. The command has the follwoing format:

```
exporttable filename startTime endTime interval
```
