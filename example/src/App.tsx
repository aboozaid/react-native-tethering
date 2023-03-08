import * as React from 'react';

import { StyleSheet, View, Text, Pressable } from 'react-native';
import TetheringManager, { TetheringError } from 'react-native-tethering';
// import WifiManager from 'react-native-wifi-reborn';

export default function App() {
  // const [result, setResult] = React.useState<number | undefined>();

  // React.useEffect(() => {
  //   multiply(3, 7).then(setResult);
  // }, []);

  return (
    <View style={styles.container}>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#fff' }}
        onPress={async () => {
          try {
            const state = await TetheringManager.isHotspotEnabled();
            console.log(state);
          } catch (error) {
            if (error instanceof TetheringError) {
              console.log(error);
            }
          }
        }}
      >
        <Text>Hotspot State?</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#fff' }}
        onPress={async () => {
          try {
            await TetheringManager.setHotspotEnabled(true);
          } catch (error) {
            if (error instanceof TetheringError) {
              console.log(error.code + ' ' + error.message);
            }
          }
        }}
      >
        <Text>Turn on hotspot</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#fff' }}
        onPress={async () => {
          try {
            await TetheringManager.setHotspotEnabled(false);
          } catch (error) {
            console.log(error);
          }
        }}
      >
        <Text>Turn off hotspot</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#fff' }}
        onPress={async () => {
          try {
            const network = await TetheringManager.setLocalHotspotEnabled();
            console.log(network);
          } catch (error) {
            console.log(error);
          }
        }}
      >
        <Text>Turn on local hotspot</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#fff' }}
        onPress={async () => {
          try {
            await TetheringManager.setLocalHotspotDisabled();
          } catch (error) {
            console.log(error);
          }
        }}
      >
        <Text>Turn off local hotspot</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#fff' }}
        onPress={async () => {
          try {
            const ip = await TetheringManager.getHotspotDeviceIp();
            console.log(ip);
          } catch (error) {
            console.log(error);
          }
        }}
      >
        <Text>Get my IP</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#fff' }}
        onPress={async () => {
          try {
            const peers = await TetheringManager.getHotspotPeersAddresses();
            console.log(peers);
          } catch (error) {
            console.log(error);
          }
        }}
      >
        <Text>Get peers IP</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#fff' }}
        onPress={async () => {
          try {
            const state = await TetheringManager.isWifiEnabled();
            console.log(state);
          } catch (error) {
            console.log(error);
          }
        }}
      >
        <Text>Check wifi state</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#fff' }}
        onPress={async () => {
          try {
            await TetheringManager.setWifiEnabled(true);
            console.log('wifi enabled');
          } catch (error) {
            console.log(error);
          }
        }}
      >
        <Text>Enable Wifi</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#fff' }}
        onPress={async () => {
          try {
            await TetheringManager.setWifiEnabled(false);
          } catch (error) {
            console.log(error);
          }
        }}
      >
        <Text>Disable Wifi</Text>
      </Pressable>
    </View>
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
    backgroundColor: '#A1A1A1',
    borderRadius: 10,
    marginBottom: 10,
  },
});
