# Music Wearable MQP 2017
This app is designed to select music based on fitness data collected on an Android Wear device. This implementation primarily serves as a proof-of-concept, and performs as such.

## Notes/Helpful Info:
 - This App was written for Android Wear  1.5 (1.5.0.3336103 if you really care) on Android 6.0.1. NO idea what will/will not work on the recently released Wear 2.0.
 - Unpairing an Android Wear device from a phone causes the Wear device to factory reset. AKA you can't just share the device between phones.
 - You will need to enable developer mode on both your phone and watch. This is generally accomplished by rapidly tapping "Build Number" in "About Phone/Device/Etc" within device settings, but YMMV.
 - Make sure to authorize both the phone and watch for ADB

 ### Using Android Studio and Wear with no Main Activity:

To fix the error in Android Studio that doesn't allow directly launching the Wear component ("Main Activity Not Found"), do the following ([via StackOverflow](http://stackoverflow.com/a/40113469)):
Run -> Edit Configurations -> Android App -> wear -> General -> Launch Options -> **Launch: Nothing**

Alternatively, you can build the apks (Build -> Build APK from within Android Studio) and then install it manually via ADB (see below for how to connect to wear device over Bluetooth).

### Debugging Over Bluetooth:
After configuring and enabling ADB (see Source: [Android Developers](https://developer.android.com/training/wearables/apps/bt-debugging.html "Debugging over Bluetooth | Android Developers") and ADB help files), run the following to link the debugger to the watch: 

    adb forward tcp:4444 localabstract:/adb-hub     //Any port you have full access to should work.
    adb connect 127.0.0.1:4444                          

You can then run any ADB command as follows:
`adb -s 127.0.0.1:4444 [some command]`

Source: [Android Developers](https://developer.android.com/training/wearables/apps/bt-debugging.html "Debugging over Bluetooth | Android Developers")


# Credits

Sensor Dashboard was originally written at the Android Wear Hackathon 2014 in London by [Juhani Lehtimäki](https://plus.google.com/+JuhaniLehtim%C3%A4ki/posts), [Benjamin Stürmer](https://stuermer-benjamin.de/) and [Sebastian Kaspari](https://plus.google.com/+SebastianKaspari/posts). It is avalible under the Apache 2.0 License (below).


## Licenses

Parts of this code are licensed as follows:

### License (Sensor Dashboard)

    Copyright 2014 Juhani Lehtimäki, Benjamin Stürmer, Sebastian Kaspari
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
