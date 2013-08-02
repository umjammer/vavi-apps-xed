XED XML EDitor
--------------

Goal
----

* sort by tag
* edit inner text specified by tag



```
$ xed -s "/kml/Folder/Placemark" "/kml/Folder/Placemark/updated/text()" asc datetime "EEE, d MMM yy HH:mm:ss Z" \
      -e "/kml/Folder/Placemark" "/kml/Folder/Placemark/description" "xpath_sdf('/kml/Folder/Placemark/updated/text()', 'EEE, d MMM yy HH:mm:ss Z', 'yyyy/MM/dd HH:mm:ss') + ' | $$'"
```