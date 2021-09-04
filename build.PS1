& javac --module net.plantabyte.tokenator -d out/build --module-source-path . --module-path "D:\CCHall\Documents\Programming\JFX-SDK\openjfx-11.0.2_windows-x64_bin-jmods\javafx-jmods-11.0.2;C:\Program Files\OpenJDK\jdk-16.0.2\jmods"
cp net.plantabyte.tokenator/net/plantabyte/tokenator/resources/* out/build/net.plantabyte.tokenator/net/plantabyte/tokenator/resources
Remove-Item out/tokenator -Recurse
Remove-Item out/installer -Recurse
& jlink --no-header-files --no-man-pages --output out/tokenator --launcher Tokenator=net.plantabyte.tokenator/net.plantabyte.tokenator.Main --add-modules net.plantabyte.tokenator --module-path "out/build;D:\CCHall\Documents\Programming\JFX-SDK\openjfx-11.0.2_windows-x64_bin-jmods\javafx-jmods-11.0.2;C:\Program Files\OpenJDK\jdk-16.0.2\jmods"
mkdir out/intaller
& iscc "/Oout/installer" InnoSetup-script.iss