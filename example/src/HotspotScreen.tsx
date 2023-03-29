import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  Pressable,
  ToastAndroid,
  ScrollView,
} from 'react-native';
import HotspotManager, { Device, TetheringError } from '@react-native-tethering/hotspot';

type WifiScreenProps = {
  back: () => void
}

export default function HotspotScreen({ back }: WifiScreenProps) {
  const [devices, setDevices] = React.useState<Device[]>([]);

  return (
    <ScrollView>
      <Pressable style={styles.backButton} android_ripple={{color: '#fff' }} onPress={back}>
        <Text>Back to home</Text>
      </Pressable>

      <View style={styles.wrapper}>
        <Text style={styles.wrapperHeader}>Hotspot States</Text>
        <View style={{ width: '100%' }}>
          <Pressable
            style={styles.button}
            android_ripple={{ color: '#ccc' }}
            onPress={async () => {
              try {
                const state = await HotspotManager.isHotspotEnabled();
                ToastAndroid.show(`Hotspot state: ${state}`, ToastAndroid.SHORT)
              } catch (error) {
                if (error instanceof TetheringError) {
                  ToastAndroid.show(error.message, ToastAndroid.LONG)
                }
                console.log(error);
              }
            }}
          >
            <Text style={{ color: '#000' }}>Check Hotspot Enabled</Text>
          </Pressable>
          <Pressable
          style={styles.button}
          android_ripple={{ color: '#fff' }}
          onPress={async () => {
            try {
              await HotspotManager.setHotspotEnabled(true);
              ToastAndroid.show('Hotspot Enabled', ToastAndroid.SHORT)
            } catch (error: any) {
              if (error instanceof TetheringError) {
                ToastAndroid.show(error.message, ToastAndroid.LONG)
              }
              console.log(error);
              
            }
          }}
        >
          <Text style={{ color: '#000' }}>Turn on hotpot</Text>
        </Pressable>
        <Pressable
          style={styles.button}
          android_ripple={{ color: '#fff' }}
          onPress={async () => {
            try {
              await HotspotManager.setHotspotEnabled(false);
              ToastAndroid.show('Hotspot Disabled', ToastAndroid.SHORT)
            } catch (error: any) {
              if (error instanceof TetheringError) {
                ToastAndroid.show(error.message, ToastAndroid.LONG)
              }
              console.log(error);
              
            }
          }}
        >
          <Text style={{ color: '#000' }}>Turn off hotpot</Text>
        </Pressable>
        <Pressable
          style={styles.button}
          android_ripple={{ color: '#fff' }}
          onPress={async () => {
            try {
              const network = await HotspotManager.setLocalHotspotEnabled(true);
              ToastAndroid.show('Local Hotspot Enabled', ToastAndroid.SHORT)
              console.log(network);
            } catch (error: any) {
              if (error instanceof TetheringError) {
                ToastAndroid.show(error.message, ToastAndroid.LONG)
              }
              console.log(error);
              
            }
          }}
        >
          <Text style={{ color: '#000' }}>Turn on local hotpot</Text>
        </Pressable>
        <Pressable
          style={styles.button}
          android_ripple={{ color: '#fff' }}
          onPress={async () => {
            try {
              await HotspotManager.setLocalHotspotEnabled(false);
              ToastAndroid.show('Local Hotspot Disabled', ToastAndroid.SHORT)
            } catch (error: any) {
              if (error instanceof TetheringError) {
                ToastAndroid.show(error.message, ToastAndroid.LONG)
              }
              console.log(error);
              
            }
          }}
        >
          <Text style={{ color: '#000' }}>Turn off local hotpot</Text>
        </Pressable>
        </View>
      </View>
      
      <View style={styles.wrapper}>
        <Text style={styles.wrapperHeader}>Hotspot Helpers</Text>
        <View style={{ width: '100%' }}>
          <Pressable
            style={styles.button}
            android_ripple={{ color: '#ccc' }}
            onPress={async () => {
              try {
                const state = await HotspotManager.isWriteSettingsGranted();
                ToastAndroid.show(`Write settings permission is ${state ? 'granted' : 'not graned'}`, ToastAndroid.SHORT)
              } catch (error) {
                if (error instanceof TetheringError) {
                  ToastAndroid.show(error.message, ToastAndroid.LONG)
                }
                console.log(error);
              }
            }}
          >
            <Text style={{ color: '#000' }}>Check write settings permission</Text>
          </Pressable>
          <Pressable
          style={styles.button}
          android_ripple={{ color: '#fff' }}
          onPress={async () => {
            try {
              const ip = await HotspotManager.getMyDeviceIp()
              ToastAndroid.show(`Your device IP: ${ip}`, ToastAndroid.SHORT)
              console.log(ip)
            } catch (error: any) {
              if (error instanceof TetheringError) {
                ToastAndroid.show(error.message, ToastAndroid.LONG)
              }
              console.log(error);
              
            }
          }}
        >
          <Text style={{ color: '#000' }}>Get your hotspot IP</Text>
        </Pressable>
        <Pressable
          style={styles.button}
          android_ripple={{ color: '#fff' }}
          onPress={async () => {
            try {
              const devices = await HotspotManager.getConnectedDevices();
              setDevices(devices)
            } catch (error: any) {
              if (error instanceof TetheringError) {
                ToastAndroid.show(error.message, ToastAndroid.LONG)
              }
              console.log(error);
              
            }
          }}
        >
          <Text style={{ color: '#000' }}>find connected devices</Text>
        </Pressable>
        <Pressable
          style={styles.button}
          android_ripple={{ color: '#fff' }}
          onPress={async () => {
            try {
              await HotspotManager.openWriteSettings();
            } catch (error: any) {
              if (error instanceof TetheringError) {
                ToastAndroid.show(error.message, ToastAndroid.LONG)
              }
              console.log(error);
              
            }
          }}
        >
          <Text style={{ color: '#000' }}>Navigate to write settings screen</Text>
        </Pressable>
        <Pressable
          style={styles.button}
          android_ripple={{ color: '#fff' }}
          onPress={async () => {
            try {
              await HotspotManager.navigateToTethering();
            } catch (error: any) {
              if (error instanceof TetheringError) {
                ToastAndroid.show(error.message, ToastAndroid.LONG)
              }
              console.log(error);
            }
          }}
        >
          <Text style={{ color: '#000' }}>Navigate to tethering screen</Text>
        </Pressable>
        </View>
      </View>
      <View style={{ marginVertical: 15 }}>
        {devices.map((d, i) => (
          <View
            style={styles.devicesWrapper}
            key={i}
          >
            <Text style={{ color: '#000' }}>ipAddress: {d.ipAddress}</Text>
            <Text style={{ color: '#000' }}>macAddress: {d.macAddress}</Text>
            <Text style={{ color: '#000' }}>status: {d.status}</Text>
          </View>
        ))}
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  button: {
    width: '70%',
    padding: 14,
    backgroundColor: '#fff',
    borderRadius: 10,
    marginBottom: 10,
    alignItems: 'center'
  },
  wrapper: { 
    backgroundColor: '#359962',
    padding: 15,
    marginBottom: 8
  },
  wrapperHeader: { 
    fontSize: 16, 
    color: '#fff', 
    marginBottom: 12 
  },
  row: {
    flexDirection: 'row', 
    justifyContent: 'space-between', 
    width: '100%' 
  },
  devicesWrapper: {
    padding: 12, 
    backgroundColor: '#ccc', 
    marginVertical: 6
  },
  backButton: {
    backgroundColor: '#ddd', 
    padding: 15, 
    alignItems: 'center'
  }
});
