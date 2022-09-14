An app that displays time. It's really that simple! 
The app tries to connect to an NTP server and, if it's
successful in doing so, displays the device's time 
plus any offset gained from the NTP (which is gathered
every 30 seconds). If no connection has been established, 
the app will fallback to only displaying the device's time.
