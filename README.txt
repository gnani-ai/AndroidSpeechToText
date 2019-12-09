# SpeechtoText

# Add certificate for TLS :

1) Add a new android resource raw directory inside res directory
2) Copy and paste the "cert.pem" file inside raw directory

# Add Netwrok Security for TLS :

1) Add a new android resource xml directory inside res directory
2) Add a new xml resource file inside xml directory and name it as "network_security_config.xml"
3) Copy and paste the following code in "network_security_config.xml"

```
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">gnani.ai</domain>
        <trust-anchors>
            <certificates src="@raw/cert"/>
        </trust-anchors>
    </domain-config>
</network-security-config>
```

# Enable TLS in Manifest :
1) Copy and paste the following code in application tag inside "AndroidManifest.xml"

```
android:networkSecurityConfig="@xml/network_security_config"
```

# Permissions :
1) Copy and paste the following code above application tag inside "AndroidManifest.xml"

```
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
```

2) Write your own code to ask these permissions at runtime

# build.gradle(Project) :
1) Copy and paste the following code in dependencies inside build.gradle(Project)

```
classpath 'com.google.protobuf:protobuf-gradle-plugin:0.8.8'
```

2) Add the following code in repositories in allprojects

```
maven { url 'https://jitpack.io' }
```

# build.gradle(Module) :
1) Add the following code in every buildType
```
buildConfigField("String", "token", project.properties['token'])
buildConfigField "String", "language", project.properties['language']
buildConfigField "String", "accesskey", project.properties['accesskey']
buildConfigField "String", "audioformat", project.properties['audioformat']
buildConfigField "String", "encoding", project.properties['encoding']
buildConfigField "String", "sad", project.properties['sad']
buildConfigField "String", "ip", project.properties['ip']
buildConfigField("int", "port", project.properties['port'])
buildConfigField("boolean", "tls", project.properties['tls'])
```

2) Add the following dependency in dependencies inside build.gradle(Module)
```
implementation 'com.github.gnani-ai:SpeechToText:1.0.2'
```

 # Service Declaration :
1) Add the following code in application tag inside "AndroidManifest.xml"

```
<service android:name="com.gnani.stt.SpeechService" />
```

# gradle.properties :
1) Add the following properties in gradle.properties

```
token="yourToken"
language="eng_IN"
accesskey="yourAccessKey"
audioformat="wav"
encoding="pcm16"
sad="yes"
ip="asr.gnani.ai"
port=443
tls=true
```

# Java code :
1) Implement these two intefaces to your activity or fragment or service

```
SpeechService.Listener, Recorder.RecordingStatusListener
```

2) Add the following code in onCreate method 

```
Recorder.bind(yourContext, yourContext, yourContext);
```

3) Add the following code in clicklisetner of view with which you want to start the recording

```
Recorder.onRecord(BuildConfig.token, BuildConfig.language, BuildConfig.accesskey, BuildConfig.audioformat, BuildConfig.encoding, BuildConfig.sad, BuildConfig.ip, BuildConfig.port, BuildConfig.tls);
```

4) Recording will be stopeed in two cases :

a) If you click same view again
b) after 15 seconds 

5) Add the following code in onDestroy method

```
Recorder.unbind(yourContext);
```
