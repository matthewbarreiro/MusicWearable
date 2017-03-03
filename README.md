# Music Wearable MQP 2017
This app ...

## Notes/Helpful Info:
 - You will need to enable developer mode on both your phone and watch. This is generally accomplished by rapidly tapping "Build Number" in "About Phone/Device/Etc" within settings, but YMMV.
 - Make sure to authorize both the phone and watch for ADB
 - 

 ### Using Android Studio and Wear with no Main Activity:
 
 http://stackoverflow.com/a/40113469

### Debugging Over Bluetooth:
After configuring and enabling ADB (see Source: [Android Developers](https://developer.android.com/training/wearables/apps/bt-debugging.html "Debugging over Bluetooth | Android Developers") and ADB help files), run the following to link the debugger to the watch: 

    adb forward tcp:4444 localabstract:/adb-hub
    adb connect 127.0.0.1:4444

Source: [Android Developers](https://developer.android.com/training/wearables/apps/bt-debugging.html "Debugging over Bluetooth | Android Developers")

### Updating Watch App:
The original Sensor Dashboard app did not have any main activity for the Android Wear component, so the app cannot be pushed and run directly from Android Studio. Instead, you must build the APK (Build -> Build APK from within Android Studio) and then install it manually via ADB.

First install:
`adb -s 127.0.0.1:4444 install "[PATH]\wear-debug.apk" `

Update (replace) the app:
`adb -s 127.0.0.1:4444 install -r "[PATH]\wear-debug.apk" `

And it take a long time because Bluetooth is relatively slow.


------------------------------------------------------------------------------------------------------------------------------------------
# SensorDashboard

Written at the Android Wear Hackathon 2014 in London by [Juhani Lehtimäki](https://plus.google.com/+JuhaniLehtim%C3%A4ki/posts), [Benjamin Stürmer](https://stuermer-benjamin.de/) and [Sebastian Kaspari](https://plus.google.com/+SebastianKaspari/posts).


## License

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
