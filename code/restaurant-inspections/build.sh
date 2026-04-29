#!/bin/bash

rm -f *.class nyc-inspection.jar
rm -rf jar_test_dir

echo "Create class files"
javac -classpath ".:commons-csv-1.10.0.jar:`hadoop classpath`" *.java

if [ $? -ne 0 ]; then
    echo "Failed!"
    exit 1
fi

echo "Create JAR"
mkdir -p jar_test_dir
cp *.class jar_test_dir/
cd jar_test_dir
jar xf ../commons-csv-1.10.0.jar
cd ..
jar cf nyc-inspection.jar -C jar_test_dir .
rm -rf jar_test_dir

echo "Compilation successful and jar is created"