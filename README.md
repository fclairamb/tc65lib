TC65Lib (javacint)
=======

General library to simplify development around the Cinterion / Gemalto java enabled devices.

Goals
----
* Support most devices
* Shorten the devleopment and release to market time at the maximum (see the demo code)
* Have a clean and simple to use base code

This library is intended to be used as a framework. You don't have to use all the components but some of them are mandatory (like logging, settings, AT commands handling).

It is highly recommended to obfuscate your programs to remove the unnecessary components of the library.

Supported devices
-----------------
The goal is to support all these devices:

* TC65
* TC65i / TC65i-X
* BSG5 / BSG5-X
* EHS5 / EHS6

Only devices with the siemens and cinterion SDK namespaces are supported at this stage. Supporting other devices is relatively simple (see how the sdkns variable works). Contributions are welcome.

Documentation
-------------
There is no non-automatic documentation at this stage. Only some demo code.

Javadoc: http://docs.webingenia.com/tc65lib/javadoc/ (automatically updated)

Doxygen: http://docs.webingenia.com/tc65lib/doxygen/ (includes source-code, automatically updated)

Test it
-------

If you have an APN named ''websfr'', you can load the latest version by typing the following AT commands:

    AT^SJOTAP=,http://94.23.55.152:8080/demo/demo.jad,a:,,,"gprs","websfr",,
    AT^SJOTAP
    
Don't forget to prepare your chip if you didn't do it:

    AT^SCFG="Userware/Autostart/Delay","","50"
    AT^SCFG="Userware/Autostart","","1"
    AT^SCFG="Userware/Stdout","ASC0"

If the latest version is broken, you can use this one:
http://94.23.55.152:8080/demo/demo-0.1.9.jad

Demo
----
Quick look at the console: 
* http://asciinema.org/a/6782 (settings management demo + few commands)
* http://asciinema.org/a/6783 (auto-update on new version detection)


Features
--------
The features that will be provided in this library are:
- Settings management (tested)
- Watchdog management (tested)
- Logging (tested)
- Console (tested)
- Time sync (tested)
- SMS receiving and sending (tested)
- Call receiving and making (not done)
- Automatic OTAP management (tested)
- Communication with server: MQTT / M2MP / HTTP ? (HTTP comm is functionnal)
- Email sending (tested)
- GPS management (tested)

You are welcome to request features and pulls, they might be accepted.
