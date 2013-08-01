XED XML EDitor
--------------

Goal
----

* sort by tag
* edit inner text specified by tag



```
$ xed -s "/kml/Folder/Placemark" "/Placemark/updated/text()" desc datetime "EEE, d MMM yy HH:mm:ss Z" \
      -e "/kml/Folder/Placemark" "/Placemark/descriptopn" "$1 | xpath('/kml/Folder/Placemark/updated/text()')"
```  