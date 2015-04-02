#!/bin/sh
# Sizes extracted from
# http://developer.android.com/design/style/iconography.html

[ -z $2 ] && echo -e "ERROR: filename and one dimension (-w or -h) is required, for example:\nsvg2png -w48 icon.svg\n" && exit 1;
FILENAME=$2
DEST_FILENAME=`echo $2 | sed s/\.svg/\.png/`
FLAG=`echo $1 | cut -c1-2`
ORIGINAL_VALUE=`echo $1 | cut -c3-`

if [ "$FLAG" != "-w" ] && [ "$FLAG" != "-h" ]; then
    echo "Unknown parameter: $FLAG" 
    exit 1
fi

# PARAMETERS: {multiplier} {destination folder}
exportToPng() {
  VALUE=$(echo "scale=0; $ORIGINAL_VALUE*$1" | bc -l)
  CMD="inkscape $FLAG$VALUE --export-background-opacity=0 --export-png=app/src/main/res/$2/$DEST_FILENAME resources/svg/$FILENAME > /dev/null"
  echo $CMD
  eval $CMD
} 


exportToPng 1 drawable-mdpi
exportToPng 1.5 drawable-hdpi
exportToPng 2 drawable-xhdpi
exportToPng 3 drawable-xxhdpi
exportToPng 4 drawable-xxxhdpi

