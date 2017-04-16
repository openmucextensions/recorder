# Building Flight Recorder
This OpenMUC application automatically logs available channels and provides the logged values for further analysis. The main features are:

* Automated configuration of channels to log from driver's channel scan (updated periodically)
* Common sampling and logging intervals, synchronized to full hours for better post-processing
* Blacklists for devices or channels that shouldn't be logged
* Data will be formated in tables for export
* Optimized export format for Microsoft Excel
