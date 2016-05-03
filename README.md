Android-Wear-Demo
=================

Android Wear MessageApi implementation.

```com.google.android.gms.wearable.BIND_LISTENER``` is deprecated now need to use updated code as mentioned below.

Deprecated
----------

```<action android:name="com.google.android.gms.wearable.BIND_LISTENER" />```

Updated Code
------------
```
<action android:name="com.google.android.gms.wearable.DATA_CHANGED" />
<action android:name="com.google.android.gms.wearable.MESSAGE_RECEIVED" />
```
