for (($i = 400); $i -lt 4001; $i += 400) {
    java -jar .\target\raven-1.6.2.jar -ir "C:\Users\alexa\Desktop\columns" -iv "C:\Users\alexa\Desktop\Raven_Data\rects3\$i\geom.shp" -cl "C:\Users\alexa\Desktop\caches" -ts 4096
}
