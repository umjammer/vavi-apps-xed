[![Release](https://jitpack.io/v/umjammer/vavi-apps-xed.svg)](https://jitpack.io/#umjammer/vavi-apps-xed)

# XED XML EDitor

## Goal

* sort by tag
* edit inner text specified by tag

## Example

* a KML exported from the [FourSquare](https://ja.foursquare.com/feeds/)
 * sort by updated time ascending (`-s` option)
 * add updated time to description tag (`-e` option)
    * `'$$'` inside a string is replaced by the original inner text
    * function `xpath_sdf(xpath, format1, format2)` reformats date/time text using `java.util.SimpleDateFormat`

```shell
$ java -cp foo xed \
      -s "/kml/Folder/Placemark" "/kml/Folder/Placemark/updated/text()" asc datetime "EEE, d MMM yy HH:mm:ss Z" \
      -e "/kml/Folder/Placemark" "/kml/Folder/Placemark/description" "xpath_sdf('/kml/Folder/Placemark/updated/text()', 'EEE, d MMM yy HH:mm:ss Z', 'yyyy/MM/dd HH:mm:ss') + ' | $$'" \
      in_file.kml
```

```
$ xmllint --format foo.kml > bar.kml
```
