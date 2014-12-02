XED XML EDitor
--------------

Goal
----

* sort by tag
* edit inner text specified by tag

Example
-------

* a KML exported from the FourSquare
** sort by updated time ascending
** add updated time to description tag
*** '$$' inside a string is replaced by the original inner text

```
$ xed -s "/kml/Folder/Placemark" "/kml/Folder/Placemark/updated/text()" asc datetime "EEE, d MMM yy HH:mm:ss Z" \
      -e "/kml/Folder/Placemark" "/kml/Folder/Placemark/description" "xpath_sdf('/kml/Folder/Placemark/updated/text()', 'EEE, d MMM yy HH:mm:ss Z', 'yyyy/MM/dd HH:mm:ss') + ' | $$'"
```