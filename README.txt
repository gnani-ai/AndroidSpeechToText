# SpeechtoText

# Add certificate for TLS :

1) Add a new android resource raw directory inside res directory
2) Copy and paste the "cert.pem" file inside raw directory

# Add Netwrok Security for TLS :

1) Add a new android resource xml directory inside res directory
2) Add a new xml resource file inside xml directory and name it as "network_security_config.xml"
3) Copy and paste the following code in "network_security_config.xml"

<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">gnani.ai</domain>
        <trust-anchors>
            <certificates src="@raw/cert"/>
        </trust-anchors>
    </domain-config>
</network-security-config>

# Enable TLS in Manifest :
1) Copy and paste the following code in application tag inside "AndroidManifest.xml"

android:networkSecurityConfig="@xml/network_security_config"

# Permissions :
1) Copy and paste the following code above application tag inside "AndroidManifest.xml"

<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.RECORD_AUDIO"/>

2) Write your own code to ask these permissions at runtime

# build.gradle(Project) :
1) Copy and paste the following code in dependencies inside build.gradle(Project)

classpath 'com.google.protobuf:protobuf-gradle-plugin:0.8.8'

2) Add the following code in repositories in allprojects

maven { url 'https://jitpack.io' }

# build.gradle(Module) :
1) Add the following dependency in dependencies inside build.gradle(Module)

implementation 'com.github.gnani-ai:SpeechToText:1.0.3'

 # Service Declaration :
1) Add the following code in application tag inside "AndroidManifest.xml"

 <service android:name="com.gnani.speechtotext.SpeechService"/>

# Java code :
1) Add the following code in Application class of your project

Recorder.init("yourToken", "yourAccessKey");

1) Implement these two intefaces to your activity or fragment or service

SpeechService.Listener, Recorder.RecordingStatusListener

2) Add the following code in onCreate method 

Recorder.bind(yourContext);

3) Add the following code in clicklisetner of view with which you want to start the recording

Recorder.onRecord(yourLanguage);

4) Recording will be stopeed in two cases :

a) If you click same view again
b) after 15 seconds 

5) Add the following code in onDestroy method

Recorder.unbind(yourContext);
