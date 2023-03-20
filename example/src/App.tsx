import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  Pressable,
  ToastAndroid,
  ScrollView,
} from 'react-native';
import TetheringManager, { Network, Event, TetheringError } from '@react-native-tethering/wifi';
// import WifiManager from 'react-native-wifi-reborn';
import { useNetInfo } from '@react-native-community/netinfo';

export default function App() {
  const [networks, setNetworks] = React.useState<Network[]>([]);

  React.useEffect(() => {
    const subscriber = TetheringManager.addEventListener(Event.OnNetworkDisconnected, () => {

    })

    return () => subscriber.remove();
  }, []);

  const netInfo = useNetInfo();
  return (
    <ScrollView>
      {/* <Pressable
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
      </Pressable> */}
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
            await TetheringManager.setWifiEnabled();
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
            await TetheringManager.setWifiDisabled();
          } catch (error) {
            console.log(error);
          }
        }}
      >
        <Text>Disable Wifi</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#fff' }}
        onPress={async () => {
          try {
            await TetheringManager.saveNetworkInDevice({
              ssid: 'Assem’s iPhone',
              password: 'a123456789'
            });
            console.log('Network saved');
          } catch (error) {
            console.log(error);
          }
        }}
      >
        <Text>Save network</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#fff' }}
        onPress={async () => {
          try {
            await TetheringManager.connectToNetwork({
              ssid: 'Assem’s iPhone',
              password: 'a123456789'
            });
            console.log('connected to network');
          } catch (error) {
            console.log(error);
          }
        }}
      >
        <Text>Connect to wifi</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#fff' }}
        onPress={async () => {
          try {
            await TetheringManager.disconnectFromNetwork();
          } catch (error) {
            console.log(error);
          }
        }}
      >
        <Text>Disconnect from wifi</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#fff' }}
        onPress={async () => {
          try {
            const wifiNetworks = await TetheringManager.getWifiNetworks(true);
            setNetworks(wifiNetworks);
          } catch (error: any) {
            if (error instanceof TetheringError) {
              console.log(error.code);
            }
          }
        }}
      >
        <Text>Get list of networks</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#fff' }}
        onPress={async () => {
          try {
            const suggestionNum = await TetheringManager.getMaxNumberOfNetworkSuggestions();
            console.log(suggestionNum);
          } catch (error: any) {
            if (error instanceof TetheringError) {
              console.log(error.code);
            }
          }
        }}
      >
        <Text>Get max suggestions</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#fff' }}
        onPress={async () => {
          try {
            const ip = await TetheringManager.getDeviceIP();
            console.log(ip);
          } catch (error: any) {
            if (error instanceof TetheringError) {
              console.log(error.code);
            }
          }
        }}
      >
        <Text>Get device ip</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#fff' }}
        onPress={async () => {
          try {
            const status = await TetheringManager.isDeviceAlreadyConnected();
            console.log(status);
          } catch (error: any) {
            if (error instanceof TetheringError) {
              console.log(error.code);
            }
          }
        }}
      >
        <Text>is connected to network?</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#fff' }}
        onPress={() => {
          TetheringManager.openWifiSettings(false)
        }}
      >
        <Text>Open wifi settings</Text>
      </Pressable>
      <Pressable
        style={styles.button}
        android_ripple={{ color: '#fff' }}
        onPress={async () => {
          ToastAndroid.show(
            `${netInfo.isInternetReachable} and ${netInfo.isConnected}`,
            ToastAndroid.SHORT
          );
        }}
      >
        <Text>Is App has internet</Text>
      </Pressable>

      <View style={{ marginVertical: 15 }}>
        {networks.map((n, i) => (
          <View
            style={{ padding: 12, backgroundColor: '#222', marginVertical: 6 }}
            key={i}
          >
            <Text>SSID: {n.ssid}</Text>
            <Text>BSSID: {n.bssid}</Text>
            <Text>capabilities: {n.capabilities}</Text>
            <Text>frequency: {n.frequency}</Text>
            <Text>level: {n.level}</Text>
            <Text>timestamp: {n.timestamp}</Text>
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
    backgroundColor: '#A1A1A1',
    borderRadius: 10,
    marginBottom: 10,
  },
});
